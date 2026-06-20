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
                message = "vidZest ai dashboard initialized.",
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

    // --- Video Generation Tool integration states ---
    var isDraftingSessionActive by mutableStateOf(false)
    var activeDraftVideoEntity by mutableStateOf<VideoEntity?>(null)
    var activeDraftScenes by mutableStateOf<List<VideoDraftScene>>(emptyList())
    var isGeneratingDrafts by mutableStateOf(false)
    var draftGenerationProgress by mutableStateOf(0f)
    var selectedDraftStyle by mutableStateOf("Hyper-realistic Cinema")
    var selectedDraftMusic by mutableStateOf("Vibrant Tech Synth")
    var isVideoDraftCompiled by mutableStateOf(false)
    var isDraftMusicPlaying by mutableStateOf(false)
    var activeDraftTimeCursor by mutableStateOf(0L) // simulated timeline position in milliseconds

    fun openDraftingStudio(video: VideoEntity) {
        activeDraftVideoEntity = video
        isDraftingSessionActive = true
        isVideoDraftCompiled = false
        isDraftMusicPlaying = false
        activeDraftTimeCursor = 0L
        
        // Parse the script into dynamic storyboard scene drafts
        val parsed = parseScriptToDraftScenes(video.script)
        activeDraftScenes = if (parsed.isNotEmpty()) parsed else listOf(
            VideoDraftScene("sc1", "0:00 - 0:10", "Introduction to: ${video.trendingTopic}", "Close shot of YouTube interface"),
            VideoDraftScene("sc2", "0:10 - 0:30", "Deep explanation of niche topic: ${video.niche}", "AI concept art with binary overlays"),
            VideoDraftScene("sc3", "0:30 - 1:00", "Actionable visual summary & CTA", "High-contrast end frame subscription button slider")
        )
    }

    fun closeDraftingStudio() {
        isDraftingSessionActive = false
        activeDraftVideoEntity = null
        isDraftMusicPlaying = false
    }

    fun generateVideoVisualDrafts() {
        if (isGeneratingDrafts) return
        isGeneratingDrafts = true
        draftGenerationProgress = 0f
        isVideoDraftCompiled = false
        
        viewModelScope.launch {
            val video = activeDraftVideoEntity ?: return@launch
            
            repository.log(
                agent = "Designer",
                level = "INFO",
                message = "AI Video Generation Tool Handshake Initiated.",
                details = "Topic: '${video.trendingTopic}'\\nTarget Style: $selectedDraftStyle\\nScene Elements Space size: ${activeDraftScenes.size} scenes."
            )
            
            val updatedScenes = activeDraftScenes.mapIndexed { idx, scene ->
                delay(1200) // Simulating frame drawing
                draftGenerationProgress = (idx + 1).toFloat() / activeDraftScenes.size
                
                val seedPrompt = "${video.niche} ${scene.brollSuggestion}, style: $selectedDraftStyle, high fidelity, vivid colors"
                val imageUrl = getDraftSceneThematicImage(seedPrompt, video.niche, selectedDraftStyle)
                
                repository.log(
                    agent = "Designer",
                    level = "INFO",
                    message = "Synthesized Video Draft Frame for Scene #${idx + 1} ('${scene.timestamp}')",
                    details = "Prompt used: $seedPrompt\\nResolved Draft Frame URL: $imageUrl"
                )
                
                scene.copy(frameUrl = imageUrl, visualStyleApplied = selectedDraftStyle)
            }
            
            activeDraftScenes = updatedScenes
            isVideoDraftCompiled = true
            isGeneratingDrafts = false
            draftGenerationProgress = 1.0f
            
            repository.log(
                agent = "Publisher",
                level = "INFO",
                message = "Full Video visual drafts pipeline compiled successfully!",
                details = "Stitched ${updatedScenes.size} frames with dynamic soundtrack '$selectedDraftMusic'. Draft is fully renderable in dashboard player preview."
            )
        }
    }

    private fun getDraftSceneThematicImage(prompt: String, niche: String, style: String): String {
        val techList = listOf(
            "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&q=80&w=700",
            "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&q=80&w=700",
            "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&q=80&w=700",
            "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&q=80&w=700"
        )
        val financeList = listOf(
            "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?auto=format&fit=crop&q=80&w=700",
            "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&q=80&w=700",
            "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?auto=format&fit=crop&q=80&w=700"
        )
        val designList = listOf(
            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&q=80&w=700",
            "https://images.unsplash.com/photo-1561070791-26c113006238?auto=format&fit=crop&q=80&w=700",
            "https://images.unsplash.com/photo-1558655146-d09347e92766?auto=format&fit=crop&q=80&w=700"
        )
        val genericList = listOf(
            "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?auto=format&fit=crop&q=80&w=700",
            "https://images.unsplash.com/photo-1536240478700-b869070f9279?auto=format&fit=crop&q=80&w=700",
            "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?auto=format&fit=crop&q=80&w=700"
        )

        val lowerNiche = niche.lowercase()
        val list = when {
            lowerNiche.contains("tech") || lowerNiche.contains("saas") || lowerNiche.contains("space") -> techList
            lowerNiche.contains("money") || lowerNiche.contains("secret") || lowerNiche.contains("market") -> financeList
            lowerNiche.contains("horror") || lowerNiche.contains("trivia") -> designList
            else -> genericList
        }
        val seed = Math.abs(prompt.hashCode())
        return list[seed % list.size]
    }

    // --- Social Media Sharing Integration states ---
    var isSharingSessionActive by mutableStateOf(false)
    var activeSharingVideoEntity by mutableStateOf<VideoEntity?>(null)
    var selectedSharingPlatforms by mutableStateOf(setOf("Twitter/X", "Facebook", "Reddit"))
    
    var twitterPostCopy by mutableStateOf("")
    var facebookPostCopy by mutableStateOf("")
    var redditPostCopy by mutableStateOf("")
    var redditPostTitle by mutableStateOf("")
    var redditSubreddit by mutableStateOf("r/videos")
    
    var isGeneratingSocialPosts by mutableStateOf(false)
    var isSocialPostingInProgress by mutableStateOf(false)
    var socialPostingProgress by mutableStateOf(0f)
    var currentPostingPlatform by mutableStateOf("")
    var successfulSharedLogs by mutableStateOf<List<String>>(emptyList())

    fun openSharingStudio(video: VideoEntity) {
        activeSharingVideoEntity = video
        isSharingSessionActive = true
        isSocialPostingInProgress = false
        socialPostingProgress = 0f
        currentPostingPlatform = ""
        successfulSharedLogs = emptyList()
        selectedSharingPlatforms = setOf("Twitter/X", "Facebook", "Reddit")
        
        generateSocialPostsCopy(video)
    }

    fun closeSharingStudio() {
        isSharingSessionActive = false
        activeSharingVideoEntity = null
    }

    fun toggleSharingPlatform(platform: String) {
        selectedSharingPlatforms = if (selectedSharingPlatforms.contains(platform)) {
            selectedSharingPlatforms - platform
        } else {
            selectedSharingPlatforms + platform
        }
    }

    fun generateSocialPostsCopy(video: VideoEntity) {
        viewModelScope.launch {
            isGeneratingSocialPosts = true
            delay(1000) // Aesthetic delay for AI synthesis scanning
            val title = if (video.optimizedTitle.isNotEmpty()) video.optimizedTitle else video.trendingTopic
            
            // Twitter/X Copy: punchy hook, key point outline, standard action link
            twitterPostCopy = "🚀 NEW VIDEO ALERT!\n\n\"$title\"\n\nEver wanted to solve the mystery behind the ${video.niche}? Here is a quick 3-minute breakdown of the absolute essentials!\n\n👇 Full video in comments!\n#${video.niche.replace(" ", "").replace("&", "")} #youtube #trends"
            
            // Facebook Copy: engaging conversational tone
            facebookPostCopy = "🎬 Freshly customized and published YouTube topic: \"$title\"!\n\nWe deep-dive into the secrets of the ${video.niche} to figure out what's working right now. Check out the storyboard structure, scripts, and production cues we used to optimize this.\n\nLet us know in the comments if you have any questions! 🚀\n\n📺 Video watch link in first comment or bio.\n#contentcreator #automation #marketing"
            
            // Reddit Subreddit Recommendation based on Niche keyword matching
            val lowerNiche = video.niche.lowercase()
            redditSubreddit = when {
                lowerNiche.contains("tech") || lowerNiche.contains("saas") || lowerNiche.contains("dev") -> "r/technology"
                lowerNiche.contains("money") || lowerNiche.contains("wealth") || lowerNiche.contains("finance") || lowerNiche.contains("stock") -> "r/personalfinance"
                lowerNiche.contains("horror") || lowerNiche.contains("spooky") || lowerNiche.contains("scary") -> "r/horror"
                lowerNiche.contains("trivia") || lowerNiche.contains("fun") || lowerNiche.contains("fact") -> "r/todayilearned"
                else -> "r/videos"
            }
            
            redditPostTitle = "How we automated a complete visual video on '$title' using AI agents"
            redditPostCopy = "We recently set up an AI agent workflow targeting the **${video.niche}** niche. \n\nOur system produced research coordinates, drafted full-length timelines, generated cinematic image frames, and optimized YouTube descriptions automatically.\n\nHere's the visual prompt layout or insights we logged:\n- **Niche Focus**: ${video.niche}\n- **Core Topic**: ${video.trendingTopic}\n\nWhat are your thoughts on agentic content publishing workflows? Do they save meaningful time for video teams, or do they reduce authentic value?"
            
            isGeneratingSocialPosts = false
        }
    }

    fun publishSocialPosts() {
        if (isSocialPostingInProgress) return
        val video = activeSharingVideoEntity ?: return
        val platformsToPost = selectedSharingPlatforms.toList()
        if (platformsToPost.isEmpty()) return
        
        isSocialPostingInProgress = true
        socialPostingProgress = 0f
        
        viewModelScope.launch {
            val shareHistory = mutableListOf<String>()
            if (video.sharedPlatforms.isNotEmpty()) {
                shareHistory.addAll(video.sharedPlatforms.split(", "))
            }
            
            platformsToPost.forEachIndexed { index, platform ->
                currentPostingPlatform = platform
                // Simulate publishing latency for security handshakes, metadata uploads, api response waits
                val steps = 5
                for (step in 1..steps) {
                    delay(500)
                    socialPostingProgress = (index.toFloat() + (step.toFloat() / steps)) / platformsToPost.size
                }
                
                shareHistory.add(platform)
                
                val postSampleText = when (platform) {
                    "Twitter/X" -> twitterPostCopy
                    "Facebook" -> facebookPostCopy
                    "Reddit" -> "[$redditSubreddit] $redditPostTitle"
                    else -> ""
                }
                
                repository.log(
                    agent = "Publisher",
                    level = "INFO",
                    message = "Successfully micro-blogged video post update to $platform!",
                    details = "Platform: $platform\\nContent Length: ${postSampleText.length} characters.\\nSample:\\n\"$postSampleText\""
                )
            }
            
            // Mark as shared in database
            val distinctShares = shareHistory.distinct().joinToString(", ")
            val updatedVideo = video.copy(sharedPlatforms = distinctShares)
            repository.updateVideo(updatedVideo)
            activeSharingVideoEntity = updatedVideo
            
            isSocialPostingInProgress = false
            currentPostingPlatform = ""
            successfulSharedLogs = platformsToPost
            
            repository.log(
                agent = "Publisher",
                level = "INFO",
                message = "Social syndication complete for video: '${video.optimizedTitle.ifEmpty { video.trendingTopic }}'",
                details = "Successfully cross-posted to channels: $distinctShares"
            )
        }
    }
}

data class VideoDraftScene(
    val id: String,
    val timestamp: String,
    val narration: String,
    val brollSuggestion: String,
    val frameUrl: String = "",
    val visualStyleApplied: String = ""
)

fun parseScriptToDraftScenes(scriptText: String): List<VideoDraftScene> {
    val scenes = mutableListOf<VideoDraftScene>()
    if (scriptText.isBlank()) return scenes

    try {
        val trimmed = scriptText.trim()
        val json = if (trimmed.startsWith("{")) {
            org.json.JSONObject(trimmed)
        } else {
            val startIdx = trimmed.indexOf("{")
            val endIdx = trimmed.lastIndexOf("}")
            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                org.json.JSONObject(trimmed.substring(startIdx, endIdx + 1))
            } else {
                null
            }
        }

        if (json != null) {
            val scriptArray = when {
                json.has("script") -> json.get("script")
                json.has("shorts") -> json.get("shorts")
                else -> null
            }
            if (scriptArray is org.json.JSONArray) {
                for (i in 0 until scriptArray.length()) {
                    val item = scriptArray.getJSONObject(i)
                    scenes.add(
                        VideoDraftScene(
                            id = "json_$i",
                            timestamp = item.optString("timestamp", "00:${String.format("%02d", i * 15)}"),
                            narration = item.optString("narration", ""),
                            brollSuggestion = item.optString("visual_broll", item.optString("broll", "Illustrative clip showing scene context.")),
                            frameUrl = ""
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("ScriptToDraftScenes", "Failed JSON parse, fallback to text parser", e)
    }

    if (scenes.isEmpty()) {
        val sections = scriptText.split(Regex("(?=\\[\\d+:\\d+)"))
        var idx = 0
        for (sec in sections) {
            val trimmedSec = sec.trim()
            if (trimmedSec.isNotEmpty()) {
                val timestampPart = trimmedSec.substringBefore("]", "").removePrefix("[").trim()
                val timestamp = if (timestampPart.contains(":") || timestampPart.contains("-")) {
                    timestampPart
                } else {
                    "0:${String.format("%02d", idx * 15)}"
                }

                var visual = "High quality themed illustration clip"
                var narration = ""

                val lines = trimmedSec.split("\n")
                for (line in lines) {
                    val l = line.trim()
                    if (l.startsWith("VISUAL:", ignoreCase = true)) {
                        visual = l.substringAfter("VISUAL:", "").trim()
                    } else if (l.startsWith("NARRATOR:", ignoreCase = true)) {
                        narration = l.substringAfter("NARRATOR:", "").trim().removeSurrounding("\"")
                    } else if (l.startsWith("NARRATION:", ignoreCase = true)) {
                        narration = l.substringAfter("NARRATION:", "").trim().removeSurrounding("\"")
                    } else if (l.isNotEmpty() && !l.startsWith("[") && narration.isEmpty()) {
                        if (!l.startsWith("VISUAL:", ignoreCase = true) && !l.startsWith("AUDIO:", ignoreCase = true)) {
                            narration = l
                        }
                    }
                }

                if (narration.isNotEmpty() || visual.isNotEmpty()) {
                    scenes.add(
                        VideoDraftScene(
                            id = "txt_$idx",
                            timestamp = timestamp,
                            narration = if (narration.isNotEmpty()) narration else "No verbal cue, dynamic background audio track.",
                            brollSuggestion = visual,
                            frameUrl = ""
                        )
                    )
                    idx++
                }
            }
        }
    }

    if (scenes.isEmpty()) {
        val paras = scriptText.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
        paras.forEachIndexed { i, p ->
            if (!p.startsWith("🎬") && !p.startsWith("---")) {
                scenes.add(
                    VideoDraftScene(
                        id = "fallback_$i",
                        timestamp = "00:${String.format("%02d", i * 15)}",
                        narration = p.replace(Regex("\\[.*?\\]"), "").trim(),
                        brollSuggestion = "Thematic imagery representing: ${p.take(40)}...",
                        frameUrl = ""
                    )
                )
            }
        }
    }

    return scenes
}
