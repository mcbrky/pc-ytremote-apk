package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RemoteChassisBorder
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.voice.VoiceSearchManager
import com.example.voice.VoiceState

@Composable
fun SearchBarSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSendQuery: (String) -> Unit,
    voiceManager: VoiceSearchManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val voiceState by voiceManager.voiceState.collectAsState()

    val isListening = voiceState is VoiceState.Listening

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceManager.startListening { resultText ->
                onQueryChange(resultText)
            }
        }
    }

    val micPulseTransition = rememberInfiniteTransition(label = "micPulse")
    val micScale by micPulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

    val micButtonColor by animateColorAsState(
        targetValue = if (isListening) StatusError else AccentCyan,
        label = "micButtonColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Input Field Container (Sleek Recessed Slot)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .shadow(4.dp, RoundedCornerShape(21.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(21.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0F1A2A), Color(0xFF0A121E))
                    )
                )
                .border(1.dp, if (isListening) AccentCyan else RemoteChassisBorder, RoundedCornerShape(21.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search YouTube",
                    tint = TextMuted,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty() && !isListening) {
                        Text(
                            text = "Search YouTube TV...",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    } else if (isListening && query.isEmpty()) {
                        Text(
                            text = "Listening... Speak now",
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(AccentCyan),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            keyboardController?.hide()
                            onSendQuery(query)
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_input")
                    )
                }

                // Clear button
                AnimatedVisibility(visible = query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // Round Physical Microphone Button
        IconButton(
            onClick = {
                if (isListening) {
                    voiceManager.stopListening()
                } else {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        voiceManager.startListening { recognizedText ->
                            onQueryChange(recognizedText)
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            },
            modifier = Modifier
                .size(42.dp)
                .shadow(4.dp, CircleShape, spotColor = if (isListening) StatusError.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f))
                .clip(CircleShape)
                .background(
                    if (isListening) {
                        Brush.radialGradient(listOf(StatusError.copy(alpha = 0.3f), Color(0xFF1B0D12)))
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFF19273E), Color(0xFF0E1726)))
                    }
                )
                .border(1.dp, if (isListening) StatusError else RemoteChassisBorder, CircleShape)
                .testTag("voice_search_button")
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = if (isListening) "Stop Listening" else "Voice Search",
                tint = micButtonColor,
                modifier = Modifier
                    .size(20.dp)
                    .scale(if (isListening) micScale else 1f)
            )
        }

        // Round Physical Send Button
        IconButton(
            onClick = {
                keyboardController?.hide()
                onSendQuery(query)
            },
            enabled = query.isNotBlank(),
            modifier = Modifier
                .size(42.dp)
                .shadow(4.dp, CircleShape, spotColor = if (query.isNotBlank()) PrimaryBlue.copy(alpha = 0.5f) else Color.Transparent)
                .clip(CircleShape)
                .background(
                    if (query.isNotBlank()) {
                        Brush.linearGradient(listOf(Color(0xFF2695FF), PrimaryBlue, Color(0xFF0C5CB8)))
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFF141F30), Color(0xFF0C1320)))
                    }
                )
                .border(1.dp, if (query.isNotBlank()) Color(0xFF67BCFF) else RemoteChassisBorder, CircleShape)
                .testTag("send_search_button")
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send Search to YouTube",
                tint = if (query.isNotBlank()) TextPrimary else TextMuted,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}
