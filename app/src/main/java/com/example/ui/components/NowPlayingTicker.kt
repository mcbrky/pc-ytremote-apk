package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RemoteDesignStyle

@Composable
fun NowPlayingTicker(
    title: String,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null,
    onTitleChange: ((String) -> Unit)? = null,
    style: RemoteDesignStyle = RemoteDesignStyle.OBSIDIAN_PRO
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizerTransition")
    var showEditDialog by remember { mutableStateOf(false) }
    var editTextInput by remember(title) { mutableStateOf(title) }

    // Animated equalizer bar heights
    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val displayText = if (title.isNotBlank()) title else "YouTube: Tap to set or sync currently playing title"

    // Sleek single-line running ticker bar
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(17.dp),
                spotColor = style.accentPrimary.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(17.dp))
            .background(style.buttonBgTop)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        style.accentPrimary.copy(alpha = 0.7f),
                        style.accentSecondary.copy(alpha = 0.5f),
                        style.buttonBorder
                    )
                ),
                shape = RoundedCornerShape(17.dp)
            )
            .padding(horizontal = 8.dp)
            .testTag("now_playing_ticker"),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Pill: Play/Pause button + Animated Equalizer
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFCC0000).copy(alpha = 0.22f))
                    .border(0.8.dp, Color(0xFFFF2A2A).copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    .clickable { onTogglePlay() }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPlaying) "Playing" else "Paused",
                    tint = Color(0xFFFF3333),
                    modifier = Modifier.size(12.dp)
                )

                if (isPlaying) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(bar1Height.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(Color(0xFFFF3333))
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(bar2Height.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(Color(0xFFFFAA00))
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(bar3Height.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(style.accentSecondary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Running Marquee Text (Tap to edit / sync custom title)
            Text(
                text = displayText,
                color = Color(0xFFF3F4F6),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .clickable { showEditDialog = true }
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        animationMode = MarqueeAnimationMode.Immediately,
                        initialDelayMillis = 1000,
                        velocity = 38.dp
                    )
            )

            // Right Action: Edit or Sync icon
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .clickable { showEditDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Playing Title",
                        tint = style.accentPrimary.copy(alpha = 0.85f),
                        modifier = Modifier.size(13.dp)
                    )
                }

                if (onRefresh != null) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .clickable { onRefresh() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync YouTube Title",
                            tint = style.accentSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }

    // Quick Title Edit & Live Fetch Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "Now Playing YouTube Title",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter or paste the YouTube video title currently playing:",
                        fontSize = 12.sp,
                        color = Color(0xFFA3B4C9)
                    )

                    OutlinedTextField(
                        value = editTextInput,
                        onValueChange = { editTextInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = style.accentPrimary,
                            unfocusedBorderColor = style.buttonBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Lofi Beats" to "YouTube: Lofi Hip Hop Radio ☕ - Beats to Relax/Study to",
                            "Bollywood" to "YouTube: Top Bollywood Hits 2026",
                            "Bangla" to "YouTube: Best Bangla Songs Collection"
                        ).forEach { (label, full) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(style.buttonBgBottom)
                                    .border(1.dp, style.buttonBorder, RoundedCornerShape(6.dp))
                                    .clickable { editTextInput = full }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    color = style.accentPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    if (onRefresh != null) {
                        OutlinedButton(
                            onClick = {
                                onRefresh()
                                showEditDialog = false
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = style.accentSecondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fetch Active Title From PC", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val formatted = if (editTextInput.isNotBlank()) {
                            if (editTextInput.startsWith("YouTube", ignoreCase = true)) {
                                editTextInput
                            } else {
                                "YouTube: $editTextInput"
                            }
                        } else {
                            "YouTube: Playing"
                        }
                        onTitleChange?.invoke(formatted)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = style.accentPrimary)
                ) {
                    Text("Apply & Save", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color.Gray, fontSize = 12.sp)
                }
            },
            containerColor = Color(0xFF111D30),
            shape = RoundedCornerShape(18.dp)
        )
    }
}
