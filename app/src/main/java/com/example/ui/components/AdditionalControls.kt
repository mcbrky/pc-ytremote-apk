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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardTab
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SpaceBar
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AdditionalControls(
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Physical Remote Navigation Utility Bar
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(12.dp))
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
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag("additional_controls_row"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back / ESC
        KeyPillButton(
            label = "ESC",
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            testTag = "key_esc",
            modifier = Modifier.weight(1f),
            onClick = { onKeyClick(RemoteKeyNames.ESC) }
        )

        // Home
        KeyPillButton(
            label = "HOME",
            icon = Icons.Default.Home,
            testTag = "key_home",
            modifier = Modifier.weight(1f),
            onClick = { onKeyClick(RemoteKeyNames.HOME) }
        )

        // Fullscreen F11
        KeyPillButton(
            label = "F11",
            icon = Icons.Default.Fullscreen,
            testTag = "key_f11",
            modifier = Modifier.weight(1f),
            onClick = { onKeyClick(RemoteKeyNames.F11) }
        )

        // Space
        KeyPillButton(
            label = "SPACE",
            icon = Icons.Default.SpaceBar,
            testTag = "key_space",
            modifier = Modifier.weight(1f),
            onClick = { onKeyClick(RemoteKeyNames.SPACE) }
        )

        // Tab
        KeyPillButton(
            label = "TAB",
            icon = Icons.AutoMirrored.Filled.KeyboardTab,
            testTag = "key_tab",
            modifier = Modifier.weight(1f),
            onClick = { onKeyClick(RemoteKeyNames.TAB) }
        )
    }
}

@Composable
private fun KeyPillButton(
    label: String,
    icon: ImageVector,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = tween(70),
        label = "pillPress"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
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
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPressed) AccentCyan else TextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPressed) AccentCyan else TextPrimary
            )
        }
    }
}
