package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.communication.CommunicationManager
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunicationsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var testPhone by remember { mutableStateOf("") }
    var testText by remember { mutableStateOf("Assalam-o-alaikum bhai! Master Agent integration test.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Communications & Integrations", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("واٹس ایپ، ایس ایم ایس اور ای میل رابطے", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("comms_back_button")) {
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
            // Transparency notice
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Transparency",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Authentic Connection Security Policy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Master Agent Android intents ke zariye aapke official apps (WhatsApp, Messages, Gmail) me pre-filled message dispatch karta hai. Background me secret reading ya fake simulation nahi hoti.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Integration Status Overview", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val statuses = CommunicationManager.IntegrationStatus.values()
            statuses.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(if (item.isConnected) Color(0xFF10B981) else Color(0xFFF59E0B), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = if (item.isConnected) "Active (Intent)" else "Requires Cloud Auth",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isConnected) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                            }
                            Text(
                                item.note,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Direct Test Dispatch
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Direct Test Dispatch", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Test real communication on your Android phone", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = testPhone,
                        onValueChange = { testPhone = it },
                        label = { Text("Phone Number / WhatsApp (Optional)", fontSize = 12.sp) },
                        placeholder = { Text("+923001234567") },
                        modifier = Modifier.fillMaxWidth().testTag("test_phone_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = testText,
                        onValueChange = { testText = it },
                        label = { Text("Draft Message", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("test_msg_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val res = CommunicationManager.openWhatsApp(context, testPhone, testText)
                                Toast.makeText(context, res.getOrNull() ?: res.exceptionOrNull()?.message, Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                            modifier = Modifier.weight(1f).testTag("test_whatsapp_button")
                        ) {
                            Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val res = CommunicationManager.openSms(context, testPhone, testText)
                                Toast.makeText(context, res.getOrNull() ?: res.exceptionOrNull()?.message, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("test_sms_button")
                        ) {
                            Text("SMS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val res = CommunicationManager.openEmail(context = context, body = testText)
                                Toast.makeText(context, res.getOrNull() ?: res.exceptionOrNull()?.message, Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335), contentColor = Color.White),
                            modifier = Modifier.weight(1f).testTag("test_email_button")
                        ) {
                            Text("Gmail", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
