package com.example.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

enum class AutomationStep {
    IDLE,
    RESEARCHING,
    SCRIPTING,
    OPTIMIZING,
    UPLOADING,
    COMPLETED
}

class YoutubeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = YoutubeAutomationRepository(db)
    private val aiService = AiAutomationService()

    // Database flow streams
    val videos: StateFlow<List<VideoEntity>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<LogEntity>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active configuration state
    var activeNiche by mutableStateOf("Tech Gossip")
        private set
        
    var selectedProvider by mutableStateOf("Gemini")
        private set

    var customApiKey by mutableStateOf("")
        private set

    // Workspace Active Agent state
    var isRunning by mutableStateOf(false)
        private set

    var currentStep by mutableStateOf(AutomationStep.IDLE)
        private set

    var researchResult by mutableStateOf("")
        private set

    var researchedTopics by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedTopic by mutableStateOf("")
        private set

    var generatedScript by mutableStateOf("")
        private set

    var optimizedTitle by mutableStateOf("")
        private set

    var optimizedDescription by mutableStateOf("")
        private set

    var optimizedTags by mutableStateOf("")
        private set

    var generatedThumbnail by mutableStateOf("")
        private set

    var errorState by mutableStateOf<String?>(null)
        private set

    var isResearchingBySearch by mutableStateOf(false)
        private set

    var groundedResearchResult by mutableStateOf<GroundedResearchResult?>(null)
        private set

    var groundedResearchError by mutableStateOf<String?>(null)
        private set

    init {
        // Load configurations on initialization
        viewModelScope.launch {
            activeNiche = repository.getConfigValue("active_niche", "SaaS Marketing")
            selectedProvider = repository.getConfigValue("selected_provider", "Gemini")
            customApiKey = repository.getConfigValue("custom_api_key", "")
            
            // Log application startup
            repository.log(
                agent = "System",
                level = "INFO",
                message = "TubeAutomate dashboard initialized.",
                details = "Local Room SQLite persistent DB connected. Configs loaded safely."
            )
        }
    }

    // --- Configuration management ---
    fun updateNiche(newNiche: String) {
        activeNiche = newNiche
        viewModelScope.launch {
            repository.saveConfig("active_niche", newNiche)
            repository.log("System", "INFO", "Active automation niche updated to: $newNiche")
        }
    }

    fun updateProvider(newProvider: String) {
        selectedProvider = newProvider
        viewModelScope.launch {
            repository.saveConfig("selected_provider", newProvider)
            repository.log("System", "INFO", "AI target provider switched to: $newProvider")
        }
    }

    fun updateCustomKey(newKey: String) {
        customApiKey = newKey
        viewModelScope.launch {
            repository.saveConfig("custom_api_key", newKey)
            // Mask the key in logs for security!
            val maskedKey = if (newKey.length > 8) "${newKey.take(4)}...${newKey.takeLast(4)}" else "***"
            repository.log("System", "INFO", "Custom API key configured safely: $maskedKey")
        }
    }

    fun clearLogHistory() {
        viewModelScope.launch {
            repository.clearLogs()
            repository.log("System", "INFO", "Cleared local log history successfully.")
        }
    }

    // Secondary select topic trigger in UI list helper
    fun selectTopic(topic: String) {
        selectedTopic = topic
    }

    // --- Channel Analytics & Simulation Generator ---
    fun triggerSimulatedAnalytics() {
        viewModelScope.launch {
            repository.log("Publisher", "INFO", "Scanning YouTube API for channel metrics updates...")
            var updatedCount = 0
            
            // Loop through all saved videos and simulate organic growth
            val currentVideos = videos.value
            for (video in currentVideos) {
                if (video.status == "SCHEDULED" || video.status == "PUBLISHED") {
                    val isPublished = video.status == "PUBLISHED" || 
                                     (video.status == "SCHEDULED" && video.scheduledPublishTime <= System.currentTimeMillis())
                    
                    val newStatus = if (isPublished) "PUBLISHED" else "SCHEDULED"
                    
                    // Simulate organic views growth
                    val additionalViews = if (isPublished) Random.nextLong(150, 4200) else 0L
                    val additionalWatchHours = if (additionalViews > 0) additionalViews * Random.nextInt(2, 6) / 2 else 0L
                    val additionalLikes = if (additionalViews > 0) additionalViews * Random.nextInt(5, 12) / 100 else 0L
                    val additionalSubs = if (additionalViews > 0) additionalViews * Random.nextInt(1, 4) / 100 else 0L

                    val updatedVideo = video.copy(
                        status = newStatus,
                        views = video.views + additionalViews,
                        watchTimeMinutes = video.watchTimeMinutes + additionalWatchHours,
                        likes = video.likes + additionalLikes,
                        subscribersGained = video.subscribersGained + additionalSubs
                    )
                    
                    repository.updateVideo(updatedVideo)
                    updatedCount++
                }
            }
            
            repository.log(
                agent = "Publisher",
                level = "INFO",
                message = "Channel metrics update completed. Synced $updatedCount video charts.",
                details = "SQLite analytics refreshed with modern randomized CTR modeling."
            )
        }
    }

    // --- Core 4-Step Agent Pipeline ---
    fun runCompleteAutomationLifecycle() {
        if (isRunning) return
        isRunning = true
        errorState = null
        
        viewModelScope.launch {
            try {
                // STEP 1: Topic Research Agent
                currentStep = AutomationStep.RESEARCHING
                repository.log(
                    agent = "Researcher",
                    level = "INFO",
                    message = "Starting Topic Research Agent for Niche: '$activeNiche' using $selectedProvider API.",
                    details = "Prompt sent to isolate 5 click-worthy viral core topic structures."
                )
                
                researchResult = aiService.researchTopics(
                    niche = activeNiche,
                    apiProvider = selectedProvider,
                    customKey = customApiKey
                )
                
                // Parse topics from output (simple split by numbered list or bullet tags)
                val tempTopics = mutableListOf<String>()
                val lines = researchResult.split("\n")
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("1.") || trimmed.startsWith("2.") || trimmed.startsWith("3.") || trimmed.startsWith("4.") || trimmed.startsWith("5.") ||
                        trimmed.startsWith("-") || trimmed.startsWith("💡") || trimmed.startsWith("🚀")
                    ) {
                        // Extract Title
                        val titlePart = trimmed
                            .removePrefix("1.")
                            .removePrefix("2.")
                            .removePrefix("3.")
                            .removePrefix("4.")
                            .removePrefix("5.")
                            .removePrefix("-")
                            .removePrefix("💡")
                            .removePrefix("🚀")
                            .trim()
                            .removeSurrounding("\"")
                        if (titlePart.isNotEmpty()) {
                            tempTopics.add(titlePart)
                        }
                    }
                }
                researchedTopics = if (tempTopics.isNotEmpty()) tempTopics else listOf(
                    "The Secrets Behind $activeNiche No One Talks About",
                    "How to Automate $activeNiche with Zero Experience",
                    "The Future of $activeNiche in 2026",
                    "Why 99% of Beginners Fail at $activeNiche Explained"
                )
                
                // Select first parsed topic as default selection
                selectedTopic = researchedTopics.first()
                
                repository.log(
                    agent = "Researcher",
                    level = "INFO",
                    message = "Topic Research successfully completed. Found ${researchedTopics.size} recommendations.",
                    details = "Top proposal: '$selectedTopic'. Storing in temporary worker cache."
                )
                
                delay(1500) // Aesthetic delay for user step visual scanning

                // STEP 2: Script Writer Agent
                currentStep = AutomationStep.SCRIPTING
                repository.log(
                    agent = "Writer",
                    level = "INFO",
                    message = "Script Writer Agent engaged for topic: '$selectedTopic'.",
                    details = "Writing full retention-optimized 3-minute structural narration script."
                )
                
                generatedScript = aiService.writeScript(
                    topic = selectedTopic,
                    niche = activeNiche,
                    apiProvider = selectedProvider,
                    customKey = customApiKey
                )
                
                repository.log(
                    agent = "Writer",
                    level = "INFO",
                    message = " Narration Script generated successfully (${generatedScript.length} characters).",
                    details = "Structure includes dynamic intro hooks, 3 segment chapters, and custom CTAs."
                )
                
                delay(1500)

                // STEP 3: SEO Optimizer & Thumbnail Agent
                currentStep = AutomationStep.OPTIMIZING
                repository.log(
                    agent = "Designer",
                    level = "INFO",
                    message = "Thumbnail generation & SEO Optimizer Agent running.",
                    details = "Initiating parallel description crafting and high-contrast creative design."
                )
                
                val seo = aiService.generateSeo(
                    topic = selectedTopic,
                    script = generatedScript,
                    apiProvider = selectedProvider,
                    customKey = customApiKey
                )
                
                optimizedTitle = seo.title
                optimizedDescription = seo.description
                optimizedTags = seo.tags
                
                // Generate thumbnail URL or high quality unsplash vector search fallback
                generatedThumbnail = aiService.generateThumbnailUrl(
                    prompt = "$activeNiche: $selectedTopic",
                    apiProvider = selectedProvider,
                    customKey = customApiKey
                )
                
                repository.log(
                    agent = "Designer",
                    level = "INFO",
                    message = "SEO metadata & eye-catching thumbnail generated perfectly.",
                    details = "Title: '$optimizedTitle'\nThumbnail URL: $generatedThumbnail\nTags: $optimizedTags"
                )
                
                delay(1500)

                // STEP 4: Scheduler & Publisher
                currentStep = AutomationStep.UPLOADING
                repository.log(
                    agent = "Publisher",
                    level = "INFO",
                    message = "Packaging video file, thumbnail stream, and SEO details for upload.",
                    details = "Connecting to simulated YouTube Data API V3 endpoint securely. Fetching publish queues."
                )
                
                // Construct Tomorrow 9:00 AM as publication target
                val tomorrowCalendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val scheduledTime = tomorrowCalendar.timeInMillis
                
                val videoEntity = VideoEntity(
                    niche = activeNiche,
                    trendingTopic = selectedTopic,
                    script = generatedScript,
                    optimizedTitle = optimizedTitle,
                    optimizedDescription = optimizedDescription,
                    optimizedTags = optimizedTags,
                    thumbnailUrl = generatedThumbnail,
                    status = "SCHEDULED",
                    scheduledPublishTime = scheduledTime,
                    // Give some minor initial views simulation to look interesting
                    views = 0L,
                    watchTimeMinutes = 0L
                )
                
                val id = repository.insertVideo(videoEntity)
                
                repository.log(
                    agent = "Publisher",
                    level = "INFO",
                    message = "Video uploaded and scheduled successfully! Target Publish: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(scheduledTime))}",
                    details = "Inserted into Room SQLite database with Unique ID #$id."
                )

                currentStep = AutomationStep.COMPLETED
                delay(1000)
                currentStep = AutomationStep.IDLE
                isRunning = false
                
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Agent processing error"
                errorState = errorMsg
                repository.log(
                    agent = "System",
                    level = "ERROR",
                    message = "Automation Loop Aborted! $errorMsg",
                    details = "Trace: ${Log.getStackTraceString(e)}"
                )
                isRunning = false
                currentStep = AutomationStep.IDLE
            }
        }
    }
    
    // Direct scheduled delete option in list
    fun deleteVideoRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteVideo(id)
            repository.log("System", "INFO", "Removed scheduled video ID #$id from SQLite persistent cache.")
        }
    }

    fun updateVideoSchedule(id: Long, newScheduledTime: Long) {
        viewModelScope.launch {
            val video = videos.value.find { it.id == id }
            if (video != null) {
                val updatedVideo = video.copy(
                    scheduledPublishTime = newScheduledTime,
                    status = if (newScheduledTime <= System.currentTimeMillis()) "PUBLISHED" else "SCHEDULED"
                )
                repository.updateVideo(updatedVideo)
                val format = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(newScheduledTime))
                repository.log("Publisher", "INFO", "Rescheduled video #${id} ('${if (video.optimizedTitle.isNotEmpty()) video.optimizedTitle else video.trendingTopic}') to simulated date: $format")
            }
        }
    }

    fun performSearchGroundedResearch() {
        if (isResearchingBySearch) return
        isResearchingBySearch = true
        groundedResearchError = null
        
        viewModelScope.launch {
            try {
                repository.log(
                    agent = "Researcher",
                    level = "INFO",
                    message = "Triggering Google Search Grounded research for niche: '$activeNiche'",
                    details = "Constructing live tools config and querying Gemini $selectedProvider..."
                )
                
                val result = aiService.researchGroundedTopicsAndKeywords(
                    niche = activeNiche,
                    apiProvider = selectedProvider,
                    customKey = customApiKey
                )
                
                groundedResearchResult = result
                repository.log(
                    agent = "Researcher",
                    level = "INFO",
                    message = "Google Grounded Research complete. Found ${result.topics.size} trend topics and ${result.keywords.size} hot keywords.",
                    details = "Search Grounding references stored. Topics successfully created."
                )
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Research failed"
                groundedResearchError = errorMsg
                repository.log(
                    agent = "Researcher",
                    level = "ERROR",
                    message = "Grounded research failed: $errorMsg",
                    details = Log.getStackTraceString(e)
                )
            } finally {
                isResearchingBySearch = false
            }
        }
    }

    fun adoptGroundedTopic(topicTitle: String) {
        selectedTopic = topicTitle
        viewModelScope.launch {
            repository.log(
                agent = "System",
                level = "INFO",
                message = "Adopted trending topic: '$topicTitle'",
                details = "User bypassed fully random script topics and imported the grounded trend topic directly of choice."
            )
        }
    }
}
