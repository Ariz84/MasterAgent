package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MasterAgentApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MasterAgentApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle system back button: return to Conversation from other tabs
    BackHandler(enabled = currentScreen != AppScreen.CONVERSATION) {
        viewModel.setScreen(AppScreen.CONVERSATION)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp,
                containerColor = com.example.ui.theme.FuturisticDarkSurface,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.CONVERSATION,
                    onClick = { viewModel.setScreen(AppScreen.CONVERSATION) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Voice Orb", modifier = Modifier.size(20.dp)) },
                    label = { Text("Orb Voice", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_chat")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.SPECIALIST_INSPECTOR,
                    onClick = { viewModel.setScreen(AppScreen.SPECIALIST_INSPECTOR) },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Specialists", modifier = Modifier.size(20.dp)) },
                    label = { Text("Specialists", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_specialists")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.TRADING_TOOLS,
                    onClick = { viewModel.setScreen(AppScreen.TRADING_TOOLS) },
                    icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Trading", modifier = Modifier.size(20.dp)) },
                    label = { Text("Trading", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_trading")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.TERMUX_COMPANION,
                    onClick = { viewModel.setScreen(AppScreen.TERMUX_COMPANION) },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "Termux", modifier = Modifier.size(20.dp)) },
                    label = { Text("Termux", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_termux")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.COMMUNICATIONS_STATUS,
                    onClick = { viewModel.setScreen(AppScreen.COMMUNICATIONS_STATUS) },
                    icon = { Icon(Icons.Default.Share, contentDescription = "Comms", modifier = Modifier.size(20.dp)) },
                    label = { Text("Comms", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_comms")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.CONVERSATION -> VoiceOrbScreen(
                    viewModel = viewModel,
                    onNavigateToTrading = { viewModel.setScreen(AppScreen.TRADING_TOOLS) }
                )
                AppScreen.SPECIALIST_INSPECTOR -> SpecialistInspectorScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.setScreen(AppScreen.CONVERSATION) }
                )
                AppScreen.TRADING_TOOLS -> TradingToolsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.setScreen(AppScreen.CONVERSATION) }
                )
                AppScreen.TERMUX_COMPANION -> TermuxCompanionScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.setScreen(AppScreen.CONVERSATION) }
                )
                AppScreen.COMMUNICATIONS_STATUS -> CommunicationsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.setScreen(AppScreen.CONVERSATION) }
                )
            }
        }
    }
}
