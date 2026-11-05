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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RemoteKeyNames
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.RemoteChassisBorder
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VolumeControls(
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Physical Remote Volume Rocker Bar
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .shadow(4.dp, RoundedCornerShape(14.dp), spotColor = Color.Black.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF142034), Color(0xFF0C1422))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(RemoteChassisBorder, Color(0xFF0B1320))
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 4.dp, vertical = 3.dp)
            .testTag("volume_controls_row"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Volume Down
        VolumeActionButton(
            label = "VOL -",
            icon = Icons.Default.VolumeDown,
            testTag = "volume_down_button",
            modifier = Modifier.weight(1f),
            onClick = { onKeyClick(RemoteKeyNames.VOLUME_DOWN) }
        )

        // Divider
        Box(
            modifier = Modifier
                .size(width = 1.dp, height = 24.dp)
                .background(Color(0xFF1E314D))
        )

        // Mute
        VolumeActionButton(
            label = "MUTE",
            icon = Icons.Default.VolumeOff,
            testTag = "volume_mute_button",
            accentOnPress = StatusError,
            modifier = Modifier.weight(1f),
            onClick = { onKeyClick(RemoteKeyNames.MUTE) }
        )

        // Divider
        Box(
            modifier = Modifier
                .size(width = 1.dp, height = 24.dp)
                .background(Color(0xFF1E314D))
        )

        // Volume Up
        VolumeActionButton(
            label = "VOL +",
            icon = Icons.Default.VolumeUp,
            testTag = "volume_up_button",
            modifier = Modifier.weight(1f),
            onClick = { onKeyClick(RemoteKeyNames.VOLUME_UP) }
        )
    }
}

@Composable
private fun VolumeActionButton(
    label: String,
    icon: ImageVector,
    testTag: String,
    modifier: Modifier = Modifier,
    accentOnPress: Color = AccentCyan,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = tween(70),
        label = "volPress"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (isPressed) {
                    Modifier.background(
                        Brush.verticalGradient(listOf(Color(0xFF0C1320), Color(0xFF060A12)))
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPressed) accentOnPress else TextSecondary,
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPressed) accentOnPress else TextPrimary
            )
        }
    }
}
