package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.communication.CommunicationManager
import com.example.data.local.entity.ChatMessageEntity
import com.example.model.SpecialistAgentType
import com.example.ui.theme.AgentMasterColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    onNavigateToTrading: () -> Unit
) {
    val context = LocalContext.current
    val isUser = message.isUser
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))

    val agent = try {
        SpecialistAgentType.valueOf(message.agentType)
    } catch (e: Exception) {
        SpecialistAgentType.MASTER
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(agent.color.copy(alpha = 0.2f))
                    .border(1.dp, agent.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = agent.icon,
                    contentDescription = agent.title,
                    tint = agent.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Header for assistant messages
            if (!isUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = agent.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = agent.color
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (message.statusTag != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = message.statusTag,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Bubble container
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, agent.color.copy(alpha = 0.3f)) else null,
                tonalElevation = 2.dp,
                modifier = Modifier.testTag("chat_message_${message.id}")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    // Action button cards
                    if (!message.actionType.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        when (message.actionType) {
                            "WHATSAPP" -> {
                                Button(
                                    onClick = {
                                        CommunicationManager.openWhatsApp(context, text = message.actionPayload ?: message.text)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF25D366),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("whatsapp_action_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open in WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            "SMS" -> {
                                Button(
                                    onClick = {
                                        CommunicationManager.openSms(context, text = message.actionPayload ?: message.text)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("sms_action_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "SMS", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Send via Native SMS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            "EMAIL" -> {
                                Button(
                                    onClick = {
                                        CommunicationManager.openEmail(
                                            context = context,
                                            body = message.actionPayload ?: message.text
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEA4335),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("email_action_button")
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open in Gmail App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            "TRADING_CALC" -> {
                                FilledTonalButton(
                                    onClick = onNavigateToTrading,
                                    modifier = Modifier.fillMaxWidth().testTag("trading_calc_button")
                                ) {
                                    Text("View in Forex Calculator & Journal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            "CODE_SNIPPET" -> {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Master Agent Code", message.actionPayload ?: message.text)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("copy_code_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Code for Termux", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (isUser) {
                Text(
                    text = timeStr,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 2.dp, end = 4.dp)
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
