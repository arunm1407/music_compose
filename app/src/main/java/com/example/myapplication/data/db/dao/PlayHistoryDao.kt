package com.example.myapplication.data.db.dao

import androidx.room.*
import com.example.myapplication.data.db.entity.PlayHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {
    @Insert
    suspend fun logPlay(entry: PlayHistoryEntity)

    @Query("SELECT * FROM play_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<PlayHistoryEntity>>

    @Query("DELETE FROM play_history")
    suspend fun clearHistory()

    @Query("SELECT COUNT(*) FROM play_history")
    fun getHistoryCount(): Flow<Int>
}
