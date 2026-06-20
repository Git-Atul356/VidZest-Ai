package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.YoutubeViewModel
import com.example.ui.components.CreatorLabScreen
import com.example.ui.components.DashboardScreen
import com.example.ui.components.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: YoutubeViewModel = viewModel()
                var currentTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = "TubeAutomate Engine",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Dashboard") },
                                modifier = Modifier.testTag("nav_item_dashboard")
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.Default.SmartToy, contentDescription = "Creator Lab") },
                                label = { Text("Creator Lab") },
                                modifier = Modifier.testTag("nav_item_lab")
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Configs") },
                                modifier = Modifier.testTag("nav_item_settings")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            0 -> DashboardScreen(viewModel = viewModel)
                            1 -> CreatorLabScreen(viewModel = viewModel)
                            2 -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
