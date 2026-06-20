package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoEntity
import com.example.ui.YoutubeViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@Composable
fun DashboardScreen(viewModel: YoutubeViewModel) {
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
                        onDelete = { viewModel.deleteVideoRecord(video.id) }
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
    onDelete: () -> Unit
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
