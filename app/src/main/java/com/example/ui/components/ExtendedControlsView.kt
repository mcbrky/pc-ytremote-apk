package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RemoteKeyNames

@Composable
fun ExtendedControlsView(
    onKeyClick: (String) -> Unit,
    onBackToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("extended_controls_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Media Playback Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF23252A))
                .border(1.dp, Color(0xFF383A42), RoundedCornerShape(20.dp))
                .padding(vertical = 10.dp, horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediaSmallButton(
                    icon = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Video",
                    onClick = { onKeyClick(RemoteKeyNames.MEDIA_PREV) }
                )

                MediaSmallButton(
                    icon = Icons.Default.FastRewind,
                    contentDescription = "Rewind",
                    onClick = { onKeyClick(RemoteKeyNames.LEFT) }
                )

                // Play / Pause Prominent Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0xFF2D9CDB).copy(alpha = 0.5f))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF9B51E0), Color(0xFF2D9CDB)))
                        )
                        .clickable {
                            isPlaying = !isPlaying
                            onKeyClick(RemoteKeyNames.MEDIA_PLAY_PAUSE)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                MediaSmallButton(
                    icon = Icons.Default.FastForward,
                    contentDescription = "Fast Forward",
                    onClick = { onKeyClick(RemoteKeyNames.RIGHT) }
                )

                MediaSmallButton(
                    icon = Icons.Default.SkipNext,
                    contentDescription = "Next Video",
                    onClick = { onKeyClick(RemoteKeyNames.MEDIA_NEXT) }
                )
            }
        }

        // 2. 0-9 Numeric Keypad (for TV channels and video selection)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF23252A))
                .border(1.dp, Color(0xFF383A42), RoundedCornerShape(20.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: 1, 2, 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KeypadNumberButton(number = "1", onClick = { onKeyClick("1") })
                KeypadNumberButton(number = "2", onClick = { onKeyClick("2") })
                KeypadNumberButton(number = "3", onClick = { onKeyClick("3") })
            }

            // Row 2: 4, 5, 6
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KeypadNumberButton(number = "4", onClick = { onKeyClick("4") })
                KeypadNumberButton(number = "5", onClick = { onKeyClick("5") })
                KeypadNumberButton(number = "6", onClick = { onKeyClick("6") })
            }

            // Row 3: 7, 8, 9
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KeypadNumberButton(number = "7", onClick = { onKeyClick("7") })
                KeypadNumberButton(number = "8", onClick = { onKeyClick("8") })
                KeypadNumberButton(number = "9", onClick = { onKeyClick("9") })
            }

            // Row 4: Fullscreen, 0, Space
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KeypadIconButton(
                    icon = Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen",
                    onClick = { onKeyClick(RemoteKeyNames.F11) }
                )
                KeypadNumberButton(number = "0", onClick = { onKeyClick("0") })
                KeypadIconButton(
                    icon = Icons.Default.SpaceBar,
                    contentDescription = "Space",
                    onClick = { onKeyClick(RemoteKeyNames.SPACE) }
                )
            }
        }

        // 3. Return to Main Remote Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF2C2E35))
                .border(1.dp, Color(0xFF40434C), RoundedCornerShape(14.dp))
                .clickable { onBackToMain() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Back to Main Remote",
                color = Color(0xFF2D9CDB),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun KeypadNumberButton(
    number: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(90),
        label = "numPress"
    )

    Box(
        modifier = Modifier
            .size(54.dp, 44.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPressed) Color(0xFF383B44) else Color(0xFF2C2E35))
            .border(1.dp, Color(0xFF40434C), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun KeypadIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(90),
        label = "iconPress"
    )

    Box(
        modifier = Modifier
            .size(54.dp, 44.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPressed) Color(0xFF383B44) else Color(0xFF2C2E35))
            .border(1.dp, Color(0xFF40434C), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MediaSmallButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isPressed) Color(0xFF383B44) else Color(0xFF2C2E35))
            .border(1.dp, Color(0xFF40434C), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}
