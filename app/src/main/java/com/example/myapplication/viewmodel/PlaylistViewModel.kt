package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Song
import com.example.myapplication.data.isPlayable
import com.example.myapplication.data.mergeSongForPlayback
import com.example.myapplication.data.toCacheEntity
import com.example.myapplication.data.toSong
import com.example.myapplication.data.db.dao.DownloadDao
import com.example.myapplication.data.db.dao.FavoriteDao
import com.example.myapplication.data.db.dao.PlayHistoryDao
import com.example.myapplication.data.db.dao.PlaylistDao
import com.example.myapplication.data.db.dao.SongCacheDao
import com.example.myapplication.data.db.entity.FavoriteEntity
import com.example.myapplication.data.db.entity.PlayHistoryEntity
import com.example.myapplication.data.db.entity.PlaylistEntity
import com.example.myapplication.data.db.entity.PlaylistSongCrossRef
import com.example.myapplication.download.DownloadProgress
import com.example.myapplication.download.SongDownloadManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class PlaylistViewModel(
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val playHistoryDao: PlayHistoryDao,
    private val songCacheDao: SongCacheDao,
    private val downloadDao: DownloadDao,
    private val downloadManager: SongDownloadManager,
) : ViewModel() {

    val playlists: StateFlow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlistSongCounts: StateFlow<Map<Long, Int>> = playlistDao.observePlaylistSongCounts()
        .map { rows -> rows.associate { it.playlistId to it.songCount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val favoriteIds: StateFlow<List<String>> = favoriteDao.getAllFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = favorites
        .map { entities -> entities.map { favEntityToSong(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<PlayHistoryEntity>> = playHistoryDao.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val validDownloads = downloadDao.getAllDownloads()
        .map { entries -> entries.filter { File(it.localPath).exists() } }
        .distinctUntilChanged()

    val downloadedSongs: StateFlow<List<Song>> = validDownloads
        .map { list -> list.map { it.toSong() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedIds: StateFlow<Set<String>> = validDownloads
        .map { list -> list.map { it.songId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val downloadProgress: StateFlow<Map<String, DownloadProgress>> = downloadManager.progress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun createPlaylist(name: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = playlistDao.createPlaylist(PlaylistEntity(name = name))
            onCreated(id)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            playlistDao.clearPlaylist(id)
            playlistDao.deletePlaylistById(id)
        }
    }

    fun renamePlaylist(id: Long, newName: String) {
        viewModelScope.launch {
            playlistDao.renamePlaylist(id, newName)
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song, onAdded: () -> Unit = {}) {
        viewModelScope.launch {
            if (playlistDao.isSongInPlaylist(playlistId, song.id)) return@launch
            val enriched = enrichWithDownloads(listOf(song)).first()
            songCacheDao.upsert(enriched.toCacheEntity())
            val count = playlistDao.getPlaylistSongCount(playlistId).first()
            playlistDao.addSongToPlaylist(
                PlaylistSongCrossRef(
                    playlistId = playlistId,
                    songId = enriched.id,
                    position = count,
                )
            )
            onAdded()
        }
    }

    suspend fun getPlaylistSongIds(playlistId: Long): Set<String> {
        return playlistDao.getPlaylistSongIds(playlistId).toSet()
    }

    fun saveQueueAsPlaylist(name: String, songs: List<Song>) {
        viewModelScope.launch {
            val enriched = enrichWithDownloads(songs)
            val playlistId = playlistDao.createPlaylist(PlaylistEntity(name = name))
            songCacheDao.upsertAll(enriched.map { it.toCacheEntity() })
            enriched.forEachIndexed { index, song ->
                playlistDao.addSongToPlaylist(
                    PlaylistSongCrossRef(playlistId = playlistId, songId = song.id, position = index)
                )
            }
        }
    }

    suspend fun getPlaylistSongs(playlistId: Long, catalog: List<Song> = emptyList()): List<Song> {
        val refs = playlistDao.getPlaylistSongs(playlistId).first()
        if (refs.isEmpty()) return emptyList()
        val ids = refs.map { it.songId }
        val cached = songCacheDao.getByIds(ids).associateBy { it.songId }
        val catalogMap = catalog.associateBy { it.id }
        val songs = refs.mapNotNull { ref ->
            mergeSongForPlayback(cached[ref.songId]?.toSong(), catalogMap[ref.songId])
                ?: songCacheDao.getById(ref.songId)?.toSong()
        }
        val enriched = enrichWithDownloads(songs)
        val resolved = enriched.map { song ->
            if (song.isPlayable()) song
            else catalogMap[song.id]?.let { mergeSongForPlayback(song, it) ?: it } ?: song
        }
        songCacheDao.upsertAll(
            resolved.filter { it.mediaUrl.isNotBlank() || it.filePath != null }.map { it.toCacheEntity() }
        )
        return enrichWithDownloads(resolved)
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            playlistDao.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun reorderPlaylistSongs(playlistId: Long, songs: List<Song>, onReordered: () -> Unit = {}) {
        viewModelScope.launch {
            if (songs.isEmpty()) return@launch
            songCacheDao.upsertAll(songs.map { it.toCacheEntity() })
            playlistDao.updateSongPositions(
                songs.mapIndexed { index, song ->
                    PlaylistSongCrossRef(
                        playlistId = playlistId,
                        songId = song.id,
                        position = index,
                    )
                },
            )
            onReordered()
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isFav = favoriteIds.value.contains(song.id)
            if (isFav) {
                favoriteDao.removeFavorite(song.id)
            } else {
                favoriteDao.addFavorite(
                    FavoriteEntity(
                        songId = song.id,
                        songTitle = song.title,
                        songArtist = song.artist,
                        songAlbum = song.album,
                        songCoverUrl = song.coverUrl,
                        songMediaUrl = song.mediaUrl,
                        songDurationMs = song.durationMs,
                        songSource = song.source.name,
                        songFilePath = song.filePath,
                    )
                )
            }
        }
    }

    fun downloadSong(song: Song) {
        viewModelScope.launch { downloadManager.download(song) }
    }

    fun deleteDownload(songId: String) {
        viewModelScope.launch { downloadManager.delete(songId) }
    }

    suspend fun enrichWithDownloads(songs: List<Song>): List<Song> {
        return songs.map { song ->
            val path = downloadManager.getLocalPath(song.id)
            if (path != null) {
                song.copy(source = com.example.myapplication.data.SongSource.LOCAL, filePath = path)
            } else {
                song
            }
        }
    }

    suspend fun enrichSong(song: Song): Song = enrichWithDownloads(listOf(song)).first()

    fun logPlay(song: Song) {
        viewModelScope.launch {
            playHistoryDao.logPlay(
                PlayHistoryEntity(
                    songId = song.id,
                    songTitle = song.title,
                    songArtist = song.artist,
                    songAlbum = song.album,
                    songCoverUrl = song.coverUrl,
                    songMediaUrl = song.mediaUrl,
                    songDurationMs = song.durationMs,
                    songSource = song.source.name,
                    songFilePath = song.filePath,
                )
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch { playHistoryDao.clearHistory() }
    }

    fun favEntityToSong(entity: FavoriteEntity): Song = entity.toSong()

    fun historyEntityToSong(entity: PlayHistoryEntity): Song = entity.toSong()
}
