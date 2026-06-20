package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay
import com.example.data.VideoEntity
import com.example.ui.YoutubeViewModel
import com.example.ui.VideoDraftScene
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@Composable
fun DashboardScreen(viewModel: YoutubeViewModel) {
    if (viewModel.isSharingSessionActive) {
        SocialMediaShareStudio(viewModel = viewModel)
        return
    }

    if (viewModel.isDraftingSessionActive) {
        VideoVisualDraftsStudio(viewModel = viewModel)
        return
    }

    val videos by viewModel.videos.collectAsState()
    var isCalendarMode by remember { mutableStateOf(false) }
    var activeRescheduleVideo by remember { mutableStateOf<VideoEntity?>(null) }
    
    // Compute cumulative stats
    val totalViews = remember(videos) { videos.sumOf { it.views } }
    val totalWatchTime = remember(videos) { videos.sumOf { it.watchTimeMinutes } }
    val totalLikes = remember(videos) { videos.sumOf { it.likes } }
    val totalSubs = remember(videos) { videos.sumOf { it.subscribersGained } }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Hero Branding Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Channel Performance",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Active Niche: ${viewModel.activeNiche}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    FilledIconButton(
                        onClick = { viewModel.triggerSimulatedAnalytics() },
                        modifier = Modifier.testTag("refresh_analytics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Sync analytics simulation"
                        )
                    }
                }
            }
        }

        // Stats Row Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Subscribers",
                    value = formatStatValue(totalSubs + 1280), // Baseline channel size
                    icon = Icons.Default.People,
                    color = Color(0xFF3F51B5)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Views",
                    value = formatStatValue(totalViews + 45200),
                    icon = Icons.Default.PlayArrow,
                    color = Color(0xFFE91E63)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Watch Time (Min)",
                    value = formatStatValue(totalWatchTime + 135600),
                    icon = Icons.Default.Schedule,
                    color = Color(0xFF4CAF50)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Engagement",
                    value = formatStatValue(totalLikes + 3410),
                    icon = Icons.Default.ThumbUp,
                    color = Color(0xFFFF9800)
                )
            }
        }

        // Beautiful Interactive Growth Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Views Analytics Trend (24/7 Stream)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Simple Canvas views chart drawing
                    val chartData = remember(videos) {
                        val points = mutableListOf<Float>()
                        var currentVal = 1000f
                        points.add(currentVal)
                        
                        // Add elements of growth per video
                        val reversed = videos.reversed()
                        for (v in reversed) {
                            currentVal += (v.views / 20f).coerceAtLeast(30f)
                            points.add(currentVal)
                        }
                        
                        // pad to at least 6 points
                        while (points.size < 7) {
                            currentVal += Random.nextInt(20, 50)
                            points.add(currentVal)
                        }
                        points.takeLast(10)
                    }

                    val colorPrimary = MaterialTheme.colorScheme.primary

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val maxVal = chartData.maxOrNull() ?: 100f
                        val minVal = chartData.minOrNull() ?: 0f
                        val range = (maxVal - minVal).coerceAtLeast(1f)

                        val pointsCount = chartData.size
                        val stepX = width / (pointsCount - 1)

                        val path = Path()
                        val fillPath = Path()

                        chartData.forEachIndexed { idx, value ->
                            val x = idx * stepX
                            val fraction = (value - minVal) / range
                            val y = height - (fraction * (height - 30.dp.toPx())) - 10.dp.toPx()

                            if (idx == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, height)
                                fillPath.lineTo(x, y)
                            } else {
                                path.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }

                            if (idx == pointsCount - 1) {
                                fillPath.lineTo(x, height)
                                fillPath.close()
                            }
                            
                            // Draw circular dots
                            drawCircle(
                                color = colorPrimary,
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }

                        // Draw line
                        drawPath(
                            path = path,
                            color = colorPrimary,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw background gradient fill
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorPrimary.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mon", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("Wed", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("Fri", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("Today", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colorPrimary)
                    }
                }
            }
        }

        // Section Title: Upload Queue & Automation Schedule
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Automation Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${videos.size} Videos Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Beautiful interactive Toggle buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !isCalendarMode,
                        onClick = { isCalendarMode = false },
                        label = { Text("List Queue") },
                        leadingIcon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("toggle_list_mode")
                    )
                    FilterChip(
                        selected = isCalendarMode,
                        onClick = { isCalendarMode = true },
                        label = { Text("Calendar") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("toggle_calendar_mode")
                    )
                }
            }
        }

        if (isCalendarMode) {
            item {
                CalendarSchedulerPanel(
                    viewModel = viewModel,
                    videos = videos
                )
            }
        } else {
            if (videos.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "No simulated videos in SQLite",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Scheduled Uploads in Database",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Head to the Creator Lab page and trigger your agents to begin writing and scheduling!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(videos) { video ->
                    VideoScheduleListItem(
                        video = video,
                        onRescheduleClick = { activeRescheduleVideo = video },
                        onDelete = { viewModel.deleteVideoRecord(video.id) },
                        onDraftsClick = { viewModel.openDraftingStudio(video) },
                        onShareClick = { viewModel.openSharingStudio(video) }
                    )
                }
            }
        }
    }

    // Interactive Dialog triggers
    activeRescheduleVideo?.let { video ->
        VideoReschedulingDialog(
            video = video,
            onDismiss = { activeRescheduleVideo = null },
            onReschedule = { newTime ->
                viewModel.updateVideoSchedule(video.id, newTime)
                activeRescheduleVideo = null
            }
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun VideoScheduleListItem(
    video: VideoEntity,
    onRescheduleClick: () -> Unit,
    onDelete: () -> Unit,
    onDraftsClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(video.scheduledPublishTime) { formatter.format(Date(video.scheduledPublishTime)) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val badgeColor = if (video.status == "PUBLISHED" || video.scheduledPublishTime <= System.currentTimeMillis()) {
                            Color(0xFF4CAF50) // Green
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                        val statusText = if (video.status == "PUBLISHED" || video.scheduledPublishTime <= System.currentTimeMillis()) {
                            "PUBLISHED"
                        } else {
                            "SCHEDULED"
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = video.niche,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (video.optimizedTitle.isNotEmpty()) video.optimizedTitle else video.trendingTopic,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete configuration log",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Interactive clickable scheduling badge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { onRescheduleClick() }
                            .testTag("reschedule_trigger_${video.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Edit Schedule",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Publish: $formattedTime",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Interactive AI Visual Drafts Studio badge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { onDraftsClick() }
                            .testTag("drafts_trigger_${video.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MovieFilter,
                                contentDescription = "AI Visual Drafts",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🎬 AI Drafts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Interactive Social Cross-Poster share badge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { onShareClick() }
                            .testTag("share_trigger_${video.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Social Syndication Shares",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "📣 Share",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Simulated analytics performance metrics displays
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatStatValue(video.views),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatStatValue(video.likes),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

private fun formatStatValue(value: Long): String {
    return when {
        value >= 1000000 -> String.format("%.1fM", value / 1000000.0)
        value >= 1000 -> String.format("%.1fK", value / 1000.0)
        else -> value.toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoVisualDraftsStudio(viewModel: YoutubeViewModel) {
    val video = viewModel.activeDraftVideoEntity ?: return
    var activeSceneIndex by remember { mutableStateOf(0) }
    
    // Automatic scene traversal when music/preview starts playing
    LaunchedEffect(viewModel.isDraftMusicPlaying, viewModel.activeDraftScenes) {
        if (viewModel.isDraftMusicPlaying && viewModel.activeDraftScenes.isNotEmpty()) {
            while (viewModel.isDraftMusicPlaying) {
                delay(3000) // 3 seconds per draft frame
                activeSceneIndex = (activeSceneIndex + 1) % viewModel.activeDraftScenes.size
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("visual_drafts_panel")
    ) {
        // Studio Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.closeDraftingStudio() },
                    modifier = Modifier.testTag("close_drafts_studio")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to dashboard",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "vidZest AI Studio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Visual drafts for: ${video.niche.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "DRAFT ENGINE ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Active video title card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Video Concept:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = video.trendingTopic,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Split Layout (Top: Video player simulation / controls; Bottom Scroll: Storyboard list)
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Interactive Video Player Simulator
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .testTag("video_player_simulator"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (viewModel.isVideoDraftCompiled && viewModel.activeDraftScenes.isNotEmpty()) {
                            // Active compiled draft slideshow picture
                            val scene = viewModel.activeDraftScenes.getOrNull(activeSceneIndex)
                            if (scene != null && scene.frameUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = scene.frameUrl,
                                    contentDescription = "Visual draft scene rendering",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            // Dynamic high-contrast black vignette overlay for subtitles
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Black.copy(alpha = 0.4f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )

                            // Timestamp Tag Top-Right
                            Card(
                                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Scene ${activeSceneIndex + 1}/${viewModel.activeDraftScenes.size} [${viewModel.activeDraftScenes.getOrNull(activeSceneIndex)?.timestamp ?: "0:00"}]",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Subtitle overlay
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 50.dp) // Leave spot for player scrubber
                                    .padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = viewModel.activeDraftScenes.getOrNull(activeSceneIndex)?.narration ?: "",
                                        color = Color.Yellow,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Timeline & Audio Player Controls
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(6.dp)
                            ) {
                                // Scrubber / Timeline
                                LinearProgressIndicator(
                                    progress = if (viewModel.activeDraftScenes.isNotEmpty()) {
                                        (activeSceneIndex + 1).toFloat() / viewModel.activeDraftScenes.size
                                    } else 0f,
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = Color.Gray.copy(alpha = 0.5f)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { viewModel.isDraftMusicPlaying = !viewModel.isDraftMusicPlaying },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (viewModel.isDraftMusicPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                                                contentDescription = "Play preview",
                                                tint = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Music: ${viewModel.selectedDraftMusic}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.LightGray
                                        )
                                    }

                                    // Interactive Audio Waveform graphic when playing
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        repeat(5) { i ->
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(if (viewModel.isDraftMusicPlaying) (8 + (i * 3) % 10).dp else 4.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            )
                                        }
                                    }
                                }
                            }

                        } else {
                            // Uncompiled, click block to initialize visual drafts synthesis
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Generative Visual Storyboard Ready",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Stitch narrative scenes with customized image modeling style and backtracks below.",
                                    color = Color.LightGray,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Draft Styling and Orchestrator controls
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Draft Synthesis Configurations",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Choices: 1. Style Selection
                        Text(
                            text = "Generative Visual Style:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val styleOptions = listOf(
                            "Hyper-realistic Cinema",
                            "Cyberpunk Neon",
                            "Japanese Anime Art",
                            "Creative Flat Explainer",
                            "3D Claymation Pop"
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(styleOptions) { styleOpt ->
                                FilterChip(
                                    selected = viewModel.selectedDraftStyle == styleOpt,
                                    onClick = { viewModel.selectedDraftStyle = styleOpt },
                                    label = { Text(styleOpt, fontSize = 11.sp) },
                                    leadingIcon = if (viewModel.selectedDraftStyle == styleOpt) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    } else null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Choices: 2. Music Track selections
                        Text(
                            text = "Background Narrative Music Track:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val musicOptions = listOf(
                            "Vibrant Tech Synth",
                            "Chill Lofi HipHop",
                            "Cinematic Orchestra",
                            "No Music"
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(musicOptions) { musicOpt ->
                                FilterChip(
                                    selected = viewModel.selectedDraftMusic == musicOpt,
                                    onClick = { viewModel.selectedDraftMusic = musicOpt },
                                    label = { Text(musicOpt, fontSize = 11.sp) },
                                    leadingIcon = if (viewModel.selectedDraftMusic == musicOpt) {
                                        { Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    } else null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large action generator button
                        if (viewModel.isGeneratingDrafts) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LinearProgressIndicator(
                                    progress = viewModel.draftGenerationProgress,
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Orchestrating Visual Frames ... ${Math.round(viewModel.draftGenerationProgress * 100)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Button(
                                onClick = { viewModel.generateVideoVisualDrafts() },
                                modifier = Modifier.fillMaxWidth().testTag("compile_drafts_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (viewModel.isVideoDraftCompiled) "⚡ Regenerate Full Storyboard Draft" else "⚡ Turn Concept to Actual Visual Drafts"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (viewModel.activeDraftScenes.isNotEmpty()) {
                item {
                    Text(
                        text = "Storyboard Scenes Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(viewModel.activeDraftScenes.size) { idx ->
                    val scene = viewModel.activeDraftScenes[idx]
                    var expandedEdit by remember { mutableStateOf(false) }
                    var tempBrollPrompt by remember { mutableStateOf(scene.brollSuggestion) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeSceneIndex = idx },
                        colors = CardDefaults.cardColors(
                            containerColor = if (activeSceneIndex == idx) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            } else MaterialTheme.colorScheme.surface
                        ),
                        border = if (activeSceneIndex == idx) {
                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        } else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                color = if (activeSceneIndex == idx) MaterialTheme.colorScheme.primary else Color.Gray,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (idx + 1).toString(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "[${scene.timestamp}]",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                IconButton(
                                    onClick = { expandedEdit = !expandedEdit },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (expandedEdit) Icons.Default.Close else Icons.Default.Edit,
                                        contentDescription = "Modify scene cue",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Frame draft image
                                if (scene.frameUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = scene.frameUrl,
                                        contentDescription = "Scene draft thumbnail",
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "NARRATION:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = scene.narration,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "VISUAL B-ROLL:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = scene.brollSuggestion,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Interactive inline editor for scene custom parameters
                            AnimatedVisibility(visible = expandedEdit) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                        .background(Color.Black.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Modify Scene Visual Prompts Description:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = tempBrollPrompt,
                                        onValueChange = { tempBrollPrompt = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            viewModel.activeDraftScenes = viewModel.activeDraftScenes.mapIndexed { sIdx, sEntity ->
                                                if (sIdx == idx) sEntity.copy(brollSuggestion = tempBrollPrompt) else sEntity
                                            }
                                            expandedEdit = false
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Save Changes", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SOCIAL SYNDICATION CROSS-POSTER STUDIO
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaShareStudio(viewModel: YoutubeViewModel) {
    val video = viewModel.activeSharingVideoEntity ?: return
    var activePreviewTab by remember { mutableStateOf("Twitter/X") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("social_share_panel")
    ) {
        // App header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.closeSharingStudio() },
                    modifier = Modifier.testTag("close_share_studio")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to dashboard",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "vidZest Syndication Center",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Syndicating social posts for video releases",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "CROSS-POSTER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Active video content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (video.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = "Thumbnail preview",
                        modifier = Modifier
                            .size(70.dp, 40.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(70.dp, 40.dp)
                            .background(Color.Gray, RoundedCornerShape(4.dp))
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (video.optimizedTitle.isNotEmpty()) video.optimizedTitle else video.trendingTopic,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Status: ${video.status} | Niche: ${video.niche.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    
                    if (video.sharedPlatforms.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Shared on: ${video.sharedPlatforms}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Platform selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Target Social Media Syndication Networks",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Extract metadata automatically and dispatch custom-styled payloads concurrently.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val platforms = listOf("Twitter/X", "Facebook", "Reddit")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            platforms.forEach { platform ->
                                val isSelected = viewModel.selectedSharingPlatforms.contains(platform)
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.toggleSharingPlatform(platform) }
                                        .testTag("select_platform_$platform"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        }
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = when (platform) {
                                                "Twitter/X" -> Icons.Default.Share
                                                "Facebook" -> Icons.Default.Public
                                                else -> Icons.Default.Forum
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = platform,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (viewModel.isGeneratingSocialPosts) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Extracting video script telemetry & synthesizing social copy...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                item {
                    // Preview Tabs
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Twitter/X", "Facebook", "Reddit").forEach { tab ->
                                if (viewModel.selectedSharingPlatforms.contains(tab)) {
                                    ElevatedFilterChip(
                                        selected = activePreviewTab == tab,
                                        onClick = { activePreviewTab = tab },
                                        label = { Text(tab) },
                                        modifier = Modifier.testTag("preview_tab_$tab")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Active Tab Editor & Visual Feed Preview
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Post Copy Generation (Editable)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                when (activePreviewTab) {
                                    "Twitter/X" -> {
                                        OutlinedTextField(
                                            value = viewModel.twitterPostCopy,
                                            onValueChange = { viewModel.twitterPostCopy = it },
                                            modifier = Modifier.fillMaxWidth().height(110.dp).testTag("twitter_editor"),
                                            textStyle = MaterialTheme.typography.bodySmall
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Feed Preview Simulation (Twitter/X style Card)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Mock Twitter Post
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF15202B)),
                                            border = BorderStroke(1.dp, Color.DarkGray)
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                // Avatar
                                                Box(
                                                    modifier = Modifier.size(36.dp).background(Color.White, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("vZ", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("vidZest Automation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("@CreatorBot • Just now", color = Color.Gray, fontSize = 11.sp)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = viewModel.twitterPostCopy,
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    // Embedded URL Attachment Card (Twitter Card style)
                                                    Card(
                                                        shape = RoundedCornerShape(12.dp),
                                                        border = BorderStroke(1.dp, Color.DarkGray),
                                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF192734))
                                                    ) {
                                                        Column {
                                                            if (video.thumbnailUrl.isNotEmpty()) {
                                                                AsyncImage(
                                                                    model = video.thumbnailUrl,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.fillMaxWidth().height(115.dp),
                                                                    contentScale = ContentScale.Crop
                                                                )
                                                            } else {
                                                                Box(modifier = Modifier.fillMaxWidth().height(115.dp).background(Color.Gray))
                                                            }
                                                            Column(modifier = Modifier.padding(10.dp)) {
                                                                Text("youtube.com", color = Color.Gray, fontSize = 11.sp)
                                                                Text(
                                                                    text = if (video.optimizedTitle.isNotEmpty()) video.optimizedTitle else video.trendingTopic,
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 12.sp,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                Text(
                                                                    text = "Watch this exclusive structured automated video stream in high definition.",
                                                                    color = Color.Gray,
                                                                    fontSize = 11.sp,
                                                                    maxLines = 2,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    "Facebook" -> {
                                        OutlinedTextField(
                                            value = viewModel.facebookPostCopy,
                                            onValueChange = { viewModel.facebookPostCopy = it },
                                            modifier = Modifier.fillMaxWidth().height(110.dp).testTag("facebook_editor"),
                                            textStyle = MaterialTheme.typography.bodySmall
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Feed Preview Simulation (Facebook post)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Mock Facebook Post
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, Color.LightGray)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    // FB blue avatar
                                                    Box(
                                                        modifier = Modifier.size(36.dp).background(Color(0xFF1877F2), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Public, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                    }
                                                    Column {
                                                        Text("vidZest Creators Network", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        Text("Just now • Public 👥", color = Color.Gray, fontSize = 10.sp)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = viewModel.facebookPostCopy,
                                                    color = Color.Black,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))

                                                // Embedded FB large link card
                                                Card(
                                                    shape = RoundedCornerShape(0.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F3F5))
                                                ) {
                                                    Column {
                                                        if (video.thumbnailUrl.isNotEmpty()) {
                                                            AsyncImage(
                                                                model = video.thumbnailUrl,
                                                                contentDescription = null,
                                                                modifier = Modifier.fillMaxWidth().height(130.dp),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        } else {
                                                            Box(modifier = Modifier.fillMaxWidth().height(130.dp).background(Color.Gray))
                                                        }
                                                        Column(modifier = Modifier.padding(10.dp)) {
                                                            Text("YOUTUBE.COM", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                            Text(
                                                                text = if (video.optimizedTitle.isNotEmpty()) video.optimizedTitle else video.trendingTopic,
                                                                color = Color.Black,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    "Reddit" -> {
                                        // Subreddit suggestion line
                                        Text(
                                            text = "Target Subreddit recommendation:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = viewModel.redditSubreddit,
                                            onValueChange = { viewModel.redditSubreddit = it },
                                            modifier = Modifier.fillMaxWidth().testTag("reddit_subreddit_editor"),
                                            textStyle = MaterialTheme.typography.titleSmall
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "Reddit Thread Title:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = viewModel.redditPostTitle,
                                            onValueChange = { viewModel.redditPostTitle = it },
                                            modifier = Modifier.fillMaxWidth().testTag("reddit_title_editor"),
                                            textStyle = MaterialTheme.typography.bodySmall
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "Self-Post Core Text Body (Markdown style):",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = viewModel.redditPostCopy,
                                            onValueChange = { viewModel.redditPostCopy = it },
                                            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("reddit_body_editor"),
                                            textStyle = MaterialTheme.typography.bodySmall
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Feed Preview Simulation (Reddit self-post thread style)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Mock Reddit Post
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1B)),
                                            border = BorderStroke(1.dp, Color(0xFF343536))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // Reddit orange visual
                                                    Box(
                                                        modifier = Modifier.size(24.dp).background(Color(0xFFFF4500), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("r/", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = viewModel.redditSubreddit,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "• Posted by u/ContentSys",
                                                        color = Color.Gray,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = viewModel.redditPostTitle,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF272729)),
                                                    border = BorderStroke(1.dp, Color(0xFF343536))
                                                ) {
                                                    Text(
                                                        text = viewModel.redditPostCopy,
                                                        color = Color.LightGray,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.padding(10.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large actions cross-poster drawer buttons
        if (viewModel.isSocialPostingInProgress) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(
                        progress = viewModel.socialPostingProgress,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cross-posting to ${viewModel.currentPostingPlatform}... ${Math.round(viewModel.socialPostingProgress * 100)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Negotiating OAuth handles, writing meta tags, uploading video linkages",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        } else {
            if (viewModel.successfulSharedLogs.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF2E7D32)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Syndicated successfully!",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Published to: ${viewModel.successfulSharedLogs.joinToString(", ")}. Real-time agentic logs posted in Activity feed.",
                                color = Color.DarkGray,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.publishSocialPosts() },
                enabled = viewModel.selectedSharingPlatforms.isNotEmpty() && !viewModel.isGeneratingSocialPosts,
                modifier = Modifier.fillMaxWidth().testTag("publish_social_syndication_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PostAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (viewModel.successfulSharedLogs.isNotEmpty()) "⚡ Re-Publish social posts update" else "⚡ Publish & Sync social posts now",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

