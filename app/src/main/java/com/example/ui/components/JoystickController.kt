package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RemoteKeyNames
import com.example.ui.theme.RemoteDesignStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun JoystickController(
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 275.dp,
    sensitivity: Float = 1.0f,
    style: RemoteDesignStyle = RemoteDesignStyle.OBSIDIAN_PRO
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // Physics state for thumbstick offset in pixels
    val animOffsetX = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(0f) }

    val safeSens = sensitivity.coerceIn(0.4f, 2.5f)
    val sizePx = with(density) { size.toPx() }
    val maxRadius = sizePx * 0.28f
    val baseDeadZone = sizePx * 0.08f
    // Higher sensitivity reduces deadzone so lighter touches register immediately
    val effectiveDeadZone = (baseDeadZone / safeSens).coerceIn(10f, baseDeadZone * 1.5f)
    val thumbSize = (size * 0.42f).coerceIn(100.dp, 126.dp)

    var currentActiveDirection by remember { mutableStateOf<String?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var isPressedCenter by remember { mutableStateOf(false) }

    // Automatic repeat loop when held in a direction; delay decreases with higher sensitivity
    LaunchedEffect(currentActiveDirection, safeSens) {
        val direction = currentActiveDirection
        if (direction != null) {
            onKeyClick(direction)
            val initialDelay = (300L / safeSens).toLong().coerceIn(120L, 450L)
            delay(initialDelay)
            val repeatDelay = (200L / safeSens).toLong().coerceIn(60L, 300L)
            while (isActive && currentActiveDirection == direction) {
                onKeyClick(direction)
                delay(repeatDelay)
            }
        }
    }

    val rimBrush = Brush.sweepGradient(
        listOf(
            style.accentPrimary,
            style.accentSecondary,
            style.accentPrimary
        )
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.8f),
                ambientColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(style.dpadWell)
            .border(
                width = 2.5.dp,
                brush = rimBrush,
                shape = CircleShape
            )
            .testTag("joystick_controller"),
        contentAlignment = Alignment.Center
    ) {
        // Background graduation rings & directional guide notches
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val center = Offset(canvasW / 2f, canvasH / 2f)
            val outerR = canvasW / 2f

            // Inner boundary guide ring
            drawCircle(
                color = style.chassisBorder.copy(alpha = 0.4f),
                radius = maxRadius + (thumbSize.toPx() / 3f),
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Inner neutral concentric circle
            drawCircle(
                color = style.accentPrimary.copy(alpha = 0.15f),
                radius = maxRadius * 0.55f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Crosshair tick marks
            val tickLength = 12.dp.toPx()
            val tickDistance = outerR - 16.dp.toPx()

            // North
            drawLine(
                color = if (currentActiveDirection == RemoteKeyNames.UP) style.accentPrimary else Color.White.copy(alpha = 0.25f),
                start = Offset(center.x, tickDistance - tickLength),
                end = Offset(center.x, tickDistance),
                strokeWidth = if (currentActiveDirection == RemoteKeyNames.UP) 3.dp.toPx() else 1.5.dp.toPx()
            )
            // South
            drawLine(
                color = if (currentActiveDirection == RemoteKeyNames.DOWN) style.accentPrimary else Color.White.copy(alpha = 0.25f),
                start = Offset(center.x, canvasH - tickDistance),
                end = Offset(center.x, canvasH - tickDistance + tickLength),
                strokeWidth = if (currentActiveDirection == RemoteKeyNames.DOWN) 3.dp.toPx() else 1.5.dp.toPx()
            )
            // West
            drawLine(
                color = if (currentActiveDirection == RemoteKeyNames.LEFT) style.accentPrimary else Color.White.copy(alpha = 0.25f),
                start = Offset(tickDistance - tickLength, center.y),
                end = Offset(tickDistance, center.y),
                strokeWidth = if (currentActiveDirection == RemoteKeyNames.LEFT) 3.dp.toPx() else 1.5.dp.toPx()
            )
            // East
            drawLine(
                color = if (currentActiveDirection == RemoteKeyNames.RIGHT) style.accentPrimary else Color.White.copy(alpha = 0.25f),
                start = Offset(canvasW - tickDistance, center.y),
                end = Offset(canvasW - tickDistance + tickLength, center.y),
                strokeWidth = if (currentActiveDirection == RemoteKeyNames.RIGHT) 3.dp.toPx() else 1.5.dp.toPx()
            )
        }

        // Directional Arrows on Outer Ring
        DirectionChevron(
            icon = Icons.Default.KeyboardArrowUp,
            isActive = currentActiveDirection == RemoteKeyNames.UP,
            accentColor = style.accentPrimary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 8.dp)
        )
        DirectionChevron(
            icon = Icons.Default.KeyboardArrowDown,
            isActive = currentActiveDirection == RemoteKeyNames.DOWN,
            accentColor = style.accentPrimary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-8).dp)
        )
        DirectionChevron(
            icon = Icons.Default.KeyboardArrowLeft,
            isActive = currentActiveDirection == RemoteKeyNames.LEFT,
            accentColor = style.accentPrimary,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 8.dp)
        )
        DirectionChevron(
            icon = Icons.Default.KeyboardArrowRight,
            isActive = currentActiveDirection == RemoteKeyNames.RIGHT,
            accentColor = style.accentPrimary,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-8).dp)
        )

        // Center Movable 3D Thumbstick Knob with Spring Physics & Crisp OK Button Action
        val currentX = animOffsetX.value
        val currentY = animOffsetY.value

        Box(
            modifier = Modifier
                .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
                .size(thumbSize)
                .scale(if (isPressedCenter) 0.93f else 1f)
                .shadow(
                    elevation = if (isDragging) 14.dp else 8.dp,
                    shape = CircleShape,
                    spotColor = style.accentPrimary.copy(alpha = 0.45f)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            style.okDishTop,
                            style.okDishCenter,
                            style.okDishBottom
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = if (isDragging || isPressedCenter) style.accentPrimary else style.okDishRim,
                    shape = CircleShape
                )
                .pointerInput(safeSens) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downTime = System.currentTimeMillis()
                        var movedFar = false
                        var totalMovedDist = 0f
                        isDragging = true
                        isPressedCenter = true
                        val pointerId = down.id

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) {
                                break
                            }
                            change.consume()
                            val dragDelta = change.position - change.previousPosition
                            val nextX = animOffsetX.value + dragDelta.x
                            val nextY = animOffsetY.value + dragDelta.y
                            val dist = hypot(nextX, nextY)
                            totalMovedDist += hypot(dragDelta.x, dragDelta.y)

                            val clampedX: Float
                            val clampedY: Float
                            if (dist > maxRadius) {
                                val scale = maxRadius / dist
                                clampedX = nextX * scale
                                clampedY = nextY * scale
                            } else {
                                clampedX = nextX
                                clampedY = nextY
                            }

                            coroutineScope.launch {
                                animOffsetX.snapTo(clampedX)
                                animOffsetY.snapTo(clampedY)
                            }

                            if (dist > effectiveDeadZone) {
                                movedFar = true
                                isPressedCenter = false
                                val angleRad = atan2(clampedY, clampedX)
                                val angleDeg = (angleRad * (180.0 / PI)).toFloat()

                                val dir = when {
                                    angleDeg >= -45f && angleDeg <= 45f -> RemoteKeyNames.RIGHT
                                    angleDeg > 45f && angleDeg < 135f -> RemoteKeyNames.DOWN
                                    angleDeg < -45f && angleDeg > -135f -> RemoteKeyNames.UP
                                    else -> RemoteKeyNames.LEFT
                                }
                                currentActiveDirection = dir
                            } else {
                                currentActiveDirection = null
                            }
                        }

                        // Pointer released
                        val duration = System.currentTimeMillis() - downTime
                        isDragging = false
                        isPressedCenter = false
                        currentActiveDirection = null

                        // If user tapped or released without dragging beyond deadzone: FIRE OK / ENTER!
                        if (!movedFar || (totalMovedDist < effectiveDeadZone && duration < 500)) {
                            onKeyClick(RemoteKeyNames.ENTER)
                        }

                        // Spring back to center
                        coroutineScope.launch {
                            animOffsetX.animateTo(
                                0f,
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                        coroutineScope.launch {
                            animOffsetY.animateTo(
                                0f,
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    }
                }
                .testTag("joystick_thumb_knob"),
            contentAlignment = Alignment.Center
        ) {
            // Textured Concave Dish on Thumbstick Head
            Box(
                modifier = Modifier
                    .size(thumbSize * 0.65f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                style.okDishBottom.copy(alpha = 0.9f),
                                style.okDishCenter,
                                style.okDishTop
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (isPressedCenter) style.accentPrimary else Color.White.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // "OK" label & tactile grip
                Text(
                    text = "OK",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDragging || isPressedCenter) style.accentPrimary else Color.White.copy(alpha = 0.9f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun DirectionChevron(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isActive) accentColor else Color.White.copy(alpha = 0.45f),
        modifier = modifier.size(28.dp)
    )
}
