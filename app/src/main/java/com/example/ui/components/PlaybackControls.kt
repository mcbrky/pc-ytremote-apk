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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.RemoteKeyNames
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RemoteButtonBorder
import com.example.ui.theme.RemoteButtonBottom
import com.example.ui.theme.RemoteButtonPressedTop
import com.example.ui.theme.RemoteButtonTop
import com.example.ui.theme.RemoteChassisBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PlaybackControls(
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: com.example.ui.theme.RemoteDesignStyle = com.example.ui.theme.RemoteDesignStyle.OBSIDIAN_PRO
) {
    // Physical Remote Media Control Cluster
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(style.buttonBgTop, style.buttonBgBottom)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(style.buttonBorder, style.chassisBottom)
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("playback_controls_row"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Track
        PlaybackButton(
            icon = Icons.Default.SkipPrevious,
            contentDescription = "Previous Video",
            accentColor = style.accentSecondary,
            testTag = "playback_prev",
            onClick = { onKeyClick(RemoteKeyNames.MEDIA_PREV) }
        )

        // Rewind (10s seek in YouTube)
        PlaybackButton(
            icon = Icons.Default.FastRewind,
            contentDescription = "Rewind 10 Seconds",
            accentColor = style.accentSecondary,
            testTag = "playback_rewind",
            onClick = { onKeyClick(RemoteKeyNames.LEFT) }
        )

        // Big Round Center Play/Pause Button
        CenterPlayPauseButton(
            style = style,
            onClick = { onKeyClick(RemoteKeyNames.MEDIA_PLAY_PAUSE) }
        )

        // Fast Forward (10s seek in YouTube)
        PlaybackButton(
            icon = Icons.Default.FastForward,
            contentDescription = "Fast Forward 10 Seconds",
            accentColor = style.accentSecondary,
            testTag = "playback_fast_forward",
            onClick = { onKeyClick(RemoteKeyNames.RIGHT) }
        )

        // Next Track
        PlaybackButton(
            icon = Icons.Default.SkipNext,
            contentDescription = "Next Video",
            accentColor = style.accentSecondary,
            testTag = "playback_next",
            onClick = { onKeyClick(RemoteKeyNames.MEDIA_NEXT) }
        )
    }
}

@Composable
private fun PlaybackButton(
    icon: ImageVector,
    contentDescription: String,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(70),
        label = "btnPress"
    )

    Box(
        modifier = modifier
            .size(46.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isPressed) {
                    Brush.verticalGradient(listOf(RemoteButtonPressedTop, Color(0xFF060A12)))
                } else {
                    Brush.verticalGradient(listOf(Color(0x18FFFFFF), Color.Transparent))
                }
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
            tint = if (isPressed) accentColor else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun CenterPlayPauseButton(
    style: com.example.ui.theme.RemoteDesignStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = tween(70),
        label = "playPress"
    )

    // Big Raised Physical Dome Play Button
    Box(
        modifier = modifier
            .size(56.dp) // Large prominent 56dp circular action button
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = CircleShape,
                spotColor = style.accentSecondary.copy(alpha = 0.5f),
                ambientColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = if (isPressed) {
                        listOf(style.accentSecondary, style.accentPrimary)
                    } else {
                        listOf(style.accentPrimary, style.accentPrimary.copy(alpha = 0.8f), style.dpadDialBottom)
                    }
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        style.accentSecondary,
                        style.accentPrimary,
                        style.okDishRim
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("playback_play_pause"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play or Pause",
            tint = TextPrimary,
            modifier = Modifier.size(32.dp)
        )
    }
}
