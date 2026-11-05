package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.PanTool
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RemoteChassisBorder
import com.example.ui.theme.RemoteChassisHighlight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TouchpadGrid
import com.example.ui.theme.TrackpadGlassBottom
import com.example.ui.theme.TrackpadGlassTop
import kotlin.math.hypot

@Composable
fun TouchpadView(
    onDrag: (dx: Float, dy: Float) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tapToClickEnabled: Boolean = true,
    isCompact: Boolean = false,
    style: com.example.ui.theme.RemoteDesignStyle = com.example.ui.theme.RemoteDesignStyle.OBSIDIAN_PRO
) {
    val density = LocalDensity.current
    val clickDistanceThresholdPx = with(density) { 10.dp.toPx() }

    var isTouching by remember { mutableStateOf(false) }
    var touchPos by remember { mutableStateOf<Offset?>(null) }
    var dragDistance by remember { mutableFloatStateOf(0f) }

    val activeGlowAlpha by animateFloatAsState(
        targetValue = if (isTouching) 0.85f else 0.25f,
        animationSpec = tween(140),
        label = "touchpadGlow"
    )

    // User Design Matte Trackpad with Theme Colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = style.accentPrimary.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(style.trackpadTop, style.trackpadBottom)
                )
            )
            .border(
                width = 1.dp,
                color = if (isTouching) style.accentPrimary else style.buttonBorder,
                shape = RoundedCornerShape(26.dp)
            )
            .testTag("touchpad_surface")
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isTouching = true
                    touchPos = down.position
                    dragDistance = 0f
                    val startTime = System.currentTimeMillis()

                    var pointer = down
                    while (true) {
                        val event = awaitPointerEvent()
                        val currentPointer = event.changes.firstOrNull { it.id == pointer.id } ?: break

                        if (currentPointer.pressed) {
                            val change = currentPointer.positionChange()
                            if (change.x != 0f || change.y != 0f) {
                                dragDistance += hypot(change.x, change.y)
                                onDrag(change.x, change.y)
                                touchPos = currentPointer.position
                                currentPointer.consume()
                            }
                        } else {
                            // Pointer released
                            val elapsed = System.currentTimeMillis() - startTime
                            if (tapToClickEnabled && dragDistance < clickDistanceThresholdPx && elapsed < 350) {
                                onClick()
                            }
                            break
                        }
                    }
                    isTouching = false
                    touchPos = null
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Finger touch ripple/glow when user drags
        Canvas(modifier = Modifier.fillMaxSize()) {
            touchPos?.let { pos ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            style.accentSecondary.copy(alpha = 0.5f),
                            style.accentPrimary.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = pos,
                        radius = 42.dp.toPx()
                    ),
                    center = pos,
                    radius = 42.dp.toPx()
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = 4.dp.toPx(),
                    center = pos
                )
            }
        }

        // Exact Clean Typography from User Design
        Text(
            text = "Touch Pad",
            color = Color(0xFFF3F4F6),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    }
}
