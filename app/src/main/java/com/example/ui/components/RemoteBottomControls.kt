package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RemoteDesignStyle

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff

@Composable
fun RemoteBottomControls(
    currentVolume: Int,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit,
    onVolumeSlide: (Int) -> Unit = {},
    onMute: () -> Unit,
    onKeyboardClick: () -> Unit,
    onBackClick: () -> Unit,
    onNumpadClick: () -> Unit,
    onHomeClick: () -> Unit,
    onChannelUp: () -> Unit = {},
    onChannelDown: () -> Unit = {},
    onScrollDrag: (Float) -> Unit = {},
    onScrollUp: () -> Unit = onChannelUp,
    onScrollDown: () -> Unit = onChannelDown,
    isVoiceListening: Boolean = false,
    onVoiceClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    style: RemoteDesignStyle = RemoteDesignStyle.OBSIDIAN_PRO
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Left: Interactive Slideable Volume Bar with Fluid Level Indicator
        SlideableVolumeBar(
            currentVolume = currentVolume,
            onVolumeUp = onVolumeUp,
            onVolumeDown = onVolumeDown,
            onVolumeSlide = onVolumeSlide,
            style = style
        )

        // 2. Center: 3 Top / 3 Bottom Round Action Buttons Cluster
        Column(
            modifier = Modifier.width(176.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Keyboard, Back (Undo), Numpad (Dialpad)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundActionButton(
                    icon = Icons.Default.Keyboard,
                    contentDescription = "Search Keyboard",
                    size = 46.dp,
                    iconSize = 22.dp,
                    onClick = onKeyboardClick,
                    testTag = "btn_keyboard",
                    style = style
                )

                RoundActionButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Back / Escape",
                    size = 46.dp,
                    iconSize = 22.dp,
                    onClick = onBackClick,
                    testTag = "btn_back",
                    style = style
                )

                RoundActionButton(
                    icon = Icons.Default.Dialpad,
                    contentDescription = "Numpad & Controls",
                    size = 46.dp,
                    iconSize = 22.dp,
                    onClick = onNumpadClick,
                    testTag = "btn_numpad",
                    style = style
                )
            }

            // Bottom Row: Speaker/Mute, Built-in Voice Typing Mic, Home
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundActionButton(
                    icon = Icons.Default.VolumeMute,
                    contentDescription = "Mute",
                    size = 46.dp,
                    iconSize = 22.dp,
                    onClick = onMute,
                    testTag = "btn_mute",
                    style = style
                )

                // Built-in Voice Typing Mic button (Direct inline typing, no Google Voice activity)
                RoundActionButton(
                    icon = if (isVoiceListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isVoiceListening) "Stop Listening" else "Built-in Voice Typing",
                    size = 48.dp,
                    iconSize = 24.dp,
                    onClick = onVoiceClick,
                    tintColor = if (isVoiceListening) Color(0xFFFF3333) else style.accentPrimary,
                    borderColor = if (isVoiceListening) Color(0xFFFF3333) else style.accentPrimary.copy(alpha = 0.6f),
                    testTag = "btn_voice_typing",
                    style = style
                )

                RoundActionButton(
                    icon = Icons.Default.Home,
                    contentDescription = "Home",
                    size = 46.dp,
                    iconSize = 22.dp,
                    onClick = onHomeClick,
                    testTag = "btn_home",
                    style = style
                )
            }
        }

        // 3. Right: Interactive Vertical Screen Scroll Slider (Replaces legacy CH rocker)
        SlideableScrollBar(
            onScrollDrag = onScrollDrag,
            onScrollUp = onScrollUp,
            onScrollDown = onScrollDown,
            style = style
        )
    }
}

/**
 * Slideable Volume Bar that can be smoothly dragged up/down or tapped
 * to adjust volume between 0 and 100 with fluid visual level feedback.
 */
@Composable
private fun SlideableVolumeBar(
    currentVolume: Int,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit,
    onVolumeSlide: (Int) -> Unit,
    style: RemoteDesignStyle,
    modifier: Modifier = Modifier
) {
    val barHeight = 144.dp
    val barWidth = 56.dp
    val density = LocalDensity.current
    val barHeightPx = with(density) { barHeight.toPx() }

    val volumeFraction = (currentVolume / 100f).coerceIn(0f, 1f)

    val animatedFillFraction by animateFloatAsState(
        targetValue = volumeFraction,
        animationSpec = tween(90),
        label = "volumeFillAnim"
    )

    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(barWidth)
            .height(barHeight)
            .shadow(
                elevation = if (isDragging) 12.dp else 8.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = style.accentPrimary.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        style.buttonBgTop,
                        style.buttonBgBottom
                    )
                )
            )
            .border(
                width = 1.5.dp,
                color = if (isDragging) style.accentPrimary else style.buttonBorder,
                shape = RoundedCornerShape(28.dp)
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, _ ->
                        change.consume()
                        // Calculate volume based on vertical touch position (bottom = 0%, top = 100%)
                        val touchY = change.position.y.coerceIn(0f, barHeightPx)
                        val fraction = 1f - (touchY / barHeightPx)
                        val targetVol = (fraction * 100).toInt().coerceIn(0, 100)
                        onVolumeSlide(targetVol)
                    }
                )
            }
            .testTag("volume_rocker"),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Dynamic Fill Level Track (Bottom to Top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(animatedFillFraction)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            style.accentSecondary.copy(alpha = 0.55f),
                            style.accentPrimary.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // Buttons & Content Layer
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // VOL + Button (Top)
            RockerSectionButton(
                icon = Icons.Default.Add,
                contentDescription = "Volume Up",
                onClick = onVolumeUp,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Center Label "VOL" and dynamic level "$currentVolume"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "VOL",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$currentVolume",
                    color = style.accentPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // VOL - Button (Bottom)
            RockerSectionButton(
                icon = Icons.Default.Remove,
                contentDescription = "Volume Down",
                onClick = onVolumeDown,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

/**
 * Interactive Vertical Screen Scroll Slider.
 * Allows smooth continuous scrolling up and down via dragging or tapping top/bottom buttons,
 * replacing the legacy CH rocker pill.
 */
@Composable
private fun SlideableScrollBar(
    onScrollDrag: (Float) -> Unit,
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit,
    style: RemoteDesignStyle,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragVisualOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .width(56.dp)
            .height(144.dp)
            .shadow(
                elevation = if (isDragging) 12.dp else 8.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = style.accentPrimary.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        style.buttonBgTop,
                        style.buttonBgBottom
                    )
                )
            )
            .border(
                width = 1.5.dp,
                color = if (isDragging) style.accentPrimary else style.buttonBorder,
                shape = RoundedCornerShape(28.dp)
            )
            .testTag("scroll_slider"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Scroll Up Button (Top) - Directly clickable
            RockerSectionButton(
                icon = Icons.Default.KeyboardArrowUp,
                contentDescription = "Scroll Up",
                onClick = onScrollUp,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Center Scroll Indicator & Draggable Middle Track
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                dragVisualOffset = 0f
                            },
                            onDragEnd = {
                                isDragging = false
                                dragVisualOffset = 0f
                            },
                            onDragCancel = {
                                isDragging = false
                                dragVisualOffset = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dy = dragAmount.y
                                dragVisualOffset = (dragVisualOffset + dy * 0.4f).coerceIn(-18f, 18f)
                                onScrollDrag(dy)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.offset { IntOffset(0, dragVisualOffset.roundToInt()) }
                ) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isDragging) style.accentPrimary else Color.White.copy(alpha = 0.25f)
                            )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "SCRL",
                        color = if (isDragging) style.accentPrimary else Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isDragging) style.accentPrimary else Color.White.copy(alpha = 0.25f)
                            )
                    )
                }
            }

            // Scroll Down Button (Bottom) - Directly clickable
            RockerSectionButton(
                icon = Icons.Default.KeyboardArrowDown,
                contentDescription = "Scroll Down",
                onClick = onScrollDown,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun RockerSectionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(90),
        label = "rockerBtnPress"
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .clip(CircleShape)
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
            tint = if (isPressed) Color.White else Color(0xFFE2E8F0),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun RoundActionButton(
    icon: ImageVector,
    contentDescription: String,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    testTag: String,
    style: RemoteDesignStyle,
    modifier: Modifier = Modifier,
    tintColor: Color? = null,
    borderColor: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(90),
        label = "roundBtnPress"
    )

    val activeBorder = borderColor ?: if (isPressed) style.accentPrimary else style.buttonBorder
    val activeTint = tintColor ?: if (isPressed) style.accentPrimary else Color(0xFFF1F5F9)

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(
                if (isPressed) style.buttonBgBottom else style.buttonBgTop
            )
            .border(
                width = if (borderColor != null) 1.5.dp else 1.dp,
                color = activeBorder,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = activeTint,
            modifier = Modifier.size(iconSize)
        )
    }
}
