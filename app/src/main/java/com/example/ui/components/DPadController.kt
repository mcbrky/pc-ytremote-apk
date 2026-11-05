package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.RemoteKeyNames
import com.example.ui.theme.RemoteDesignStyle
import com.example.ui.theme.TextPrimary

@Composable
fun DPadController(
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 275.dp, // Enlarged size for 6.4" display
    style: RemoteDesignStyle = RemoteDesignStyle.OBSIDIAN_PRO
) {
    val ringGradient = Brush.sweepGradient(
        listOf(style.accentPrimary, style.accentSecondary, style.accentPrimary)
    )

    val edgeOffset = (size * 0.05f).coerceIn(10.dp, 18.dp)
    val buttonTouchSize = (size * 0.24f).coerceIn(58.dp, 72.dp)
    val iconSize = (size * 0.14f).coerceIn(34.dp, 44.dp)

    // Outer circle with dynamic theme colors
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                spotColor = style.accentPrimary.copy(alpha = 0.4f),
                ambientColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(style.dpadDialTop, style.dpadDialBottom)
                )
            )
            .border(
                width = 3.dp,
                brush = ringGradient,
                shape = CircleShape
            )
            .testTag("dpad_controller"),
        contentAlignment = Alignment.Center
    ) {
        // UP Button
        LargeDPadButton(
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Up",
            accentColor = style.accentPrimary,
            buttonSize = buttonTouchSize,
            iconSize = iconSize,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = edgeOffset)
                .testTag("dpad_up"),
            onClick = { onKeyClick(RemoteKeyNames.UP) }
        )

        // DOWN Button
        LargeDPadButton(
            icon = Icons.Default.KeyboardArrowDown,
            contentDescription = "Down",
            accentColor = style.accentSecondary,
            buttonSize = buttonTouchSize,
            iconSize = iconSize,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -edgeOffset)
                .testTag("dpad_down"),
            onClick = { onKeyClick(RemoteKeyNames.DOWN) }
        )

        // LEFT Button
        LargeDPadButton(
            icon = Icons.Default.KeyboardArrowLeft,
            contentDescription = "Left",
            accentColor = style.accentPrimary,
            buttonSize = buttonTouchSize,
            iconSize = iconSize,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = edgeOffset)
                .testTag("dpad_left"),
            onClick = { onKeyClick(RemoteKeyNames.LEFT) }
        )

        // RIGHT Button
        LargeDPadButton(
            icon = Icons.Default.KeyboardArrowRight,
            contentDescription = "Right",
            accentColor = style.accentSecondary,
            buttonSize = buttonTouchSize,
            iconSize = iconSize,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = -edgeOffset)
                .testTag("dpad_right"),
            onClick = { onKeyClick(RemoteKeyNames.RIGHT) }
        )

        // Center Button with Theme Ok Dish
        BigCenterOkButton(
            size = size * 0.40f,
            style = style,
            ringGradient = ringGradient,
            onClick = { onKeyClick(RemoteKeyNames.ENTER) }
        )
    }
}

@Composable
private fun LargeDPadButton(
    icon: ImageVector,
    contentDescription: String,
    accentColor: Color,
    buttonSize: Dp,
    iconSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(90),
        label = "largeDpadPress"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isPressed) accentColor else TextPrimary.copy(alpha = 0.9f),
        label = "iconColor"
    )

    Box(
        modifier = modifier
            .size(buttonSize)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isPressed) {
                    Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.3f), Color.Transparent)
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(Color(0x18FFFFFF), Color.Transparent)
                    )
                }
            )
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
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun BigCenterOkButton(
    size: Dp,
    style: RemoteDesignStyle,
    ringGradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = tween(90),
        label = "bigOkPress"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = if (isPressed) {
                        listOf(style.okDishBottom, style.okDishCenter)
                    } else {
                        listOf(style.okDishTop, style.okDishCenter, style.okDishBottom)
                    }
                )
            )
            .border(
                width = 2.5.dp,
                brush = ringGradient,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("dpad_ok"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = if (isPressed) style.accentPrimary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.12f),
                radius = (this.size.width / 2f) - 4.dp.toPx()
            )
        }
    }
}
