package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.communication.CommunicationManager
import com.example.ui.components.AudioFrequencyVisualizer
import com.example.ui.components.HolographicOrbVisualizer
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun VoiceOrbScreen(
    viewModel: MainViewModel,
    onNavigateToTrading: () -> Unit
) {
    val context = LocalContext.current

    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val isListening by viewModel.voiceEngine.isListening.collectAsState()
    val audioRms by viewModel.voiceEngine.audioRms.collectAsState()
    val statusText by viewModel.voiceEngine.currentStatusText.collectAsState()
    val latestAssistantSpeech by viewModel.latestAssistantSpeech.collectAsState()
    val latestUserSpeech by viewModel.latestUserSpeech.collectAsState()
    val activeSpecialist by viewModel.currentSpecialist.collectAsState()
    val activeAction by viewModel.activeAction.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    var showTextInput by remember { mutableStateOf(false) }
    var textQuery by remember { mutableStateOf("") }

    // Audio permission handling for continuous hands-free voice
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setAudioPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.setAudioPermissionGranted(true)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val suggestionChips = listOf(
        "EUR/USD lot size calculate karo",
        "Termux Python script generate karo",
        "Emails check karo aur report suna do",
        "Quotex / Pocket Option price action rules",
        "WhatsApp par rabta karo"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        FuturisticDarkBackground,
                        Color(0xFF0F0624),
                        FuturisticDarkBackground
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top HUD Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (isListening) NeonCyanSecondary else NeonPurpleBright)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MASTER AGENT",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "مرکزی آرکیسٹریٹر • Voice-First AI",
                        fontSize = 11.sp,
                        color = NeonPurpleBright.copy(alpha = 0.85f)
                    )
                }

                // Active Status Pill (Hands-Free / Speaking)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isSpeaking -> NeonPinkContainer.copy(alpha = 0.85f)
                        isListening -> NeonCyanContainer.copy(alpha = 0.85f)
                        else -> NeonPurpleContainer.copy(alpha = 0.85f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isSpeaking -> NeonPinkTertiary.copy(alpha = 0.6f)
                            isListening -> NeonCyanSecondary.copy(alpha = 0.6f)
                            else -> NeonPurpleBright.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSpeaking -> NeonPinkTertiary
                                        isListening -> NeonCyanSecondary
                                        else -> NeonPurpleBright
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isSpeaking -> "Speaking (Urdu)"
                                isListening -> "Listening (Hands-Free)"
                                isProcessing -> "Thinking..."
                                else -> "Active"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isSpeaking -> NeonPinkOnContainer
                                isListening -> NeonCyanOnContainer
                                else -> NeonPurpleOnContainer
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Primary 3D Robot / Visualizer Orb Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("holographic_orb_avatar"),
                contentAlignment = Alignment.Center
            ) {
                HolographicOrbVisualizer(
                    isSpeaking = isSpeaking,
                    isListening = isListening,
                    audioRms = audioRms,
                    onOrbTap = { viewModel.onOrbTapped() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Glowing Audio Frequency Waveform Bars
            AudioFrequencyVisualizer(
                isActive = isSpeaking || isListening,
                audioRms = audioRms,
                isSpeaking = isSpeaking,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .testTag("audio_frequency_waveform")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    isSpeaking -> NeonPinkTertiary
                    isListening -> NeonCyanSecondary
                    else -> NeonPurpleBright
                },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Holographic Subtitle / Speech Display HUD (Replaces standard chat dashboard)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FuturisticDarkSurfaceVariant.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, FuturisticDarkOutline),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.8f)
                    .testTag("voice_transcript_hud")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Active Specialist Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(activeSpecialist.color.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = activeSpecialist.icon,
                                    contentDescription = activeSpecialist.title,
                                    tint = activeSpecialist.color,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeSpecialist.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = activeSpecialist.color
                            )
                        }

                        Text(
                            text = activeSpecialist.urduTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    // User Prompt (if recently spoken)
                    if (latestUserSpeech.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aap ne farmaya: \"$latestUserSpeech\"",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeonCyanSecondary.copy(alpha = 0.9f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = FuturisticDarkOutline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Assistant Roman Urdu Spoken Text
                    Text(
                        text = latestAssistantSpeech,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFFF3E8FF)
                    )

                    // Active Action Integration Dispatcher
                    activeAction?.let { action ->
                        Spacer(modifier = Modifier.height(12.dp))
                        when (action.type) {
                            "WHATSAPP" -> {
                                Button(
                                    onClick = {
                                        CommunicationManager.openWhatsApp(context, text = action.payload)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("whatsapp_action_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open in WhatsApp", fontWeight = FontWeight.Bold)
                                }
                            }
                            "SMS" -> {
                                Button(
                                    onClick = {
                                        CommunicationManager.openSms(context, text = action.payload)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("sms_action_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Send via SMS", fontWeight = FontWeight.Bold)
                                }
                            }
                            "EMAIL" -> {
                                Button(
                                    onClick = {
                                        CommunicationManager.openEmail(context = context, body = action.payload)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335), contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("email_action_button")
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open in Gmail Draft", fontWeight = FontWeight.Bold)
                                }
                            }
                            "TRADING_CALC" -> {
                                FilledTonalButton(
                                    onClick = onNavigateToTrading,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("trading_calc_button")
                                ) {
                                    Text("View Forex Risk & Journal", fontWeight = FontWeight.Bold)
                                }
                            }
                            "CODE_SNIPPET" -> {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Termux Script", action.payload)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Code copied for Termux!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("copy_code_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Copy Code for Termux", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick speech prompt chips (1-tap hands-free alternatives)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestionChips.forEach { chipText ->
                    SuggestionChip(
                        onClick = {
                            viewModel.onUserSpoke(chipText)
                        },
                        label = { Text(chipText, fontSize = 11.sp, color = NeonPurpleBright) },
                        border = androidx.compose.foundation.BorderStroke(1.dp, FuturisticDarkOutline.copy(alpha = 0.6f)),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = FuturisticDarkSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Discrete keyboard toggle for noisy environments (No push-to-talk/recording buttons!)
            AnimatedVisibility(visible = showTextInput, enter = fadeIn(), exit = fadeOut()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    OutlinedTextField(
                        value = textQuery,
                        onValueChange = { textQuery = it },
                        placeholder = { Text("Ya likh kar batayein...", fontSize = 12.sp) },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("discrete_text_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurpleBright,
                            unfocusedBorderColor = FuturisticDarkOutline
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textQuery.isNotBlank()) {
                                viewModel.onUserSpoke(textQuery)
                                textQuery = ""
                                showTextInput = false
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(NeonPurplePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (!showTextInput) {
                TextButton(
                    onClick = { showTextInput = true },
                    modifier = Modifier.testTag("toggle_keyboard_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Keyboard",
                        tint = NeonPurpleBright.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Silent environment? Tap to type",
                        fontSize = 11.sp,
                        color = NeonPurpleBright.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
