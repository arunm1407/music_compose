package com.example.myapplication.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.db.dao.*
import com.example.myapplication.data.db.entity.*

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        FavoriteEntity::class,
        PlayHistoryEntity::class,
        PlayCountEntity::class,
        LyricsEntity::class,
        SongCacheEntity::class,
        DownloadEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun playCountDao(): PlayCountDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun songCacheDao(): SongCacheDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS song_cache (
                        songId TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL DEFAULT '',
                        artist TEXT NOT NULL DEFAULT '',
                        album TEXT NOT NULL DEFAULT '',
                        coverUrl TEXT NOT NULL DEFAULT '',
                        mediaUrl TEXT NOT NULL DEFAULT '',
                        durationMs INTEGER NOT NULL DEFAULT 0,
                        source TEXT NOT NULL DEFAULT 'STREAM',
                        filePath TEXT,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS downloads (
                        songId TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        artist TEXT NOT NULL,
                        album TEXT NOT NULL,
                        coverUrl TEXT NOT NULL,
                        mediaUrl TEXT NOT NULL,
                        localPath TEXT NOT NULL,
                        durationMs INTEGER NOT NULL DEFAULT 0,
                        downloadedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent(),
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dvibess_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
