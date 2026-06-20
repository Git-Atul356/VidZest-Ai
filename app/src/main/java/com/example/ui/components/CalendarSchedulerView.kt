package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoEntity
import com.example.ui.YoutubeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MonthlyCalendarView(
    videos: List<VideoEntity>,
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentDisplayMonth by remember(selectedDate) { mutableStateOf(selectedDate.get(Calendar.MONTH)) }
    var currentDisplayYear by remember(selectedDate) { mutableStateOf(selectedDate.get(Calendar.YEAR)) }

    val calendarInstance = remember(currentDisplayMonth, currentDisplayYear) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentDisplayYear)
            set(Calendar.MONTH, currentDisplayMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val daysInMonth = calendarInstance.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendarInstance.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...

    val monthName = remember(currentDisplayMonth) {
        val format = SimpleDateFormat("MMMM", Locale.getDefault())
        format.format(calendarInstance.time)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Calendar Month-Year selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentDisplayMonth == Calendar.JANUARY) {
                            currentDisplayMonth = Calendar.DECEMBER
                            currentDisplayYear -= 1
                        } else {
                            currentDisplayMonth -= 1
                        }
                    },
                    modifier = Modifier.testTag("prev_month_btn")
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }

                Text(
                    text = "$monthName $currentDisplayYear",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("calendar_month_year_header")
                )

                IconButton(
                    onClick = {
                        if (currentDisplayMonth == Calendar.DECEMBER) {
                            currentDisplayMonth = Calendar.JANUARY
                            currentDisplayYear += 1
                        } else {
                            currentDisplayMonth += 1
                        }
                    },
                    modifier = Modifier.testTag("next_month_btn")
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weekday Headers
            val weekdays = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
            Row(modifier = Modifier.fillMaxWidth()) {
                weekdays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid Layout calculation of days
            val paddingDays = firstDayOfWeek - 1
            val totalCells = paddingDays + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - paddingDays + 1

                        if (dayNumber in 1..daysInMonth) {
                            val hasVideo = remember(videos, currentDisplayYear, currentDisplayMonth, dayNumber) {
                                videos.any { video ->
                                    val videoCal = Calendar.getInstance().apply { timeInMillis = video.scheduledPublishTime }
                                    videoCal.get(Calendar.YEAR) == currentDisplayYear &&
                                            videoCal.get(Calendar.MONTH) == currentDisplayMonth &&
                                            videoCal.get(Calendar.DAY_OF_MONTH) == dayNumber
                                }
                            }

                            val isSelected = selectedDate.get(Calendar.YEAR) == currentDisplayYear &&
                                    selectedDate.get(Calendar.MONTH) == currentDisplayMonth &&
                                    selectedDate.get(Calendar.DAY_OF_MONTH) == dayNumber

                            val bgSelected = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            val borderSelected = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant
                            val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(bgSelected)
                                    .border(1.dp, borderSelected, CircleShape)
                                    .clickable {
                                        val newSelection = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, currentDisplayYear)
                                            set(Calendar.MONTH, currentDisplayMonth)
                                            set(Calendar.DAY_OF_MONTH, dayNumber)
                                        }
                                        onDateSelected(newSelection)
                                    }
                                    .testTag("calendar_day_$dayNumber"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNumber.toString(),
                                        color = textColor,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (hasVideo) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 1.dp)
                                                .size(5.dp)
                                                .background(
                                                    if (isSelected) Color.White else Color(0xFFFF0000), // YouTube red
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoReschedulingDialog(
    video: VideoEntity,
    onDismiss: () -> Unit,
    onReschedule: (Long) -> Unit
) {
    var selectedCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            timeInMillis = video.scheduledPublishTime.coerceAtLeast(System.currentTimeMillis())
        })
    }

    var selectedHour by remember { mutableStateOf(selectedCalendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(selectedCalendar.get(Calendar.MINUTE)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Simulate Publish Date",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Rescheduling: ${if (video.optimizedTitle.isNotEmpty()) video.optimizedTitle else video.trendingTopic}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Calendar element
                MonthlyCalendarView(
                    videos = emptyList(), // Standard simple calendar inside selector dialog
                    selectedDate = selectedCalendar,
                    onDateSelected = { newDate ->
                        selectedCalendar = Calendar.getInstance().apply {
                            timeInMillis = newDate.timeInMillis
                            set(Calendar.HOUR_OF_DAY, selectedHour)
                            set(Calendar.MINUTE, selectedMinute)
                        }
                    }
                )

                // Time selector
                Column {
                    val amPm = if (selectedHour >= 12) "PM" else "AM"
                    val displayHour = when {
                        selectedHour == 0 -> 12
                        selectedHour > 12 -> selectedHour - 12
                        else -> selectedHour
                    }
                    Text(
                        text = "Publication Time: ${String.format("%02d:%02d %s", displayHour, selectedMinute, amPm)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Hour (24h format): $selectedHour", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Slider(
                        value = selectedHour.toFloat(),
                        onValueChange = {
                            selectedHour = it.toInt()
                            selectedCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                        },
                        valueRange = 0f..23f,
                        steps = 23,
                        modifier = Modifier.fillMaxWidth().testTag("hour_slider")
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Minute: $selectedMinute", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Slider(
                        value = selectedMinute.toFloat(),
                        onValueChange = {
                            selectedMinute = it.toInt()
                            selectedCalendar.set(Calendar.MINUTE, selectedMinute)
                        },
                        valueRange = 0f..59f,
                        steps = 59,
                        modifier = Modifier.fillMaxWidth().testTag("minute_slider")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onReschedule(selectedCalendar.timeInMillis)
                },
                modifier = Modifier.testTag("confirm_reschedule_button")
            ) {
                Text("Confirm Simulated Date")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CalendarSchedulerPanel(
    viewModel: YoutubeViewModel,
    videos: List<VideoEntity>
) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var activeRescheduleVideo by remember { mutableStateOf<VideoEntity?>(null) }
    
    val filteredVideos = remember(videos, selectedDate) {
        videos.filter { video ->
            val videoCal = Calendar.getInstance().apply { timeInMillis = video.scheduledPublishTime }
            videoCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                    videoCal.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                    videoCal.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Render custom interactive monthly calendar grid
        MonthlyCalendarView(
            videos = videos,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )

        val dateString = remember(selectedDate) {
            SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(selectedDate.time)
        }

        // Header for selected date items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scheduled for $dateString",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${filteredVideos.size} videos",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        if (filteredVideos.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = "Empty Schedule",
                        tint = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No videos scheduled for this simulated day.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            filteredVideos.forEach { video ->
                VideoCalendarItemRow(
                    video = video,
                    onRescheduleClick = { activeRescheduleVideo = video },
                    onDelete = { viewModel.deleteVideoRecord(video.id) }
                )
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
                // Refresh our local selection to matching rescheduled date
                selectedDate = Calendar.getInstance().apply { timeInMillis = newTime }
            }
        )
    }
}

@Composable
fun VideoCalendarItemRow(
    video: VideoEntity,
    onRescheduleClick: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(video.scheduledPublishTime) { timeFormat.format(Date(video.scheduledPublishTime)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onRescheduleClick,
                        modifier = Modifier.size(32.dp).testTag("calendar_reschedule_action_${video.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = "Reschedule",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (video.optimizedTitle.isNotEmpty()) video.optimizedTitle else video.trendingTopic,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Niche: ${video.niche}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
