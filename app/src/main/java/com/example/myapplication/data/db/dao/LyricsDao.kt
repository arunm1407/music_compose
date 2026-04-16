package com.example.myapplication.data.db.dao

import androidx.room.*
import com.example.myapplication.data.db.entity.LyricsEntity

@Dao
interface LyricsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheLyrics(lyrics: LyricsEntity)

    @Query("SELECT * FROM cached_lyrics WHERE songId = :songId")
    suspend fun getCachedLyrics(songId: String): LyricsEntity?

    @Query("DELETE FROM cached_lyrics")
    suspend fun clearCache()
}
