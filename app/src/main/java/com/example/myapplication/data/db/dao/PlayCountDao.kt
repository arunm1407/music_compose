package com.example.myapplication.data.db.dao

import androidx.room.*
import com.example.myapplication.data.db.entity.PlayCountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayCountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayCount(playCount: PlayCountEntity)

    @Query("SELECT * FROM play_count WHERE songId = :songId")
    suspend fun getPlayCount(songId: String): PlayCountEntity?

    @Query("SELECT * FROM play_count ORDER BY count DESC LIMIT :limit")
    fun getTopPlayed(limit: Int = 50): Flow<List<PlayCountEntity>>

    @Query("DELETE FROM play_count")
    suspend fun clearPlayCounts()
}
