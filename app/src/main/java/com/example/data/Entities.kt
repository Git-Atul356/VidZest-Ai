package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configs")
data class ConfigEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val niche: String,
    val trendingTopic: String,
    val script: String = "",
    val optimizedTitle: String = "",
    val optimizedDescription: String = "",
    val optimizedTags: String = "",
    val thumbnailUrl: String = "",
    val status: String, // "RESEARCHED", "SCRIPTED", "OPTIMIZED", "SCHEDULED", "PUBLISHED"
    val scheduledPublishTime: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    
    // Analytics (Simulated for feedback metric display)
    val views: Long = 0,
    val watchTimeMinutes: Long = 0,
    val likes: Long = 0,
    val subscribersGained: Long = 0
)

@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val agentName: String, // "Researcher", "Writer", "Designer", "Publisher", "Scheduler", "System"
    val level: String, // "INFO", "WARN", "ERROR"
    val message: String,
    val details: String = ""
)
