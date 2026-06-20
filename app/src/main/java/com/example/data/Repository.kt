package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class YoutubeAutomationRepository(private val db: AppDatabase) {
    private val configDao = db.configDao()
    private val videoDao = db.videoDao()
    private val logDao = db.logDao()

    val allVideos: Flow<List<VideoEntity>> = videoDao.getAllVideos()
    val recentLogs: Flow<List<LogEntity>> = logDao.getRecentLogs()
    
    suspend fun saveConfig(key: String, value: String) {
        configDao.saveConfig(ConfigEntity(key, value))
    }

    suspend fun getConfigValue(key: String, default: String): String {
        return configDao.getConfig(key)?.value ?: default
    }

    suspend fun insertVideo(video: VideoEntity): Long {
        return videoDao.insertVideo(video)
    }

    suspend fun updateVideo(video: VideoEntity) {
        videoDao.updateVideo(video)
    }

    suspend fun getVideoById(id: Long): VideoEntity? {
        return videoDao.getVideoById(id)
    }

    suspend fun deleteVideo(id: Long) {
        videoDao.deleteVideo(id)
    }

    suspend fun log(agent: String, level: String, message: String, details: String = "") {
        val logEntity = LogEntity(
            agentName = agent,
            level = level,
            message = message,
            details = details
        )
        logDao.insertLog(logEntity)
    }

    suspend fun clearLogs() {
        logDao.clearLogs()
    }
}
