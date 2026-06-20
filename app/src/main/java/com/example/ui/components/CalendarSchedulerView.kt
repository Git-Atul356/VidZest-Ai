package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
    draggedVideo: VideoEntity? = null,
    dragPosition: Offset = Offset.Zero,
    onHoverDay: (Int?, Int?, Int?) -> Unit = { _, _, _ -> },
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

                            // Drag & Drop hover coordinates tracking
                            var cellCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
                            val isCurrentlyHovered = remember(dragPosition, cellCoords) {
                                cellCoords?.let { coords ->
                                    if (coords.isAttached && dragPosition != Offset.Zero) {
                                        val windowPos = coords.localToWindow(Offset.Zero)
                                        val size = coords.size
                                        dragPosition.x >= windowPos.x &&
                                                dragPosition.x <= windowPos.x + size.width &&
                                                dragPosition.y >= windowPos.y &&
                                                dragPosition.y <= windowPos.y + size.height
                                    } else false
                                } ?: false
                            }

                            // Trigger parent hover updates
                            LaunchedEffect(isCurrentlyHovered) {
                                if (isCurrentlyHovered) {
                                    onHoverDay(dayNumber, currentDisplayMonth, currentDisplayYear)
                                }
                            }

                            // Distinct styling for hover vs standard selection
                            val bgSelected = when {
                                isCurrentlyHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> Color.Transparent
                            }
                            val borderSelected = when {
                                isCurrentlyHovered -> androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                isSelected -> null
                                else -> androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            }
                            val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(bgSelected)
                                    .then(if (borderSelected != null) Modifier.border(borderSelected, CircleShape) else Modifier)
                                    .onGloballyPositioned { cellCoords = it }
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
                                        fontWeight = if (isSelected || isCurrentlyHovered) FontWeight.Bold else FontWeight.Normal,
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hour_slider")
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("minute_slider")
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
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var activeRescheduleVideo by remember { mutableStateOf<VideoEntity?>(null) }
    
    // Drag and drop parameters
    var draggedVideo by remember { mutableStateOf<VideoEntity?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }

    var hoveredDayNumber by remember { mutableStateOf<Int?>(null) }
    var hoveredMonth by remember { mutableStateOf<Int?>(null) }
    var hoveredYear by remember { mutableStateOf<Int?>(null) }

    val filteredVideos = remember(videos, selectedDate) {
        videos.filter { video ->
            val videoCal = Calendar.getInstance().apply { timeInMillis = video.scheduledPublishTime }
            videoCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                    videoCal.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                    videoCal.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)
        }
    }

    var parentCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { parentCoords = it }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Render custom interactive monthly calendar grid with Drag & Drop parameters
            MonthlyCalendarView(
                videos = videos,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                draggedVideo = draggedVideo,
                dragPosition = dragPosition,
                onHoverDay = { day, month, year ->
                    hoveredDayNumber = day
                    hoveredMonth = month
                    hoveredYear = year
                }
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

            // High Fidelity Tip Banner for Drag and Drop
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Tips",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "💡 Pro Tip: Long-press any video row to drag & drop it directly onto the calendar to reschedule!",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
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
                        draggedVideo = draggedVideo,
                        onDragStart = { v, offset ->
                            draggedVideo = v
                            dragPosition = offset
                        },
                        onDrag = { amount ->
                            dragPosition = dragPosition + amount
                        },
                        onDragEnd = {
                            if (draggedVideo != null && hoveredDayNumber != null && hoveredMonth != null && hoveredYear != null) {
                                val originalCal = Calendar.getInstance().apply { timeInMillis = draggedVideo!!.scheduledPublishTime }
                                val hour = originalCal.get(Calendar.HOUR_OF_DAY)
                                val minute = originalCal.get(Calendar.MINUTE)

                                val destinationCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, hoveredYear!!)
                                    set(Calendar.MONTH, hoveredMonth!!)
                                    set(Calendar.DAY_OF_MONTH, hoveredDayNumber!!)
                                    set(Calendar.HOUR_OF_DAY, hour)
                                    set(Calendar.MINUTE, minute)
                                }

                                viewModel.updateVideoSchedule(draggedVideo!!.id, destinationCal.timeInMillis)
                                val format = SimpleDateFormat("MMM dd", Locale.getDefault()).format(destinationCal.time)
                                Toast.makeText(context, "Rescheduled to $format via Drag-and-Drop!", Toast.LENGTH_SHORT).show()

                                // Transition current viewed schedule day to match the drop destination
                                selectedDate = destinationCal
                            }
                            draggedVideo = null
                            dragPosition = Offset.Zero
                            hoveredDayNumber = null
                            hoveredMonth = null
                            hoveredYear = null
                        },
                        onDragCancel = {
                            draggedVideo = null
                            dragPosition = Offset.Zero
                            hoveredDayNumber = null
                            hoveredMonth = null
                            hoveredYear = null
                        },
                        onRescheduleClick = { activeRescheduleVideo = video },
                        onDelete = { viewModel.deleteVideoRecord(video.id) }
                    )
                }
            }
        }

        // Live Floating Drag Preview Overlay
        if (draggedVideo != null && parentCoords != null) {
            val parentWindowPos = parentCoords!!.localToWindow(Offset.Zero)
            val computedLocalOffset = dragPosition - parentWindowPos
            val density = LocalDensity.current

            val localOffsetX = with(density) { (computedLocalOffset.x - 120.dp.toPx()).toDp() }
            val localOffsetY = with(density) { (computedLocalOffset.y - 45.dp.toPx()).toDp() }

            Box(
                modifier = Modifier
                    .offset(x = localOffsetX, y = localOffsetY)
                    .width(280.dp)
                    .graphicsLayer {
                        alpha = 0.90f
                        scaleX = 1.05f
                        scaleY = 1.05f
                        shadowElevation = 8.dp.toPx()
                    }
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarViewMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (hoveredDayNumber != null) {
                                "Drop for Date: ${hoveredDayNumber} day"
                            } else {
                                "Hold & Drag over Month day"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (draggedVideo!!.optimizedTitle.isNotEmpty()) draggedVideo!!.optimizedTitle else draggedVideo!!.trendingTopic,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                // Refresh our local selection to matching rescheduled date
                selectedDate = Calendar.getInstance().apply { timeInMillis = newTime }
            }
        )
    }
}

@Composable
fun VideoCalendarItemRow(
    video: VideoEntity,
    draggedVideo: VideoEntity?,
    onDragStart: (VideoEntity, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onRescheduleClick: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(video.scheduledPublishTime) { timeFormat.format(Date(video.scheduledPublishTime)) }

    val isThisItemDragged = draggedVideo?.id == video.id
    val cardAlpha = if (isThisItemDragged) 0.40f else 1.0f

    var itemCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = cardAlpha }
            .onGloballyPositioned { itemCoords = it }
            .pointerInput(video.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { touchOffset ->
                        itemCoords?.let { coords ->
                            if (coords.isAttached) {
                                val windowPosition = coords.localToWindow(touchOffset)
                                onDragStart(video, windowPosition)
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragCancel() }
                )
            }
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp),
        border = if (isExpanded) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
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
                        imageVector = Icons.Default.DragIndicator,
                        contentDescription = "Drag to Reschedule",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
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
                    
                    // Status Badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (video.status == "PUBLISHED") Color(0xFFE6F4EA) else Color(0xFFFEF7E0)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = video.status,
                            color = if (video.status == "PUBLISHED") Color(0xFF137333) else Color(0xFFB06000),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            fontSize = 8.sp
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onRescheduleClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("calendar_reschedule_action_${video.id}")
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

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Niche: ${video.niche}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Hide Metrics" else "Show Performance Metrics",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Expandable high fidelity Predicted vs. Actual Metrics Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                VideoPerformanceIndicators(video = video)
            }
        }
    }
}

@Composable
fun VideoPerformanceIndicators(video: VideoEntity) {
    val predCtr = remember(video.id) { 6.2f + (video.id * 1.3f) % 5f }
    val actualCtr = remember(video.id, video.views) {
        if (video.views > 0) {
            (predCtr + ((video.views % 21).toFloat() - 10f) / 10f).coerceAtLeast(1.5f)
        } else {
            0.0f
        }
    }
    
    val predictedViews = remember(video.id) { 1200L + (video.id * 431 % 4000) }
    val actualViews = video.views
    
    val viewsProgress = if (predictedViews > 0) {
        (actualViews.toFloat() / predictedViews.toFloat()).coerceIn(0f, 2f)
    } else {
        0f
    }
    
    val isPublished = video.status == "PUBLISHED"
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 Performance Forecast",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (isPublished) "Live Analysis" else "Estimated Projection",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPublished) Color(0xFF10B981) else Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Views comparison
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Views Target Progress",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${String.format("%,d", actualViews)} / ${String.format("%,d", predictedViews)} views",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        val progressColor = when {
            !isPublished -> MaterialTheme.colorScheme.outlineVariant
            viewsProgress >= 1.0f -> Color(0xFF10B981) // High performer green
            viewsProgress >= 0.7f -> MaterialTheme.colorScheme.primary // Average pink
            else -> Color(0xFFEF4444) // Underperformer red
        }
        
        LinearProgressIndicator(
            progress = { if (isPublished) viewsProgress.coerceAtMost(1f) else 0.1f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
        
        if (isPublished) {
            Text(
                text = if (viewsProgress >= 1.0f) {
                    "🎉 Exceeded prediction by ${(viewsProgress * 100 - 100).toInt()}%!"
                } else {
                    "Direct views are running at ${(viewsProgress * 100).toInt()}% of predicted goal."
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (viewsProgress >= 1.0f) Color(0xFF10B981) else Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        
        // CTR Comparison
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pred CTR
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("PREDICTED CTR", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                    Text(String.format("%.1f%%", predCtr), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                }
            }
            
            // Actual CTR
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPublished && actualCtr >= predCtr) Color(0xFFECFDF5) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isPublished) {
                        if (actualCtr >= predCtr) Color(0xFF10B981) else Color(0xFFEF4444)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ACTUAL CTR", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isPublished) String.format("%.1f%%", actualCtr) else "--",
                            style = MaterialTheme.typography.titleSmall, 
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPublished) {
                                if (actualCtr >= predCtr) Color(0xFF047857) else Color(0xFFB91C1C)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (isPublished) {
                            Spacer(modifier = Modifier.width(2.dp))
                            val diff = actualCtr - predCtr
                            Icon(
                                imageVector = if (diff >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (diff >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
