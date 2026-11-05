package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RemoteChassisBorder
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HeaderBar(
    status: ConnectionStatus,
    serverIp: String,
    onReconnect: () -> Unit,
    onOpenSettings: () -> Unit,
    onCycleTheme: (() -> Unit)? = null,
    onToggleAirMouse: (() -> Unit)? = null,
    isAirMouseEnabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when (status) {
            ConnectionStatus.CONNECTED -> StatusSuccess
            ConnectionStatus.CONNECTING -> StatusWarning
            ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> StatusError
        },
        label = "statusColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Remote Brand Emblem
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("app_brand_header")
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .shadow(4.dp, RoundedCornerShape(10.dp), spotColor = AccentCyan.copy(alpha = 0.4f))
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2493FF), PrimaryBlue, Color(0xFF0D54A3))
                        )
                    )
                    .border(1.dp, Color(0xFF5AB6FF), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Smart TV Remote",
                    tint = TextPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "yt",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = AccentCyan,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "remote",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "PC TV REMOTE CONTROL",
                    fontSize = 9.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            }
        }

        // Right side: Jewel Optical LED status pill & round tactile settings button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Connection Jewel LED Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F1A2B))
                    .border(1.dp, RemoteChassisBorder, RoundedCornerShape(16.dp))
                    .clickable { onReconnect() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("connection_status_indicator"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Optical LED Jewel
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .scale(if (status == ConnectionStatus.CONNECTING) pulseScale else 1f)
                        .shadow(6.dp, CircleShape, spotColor = statusColor)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.White, statusColor, statusColor.copy(alpha = 0.7f))
                            )
                        )
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = if (status == ConnectionStatus.CONNECTED) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = "Wi-Fi Status",
                    tint = statusColor,
                    modifier = Modifier.size(13.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = when (status) {
                        ConnectionStatus.CONNECTED -> "Online"
                        ConnectionStatus.CONNECTING -> "Syncing"
                        ConnectionStatus.DISCONNECTED -> "Offline"
                        ConnectionStatus.ERROR -> "Error"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            // Quick Theme Switch Button
            if (onCycleTheme != null) {
                IconButton(
                    onClick = onCycleTheme,
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(4.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.5f))
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF19273E), Color(0xFF0E1726))
                            )
                        )
                        .border(1.dp, RemoteChassisBorder, CircleShape)
                        .testTag("theme_switch_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = "Switch Remote Theme",
                        tint = AccentCyan,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Quick Air Mouse Toggle Button (1-tap on/off directly from remote)
            if (onToggleAirMouse != null) {
                IconButton(
                    onClick = onToggleAirMouse,
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(
                            elevation = if (isAirMouseEnabled) 6.dp else 4.dp,
                            shape = CircleShape,
                            spotColor = if (isAirMouseEnabled) AccentCyan else Color.Black.copy(alpha = 0.5f)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                if (isAirMouseEnabled)
                                    listOf(Color(0xFF005577), Color(0xFF003344))
                                else
                                    listOf(Color(0xFF19273E), Color(0xFF0E1726))
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = if (isAirMouseEnabled) AccentCyan else RemoteChassisBorder,
                            shape = CircleShape
                        )
                        .testTag("air_mouse_toggle_button")
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.NearMe,
                        contentDescription = if (isAirMouseEnabled) "Air Mouse Active (Tap to disable)" else "Air Mouse Off (Tap to enable)",
                        tint = if (isAirMouseEnabled) AccentCyan else TextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Big round tactile Settings button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(38.dp)
                    .shadow(4.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.5f))
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF19273E), Color(0xFF0E1726))
                        )
                    )
                    .border(1.dp, RemoteChassisBorder, CircleShape)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
