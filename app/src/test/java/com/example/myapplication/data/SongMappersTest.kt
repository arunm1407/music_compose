package com.example.myapplication.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SongMappersTest {

    private fun streamSong(
        id: String = "1",
        mediaUrl: String = "https://example.com/song.mp3",
    ) = Song(
        id = id,
        title = "Title",
        artist = "Artist",
        album = "Album",
        coverUrl = "https://example.com/cover.jpg",
        mediaUrl = mediaUrl,
    )

    @Test
    fun mergeSongForPlayback_prefersCatalogStreamWhenCacheMissingUrl() {
        val cached = streamSong(mediaUrl = "")
        val catalog = streamSong(mediaUrl = "https://example.com/stream.mp3")

        val merged = mergeSongForPlayback(cached, catalog)

        assertEquals("https://example.com/stream.mp3", merged?.mediaUrl)
    }

    @Test
    fun mergeSongForPlayback_returnsNullWhenBothMissing() {
        assertNull(mergeSongForPlayback(null, null))
    }

    @Test
    fun isPlayable_requiresPlaybackUri() {
        assertTrue(streamSong().isPlayable())
        assertFalse(streamSong(mediaUrl = "").isPlayable())
    }
}
