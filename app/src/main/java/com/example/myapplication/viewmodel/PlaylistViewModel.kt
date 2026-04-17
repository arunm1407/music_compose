package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Song
import com.example.myapplication.data.SongSource
import com.example.myapplication.data.db.dao.FavoriteDao
import com.example.myapplication.data.db.dao.PlayHistoryDao
import com.example.myapplication.data.db.dao.PlaylistDao
import com.example.myapplication.data.db.entity.FavoriteEntity
import com.example.myapplication.data.db.entity.PlayHistoryEntity
import com.example.myapplication.data.db.entity.PlaylistEntity
import com.example.myapplication.data.db.entity.PlaylistSongCrossRef
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val playHistoryDao: PlayHistoryDao,
) : ViewModel() {

    val playlists: StateFlow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favoriteIds: StateFlow<List<String>> = favoriteDao.getAllFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favorites: StateFlow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentlyPlayed: StateFlow<List<PlayHistoryEntity>> = playHistoryDao.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistDao.createPlaylist(PlaylistEntity(name = name))
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

    fun addSongToPlaylist(playlistId: Long, song: Song, position: Int = 0) {
        viewModelScope.launch {
            playlistDao.addSongToPlaylist(
                PlaylistSongCrossRef(
                    playlistId = playlistId,
                    songId = song.id,
                    position = position,
                )
            )
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            playlistDao.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun getPlaylistSongs(playlistId: Long): Flow<List<PlaylistSongCrossRef>> {
        return playlistDao.getPlaylistSongs(playlistId)
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
        viewModelScope.launch {
            playHistoryDao.clearHistory()
        }
    }

    fun favEntityToSong(entity: FavoriteEntity): Song {
        return Song(
            id = entity.songId,
            title = entity.songTitle,
            artist = entity.songArtist,
            album = entity.songAlbum,
            coverUrl = entity.songCoverUrl,
            mediaUrl = entity.songMediaUrl,
            durationMs = entity.songDurationMs,
            source = try { SongSource.valueOf(entity.songSource) } catch (_: Exception) { SongSource.STREAM },
            filePath = entity.songFilePath,
        )
    }

    fun historyEntityToSong(entity: PlayHistoryEntity): Song {
        return Song(
            id = entity.songId,
            title = entity.songTitle,
            artist = entity.songArtist,
            album = entity.songAlbum,
            coverUrl = entity.songCoverUrl,
            mediaUrl = entity.songMediaUrl,
            durationMs = entity.songDurationMs,
            source = try { SongSource.valueOf(entity.songSource) } catch (_: Exception) { SongSource.STREAM },
            filePath = entity.songFilePath,
        )
    }
}
