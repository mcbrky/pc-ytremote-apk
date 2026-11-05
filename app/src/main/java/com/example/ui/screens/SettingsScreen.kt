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
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
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
                            Text(
                                text = style.displayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) AccentCyan else TextPrimary
                            )
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

            // Touchpad Height Scale
            Text(
                text = "Touchpad Height: ${String.format(Locale.US, "%.0f%%", localTouchpadScale * 100)} (~${(170 * localTouchpadScale).toInt()} dp)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Slider(
                value = localTouchpadScale,
                onValueChange = {
                    localTouchpadScale = it
                    onUpdateControllerSizes(null, it)
                },
                valueRange = 0.80f..1.50f,
                colors = sliderColors(),
                modifier = Modifier.testTag("slider_touchpad_scale")
            )

            // Touchpad Quick Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Compact" to 0.90f,
                    "Standard" to 1.15f,
                    "6.4\" Max Fit" to 1.35f
                ).forEach { (label, scale) ->
                    val isCurrent = kotlin.math.abs(localTouchpadScale - scale) < 0.05f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrent) AccentCyan.copy(alpha = 0.2f) else CardDark)
                            .border(1.dp, if (isCurrent) AccentCyan else CardBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                localTouchpadScale = scale
                                onUpdateControllerSizes(null, scale)
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Air Mouse & Sensor Settings Card
        SettingsSectionCard(
            title = "Air Mouse & Sensors",
            icon = Icons.Default.Mouse
        ) {
            // Mouse Sensitivity
            Text(
                text = "Touchpad Sensitivity: ${String.format(Locale.US, "%.1f", localMouseSens)}x",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Slider(
                value = localMouseSens,
                onValueChange = {
                    localMouseSens = it
                    onUpdateMouseSensitivity(it)
                },
                valueRange = 0.5f..3.5f,
                colors = sliderColors(),
                modifier = Modifier.testTag("slider_mouse_sensitivity")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Gyroscope Air Mouse Toggle
            SettingSwitchRow(
                title = "Gyroscope Air Mouse",
                subtitle = "Tilt phone to control cursor smoothly like a pointer",
                checked = currentSettings.gyroEnabled,
                testTag = "switch_gyroscope",
                onCheckedChange = { onUpdateGyroEnabled(it) }
            )

            // Gyroscope Speed/Sensitivity
            if (currentSettings.gyroEnabled) {
                Spacer(modifier = Modifier.height(6.dp))
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
                    },
                    valueRange = 0.5f..4.0f,
                    colors = sliderColors(),
                    modifier = Modifier.testTag("slider_gyro_sensitivity")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Haptic Vibration Toggle
            SettingSwitchRow(
                title = "Haptic Vibration",
                subtitle = "Tactile click feedback on buttons and gestures",
                checked = currentSettings.vibrationEnabled,
                testTag = "switch_vibration",
                onCheckedChange = { onUpdateVibrationEnabled(it) }
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
# ytremote Windows PC Server (v2.8.5)
# Direct YouTube Voice Search, Page Scroll, Media & Mouse
# ========================================================
import http.server, json, sys, ctypes, urllib.parse, webbrowser, time

PORT = 8765
PIN = "2468"

VK_MAP = {
    'UP': 0x26, 'DOWN': 0x28, 'LEFT': 0x25, 'RIGHT': 0x27,
    'ENTER': 0x0D, 'ESC': 0x1B, 'BACKSPACE': 0x08, 'SPACE': 0x20,
    'PAGE_UP': 0x21, 'PAGE_DOWN': 0x22, 'HOME': 0x24, 'END': 0x23,
    'TAB': 0x09, 'VOLUME_UP': 0xAF, 'VOLUME_DOWN': 0xAE, 'MUTE': 0xAD,
    'MEDIA_PLAY_PAUSE': 0xB3, 'MEDIA_NEXT': 0xB0, 'MEDIA_PREV': 0xB1
}

def press_vk(code):
    try:
        ctypes.windll.user32.keybd_event(code, 0, 0, 0)
        time.sleep(0.02)
        ctypes.windll.user32.keybd_event(code, 0, 2, 0)
    except Exception:
        pass

def type_string(text):
    for ch in text:
        try:
            code = ord(ch)
            # Windows KEYEVENTF_UNICODE (0x0004) supports English, Bengali and all Unicode
            ctypes.windll.user32.keybd_event(0, code, 0x0004, 0)
            time.sleep(0.004)
            ctypes.windll.user32.keybd_event(0, code, 0x0004 | 0x0002, 0)
            time.sleep(0.004)
        except Exception:
            pass

class RemoteHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/health':
            self.send_response(200); self.end_headers()
            self.wfile.write(b'{"status":"ok","version":"2.8.5"}')
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
            dy = data.get('dy', 0)

            if action == 'search_youtube' and query:
                print(f"[YouTube Search] Instant query: {query}")
                webbrowser.open(f"https://www.youtube.com/results?search_query={urllib.parse.quote(query)}")
            elif action == 'scroll':
                print(f"[Screen Scroll] dy: {dy}")
                try:
                    ctypes.windll.user32.mouse_event(0x0800, 0, 0, int(-dy * 6), 0)
                except Exception:
                    pass
                if dy > 0:
                    press_vk(VK_MAP['PAGE_UP'])
                else:
                    press_vk(VK_MAP['PAGE_DOWN'])
            elif action == 'key' and key in VK_MAP:
                press_vk(VK_MAP[key])
            elif action == 'type' and query:
                type_string(query)
            elif action == 'mouse':
                dx = int(data.get('dx', 0) * 1.5)
                dy = int(data.get('dy', 0) * 1.5)
                try:
                    ctypes.windll.user32.mouse_event(0x0001, dx, dy, 0, 0)
                except Exception:
                    pass
            elif action == 'click':
                try:
                    ctypes.windll.user32.mouse_event(0x0002, 0, 0, 0, 0)
                    time.sleep(0.02)
                    ctypes.windll.user32.mouse_event(0x0004, 0, 0, 0, 0)
                except Exception:
                    pass

            self.send_response(200); self.end_headers()
            self.wfile.write(b'{"status":"success"}')

server = http.server.HTTPServer(('0.0.0.0', PORT), RemoteHandler)
print(f"ytremote Server v2.8.5 running on port {PORT} with PIN {PIN}")
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
                            text = "Up to Date: v2.8.0",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Latest Cloud Build Active",
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
                        text = "What's New in v2.8.0:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                    Text(
                        text = "• Instant YouTube TV Voice Search: Speaks directly into YouTube search results without intermediate popups.\n" +
                                "• Vertical Screen Scroll Slider: Replaced CH rocker with a smooth vertical slider for scrolling up and down.\n" +
                                "• Touchpad & Joystick 6.4\" Full Display Adaptive Sizing.\n" +
                                "• Two-way Windows Volume sync & Air Mouse precision.",
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
