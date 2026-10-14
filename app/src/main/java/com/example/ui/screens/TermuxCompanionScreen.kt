package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.TermuxConnector
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermuxCompanionScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val connector = viewModel.termuxConnector

    var urlText by remember { mutableStateOf(connector.baseUrl) }
    var tokenText by remember { mutableStateOf(connector.authToken) }
    var isEnabled by remember { mutableStateOf(connector.isEnabled) }

    val pingResult by viewModel.termuxPingMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Termux Backend Companion", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("اینڈرائڈ فون پر پائتھن ماسٹر ایجنٹ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("termux_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Mobile-Only Explanation Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = "Phone Only",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "100% Mobile Phone Setup (No PC Required)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Aapka Android phone hi server bhi hai aur client bhi! Termux me Python script background me chalegi aur yeh Master Agent app usi phone par http://127.0.0.1:8000 se rabta karegi.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Configuration Form
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Termux Integration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = {
                                isEnabled = it
                                viewModel.updateTermuxConfig(urlText, tokenText, it)
                            },
                            modifier = Modifier.testTag("termux_enable_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = urlText,
                        onValueChange = {
                            urlText = it
                            viewModel.updateTermuxConfig(it, tokenText, isEnabled)
                        },
                        label = { Text("Localhost URL", fontSize = 12.sp) },
                        placeholder = { Text("http://127.0.0.1:8000") },
                        modifier = Modifier.fillMaxWidth().testTag("termux_url_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tokenText,
                        onValueChange = {
                            tokenText = it
                            viewModel.updateTermuxConfig(urlText, it, isEnabled)
                        },
                        label = { Text("API Bearer Token (Optional)", fontSize = 12.sp) },
                        placeholder = { Text("Secured token for authentication") },
                        modifier = Modifier.fillMaxWidth().testTag("termux_token_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.testTermux() },
                        modifier = Modifier.fillMaxWidth().testTag("test_termux_button")
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Connection (Ping Localhost)")
                    }

                    pingResult?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (msg.contains("successfully", ignoreCase = true)) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step by Step Termux Phone Guide
            Text(
                text = "Phone par Termux Setup karne ka tareeqa:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            val steps = listOf(
                "1. F-Droid se Termux app phone par install karein.",
                "2. Termux open karein aur yeh command run karein:\npkg update && pkg install python -y && pip install flask",
                "3. Neeche diya gaya Python script copy karein aur Termux me 'nano master_agent_server.py' me paste kar ke save karein.",
                "4. Termux me 'python master_agent_server.py' run karein.",
                "5. Upar 'Test Connection' tap karein. Master Agent app aapke local Termux se jor jayegi!"
            )

            steps.forEach { step ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                ) {
                    Text(
                        text = step,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // One tap copy script button
            FilledTonalButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Termux Script", TermuxConnector.SAMPLE_PYTHON_SERVER)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Termux Python script copied! Paste it into Termux.", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth().testTag("copy_termux_script_button")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Python Server Script to Clipboard", fontWeight = FontWeight.Bold)
            }
        }
    }
}
