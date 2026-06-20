package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.AutomationStep
import com.example.ui.YoutubeViewModel
import androidx.compose.ui.graphics.Path
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.*
import android.widget.Toast
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorLabScreen(viewModel: YoutubeViewModel) {
    if (viewModel.isSharingSessionActive) {
        SocialMediaShareStudio(viewModel = viewModel)
        return
    }

    if (viewModel.isDraftingSessionActive) {
        VideoVisualDraftsStudio(viewModel = viewModel)
        return
    }

    val focusManager = LocalFocusManager.current
    var nicheInput by remember { mutableStateOf(viewModel.activeNiche) }
    
    // Suggestion chips
    val presetNiches = listOf("Tech Gossip", "SaaS Marketing", "Money Secrets", "Short Horror", "Space Trivia")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Active Target Niche configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Step 1: Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    OutlinedTextField(
                        value = nicheInput,
                        onValueChange = {
                            nicheInput = it
                            viewModel.updateNiche(it)
                        },
                        label = { Text("Enter Target Channel Niche") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("niche_text_input"),
                        leadingIcon = { Icon(Icons.Default.Explore, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presetNiches) { preset ->
                            val isSelected = nicheInput.equals(preset, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    nicheInput = preset
                                    viewModel.updateNiche(preset)
                                    focusManager.clearFocus()
                                },
                                label = { Text(preset) },
                                modifier = Modifier.testTag("niche_chip_$preset")
                            )
                        }
                    }
                }
            }
        }

        // Live Google Grounded Research Panel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grounded_research_panel"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google Search Grounding Lab",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "GEMINI SEARCH ENGINE",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Scan current Google Trends, YouTube indexes, and Reddit conversations for '$nicheInput' to find viral content opportunities.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Button(
                        onClick = { viewModel.performSearchGroundedResearch() },
                        enabled = !viewModel.isResearchingBySearch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("run_grounded_research_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (viewModel.isResearchingBySearch) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grounding Gemini with Google Search...")
                        } else {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Live Market Trends", fontWeight = FontWeight.Bold)
                        }
                    }

                    viewModel.groundedResearchError?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Research engine error: $err",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    viewModel.groundedResearchResult?.let { result ->
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Grounded Target Keyword Volume",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            result.keywords.forEach { keywordObj ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = keywordObj.keyword,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = keywordObj.trendStatus,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Grounded Click-Worthy Topics",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Select a topic to apply it directly to your creation pipeline.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        result.topics.forEach { topic ->
                            val isSelected = viewModel.selectedTopic == topic.title
                            val cardBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                            val cardBorder = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewModel.adoptGroundedTopic(topic.title) }
                                    .testTag("grounded_topic_card_${topic.title.take(15)}"),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
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
                                        Text(
                                            text = topic.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Adopt",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Why it works: " + topic.psychologicalHook,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Grounded Search Query: ${topic.sourceQuery}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.secondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        AIGrowthTrendForecastComponent(
                            selectedTopic = viewModel.selectedTopic,
                            activeNiche = viewModel.activeNiche,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Stepper checklist controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Step 2: AI Agents Scheduler Loop",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        if (viewModel.isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // The 4 Interactive Steps Checklist Indicator
                    AgentStepRow(
                        stepNumber = 1,
                        title = "Trend Topic Research Agent",
                        isActive = viewModel.currentStep == AutomationStep.RESEARCHING,
                        isDone = viewModel.currentStep.ordinal > AutomationStep.RESEARCHING.ordinal,
                        statusText = if (viewModel.currentStep == AutomationStep.RESEARCHING) "Scanning YouTube V3/Google Trends API..." else if (viewModel.currentStep.ordinal > AutomationStep.RESEARCHING.ordinal) "Click-Worthy Trends Isolate Done in SQLite" else "Idle"
                    )

                    AgentStepRow(
                        stepNumber = 2,
                        title = "Full Script Writing Agent",
                        isActive = viewModel.currentStep == AutomationStep.SCRIPTING,
                        isDone = viewModel.currentStep.ordinal > AutomationStep.SCRIPTING.ordinal,
                        statusText = if (viewModel.currentStep == AutomationStep.SCRIPTING) "Generating 3-min Retention Narration Script..." else if (viewModel.currentStep.ordinal > AutomationStep.SCRIPTING.ordinal) "Retention Script Generated" else "Idle"
                    )

                    AgentStepRow(
                        stepNumber = 3,
                        title = "SEO Designer & Thumbnail Agent",
                        isActive = viewModel.currentStep == AutomationStep.OPTIMIZING,
                        isDone = viewModel.currentStep.ordinal > AutomationStep.OPTIMIZING.ordinal,
                        statusText = if (viewModel.currentStep == AutomationStep.OPTIMIZING) "Designing custom thumbnail and search-tag tags..." else if (viewModel.currentStep.ordinal > AutomationStep.OPTIMIZING.ordinal) "SEO Keywords & Thumbnail Formed" else "Idle"
                    )

                    AgentStepRow(
                        stepNumber = 4,
                        title = "Auto-Upload & Schedule Agent",
                        isActive = viewModel.currentStep == AutomationStep.UPLOADING,
                        isDone = viewModel.currentStep == AutomationStep.COMPLETED,
                        statusText = if (viewModel.currentStep == AutomationStep.UPLOADING) "Saving queue file. Triggering auto-upload simulate..." else if (viewModel.currentStep == AutomationStep.COMPLETED) "Video Uploaded & Published!" else "Idle"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { viewModel.runCompleteAutomationLifecycle() },
                        enabled = !viewModel.isRunning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("run_automation_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.SmartToy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (viewModel.isRunning) "Running 24/7 Agent Automation Pipeline..." else "Execute AI Agent Automation Pipeline",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    viewModel.errorState?.let { error ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Error: $error",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sandbox Workspace Agent Results display (Dynamic Accordions)
        if (viewModel.researchedTopics.isNotEmpty() || viewModel.generatedScript.isNotEmpty() || viewModel.optimizedTitle.isNotEmpty()) {
            item {
                Text(
                    text = "Agent Workspace Cache Output",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Accordion 1: Isolate Trend Topics (Step 1)
        if (viewModel.researchedTopics.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Isolate Viral Topics Output",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Select a topic structure from research agent logs to preview or script:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        viewModel.researchedTopics.forEachIndexed { idx, topic ->
                            val isSelected = viewModel.selectedTopic == topic
                            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                            val borderAccent = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewModel.selectTopic(topic) },
                                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderAccent)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val icon = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked
                                    val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = topic,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Accordion 2: Script narrator b-roll details (Step 2)
        if (viewModel.generatedScript.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Complete Narration Script",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .background(Color.White, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            ) {
                                item {
                                    Text(
                                        text = viewModel.generatedScript,
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val currentVideo = com.example.data.VideoEntity(
                                    niche = viewModel.activeNiche,
                                    trendingTopic = viewModel.selectedTopic.ifEmpty { "Trending AI Concept" },
                                    script = viewModel.generatedScript,
                                    optimizedTitle = viewModel.optimizedTitle,
                                    thumbnailUrl = viewModel.generatedThumbnail,
                                    status = "SCRIPTED"
                                )
                                viewModel.openDraftingStudio(currentVideo)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("drafts_from_lab_trigger"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MovieFilter, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🎬 Generate Video Visual Drafts storyboard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        AIVoiceSynthesizerPanel(
                            scriptText = viewModel.generatedScript,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Accordion 3: SEO Optimization Metadata Details (Step 3 & 4)
        if (viewModel.optimizedTitle.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "YouTube Title, Dec & Graphic",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Beautiful High-fidelity Canvas Simulated Thumbnail layout overlay!
                        Text(
                            text = "Simulated Thumbnail (Generative Template View):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            // Render base image via Coil
                            AsyncImage(
                                model = viewModel.generatedThumbnail,
                                contentDescription = "Generated thumbnail background image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Visual high-contrast dark overlay to ensure text is readable
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.9f),
                                                Color.Black.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                            
                            // Accent red border styled in classic Youtube branding
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                // Bottom left duration badge
                                Card(
                                    modifier = Modifier.align(Alignment.BottomEnd),
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "3:14",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                // YouTube Live indicator or Logo placeholder top-left
                                Card(
                                    modifier = Modifier.align(Alignment.TopEnd),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF0000)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "HQ-AI",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                
                                // Topic & Title typography overlay
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .fillMaxWidth(0.75f)
                                        .padding(start = 12.dp)
                                ) {
                                    Text(
                                        text = viewModel.activeNiche.uppercase(),
                                        color = Color(0xFFFF0000), // YouTube Red
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp,
                                        style = TextStyle(
                                            shadow = Shadow(
                                                color = Color.Black,
                                                offset = Offset(1f, 1f),
                                                blurRadius = 2f
                                            )
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = viewModel.optimizedTitle,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        lineHeight = 19.sp,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(
                                            shadow = Shadow(
                                                color = Color.Black,
                                                offset = Offset(2f, 2f),
                                                blurRadius = 4f
                                            )
                                        )
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // SEO Optimized text detail components
                        Text(
                            text = "Optimized Title:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = viewModel.optimizedTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "SEO Tags:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = viewModel.optimizedTags,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "SEO Video Description:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 120.dp, min = 60.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                item {
                                    Text(
                                        text = viewModel.optimizedDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground
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

@Composable
fun AgentStepRow(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isDone: Boolean,
    statusText: String
) {
    val stepBg = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else if (isDone) {
        Color(0xFFE8F5E9)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val iconColor = if (isDone) {
        Color(0xFF2E7D32)
    } else if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Gray
    }

    val icon = if (isDone) {
        Icons.Default.Check
    } else if (isActive) {
        Icons.Default.HourglassEmpty
    } else {
        Icons.Default.Circle
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = stepBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(iconColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                } else {
                    Text(
                        text = stepNumber.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDone) Color(0xFF2E7D32) else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (isActive) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AIGrowthTrendForecastComponent(
    selectedTopic: String,
    activeNiche: String,
    modifier: Modifier = Modifier
) {
    // Generate the trend data based on selected topic and niche
    val trendData = remember(selectedTopic, activeNiche) {
        val seed = (selectedTopic + activeNiche).hashCode().toLong()
        val random = java.util.Random(seed)
        
        val list = mutableListOf<TrendPoint>()
        val baseValue = 25 + random.nextInt(30) // 25 to 55
        val trendType = random.nextInt(3) // 0: Steady Rise, 1: Peak & Stabilize, 2: Breakout
        
        val days = listOf("Day 1", "Day 6", "Day 12", "Day 18", "Day 24", "Day 30")
        
        for (i in 0 until 6) {
            val interest: Float
            val velocity: Float
            
            when (trendType) {
                0 -> { // Steady Rise
                    interest = (baseValue + (i * 9f) + random.nextInt(10)).coerceAtMost(100f)
                    velocity = (40f + (i * 7f) + random.nextInt(12)).coerceAtMost(100f)
                }
                1 -> { // Peak & Stabilize
                    if (i == 2 || i == 3) {
                        interest = (82f + random.nextInt(15)).coerceAtMost(100f)
                        velocity = (88f + random.nextInt(10)).coerceAtMost(100f)
                    } else if (i > 3) {
                        interest = (65f + random.nextInt(15)).coerceAtMost(100f)
                        velocity = (55f + random.nextInt(15)).coerceAtMost(100f)
                    } else {
                        interest = (baseValue + (i * 18f) + random.nextInt(8)).coerceAtMost(100f)
                        velocity = (baseValue + (i * 15f) + random.nextInt(10)).coerceAtMost(100f)
                    }
                }
                else -> { // Breakout
                    val mult = if (i > 2) 1.8f else 1.1f
                    interest = (baseValue * mult + (i * 5f) + random.nextInt(12)).coerceIn(10f, 100f)
                    velocity = (30f + (i * i * 2.5f) + random.nextInt(15)).coerceAtMost(100f)
                }
            }
            list.add(TrendPoint(days[i], interest, velocity))
        }
        list
    }

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var showInterestLine by remember { mutableStateOf(true) }
    var showVelocityLine by remember { mutableStateOf(true) }

    val primaryColor = Color(0xFF2563EB) // Vibrant blue
    val tertiaryColor = Color(0xFFEC4899) // Hot pink
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trend_forecast_chart_panel"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Chart Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Live Growth Potential Forecast",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (selectedTopic.isNotEmpty()) {
                            "Plotted Topic: $selectedTopic"
                        } else {
                            "Active Niche: $activeNiche (Select topic card above)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Styling visual badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "RECHARTS ENGINE",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recharts-style Leyends with interactive Line Toggles!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle 1: Search Interest Index
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showInterestLine = !showInterestLine }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (showInterestLine) primaryColor else Color.Gray.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Search Interest Index",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (showInterestLine) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Toggle 2: Velocity Forecast
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showVelocityLine = !showVelocityLine }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (showVelocityLine) tertiaryColor else Color.Gray.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Viral Velocity Forecast",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (showVelocityLine) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Graph and Y-Axis Content Grid
            Row(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                // Left hand tick marks
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(24.dp)
                        .padding(bottom = 20.dp), // offset for bottom subtitle alignment
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("100", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color.Gray)
                    Text("75", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color.Gray)
                    Text("50", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color.Gray)
                    Text("25", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color.Gray)
                    Text("0", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Line graph area with precise interaction
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val lineStrokeWidth = 3.dp
                    val circleRadius = 5.dp

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(trendData) {
                                detectTapGestures { offset ->
                                    val canvasWidth = size.width
                                    val stepX = canvasWidth / 5f
                                    val clickedIndex = (offset.x / stepX).plus(0.5f).toInt().coerceIn(0, 5)
                                    selectedPointIndex = clickedIndex
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height - 20.dp.toPx() // Account for X axis captions
                        val stepX = canvasWidth / 5f

                        // Draw Grid guidelines (Dashed Cartesian Grid)
                        val gridColor = Color.Gray.copy(alpha = 0.12f)
                        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                        // 5 horizontal grids
                        for (i in 0..4) {
                            val y = (canvasHeight / 4f) * i
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dashPathEffect
                            )
                        }

                        // 6 vertical grids
                        for (i in 0..5) {
                            val x = stepX * i
                            drawLine(
                                color = gridColor,
                                start = Offset(x, 0f),
                                end = Offset(x, canvasHeight),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dashPathEffect
                            )
                        }

                        // Selected interactive vertical crosshair selector line
                        selectedPointIndex?.let { activeIdx ->
                            val lineX = stepX * activeIdx
                            drawLine(
                                color = onSurfaceColor.copy(alpha = 0.2f),
                                start = Offset(lineX, 0f),
                                end = Offset(lineX, canvasHeight),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                        }

                        // Draw Google Search Interest Index Area Curve
                        if (showInterestLine) {
                            val interestPoints = trendData.mapIndexed { idx, pt ->
                                val x = idx * stepX
                                val y = canvasHeight - (pt.interest / 100f) * canvasHeight
                                Offset(x, y)
                            }

                            val path = Path()
                            val fillPath = Path()

                            interestPoints.forEachIndexed { idx, pt ->
                                if (idx == 0) {
                                    path.moveTo(pt.x, pt.y)
                                    fillPath.moveTo(pt.x, canvasHeight)
                                    fillPath.lineTo(pt.x, pt.y)
                                } else {
                                    val prev = interestPoints[idx - 1]
                                    path.cubicTo(
                                        x1 = prev.x + (pt.x - prev.x) / 2f,
                                        y1 = prev.y,
                                        x2 = prev.x + (pt.x - prev.x) / 2f,
                                        y2 = pt.y,
                                        x3 = pt.x,
                                        y3 = pt.y
                                    )
                                    fillPath.cubicTo(
                                        x1 = prev.x + (pt.x - prev.x) / 2f,
                                        y1 = prev.y,
                                        x2 = prev.x + (pt.x - prev.x) / 2f,
                                        y2 = pt.y,
                                        x3 = pt.x,
                                        y3 = pt.y
                                    )
                                }

                                if (idx == interestPoints.size - 1) {
                                    fillPath.lineTo(pt.x, canvasHeight)
                                    fillPath.close()
                                }
                            }

                            // Fill
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.22f), Color.Transparent)
                                )
                            )

                            // Stroke Path
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(
                                    width = lineStrokeWidth.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )

                            // Nodes
                            interestPoints.forEachIndexed { idx, pt ->
                                val active = selectedPointIndex == idx
                                drawCircle(
                                    color = primaryColor,
                                    radius = if (active) circleRadius.toPx() + 2.dp.toPx() else (circleRadius.toPx() - 1.dp.toPx()),
                                    center = pt
                                )
                                if (active) {
                                    drawCircle(
                                        color = Color.White,
                                        radius = circleRadius.toPx() - 1.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }

                        // Draw Velocity Forecast Area Curve
                        if (showVelocityLine) {
                            val velocityPoints = trendData.mapIndexed { idx, pt ->
                                val x = idx * stepX
                                val y = canvasHeight - (pt.velocity / 100f) * canvasHeight
                                Offset(x, y)
                            }

                            val path = Path()
                            val fillPath = Path()

                            velocityPoints.forEachIndexed { idx, pt ->
                                if (idx == 0) {
                                    path.moveTo(pt.x, pt.y)
                                    fillPath.moveTo(pt.x, canvasHeight)
                                    fillPath.lineTo(pt.x, pt.y)
                                } else {
                                    val prev = velocityPoints[idx - 1]
                                    path.cubicTo(
                                        x1 = prev.x + (pt.x - prev.x) / 2f,
                                        y1 = prev.y,
                                        x2 = prev.x + (pt.x - prev.x) / 2f,
                                        y2 = pt.y,
                                        x3 = pt.x,
                                        y3 = pt.y
                                    )
                                    fillPath.cubicTo(
                                        x1 = prev.x + (pt.x - prev.x) / 2f,
                                        y1 = prev.y,
                                        x2 = prev.x + (pt.x - prev.x) / 2f,
                                        y2 = pt.y,
                                        x3 = pt.x,
                                        y3 = pt.y
                                    )
                                }

                                if (idx == velocityPoints.size - 1) {
                                    fillPath.lineTo(pt.x, canvasHeight)
                                    fillPath.close()
                                }
                            }

                            // Fill
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(tertiaryColor.copy(alpha = 0.22f), Color.Transparent)
                                )
                            )

                            // Stroke Path
                            drawPath(
                                path = path,
                                color = tertiaryColor,
                                style = Stroke(
                                    width = lineStrokeWidth.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )

                            // Nodes
                            velocityPoints.forEachIndexed { idx, pt ->
                                val active = selectedPointIndex == idx
                                drawCircle(
                                    color = tertiaryColor,
                                    radius = if (active) circleRadius.toPx() + 2.dp.toPx() else (circleRadius.toPx() - 1.dp.toPx()),
                                    center = pt
                                )
                                if (active) {
                                    drawCircle(
                                        color = Color.White,
                                        radius = circleRadius.toPx() - 1.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }
                    }

                    // Lower X-Axis ticks aligned with grids
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .height(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        trendData.forEach { pt ->
                            Text(
                                text = pt.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = Color.Gray,
                                modifier = Modifier.width(36.dp),
                                maxLines = 1,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    // Floating Card Tooltip Overlay mimicking Recharts Tooltip
                    selectedPointIndex?.let { idx ->
                        val pt = trendData[idx]
                        val isRightSideShift = idx > 2

                        Box(modifier = Modifier.fillMaxSize()) {
                            Card(
                                modifier = Modifier
                                    .align(if (isRightSideShift) Alignment.TopStart else Alignment.TopEnd)
                                    .padding(horizontal = 6.dp, vertical = 6.dp)
                                    .width(135.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text(
                                        text = pt.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (showInterestLine) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(5.dp).background(primaryColor, CircleShape))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Interest Index:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color.Gray)
                                            }
                                            Text("${pt.interest.toInt()}%", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (showVelocityLine) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(5.dp).background(tertiaryColor, CircleShape))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Velocity Rate:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = Color.Gray)
                                            }
                                            Text("${pt.velocity.toInt()}%", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Interactive Live Data",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 7.sp,
                                        color = Color.LightGray,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
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

data class TrendPoint(
    val label: String,
    val interest: Float,
    val velocity: Float
)

data class SpeechItem(
    val index: Int,
    val timestamp: String,
    val text: String,
    val tone: String = "neutral"
)

fun parseScriptToSpeechItems(scriptText: String): List<SpeechItem> {
    val list = mutableListOf<SpeechItem>()
    if (scriptText.isBlank()) return list

    // Try parsing as JSON first
    try {
        val trimmed = scriptText.trim()
        val json = if (trimmed.startsWith("{")) {
            org.json.JSONObject(trimmed)
        } else {
            // Check if there is a json substring
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
                    list.add(
                        SpeechItem(
                            index = i,
                            timestamp = item.optString("timestamp", item.optString("start_time", "00:${String.format("%02d", i * 15)}")),
                            text = item.optString("narration", item.optString("suggested_caption", item.optString("reason", ""))),
                            tone = item.optString("tone", "neutral")
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("TTS_PARSER", "Failed to parse script as JSON, trying text fallback", e)
    }

    // Fallback parser for standard script layouts
    if (list.isEmpty()) {
        try {
            val lines = scriptText.split("\n")
            var idx = 0
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("NARRATOR:", ignoreCase = true)) {
                    val speechText = trimmed.substringAfter("NARRATOR:", "").trim().removeSurrounding("\"")
                    if (speechText.isNotEmpty()) {
                        list.add(
                            SpeechItem(
                                index = idx,
                                timestamp = "00:${String.format("%02d", idx * 15)}",
                                text = speechText,
                                tone = "excited"
                            )
                        )
                        idx++
                    }
                } else if (trimmed.startsWith("NARRATION:", ignoreCase = true)) {
                    val speechText = trimmed.substringAfter("NARRATION:", "").trim().removeSurrounding("\"")
                    if (speechText.isNotEmpty()) {
                        list.add(
                            SpeechItem(
                                index = idx,
                                timestamp = "00:${String.format("%02d", idx * 15)}",
                                text = speechText,
                                tone = "excited"
                            )
                        )
                        idx++
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TTS_PARSER", "Fallback parser failed", e)
        }
    }

    // Default ultra-fallback
    if (list.isEmpty()) {
        val paragraphs = scriptText.split("\n\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("🎬") && !it.startsWith("---") && !it.startsWith("Topic:") && !it.startsWith("Niche:") }
        
        paragraphs.forEachIndexed { i, p ->
            val cleanText = p.replace(Regex("\\[.*?\\]"), "").trim()
            if (cleanText.isNotEmpty()) {
                list.add(
                    SpeechItem(
                        index = i,
                        timestamp = "00:${String.format("%02d", i * 15)}",
                        text = cleanText,
                        tone = "excited"
                    )
                )
            }
        }
    }

    return list
}

@Composable
fun AudioWaveformAnimator(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val heights = listOf(0.3f, 0.7f, 1.0f, 0.5f, 0.8f, 0.4f, 0.9f)
    val animatedScales = heights.mapIndexed { i, targetVal ->
        if (isPlaying) {
            infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = targetVal,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 250 + i * 70, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "wave_bar_$i"
            )
        } else {
            remember { mutableStateOf(0.2f) }
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(28.dp)
    ) {
        animatedScales.forEach { scaleState ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(scaleState.value)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIVoiceSynthesizerPanel(
    scriptText: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val speechItems = remember(scriptText) { parseScriptToSpeechItems(scriptText) }
    
    // Auto-detect language
    val detectedLocaleValue = remember(scriptText) {
        val lowerText = scriptText.lowercase()
        val containsHinglish = lowerText.contains("aap") || lowerText.contains("hain") || lowerText.contains("hai") || lowerText.contains("bhai") || lowerText.contains("kar") || lowerText.contains("aur") || lowerText.contains("hoga") || lowerText.contains("hum")
        if (containsHinglish) Locale("hi", "IN") else Locale.US
    }

    var pitch by remember { mutableStateOf(1.0f) }
    var speed by remember { mutableStateOf(1.0f) }
    var selectedLocale by remember { mutableStateOf(detectedLocaleValue) }
    
    var isPlaying by remember { mutableStateOf(false) }
    var activeSpeakingIndex by remember { mutableStateOf<Int?>(null) }
    var isSynthesizingToFile by remember { mutableStateOf(false) }
    var synthesizedFilePath by remember { mutableStateOf<String?>(null) }
    
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var ttsErrorMsg by remember { mutableStateOf<String?>(null) }
    
    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                ttsErrorMsg = null
            } else {
                ttsErrorMsg = "Local TTS engine failed to initialize."
            }
        }
        ttsInstance = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }
    
    val setTtsListeners = {
        val tts = ttsInstance
        if (tts != null && isTtsReady) {
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    val idx = utteranceId?.toIntOrNull()
                    if (idx != null) {
                        Handler(Looper.getMainLooper()).post {
                            activeSpeakingIndex = idx
                        }
                    }
                }
                
                override fun onDone(utteranceId: String?) {
                    val idx = utteranceId?.toIntOrNull()
                    if (idx != null) {
                        Handler(Looper.getMainLooper()).post {
                            if (idx == speechItems.size - 1) {
                                isPlaying = false
                                activeSpeakingIndex = null
                            }
                        }
                    }
                }
                
                override fun onError(utteranceId: String?) {
                    Handler(Looper.getMainLooper()).post {
                        ttsErrorMsg = "Error speaking line #$utteranceId"
                        isPlaying = false
                    }
                }
            })
        }
    }

    LaunchedEffect(ttsInstance, isTtsReady, speechItems) {
        setTtsListeners()
    }

    val startFullPlayback = {
        val tts = ttsInstance
        if (tts != null && isTtsReady && speechItems.isNotEmpty()) {
            tts.stop()
            isPlaying = true
            activeSpeakingIndex = 0
            
            tts.language = selectedLocale
            tts.setPitch(pitch)
            tts.setSpeechRate(speed)
            
            speechItems.forEachIndexed { idx, item ->
                val params = android.os.Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, idx.toString())
                }
                tts.speak(item.text, TextToSpeech.QUEUE_ADD, params, idx.toString())
            }
        }
    }

    val stopPlayback = {
        ttsInstance?.stop()
        isPlaying = false
        activeSpeakingIndex = null
    }

    val speakSingleItem = { idx: Int, text: String ->
        val tts = ttsInstance
        if (tts != null && isTtsReady) {
            tts.stop()
            isPlaying = true
            activeSpeakingIndex = idx
            
            tts.language = selectedLocale
            tts.setPitch(pitch)
            tts.setSpeechRate(speed)
            
            val params = android.os.Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, idx.toString())
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, idx.toString())
        }
    }

    val compileAudioToFile = {
        val tts = ttsInstance
        if (tts != null && isTtsReady && speechItems.isNotEmpty()) {
            isSynthesizingToFile = true
            synthesizedFilePath = null
            
            val fullText = speechItems.joinToString(". ") { it.text }
            val cacheFolder = context.cacheDir
            val destFile = java.io.File(cacheFolder, "audio_track_v${System.currentTimeMillis() / 1000}.wav")
            
            tts.language = selectedLocale
            tts.setPitch(pitch)
            tts.setSpeechRate(speed)
            
            val compileParams = android.os.Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "file_compile")
            }
            
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    if (utteranceId == "file_compile") {
                        Handler(Looper.getMainLooper()).post {
                            isSynthesizingToFile = false
                            synthesizedFilePath = destFile.absolutePath
                            Toast.makeText(context, "Audio file compiled: ${destFile.name}", Toast.LENGTH_LONG).show()
                            setTtsListeners() // restore original player progress listener
                        }
                    }
                }
                override fun onError(utteranceId: String?) {
                    Handler(Looper.getMainLooper()).post {
                        isSynthesizingToFile = false
                        ttsErrorMsg = "Synthesis failed."
                        setTtsListeners()
                    }
                }
            })
            
            val synthResult = tts.synthesizeToFile(fullText, compileParams, destFile, "file_compile")
            if (synthResult == TextToSpeech.ERROR) {
                isSynthesizingToFile = false
                ttsErrorMsg = "TTS synthesis command rejected."
                setTtsListeners()
            }
        }
    }

    val voiceOptions = listOf(
        Locale.US to "English (US Voice)",
        Locale.UK to "English (UK Voice)",
        Locale("hi", "IN") to "Hindi (Hinglish Accent)"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_voice_synthesizer_panel"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎙️ AI Audio Voice Synthesizer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Auto-detected locale: ${if (detectedLocaleValue.language == "hi") "Hindi / Hinglish (IN)" else "English (US)"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isTtsReady) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "LOCAL ENGINE READY",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            fontSize = 9.sp
                        )
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }

            ttsErrorMsg?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Engine warning: $error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Voice configurations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                voiceOptions.forEach { (locale, name) ->
                    val isSelected = selectedLocale.language == locale.language
                    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedLocale = locale },
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Slider Controls: Pitch & Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voice Pitch", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(String.format("%.1fx", pitch), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = pitch,
                        onValueChange = { pitch = it },
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.height(32.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speech Rate", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(String.format("%.1fx", speed), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = speed,
                        onValueChange = { speed = it },
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isPlaying) {
                    Button(
                        onClick = { startFullPlayback() },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Speak Playtrack", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { stopPlayback() },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stop Speaking", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { compileAudioToFile() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    enabled = !isSynthesizingToFile
                ) {
                    if (isSynthesizingToFile) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export WAV", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }

            synthesizedFilePath?.let { path ->
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "✅ Compiled Consolidated Audio Track",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = path.substringAfterLast("/"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (isPlaying && activeSpeakingIndex != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Now speaking paragraph #${activeSpeakingIndex?.plus(1)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    AudioWaveformAnimator(isPlaying = true)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Narration Scene Elements Map (${speechItems.size} items parsed):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-list of individual speech nodes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(speechItems) { item ->
                        val isActive = activeSpeakingIndex == item.index
                        val cardBg = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                        val cardBorder = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { speakSingleItem(item.index, item.text) },
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = item.timestamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (item.tone != "neutral") {
                                        Text(
                                            text = "[ Tone: ${item.tone} ]",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                                
                                if (isActive) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
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

