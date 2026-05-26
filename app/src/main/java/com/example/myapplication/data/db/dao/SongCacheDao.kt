package com.example.myapplication.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.db.entity.SongCacheEntity

@Dao
interface SongCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(song: SongCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(songs: List<SongCacheEntity>)

    @Query("SELECT * FROM song_cache WHERE songId IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<SongCacheEntity>

    @Query("SELECT * FROM song_cache WHERE songId = :id")
    suspend fun getById(id: String): SongCacheEntity?
}
