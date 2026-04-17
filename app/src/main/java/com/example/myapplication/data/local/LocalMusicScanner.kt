package com.example.myapplication.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.myapplication.data.Song
import com.example.myapplication.data.SongSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LocalAlbum(
    val id: Long,
    val name: String,
    val artist: String,
    val artUri: Uri,
    val songCount: Int = 0,
    val year: Int = 0,
)

data class LocalArtist(
    val id: Long,
    val name: String,
    val songCount: Int = 0,
    val albumCount: Int = 0,
    val imageUri: String? = null,
)

data class LocalGenre(
    val id: Long,
    val name: String,
    val songCount: Int = 0,
)

class LocalMusicScanner(private val context: Context) {

    private val albumArtUri = Uri.parse("content://media/external/audio/albumart")

    suspend fun scanSongs(minDurationSeconds: Int = 30): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val minDurationMs = minDurationSeconds * 1000L

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(minDurationMs.toString())
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val albumId = cursor.getLong(albumIdCol)
                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val albumArt = ContentUris.withAppendedId(albumArtUri, albumId)

                songs.add(
                    Song(
                        id = "local_$id",
                        title = cursor.getString(titleCol) ?: "Unknown",
                        artist = cursor.getString(artistCol) ?: "Unknown",
                        album = cursor.getString(albumCol) ?: "Unknown",
                        coverUrl = albumArt.toString(),
                        mediaUrl = contentUri.toString(),
                        durationMs = cursor.getLong(durationCol),
                        source = SongSource.LOCAL,
                        filePath = cursor.getString(dataCol),
                        albumId = albumId,
                        artistId = cursor.getLong(artistIdCol),
                        year = cursor.getInt(yearCol),
                        trackNumber = cursor.getInt(trackCol),
                        dateAdded = cursor.getLong(dateAddedCol),
                    )
                )
            }
        }
        songs
    }

    suspend fun scanAlbums(): List<LocalAlbum> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<LocalAlbum>()
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums.FIRST_YEAR,
        )

        context.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            projection, null, null,
            "${MediaStore.Audio.Albums.ALBUM} ASC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            val countCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                albums.add(
                    LocalAlbum(
                        id = id,
                        name = cursor.getString(albumCol) ?: "Unknown",
                        artist = cursor.getString(artistCol) ?: "Unknown",
                        artUri = ContentUris.withAppendedId(albumArtUri, id),
                        songCount = cursor.getInt(countCol),
                        year = cursor.getInt(yearCol),
                    )
                )
            }
        }
        albums
    }

    suspend fun scanArtists(): List<LocalArtist> = withContext(Dispatchers.IO) {
        val artists = mutableListOf<LocalArtist>()
        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
        )

        // Build a map of artist name -> first album art URI
        val artistImageMap = mutableMapOf<String, String>()
        context.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ARTIST),
            null, null, null,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            while (cursor.moveToNext()) {
                val artist = cursor.getString(artistCol) ?: continue
                if (artist !in artistImageMap) {
                    val albumId = cursor.getLong(idCol)
                    artistImageMap[artist] = ContentUris.withAppendedId(albumArtUri, albumId).toString()
                }
            }
        }

        context.contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            projection, null, null,
            "${MediaStore.Audio.Artists.ARTIST} ASC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)

            while (cursor.moveToNext()) {
                val name = cursor.getString(artistCol) ?: "Unknown"
                artists.add(
                    LocalArtist(
                        id = cursor.getLong(idCol),
                        name = name,
                        songCount = cursor.getInt(trackCol),
                        albumCount = cursor.getInt(albumCol),
                        imageUri = artistImageMap[name],
                    )
                )
            }
        }
        artists
    }

    suspend fun scanGenres(): List<LocalGenre> = withContext(Dispatchers.IO) {
        val genres = mutableListOf<LocalGenre>()
        val projection = arrayOf(
            MediaStore.Audio.Genres._ID,
            MediaStore.Audio.Genres.NAME,
        )

        context.contentResolver.query(
            MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
            projection, null, null,
            "${MediaStore.Audio.Genres.NAME} ASC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol) ?: continue
                genres.add(LocalGenre(id = id, name = name))
            }
        }
        genres
    }

    suspend fun getSongsByAlbumId(albumId: Long): List<Song> {
        return scanSongs().filter { it.albumId == albumId }
    }

    suspend fun getSongsByArtistId(artistId: Long): List<Song> {
        return scanSongs().filter { it.artistId == artistId }
    }

    fun getSongsByFolder(songs: List<Song>): Map<String, List<Song>> {
        return songs.filter { it.filePath != null }
            .groupBy { it.filePath!!.substringBeforeLast("/") }
    }
}
