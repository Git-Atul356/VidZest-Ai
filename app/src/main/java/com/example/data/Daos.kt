package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao {
    @Query("SELECT * FROM configs WHERE `key` = :key LIMIT 1")
    suspend fun getConfig(key: String): ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: ConfigEntity)

    @Query("SELECT * FROM configs")
    fun getAllConfigs(): Flow<List<ConfigEntity>>
}

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY timestamp DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity): Long

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): VideoEntity?

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideo(id: Long)
}

@Dao
interface LogDao {
    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<LogEntity>>

    @Insert
    suspend fun insertLog(log: LogEntity)

    @Query("DELETE FROM logs")
    suspend fun clearLogs()
}
