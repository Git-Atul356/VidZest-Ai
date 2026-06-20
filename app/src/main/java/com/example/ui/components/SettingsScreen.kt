package com.example.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LogEntity
import com.example.ui.YoutubeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(viewModel: YoutubeViewModel) {
    var apiKeyVisible by remember { mutableStateOf(false) }
    val logs by viewModel.logs.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // AI Key config card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "AI Services Selection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Choose the AI provider below. If no key is supplied, a smart visual sandbox simulator is run so you can safely test the automation flow.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Provider toggle
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProviderSelectorChip(
                            label = "Google Gemini (Free)",
                            selected = viewModel.selectedProvider == "Gemini",
                            onClick = { viewModel.updateProvider("Gemini") }
                        )
                        ProviderSelectorChip(
                            label = "OpenAI GPTs & DALL-E",
                            selected = viewModel.selectedProvider == "OpenAI",
                            onClick = { viewModel.updateProvider("OpenAI") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Key inputs
                    OutlinedTextField(
                        value = viewModel.customApiKey,
                        onValueChange = { viewModel.updateCustomKey(it) },
                        label = { Text("Custom API Key (Optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_key_input"),
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                        trailingIcon = {
                            val icon = if (apiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                                Icon(icon, contentDescription = "Toggle API Key vision")
                            }
                        },
                        visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        placeholder = { Text("E.g. sk-Proj... or AIzaSy...") }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Absolute secret management warning statement
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "APK Key Extraction Warning",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Security Warning",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "I have included your API keys in the generated APK file for this prototype. Please be aware that Android APKs can be easily decompiled, and these keys can be extracted by anyone who has access to the file. Do not share this APK file publicly or with unauthorized individuals to prevent potential misuse.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Section: Headless Server deployment documentation guidelines
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "24/7 Headless Server Deployment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "I have automatically generated a fully functional Node.js backend directory inside this project root directory matching your spec! It contains SQLite db loaders, Docker configuration, and keys setups. Here is how you can deploy it on a server:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Step 1: Install & Set Keys",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                    CodeBlock(
                        terminalText = """
                            cd youtube-automation
                            npm install
                            ./setup.sh
                        """.trimIndent()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Step 2: Run in Docker Environment",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                    CodeBlock(
                        terminalText = """
                            docker build -t yt-automate .
                            docker run -d --name yt-channel yt-automate
                        """.trimIndent()
                    )
                }
            }
        }

        // Section: SQLite logs database viewer
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Local SQLite Logs Monitor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { viewModel.clearLogHistory() },
                    modifier = Modifier.testTag("clear_logs_button")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Stream", fontSize = 12.sp)
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Logs stream empty. Trigger the agents in the Creator Lab to populate active trace history.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            }
        } else {
            items(logs) { log ->
                LogItemRow(log = log)
            }
        }
    }
}

@Composable
fun ProviderSelectorChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun CodeBlock(terminalText: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = terminalText,
            color = Color(0xFF00FF00), // Terminal green
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(10.dp),
            fontSize = 11.sp
        )
    }
}

@Composable
fun LogItemRow(log: LogEntity) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
    val time = remember(log.timestamp) { formatter.format(Date(log.timestamp)) }
    
    val badgeColor = when (log.level.uppercase()) {
        "ERROR" -> Color(0xFFD32F2F)
        "WARN" -> Color(0xFFF57C00)
        else -> Color(0xFF388E3C)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = log.level,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = log.agentName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp
                )
                if (log.details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = log.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
