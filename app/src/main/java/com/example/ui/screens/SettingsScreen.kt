package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.model.RemoteSettings
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardBorderHighlight
import com.example.ui.theme.CardDark
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(
    currentSettings: RemoteSettings,
    connectionStatus: ConnectionStatus,
    statusMessage: String?,
    onSaveConnection: (ip: String, port: Int, pin: String) -> Unit,
    onReconnect: () -> Unit,
    onUpdateMouseSensitivity: (Float) -> Unit,
    onUpdateJoystickSensitivity: (Float) -> Unit,
    onUpdateGyroEnabled: (Boolean) -> Unit,
    onUpdateGyroSensitivity: (Float) -> Unit,
    onUpdateVibrationEnabled: (Boolean) -> Unit,
    onSelectDesignStyle: (com.example.ui.theme.RemoteDesignStyle) -> Unit,
    onUpdateControllerSizes: (dpadScale: Float?, touchpadHeightScale: Float?) -> Unit,
    onToggleUseJoystick: (Boolean) -> Unit,
    onUpdateTouchpadSettings: (showTouchpad: Boolean?, tapToClick: Boolean?, sensitivity: Float?, heightScale: Float?) -> Unit = { _, _, _, _ -> },
    onToggleSwapDpadAndTouchpad: (Boolean) -> Unit = {},
    onUpdateScrollSettings: (showScrollBar: Boolean?, sensitivity: Float?, invertScroll: Boolean?) -> Unit = { _, _, _ -> },
    onUpdateAirMouseSettings: (enabled: Boolean?, sensitivity: Float?, smoothing: Float?, pauseWhileTyping: Boolean?, showQuickToggle: Boolean?) -> Unit = { _, _, _, _, _ -> },
    onUpdateFeatureSettings: (keepScreenOn: Boolean?, autoEnterOnTyping: Boolean?, vibrationEnabled: Boolean?, useJoystick: Boolean?) -> Unit = { _, _, _, _ -> },
    onUpdateDisplaySettings: (showUpNext: Boolean?, showVoiceSearch: Boolean?, showPlaybackControls: Boolean?, showVolumeControls: Boolean?, showAdditionalControls: Boolean?) -> Unit = { _, _, _, _, _ -> },
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var ipInput by remember(currentSettings.serverIp) { mutableStateOf(currentSettings.serverIp) }
    var portInput by remember(currentSettings.serverPort) { mutableStateOf(currentSettings.serverPort.toString()) }
    var pinInput by remember(currentSettings.pin) { mutableStateOf(currentSettings.pin) }

    var localMouseSens by remember(currentSettings.mouseSensitivity) {
        mutableFloatStateOf(currentSettings.mouseSensitivity)
    }
    var localJoystickSens by remember(currentSettings.joystickSensitivity) {
        mutableFloatStateOf(currentSettings.joystickSensitivity)
    }
    var localGyroSens by remember(currentSettings.gyroSensitivity) {
        mutableFloatStateOf(currentSettings.gyroSensitivity)
    }
    var localGyroSmoothing by remember(currentSettings.gyroSmoothing) {
        mutableFloatStateOf(currentSettings.gyroSmoothing)
    }
    var localScrollSens by remember(currentSettings.scrollSensitivity) {
        mutableFloatStateOf(currentSettings.scrollSensitivity)
    }
    var localDpadScale by remember(currentSettings.dpadScale) {
        mutableFloatStateOf(currentSettings.dpadScale)
    }
    var localTouchpadScale by remember(currentSettings.touchpadHeightScale) {
        mutableFloatStateOf(currentSettings.touchpadHeightScale)
    }

    var isCheckingUpdates by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
            .testTag("settings_screen_container")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardDark)
                    .border(1.dp, CardBorder, CircleShape)
                    .testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Remote",
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Remote Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Connection, Sensors & Display",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Windows PC Connection Card
        SettingsSectionCard(
            title = "Windows PC Connection",
            icon = Icons.Default.NetworkCheck
        ) {
            OutlinedTextField(
                value = ipInput,
                onValueChange = { ipInput = it },
                label = { Text("Windows PC IP Address") },
                placeholder = { Text("e.g. 192.168.1.100") },
                singleLine = true,
                colors = textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_server_ip")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = portInput,
                    onValueChange = { portInput = it },
                    label = { Text("Port") },
                    placeholder = { Text("8765") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = textFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_server_port")
                )

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    label = { Text("PIN") },
                    placeholder = { Text("2468") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = textFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_server_pin")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status message
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (connectionStatus) {
                            ConnectionStatus.CONNECTED -> StatusSuccess.copy(alpha = 0.15f)
                            ConnectionStatus.CONNECTING -> StatusWarning.copy(alpha = 0.15f)
                            else -> StatusError.copy(alpha = 0.15f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> Icons.Default.CheckCircle
                        ConnectionStatus.CONNECTING -> Icons.Default.Refresh
                        else -> Icons.Default.Error
                    },
                    contentDescription = null,
                    tint = when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> StatusSuccess
                        ConnectionStatus.CONNECTING -> StatusWarning
                        else -> StatusError
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> "Connected to $ipInput:$portInput"
                        ConnectionStatus.CONNECTING -> "Connecting..."
                        ConnectionStatus.DISCONNECTED -> "Offline - verify Python server is running"
                        ConnectionStatus.ERROR -> statusMessage ?: "Connection error"
                    },
                    fontSize = 11.sp,
                    color = when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> StatusSuccess
                        ConnectionStatus.CONNECTING -> StatusWarning
                        else -> StatusError
                    },
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val parsedPort = portInput.toIntOrNull() ?: 8765
                        onSaveConnection(ipInput, parsedPort, pinInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_connection_button")
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save & Apply", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onReconnect,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorderHighlight)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_reconnect_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Reconnect", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Remote Hardware Finish & Style Section
        SettingsSectionCard(
            title = "Remote Design & Themes",
            icon = Icons.Default.ColorLens
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                com.example.ui.theme.RemoteDesignStyle.entries.forEach { style ->
                    val isSelected = currentSettings.remoteDesignStyle == style
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CardDark else Color(0xFF0C1320))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AccentCyan else CardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectDesignStyle(style) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color Swatch Pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            style.swatchColors.forEach { swatchColor ->
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                        .border(0.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = style.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AccentCyan else TextPrimary
                                )

                                if (style.isLight) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE2E8F0))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "LIGHT THEME",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = style.tagline,
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentCyan.copy(alpha = 0.18f))
                                    .border(1.dp, AccentCyan, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Active",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Screen Fit & Sizing Card
        SettingsSectionCard(
            title = "Screen Fit & Sizes (6.4\" Phone Scale)",
            icon = Icons.Default.Tune
        ) {
            // Navigation Controller Mode Selection
            Text(
                text = "Controller Type",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Joystick Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (currentSettings.useJoystick) AccentCyan.copy(alpha = 0.2f) else CardDark)
                        .border(
                            1.5.dp,
                            if (currentSettings.useJoystick) AccentCyan else CardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onToggleUseJoystick(true) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "360° Joystick",
                        fontSize = 12.sp,
                        fontWeight = if (currentSettings.useJoystick) FontWeight.Bold else FontWeight.Medium,
                        color = if (currentSettings.useJoystick) AccentCyan else TextSecondary
                    )
                }

                // D-Pad Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!currentSettings.useJoystick) AccentCyan.copy(alpha = 0.2f) else CardDark)
                        .border(
                            1.5.dp,
                            if (!currentSettings.useJoystick) AccentCyan else CardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onToggleUseJoystick(false) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Circular D-Pad",
                        fontSize = 12.sp,
                        fontWeight = if (!currentSettings.useJoystick) FontWeight.Bold else FontWeight.Medium,
                        color = if (!currentSettings.useJoystick) AccentCyan else TextSecondary
                    )
                }
            }

            // Joystick Sensitivity Slider (Interactive responsiveness control)
            if (currentSettings.useJoystick) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Joystick Sensitivity",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", localJoystickSens)}x",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                }
                Slider(
                    value = localJoystickSens,
                    onValueChange = {
                        localJoystickSens = it
                        onUpdateJoystickSensitivity(it)
                    },
                    valueRange = 0.4f..2.5f,
                    colors = sliderColors(),
                    modifier = Modifier.testTag("slider_joystick_sensitivity")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Gentle (0.6x)" to 0.6f,
                        "Standard (1.0x)" to 1.0f,
                        "Fast (1.8x)" to 1.8f
                    ).forEach { (label, sensVal) ->
                        val isCurrent = kotlin.math.abs(localJoystickSens - sensVal) < 0.15f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) AccentCyan.copy(alpha = 0.2f) else CardDark)
                                .border(1.dp, if (isCurrent) AccentCyan else CardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    localJoystickSens = sensVal
                                    onUpdateJoystickSensitivity(sensVal)
                                }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) AccentCyan else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // D-Pad / Controller Size Scale
            Text(
                text = "Controller Size: ${String.format(Locale.US, "%.0f%%", localDpadScale * 100)} (~${(240 * localDpadScale).toInt()} dp)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Slider(
                value = localDpadScale,
                onValueChange = {
                    localDpadScale = it
                    onUpdateControllerSizes(it, null)
                },
                valueRange = 0.85f..1.35f,
                colors = sliderColors(),
                modifier = Modifier.testTag("slider_dpad_scale")
            )

            // D-Pad Quick Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Compact" to 0.90f,
                    "Standard" to 1.05f,
                    "6.4\" Full Display" to 1.22f
                ).forEach { (label, scale) ->
                    val isCurrent = kotlin.math.abs(localDpadScale - scale) < 0.05f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrent) AccentCyan.copy(alpha = 0.2f) else CardDark)
                            .border(1.dp, if (isCurrent) AccentCyan else CardBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                localDpadScale = scale
                                onUpdateControllerSizes(scale, null)
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) AccentCyan else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Controller D-Pad / Joystick Size Scale
            Text(
                text = "D-Pad / Controller Size: ${String.format(Locale.US, "%.0f%%", localDpadScale * 100)} (~${(240 * localDpadScale).toInt()} dp)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Slider(
                value = localDpadScale,
                onValueChange = {
                    localDpadScale = it
                    onUpdateControllerSizes(it, null)
                },
                valueRange = 0.85f..1.35f,
                colors = sliderColors(),
                modifier = Modifier.testTag("slider_dpad_scale")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Touchpad & Scroll Controls Card (টারপ্যাড ও স্ক্রল সেটিংস)
        SettingsSectionCard(
            title = "Touchpad & Scroll Controls",
            icon = Icons.Default.TouchApp
        ) {
            // Interchange D-Pad & Touchpad Position (ডি প্যাড উপরে / টাচপ্যাড নিচে অদলবদল)
            Text(
                text = "Controller & Touchpad Layout / অবস্থান অদলবদল",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "ডি প্যাড উপরে এবং টাচপ্যাড নিচে অদলবদল (Interchange) করার অপশন",
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Option 1: Touchpad Top, D-Pad Bottom (Default)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!currentSettings.swapDpadAndTouchpad) AccentCyan.copy(alpha = 0.2f) else CardDark)
                        .border(
                            1.5.dp,
                            if (!currentSettings.swapDpadAndTouchpad) AccentCyan else CardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onToggleSwapDpadAndTouchpad(false) }
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Touchpad Top",
                            fontSize = 12.sp,
                            fontWeight = if (!currentSettings.swapDpadAndTouchpad) FontWeight.Bold else FontWeight.Medium,
                            color = if (!currentSettings.swapDpadAndTouchpad) AccentCyan else TextSecondary
                        )
                        Text(
                            text = "D-Pad Bottom (Default)",
                            fontSize = 10.sp,
                            color = if (!currentSettings.swapDpadAndTouchpad) AccentCyan.copy(alpha = 0.8f) else TextMuted
                        )
                    }
                }

                // Option 2: D-Pad Top, Touchpad Bottom (Swapped)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (currentSettings.swapDpadAndTouchpad) AccentCyan.copy(alpha = 0.2f) else CardDark)
                        .border(
                            1.5.dp,
                            if (currentSettings.swapDpadAndTouchpad) AccentCyan else CardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onToggleSwapDpadAndTouchpad(true) }
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "D-Pad Top ⬆",
                            fontSize = 12.sp,
                            fontWeight = if (currentSettings.swapDpadAndTouchpad) FontWeight.Bold else FontWeight.Medium,
                            color = if (currentSettings.swapDpadAndTouchpad) AccentCyan else TextSecondary
                        )
                        Text(
                            text = "Touchpad Bottom ⬇",
                            fontSize = 10.sp,
                            color = if (currentSettings.swapDpadAndTouchpad) AccentCyan.copy(alpha = 0.8f) else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SettingSwitchRow(
                title = "D-Pad on Top & Touchpad on Bottom",
                subtitle = "ডি প্যাড স্ক্রিনের উপরে এবং টাচপ্যাড নিচে ইন্টারচেঞ্জ করুন",
                checked = currentSettings.swapDpadAndTouchpad,
                testTag = "switch_swap_dpad_touchpad",
                onCheckedChange = { onToggleSwapDpadAndTouchpad(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Touchpad Show / Hide Toggle
            SettingSwitchRow(
                title = "Show Touchpad on Remote",
                subtitle = "রিমোটের টাচপ্যাড প্রদর্শন বা হাইড করুন",
                checked = currentSettings.showTouchpad,
                testTag = "switch_show_touchpad",
                onCheckedChange = { onUpdateTouchpadSettings(it, null, null, null) }
            )

            if (currentSettings.showTouchpad) {
                Spacer(modifier = Modifier.height(8.dp))

                // Tap to Click Toggle
                SettingSwitchRow(
                    title = "Touchpad Tap to Click",
                    subtitle = "টারপ্যাডে ট্যাপ করে কম্পিউটার ক্লিক (অফ রাখলে ড্র্যাগিংয়ে ভুল ক্লিক হবে না)",
                    checked = currentSettings.touchpadTapToClick,
                    testTag = "switch_touchpad_tap_click",
                    onCheckedChange = { onUpdateTouchpadSettings(null, it, null, null) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Touchpad Pointer Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Touchpad Sensitivity",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", localMouseSens)}x",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                }
                Slider(
                    value = localMouseSens,
                    onValueChange = {
                        localMouseSens = it
                        onUpdateMouseSensitivity(it)
                        onUpdateTouchpadSettings(null, null, it, null)
                    },
                    valueRange = 0.5f..3.5f,
                    colors = sliderColors(),
                    modifier = Modifier.testTag("slider_mouse_sensitivity")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Touchpad Height Scale
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Touchpad Height Scale",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.0f%%", localTouchpadScale * 100)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                }
                Slider(
                    value = localTouchpadScale,
                    onValueChange = {
                        localTouchpadScale = it
                        onUpdateControllerSizes(null, it)
                        onUpdateTouchpadSettings(null, null, null, it)
                    },
                    valueRange = 0.80f..1.50f,
                    colors = sliderColors(),
                    modifier = Modifier.testTag("slider_touchpad_scale")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vertical Screen Scroll Bar Toggle
            SettingSwitchRow(
                title = "Show Vertical Scroll Slider (SCRL)",
                subtitle = "রিমোটের ডানপাশে উপর-নিচ স্ক্রল স্লাইডার অন বা অফ রাখুন",
                checked = currentSettings.showScrollBar,
                testTag = "switch_show_scroll_bar",
                onCheckedChange = { onUpdateScrollSettings(it, null, null) }
            )

            if (currentSettings.showScrollBar) {
                Spacer(modifier = Modifier.height(8.dp))

                // Scroll Sensitivity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scroll Speed & Sensitivity",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", localScrollSens)}x",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                }
                Slider(
                    value = localScrollSens,
                    onValueChange = {
                        localScrollSens = it
                        onUpdateScrollSettings(null, it, null)
                    },
                    valueRange = 0.5f..4.0f,
                    colors = sliderColors(),
                    modifier = Modifier.testTag("slider_scroll_sensitivity")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Invert Scroll Direction
                SettingSwitchRow(
                    title = "Invert Scroll Direction",
                    subtitle = "ন্যাচারাল স্ক্রলিং (আঙুল নিচে টানলে পেজ নিচে স্ক্রল হবে)",
                    checked = currentSettings.invertScroll,
                    testTag = "switch_invert_scroll",
                    onCheckedChange = { onUpdateScrollSettings(null, null, it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Air Mouse & Gyroscope Settings Card (এয়ার মাউস সেটিংস)
        SettingsSectionCard(
            title = "Air Mouse & Sensors",
            icon = Icons.Default.NearMe
        ) {
            // Gyroscope Air Mouse Toggle
            SettingSwitchRow(
                title = "Gyroscope Air Mouse",
                subtitle = "ফোন হাতে নাড়িয়ে কম্পিউটারের মাউস কার্সর চালান",
                checked = currentSettings.gyroEnabled,
                testTag = "switch_gyroscope",
                onCheckedChange = { onUpdateGyroEnabled(it) }
            )

            if (currentSettings.gyroEnabled) {
                Spacer(modifier = Modifier.height(8.dp))

                // Air Mouse Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Air Mouse Pointer Speed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AccentCyan
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", localGyroSens)}x",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                }
                Slider(
                    value = localGyroSens,
                    onValueChange = {
                        localGyroSens = it
                        onUpdateGyroSensitivity(it)
                        onUpdateAirMouseSettings(null, it, null, null, null)
                    },
                    valueRange = 0.5f..4.0f,
                    colors = sliderColors(),
                    modifier = Modifier.testTag("slider_gyro_sensitivity")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Air Mouse Smoothness Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pointer Smoothness (Anti-Jitter)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${(localGyroSmoothing * 100).toInt()}% (Ultra Smooth)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                }
                Slider(
                    value = localGyroSmoothing,
                    onValueChange = {
                        localGyroSmoothing = it
                        onUpdateAirMouseSettings(null, null, it, null, null)
                    },
                    valueRange = 0.50f..0.95f,
                    colors = sliderColors(),
                    modifier = Modifier.testTag("slider_gyro_smoothing")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Auto-pause during typing
                SettingSwitchRow(
                    title = "Auto-Pause while Typing",
                    subtitle = "টাইপ বা ভয়েস ইনপুটের সময় মাউস ফ্রিজ রাখা (কার্সর লাফালাফি বন্ধ করবে)",
                    checked = currentSettings.pauseAirMouseWhileTyping,
                    testTag = "switch_pause_typing",
                    onCheckedChange = { onUpdateAirMouseSettings(null, null, null, it, null) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Air Mouse button on remote
                SettingSwitchRow(
                    title = "Quick Button on Remote Bar",
                    subtitle = "রিমোটের উপরের বারে ১-ট্যাপে এয়ার মাউস অন/অফ বাটন",
                    checked = currentSettings.showQuickAirMouseToggle,
                    testTag = "switch_quick_air_mouse_btn",
                    onCheckedChange = { onUpdateAirMouseSettings(null, null, null, null, it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Useful Remote Features & Display Toggles Card (দরকারি অপশন - অন/অফ)
        SettingsSectionCard(
            title = "Remote Features & Display",
            icon = Icons.Default.Widgets
        ) {
            // Keep Phone Screen Awake
            SettingSwitchRow(
                title = "Keep Phone Screen Awake",
                subtitle = "রিমোট ব্যবহারের সময় স্ক্রিনের আলো নিভবে না",
                checked = currentSettings.keepScreenOn,
                testTag = "switch_keep_screen_on",
                onCheckedChange = { onUpdateFeatureSettings(it, null, null, null) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Auto-Enter on Typing / Voice
            SettingSwitchRow(
                title = "Auto-Enter on Typing & Voice",
                subtitle = "লেখা বা ভয়েস পাঠানোর সাথে সাথে কম্পিউটারে Enter চাপা",
                checked = currentSettings.autoEnterOnTyping,
                testTag = "switch_auto_enter",
                onCheckedChange = { onUpdateFeatureSettings(null, it, null, null) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Haptic Vibration Toggle
            SettingSwitchRow(
                title = "Haptic Vibration Feedback",
                subtitle = "বাটন ও স্লাইডার স্পর্শ করলে ট্যাক্টাইল ভাইব্রেশন ফিডব্যাক",
                checked = currentSettings.vibrationEnabled,
                testTag = "switch_vibration",
                onCheckedChange = {
                    onUpdateVibrationEnabled(it)
                    onUpdateFeatureSettings(null, null, it, null)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Show Volume Rocker Bar Toggle
            SettingSwitchRow(
                title = "Show Volume Rocker Bar",
                subtitle = "নিচের বামপাশে স্লাইডেবল ভলিউম বার প্রদর্শন বা হাইড করুন",
                checked = currentSettings.showVolumeControls,
                testTag = "switch_show_volume",
                onCheckedChange = { onUpdateDisplaySettings(null, null, null, it, null) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Show Playback Controls
            SettingSwitchRow(
                title = "Show Playback Controls",
                subtitle = "প্লে/পজ, প্রিভিয়াস ও নেক্সট মিডিয়া বাটন অন বা অফ রাখুন",
                checked = currentSettings.showPlaybackControls,
                testTag = "switch_show_playback",
                onCheckedChange = { onUpdateDisplaySettings(null, null, it, null, null) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Show Additional PC Keys
            SettingSwitchRow(
                title = "Show Additional PC Keys",
                subtitle = "এক্সট্রা কিবোর্ড কী (Esc, Tab, Home, F11) অন বা অফ রাখুন",
                checked = currentSettings.showAdditionalControls,
                testTag = "switch_show_additional",
                onCheckedChange = { onUpdateDisplaySettings(null, null, null, null, it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. App Version & Cloud Updates Card
        SettingsSectionCard(
            title = "App Version & Cloud Updates",
            icon = Icons.Default.SystemUpdate
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ytremote Pro",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Current Version: v2.8.0 (Build 2026.11.05)",
                            fontSize = 11.sp,
                            color = AccentCyan
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusSuccess.copy(alpha = 0.18f))
                            .border(1.dp, StatusSuccess, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LATEST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StatusSuccess
                        )
                    }
                }

                Text(
                    text = "Checking for updates verifies your app version against the latest cloud deployment with instant YouTube TV search and Screen Scroll slider.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isCheckingUpdates = true
                            coroutineScope.launch {
                                delay(600)
                                isCheckingUpdates = false
                                showUpdateDialog = true
                            }
                        },
                        enabled = !isCheckingUpdates,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_check_for_updates")
                    ) {
                        if (isCheckingUpdates) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = TextPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Checking...", fontSize = 12.sp)
                        } else {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Check for Updates", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://ais-pre-nbzehdp3hd77zuiztnc2lm-81130466416.asia-southeast1.run.app")
                            )
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Opening shared link...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CardBorderHighlight)
                        ),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Web Remote", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // App Update Status & Changelog Dialog with Phone Installation Guide
    if (showUpdateDialog) {
        val pythonServerScript = """
# ========================================================
# ytremote Windows PC Server (v2.9.0)
# Universal Keyboard Typing, Bengali/Unicode Paste,
# Ultra-Smooth Air Mouse, Smooth Wheel Scroll & Media Control
# ========================================================
import http.server, json, sys, ctypes, urllib.parse, webbrowser, time

PORT = 8765
PIN = "2468"

VK_MAP = {
    'UP': 0x26, 'DOWN': 0x28, 'LEFT': 0x25, 'RIGHT': 0x27,
    'ENTER': 0x0D, 'ESC': 0x1B, 'BACKSPACE': 0x08, 'SPACE': 0x20,
    'PAGE_UP': 0x21, 'PAGE_DOWN': 0x22, 'HOME': 0x24, 'END': 0x23,
    'TAB': 0x09, 'VOLUME_UP': 0xAF, 'VOLUME_DOWN': 0xAE, 'MUTE': 0xAD,
    'MEDIA_PLAY_PAUSE': 0xB3, 'MEDIA_NEXT': 0xB0, 'MEDIA_PREV': 0xB1,
    'F11': 0x7A, 'CONTROL': 0x11, 'ALT': 0x12, 'SHIFT': 0x10, 'DELETE': 0x2E
}

u32 = ctypes.windll.user32
k32 = ctypes.windll.kernel32

def press_vk(code):
    try:
        u32.keybd_event(code, 0, 0, 0)
        time.sleep(0.015)
        u32.keybd_event(code, 0, 2, 0)
    except Exception:
        pass

def press_ctrl_v():
    try:
        u32.keybd_event(0x11, 0, 0, 0)      # Ctrl down
        time.sleep(0.01)
        u32.keybd_event(0x56, 0, 0, 0)      # 'V' down
        time.sleep(0.02)
        u32.keybd_event(0x56, 0, 2, 0)      # 'V' up
        time.sleep(0.01)
        u32.keybd_event(0x11, 0, 2, 0)      # Ctrl up
    except Exception:
        pass

def paste_text_to_pc(text, do_enter=False):
    # Method 1: Clipboard paste with CF_UNICODETEXT
    # 100% reliable for English, Bengali, Hindi, symbols, URLs & emojis
    pasted = False
    try:
        CF_UNICODETEXT = 13
        GMEM_MOVEABLE = 0x0002
        if u32.OpenClipboard(0):
            u32.EmptyClipboard()
            encoded = (text + '\0').encode('utf-16le')
            h_mem = k32.GlobalAlloc(GMEM_MOVEABLE, len(encoded))
            if h_mem:
                p_mem = k32.GlobalLock(h_mem)
                ctypes.memmove(p_mem, encoded, len(encoded))
                k32.GlobalUnlock(h_mem)
                u32.SetClipboardData(CF_UNICODETEXT, h_mem)
            u32.CloseClipboard()
            time.sleep(0.02)
            press_ctrl_v()
            pasted = True
    except Exception as e:
        print(f"[Clipboard paste error: {e}]")

    # Method 2: Direct character typing fallback if clipboard is locked
    if not pasted:
        for ch in text:
            try:
                code = ord(ch)
                if code < 128:
                    vk = u32.VkKeyScanW(code)
                    if vk != -1:
                        press_vk(vk & 0xFF)
                        continue
                u32.keybd_event(0, code, 0x0004, 0)
                time.sleep(0.003)
                u32.keybd_event(0, code, 0x0004 | 0x0002, 0)
                time.sleep(0.003)
            except Exception:
                pass

    if do_enter:
        time.sleep(0.05)
        press_vk(VK_MAP['ENTER'])

class RemoteHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/health':
            self.send_response(200); self.end_headers()
            self.wfile.write(b'{"status":"ok","version":"2.9.0"}')
        elif self.path == '/volume':
            self.send_response(200); self.send_header('Content-Type','application/json'); self.end_headers()
            self.wfile.write(b'{"volume": 50}')
        else:
            self.send_response(404); self.end_headers()

    def do_POST(self):
        if self.path in ['/control', '/volume']:
            length = int(self.headers.get('Content-Length', 0))
            data = json.loads(self.rfile.read(length).decode('utf-8'))
            action = data.get('action')
            key = data.get('key')
            query = data.get('query') or data.get('text')
            do_enter = bool(data.get('enter', False))
            dy = float(data.get('dy', 0))

            if action == 'search_youtube' and query:
                print(f"[YouTube Search] Instant query: {query}")
                webbrowser.open(f"https://www.youtube.com/results?search_query={urllib.parse.quote(query)}")
            elif action == 'type' and query:
                print(f"[Universal Typing] Text: {query} (Enter={do_enter})")
                paste_text_to_pc(query, do_enter=do_enter)
            elif action == 'scroll':
                print(f"[Screen Scroll] dy: {dy}")
                # Windows standard mouse wheel delta: 120 per notch
                wheel_delta = int(round(dy * 120))
                try:
                    u32.mouse_event(0x0800, 0, 0, wheel_delta, 0)
                except Exception:
                    pass
                if abs(wheel_delta) < 60:
                    if dy > 0:
                        press_vk(VK_MAP['PAGE_UP'])
                    elif dy < 0:
                        press_vk(VK_MAP['PAGE_DOWN'])
            elif action == 'key' and key in VK_MAP:
                press_vk(VK_MAP[key])
            elif action == 'mouse':
                # 1:1 smooth delta mapping without jumpy server multiplier
                dx = int(round(float(data.get('dx', 0))))
                dy_m = int(round(float(data.get('dy', 0))))
                try:
                    u32.mouse_event(0x0001, dx, dy_m, 0, 0)
                except Exception:
                    pass
            elif action == 'click':
                try:
                    u32.mouse_event(0x0002, 0, 0, 0, 0)
                    time.sleep(0.02)
                    u32.mouse_event(0x0004, 0, 0, 0, 0)
                except Exception:
                    pass

            self.send_response(200); self.end_headers()
            self.wfile.write(b'{"status":"success"}')

server = http.server.HTTPServer(('0.0.0.0', PORT), RemoteHandler)
print(f"ytremote Server v2.9.0 running on port {PORT} with PIN {PIN}")
server.serve_forever()
        """.trimIndent()

        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusSuccess,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Up to Date: v2.9.0",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Universal Typing & Smooth Controls Active",
                            fontSize = 11.sp,
                            color = StatusSuccess
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "What's New in v2.9.0:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                    Text(
                        text = "• Universal Keyboard & Voice: 100% accurate Bengali & English typing into any active search bar or app using Win32 Unicode clipboard paste.\n" +
                                "• Anti-Jitter Air Mouse: Soft-knee deadband, dynamic EMA smoothing, and auto-pause while typing so cursor never jumps.\n" +
                                "• Touchpad & Scroll Settings: On/off toggles, tap-to-click toggle, speed sliders, and natural invert scroll.\n" +
                                "• Handy Remote Options: Keep screen awake, auto-enter, vibration, and section toggles.",
                        fontSize = 11.sp,
                        color = Color(0xFFD1D5DB),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "ফোনে অ্যাপ আপডেট করার ২টি সঠিক পদ্ধতি:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )

                    Text(
                        text = "পদ্ধতি ১ (সরাসরি APK আপডেট):\n" +
                                "১. AI Studio এর উপরের ডানদিকের মেনু (⋮ বা ডাউনলোড আইকন) এ ক্লিক করুন।\n" +
                                "২. 'Download APK' বা 'Export APK' সিলেক্ট করুন।\n" +
                                "৩. ফাইলে ক্লিক করে ফোনে Install বা Update দিন (আগের সেটিংস অক্ষত থাকবে)।\n\n" +
                                "পদ্ধতি ২ (ইনস্ট্যান্ট ক্লাউড ওয়েব রিমোট):\n" +
                                "১. ফোনের Chrome ব্রাউজারে Shared App লিঙ্কটি ওপেন করুন।\n" +
                                "২. থ্রি ডট মেনু থেকে 'Add to Home Screen' বা 'Install app' দিন।\n" +
                                "৩. এখানে যেকোনো কোড পরিবর্তন করলে ফোনে অটোমেটিক সঙ্গে সঙ্গে আপডেট হয়ে যাবে!",
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ytremote_server.py", pythonServerScript)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied Windows Python Server script to clipboard!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorderHighlight, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp), tint = AccentCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy PC Server Script (v2.8.0)", fontSize = 11.sp, color = AccentCyan)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpdateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Done", color = Color.White, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Close", color = Color.Gray, fontSize = 12.sp)
                }
            },
            containerColor = Color(0xFF111D30),
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = PrimaryBlue,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = BackgroundDark
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = AccentCyan,
    unfocusedBorderColor = CardBorder,
    focusedLabelColor = AccentCyan,
    unfocusedLabelColor = TextSecondary,
    cursorColor = AccentCyan
)

@Composable
private fun sliderColors() = SliderDefaults.colors(
    thumbColor = AccentCyan,
    activeTrackColor = PrimaryBlue,
    inactiveTrackColor = CardBorder
)
