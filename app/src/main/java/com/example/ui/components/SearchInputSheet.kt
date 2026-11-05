package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.RemoteKeyNames
import com.example.voice.VoiceSearchManager
import com.example.voice.VoiceState

@Composable
fun SearchInputSheet(
    query: String,
    onQueryChange: (String) -> Unit,
    onSendQuery: (String) -> Unit,
    onSendQueryOnly: (String) -> Unit = onSendQuery,
    onKeyClick: (String) -> Unit = {},
    voiceManager: VoiceSearchManager,
    onDismiss: () -> Unit
) {
    val voiceState by voiceManager.voiceState.collectAsState()
    val isListening = voiceState is VoiceState.Listening

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("universal_keyboard_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181E2A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header with Keyboard icon and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Universal PC Keyboard",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Type or speak to write in any active search bar or app",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFA0AEC0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Text Input Field with Clear & Mic Buttons
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("universal_keyboard_input"),
                    placeholder = {
                        Text(
                            text = if (isListening) "🎙️ Listening... Speak now" else "Type anything to write on PC...",
                            color = if (isListening) Color(0xFF00E5FF) else Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color(0xFFA0AEC0),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Built-in Voice Typing Mic inside Keyboard Dialog
                            IconButton(
                                onClick = {
                                    if (isListening) {
                                        voiceManager.stopListening()
                                    } else {
                                        voiceManager.startListening { recognizedText ->
                                            if (recognizedText.isNotBlank()) {
                                                val combined = if (query.isBlank()) recognizedText else "$query $recognizedText"
                                                onQueryChange(combined)
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Voice Typing",
                                    tint = if (isListening) Color(0xFFFF3333) else Color(0xFF00E5FF),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F1523),
                        unfocusedContainerColor = Color(0xFF0F1523),
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Hardware Key Shortcuts (Backspace, Enter, Space, Tab, Esc)
                Text(
                    text = "PC Key Shortcuts:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickKeys = listOf(
                        "⌫ Back" to RemoteKeyNames.BACKSPACE,
                        "↵ Enter" to RemoteKeyNames.ENTER,
                        "␣ Space" to RemoteKeyNames.SPACE,
                        "⇥ Tab" to RemoteKeyNames.TAB,
                        "Esc" to RemoteKeyNames.ESC
                    )
                    quickKeys.forEach { (label, keyName) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF243048))
                                .border(1.dp, Color(0xFF3B4D6E), RoundedCornerShape(8.dp))
                                .clickable { onKeyClick(keyName) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row: [Type Only] and [Type & Enter ↵]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Type Text Only (without hitting Enter)
                    OutlinedButton(
                        onClick = {
                            if (query.isNotBlank()) {
                                onSendQueryOnly(query)
                                onDismiss()
                            }
                        },
                        enabled = query.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF00E5FF).copy(alpha = 0.6f))
                        )
                    ) {
                        Text(text = "Type Only", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // 2. Type & Enter (ideal for any search bar or submitting forms)
                    Button(
                        onClick = {
                            if (query.isNotBlank()) {
                                onSendQuery(query)
                                onDismiss()
                            }
                        },
                        enabled = query.isNotBlank(),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Type & Enter ↵", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
