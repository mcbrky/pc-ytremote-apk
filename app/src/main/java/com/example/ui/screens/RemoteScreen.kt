package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.model.RemoteKeyNames
import com.example.model.RemoteSettings
import com.example.ui.components.DPadController
import com.example.ui.components.ExtendedControlsView
import com.example.ui.components.JoystickController
import com.example.ui.components.RemoteBottomControls
import com.example.ui.components.SearchInputSheet
import com.example.ui.components.TouchpadView
import com.example.ui.theme.RemoteDesignStyle
import com.example.viewmodel.RemoteViewModel
import com.example.voice.VoiceState

@Composable
fun RemoteScreen(
    viewModel: RemoteViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isSearchModalOpen by viewModel.isSearchModalOpen.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val currentVolume by viewModel.currentVolume.collectAsState()
    val statusMessage by viewModel.lastStatusMessage.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val liveVoiceText by viewModel.liveVoiceText.collectAsState()

    val isVoiceListening = voiceState is VoiceState.Listening
    val style = settings.remoteDesignStyle

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startDirectVoiceSearch()
        } else {
            Toast.makeText(context, "Microphone permission required for voice search", Toast.LENGTH_SHORT).show()
        }
    }

    val handleVoiceClick = {
        if (isVoiceListening) {
            viewModel.stopVoiceSearch()
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                viewModel.startDirectVoiceSearch()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Keep phone screen awake while using remote if option is enabled
    DisposableEffect(settings.keepScreenOn) {
        val window = (context as? android.app.Activity)?.window
        if (settings.keepScreenOn) {
            window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(style.chassisBottom)
            .safeDrawingPadding()
    ) {
        if (isSettingsOpen) {
            SettingsScreen(
                currentSettings = settings,
                connectionStatus = connectionStatus,
                statusMessage = statusMessage,
                onSaveConnection = { ip, port, pin ->
                    viewModel.updateConnectionSettings(ip, port, pin)
                },
                onReconnect = { viewModel.reconnect() },
                onUpdateMouseSensitivity = { viewModel.updateMouseSensitivity(it) },
                onUpdateJoystickSensitivity = { viewModel.updateJoystickSensitivity(it) },
                onUpdateGyroEnabled = { viewModel.updateGyroEnabled(it) },
                onUpdateGyroSensitivity = { viewModel.updateGyroSensitivity(it) },
                onUpdateVibrationEnabled = { viewModel.updateVibrationEnabled(it) },
                onSelectDesignStyle = { viewModel.updateDesignStyle(it) },
                onUpdateControllerSizes = { dpad, touchpad ->
                    viewModel.updateControllerSizes(dpad, touchpad)
                },
                onToggleUseJoystick = {
                    viewModel.updateUseJoystick(it)
                },
                onUpdateTouchpadSettings = { show, tap, sens, height ->
                    viewModel.updateTouchpadSettings(show, tap, sens, height)
                },
                onToggleSwapDpadAndTouchpad = {
                    viewModel.updateSwapDpadAndTouchpad(it)
                },
                onUpdateScrollSettings = { show, sens, invert ->
                    viewModel.updateScrollSettings(show, sens, invert)
                },
                onUpdateAirMouseSettings = { enabled, sens, smooth, pause, quick ->
                    viewModel.updateAirMouseSettings(enabled, sens, smooth, pause, quick)
                },
                onUpdateFeatureSettings = { awake, enter, vib, joy ->
                    viewModel.updateFeatureSettings(awake, enter, vib, joy)
                },
                onUpdateDisplaySettings = { upNext, voice, play, vol, add ->
                    viewModel.updateDisplaySettings(upNext, voice, play, vol, add)
                },
                onBack = { viewModel.closeSettings() }
            )
        } else {
            // Main Physical Remote Body Container matching user design and active theme
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                style.chassisTop,
                                style.chassisCenter,
                                style.chassisBottom
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        color = style.chassisBorder,
                        shape = RoundedCornerShape(28.dp)
                    )
            ) {
                if (isLandscape) {
                    LandscapeRemoteLayout(
                        viewModel = viewModel,
                        settings = settings,
                        style = style,
                        connectionStatus = connectionStatus,
                        currentVolume = currentVolume,
                        currentPage = currentPage,
                        isVoiceListening = isVoiceListening,
                        liveVoiceText = liveVoiceText,
                        onOpenSettings = { viewModel.openSettings() },
                        onVoiceClick = handleVoiceClick
                    )
                } else {
                    PortraitRemoteLayout(
                        viewModel = viewModel,
                        settings = settings,
                        style = style,
                        connectionStatus = connectionStatus,
                        currentVolume = currentVolume,
                        currentPage = currentPage,
                        isVoiceListening = isVoiceListening,
                        liveVoiceText = liveVoiceText,
                        onOpenSettings = { viewModel.openSettings() },
                        onVoiceClick = handleVoiceClick
                    )
                }
            }
        }

        // Universal Keyboard Sheet (Opened via Keyboard button)
        if (isSearchModalOpen) {
            SearchInputSheet(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onSendQuery = { viewModel.sendUniversalText(it, pressEnter = true) },
                onSendQueryOnly = { viewModel.sendUniversalText(it, pressEnter = false) },
                onKeyClick = { viewModel.onKeyClicked(it) },
                voiceManager = viewModel.voiceManager,
                onDismiss = { viewModel.closeSearchModal() }
            )
        }
    }
}

@Composable
private fun PortraitRemoteLayout(
    viewModel: RemoteViewModel,
    settings: RemoteSettings,
    style: RemoteDesignStyle,
    connectionStatus: ConnectionStatus,
    currentVolume: Int,
    currentPage: Int,
    isVoiceListening: Boolean,
    liveVoiceText: String?,
    onOpenSettings: () -> Unit,
    onVoiceClick: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth

        // Adaptive calculations optimized for 6.4" phone (typically ~390-412dp wide, ~840-920dp tall)
        val calculatedDpadSize = (screenWidth * 0.74f * settings.dpadScale).coerceIn(245.dp, 305.dp)
        // With Now Playing bar removed, Touchpad takes comfortable top area
        val calculatedTouchpadHeight = (screenHeight * 0.25f * settings.touchpadHeightScale).coerceIn(180.dp, 260.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("remote_portrait_layout"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Touchpad Composable lambda
            val touchpadComposable = @Composable {
                if (settings.showTouchpad) {
                    TouchpadView(
                        onDrag = { dx, dy -> viewModel.onTouchpadDrag(dx, dy) },
                        onClick = { viewModel.onTouchpadClick() },
                        tapToClickEnabled = settings.touchpadTapToClick,
                        style = style,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(calculatedTouchpadHeight)
                    )
                }
            }

            // 2. Middle Pill-Shaped Action Island Console Bar (ডি-প্যাড ও টাচপ্যাডের মাঝের পিল শেপ কনসোল)
            val middleIslandBarComposable = @Composable {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                        .shadow(
                            elevation = 5.dp,
                            shape = RoundedCornerShape(26.dp),
                            spotColor = Color.Black.copy(alpha = 0.5f)
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    style.buttonBgTop.copy(alpha = 0.95f),
                                    style.buttonBgBottom.copy(alpha = 0.98f)
                                )
                            )
                        )
                        .border(
                            width = 1.2.dp,
                            color = style.buttonBorder,
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Connection Status Pill Badge
                        ConnectionLedIndicator(
                            status = connectionStatus,
                            style = style,
                            onReconnect = { viewModel.reconnect() }
                        )

                        // Right: Dedicated Sub-Pill Segment for Quick Actions
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(style.chassisTop.copy(alpha = 0.45f))
                                .border(
                                    0.8.dp,
                                    style.buttonBorder.copy(alpha = 0.6f),
                                    RoundedCornerShape(18.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quick 1-tap Air Mouse toggle
                            if (settings.showQuickAirMouseToggle) {
                                IconButton(
                                    onClick = { viewModel.toggleAirMouse() },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (settings.gyroEnabled) style.accentPrimary.copy(alpha = 0.28f)
                                            else style.buttonBgTop.copy(alpha = 0.8f)
                                        )
                                        .border(
                                            1.dp,
                                            if (settings.gyroEnabled) style.accentPrimary else style.buttonBorder,
                                            CircleShape
                                        )
                                        .testTag("btn_air_mouse_quick")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NearMe,
                                        contentDescription = if (settings.gyroEnabled) "Air Mouse Active" else "Air Mouse Off",
                                        tint = if (settings.gyroEnabled) style.accentPrimary else if (style.isLight) Color(0xFF0F172A) else Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            // Quick Swap D-Pad & Touchpad
                            IconButton(
                                onClick = { viewModel.toggleSwapDpadAndTouchpad() },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (settings.swapDpadAndTouchpad) style.accentPrimary.copy(alpha = 0.28f)
                                        else style.buttonBgTop.copy(alpha = 0.8f)
                                    )
                                    .border(
                                        1.dp,
                                        if (settings.swapDpadAndTouchpad) style.accentPrimary else style.buttonBorder,
                                        CircleShape
                                    )
                                    .testTag("btn_swap_layout_quick")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = if (settings.swapDpadAndTouchpad) "D-Pad Top Active" else "Touchpad Top Active",
                                    tint = if (settings.swapDpadAndTouchpad) style.accentPrimary else if (style.isLight) Color(0xFF0F172A) else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Quick Joystick vs D-Pad Mode Toggle
                            IconButton(
                                onClick = { viewModel.updateUseJoystick(!settings.useJoystick) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(style.buttonBgTop.copy(alpha = 0.8f))
                                    .border(1.dp, style.buttonBorder, CircleShape)
                                    .testTag("btn_toggle_joystick")
                            ) {
                                Icon(
                                    imageVector = if (settings.useJoystick) Icons.Default.Gamepad else Icons.Default.ControlCamera,
                                    contentDescription = if (settings.useJoystick) "Switch to D-Pad" else "Switch to Joystick",
                                    tint = style.accentPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Settings Gear
                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(style.buttonBgTop.copy(alpha = 0.8f))
                                    .border(1.dp, style.buttonBorder, CircleShape)
                                    .testTag("btn_settings")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = if (style.isLight) Color(0xFF0F172A) else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Controller Body Composable lambda
            val controllerBodyComposable = @Composable {
                if (currentPage == 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Center: Joystick Thumbstick or D-Pad based on user preference
                        if (settings.useJoystick) {
                            JoystickController(
                                onKeyClick = { viewModel.onKeyClicked(it) },
                                size = calculatedDpadSize,
                                sensitivity = settings.joystickSensitivity,
                                style = style,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        } else {
                            DPadController(
                                onKeyClick = { viewModel.onKeyClicked(it) },
                                size = calculatedDpadSize,
                                style = style,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        // Page Indicator Dots under Controller: (•) ( )
                        PageIndicatorDots(
                            currentPage = currentPage,
                            onToggle = { viewModel.togglePage() },
                            accentColor = style.accentPrimary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }
                } else {
                    // Extended Controls (Media Bar + 0-9 Numpad)
                    ExtendedControlsView(
                        onKeyClick = { viewModel.onKeyClicked(it) },
                        onBackToMain = { viewModel.setCurrentPage(0) }
                    )
                }
            }

            // Render in order: Pill Island sits right in the middle between Touchpad and D-Pad!
            if (settings.swapDpadAndTouchpad) {
                // Interchanged: D-Pad on top, Pill Bar in middle, Touchpad on bottom!
                controllerBodyComposable()
                middleIslandBarComposable()
                touchpadComposable()
            } else {
                // Default: Touchpad on top, Pill Bar in middle, D-Pad on bottom!
                touchpadComposable()
                middleIslandBarComposable()
                controllerBodyComposable()
            }

            // Inline Built-in Voice Typing Feedback Banner (Discreet, no full-screen popups)
            AnimatedVisibility(
                visible = liveVoiceText != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(style.buttonBgTop)
                        .border(
                            width = 1.2.dp,
                            color = if (isVoiceListening) Color(0xFFFF3333) else style.accentPrimary,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isVoiceListening) Icons.Default.Mic else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isVoiceListening) Color(0xFFFF3333) else style.accentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = liveVoiceText ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }

            // 3. BOTTOM SECTION: Slideable VOL Bar, Middle Buttons (including Voice Typing Mic), Scroll Slider
            RemoteBottomControls(
                currentVolume = currentVolume,
                onVolumeUp = { viewModel.onVolumeUpClicked() },
                onVolumeDown = { viewModel.onVolumeDownClicked() },
                onVolumeSlide = { viewModel.onVolumeSlide(it) },
                onMute = { viewModel.onKeyClicked(RemoteKeyNames.MUTE) },
                onKeyboardClick = { viewModel.openSearchModal() },
                onBackClick = { viewModel.onKeyClicked(RemoteKeyNames.ESC) },
                onNumpadClick = { viewModel.togglePage() },
                onHomeClick = { viewModel.onKeyClicked(RemoteKeyNames.HOME) },
                onChannelUp = { viewModel.onChannelUpClicked() },
                onChannelDown = { viewModel.onChannelDownClicked() },
                onScrollDrag = { viewModel.onScrollDrag(it) },
                onScrollUp = { viewModel.onScrollUp() },
                onScrollDown = { viewModel.onScrollDown() },
                isVoiceListening = isVoiceListening,
                onVoiceClick = onVoiceClick,
                showScrollBar = settings.showScrollBar,
                showVolumeControls = settings.showVolumeControls,
                style = style,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun LandscapeRemoteLayout(
    viewModel: RemoteViewModel,
    settings: RemoteSettings,
    style: RemoteDesignStyle,
    connectionStatus: ConnectionStatus,
    currentVolume: Int,
    currentPage: Int,
    isVoiceListening: Boolean,
    liveVoiceText: String?,
    onOpenSettings: () -> Unit,
    onVoiceClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .testTag("remote_landscape_layout"),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Touch Pad + LED + Settings + Page Dots
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                style.buttonBgTop.copy(alpha = 0.95f),
                                style.buttonBgBottom.copy(alpha = 0.98f)
                            )
                        )
                    )
                    .border(1.2.dp, style.buttonBorder, RoundedCornerShape(22.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ConnectionLedIndicator(
                        status = connectionStatus,
                        style = style,
                        onReconnect = { viewModel.reconnect() }
                    )

                    PageIndicatorDots(
                        currentPage = currentPage,
                        onToggle = { viewModel.togglePage() },
                        accentColor = style.accentPrimary
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(style.chassisTop.copy(alpha = 0.45f))
                            .border(0.8.dp, style.buttonBorder.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (settings.showQuickAirMouseToggle) {
                            IconButton(
                                onClick = { viewModel.toggleAirMouse() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (settings.gyroEnabled) style.accentPrimary.copy(alpha = 0.25f)
                                        else style.buttonBgTop.copy(alpha = 0.7f)
                                    )
                                    .border(
                                        1.dp,
                                        if (settings.gyroEnabled) style.accentPrimary else style.buttonBorder,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NearMe,
                                    contentDescription = if (settings.gyroEnabled) "Air Mouse Active" else "Air Mouse Off",
                                    tint = if (settings.gyroEnabled) style.accentPrimary else if (style.isLight) Color(0xFF0F172A) else Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.toggleSwapDpadAndTouchpad() },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (settings.swapDpadAndTouchpad) style.accentPrimary.copy(alpha = 0.25f)
                                    else style.buttonBgTop.copy(alpha = 0.7f)
                                )
                                .border(
                                    1.dp,
                                    if (settings.swapDpadAndTouchpad) style.accentPrimary else style.buttonBorder,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap D-Pad and Touchpad",
                                tint = if (settings.swapDpadAndTouchpad) style.accentPrimary else if (style.isLight) Color(0xFF0F172A) else Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.updateUseJoystick(!settings.useJoystick) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (settings.useJoystick) Icons.Default.Gamepad else Icons.Default.ControlCamera,
                                contentDescription = "Toggle Joystick",
                                tint = style.accentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (style.isLight) Color(0xFF0F172A) else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (settings.showTouchpad) {
                TouchpadView(
                    onDrag = { dx, dy -> viewModel.onTouchpadDrag(dx, dy) },
                    onClick = { viewModel.onTouchpadClick() },
                    tapToClickEnabled = settings.touchpadTapToClick,
                    style = style,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp)
                )
            }

            // Inline Voice Banner in landscape
            if (liveVoiceText != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(style.buttonBgTop)
                        .border(
                            1.dp,
                            if (isVoiceListening) Color(0xFFFF3333) else style.accentPrimary,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = liveVoiceText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }

        // Right Column: Joystick / D-Pad / Extended Controls + Bottom Controls
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentPage == 0) {
                if (settings.useJoystick) {
                    JoystickController(
                        onKeyClick = { viewModel.onKeyClicked(it) },
                        size = 195.dp,
                        sensitivity = settings.joystickSensitivity,
                        style = style,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                } else {
                    DPadController(
                        onKeyClick = { viewModel.onKeyClicked(it) },
                        size = 195.dp,
                        style = style,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            } else {
                ExtendedControlsView(
                    onKeyClick = { viewModel.onKeyClicked(it) },
                    onBackToMain = { viewModel.setCurrentPage(0) },
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            RemoteBottomControls(
                currentVolume = currentVolume,
                onVolumeUp = { viewModel.onVolumeUpClicked() },
                onVolumeDown = { viewModel.onVolumeDownClicked() },
                onVolumeSlide = { viewModel.onVolumeSlide(it) },
                onMute = { viewModel.onKeyClicked(RemoteKeyNames.MUTE) },
                onKeyboardClick = { viewModel.openSearchModal() },
                onBackClick = { viewModel.onKeyClicked(RemoteKeyNames.ESC) },
                onNumpadClick = { viewModel.togglePage() },
                onHomeClick = { viewModel.onKeyClicked(RemoteKeyNames.HOME) },
                onChannelUp = { viewModel.onChannelUpClicked() },
                onChannelDown = { viewModel.onChannelDownClicked() },
                onScrollDrag = { viewModel.onScrollDrag(it) },
                onScrollUp = { viewModel.onScrollUp() },
                onScrollDown = { viewModel.onScrollDown() },
                isVoiceListening = isVoiceListening,
                onVoiceClick = onVoiceClick,
                showScrollBar = settings.showScrollBar,
                showVolumeControls = settings.showVolumeControls,
                style = style
            )
        }
    }
}

/**
 * Connection LED Indicator with pulsing halo and explicit text label
 * displayed right after the green light as requested by the user.
 */
@Composable
private fun ConnectionLedIndicator(
    status: ConnectionStatus,
    style: RemoteDesignStyle,
    onReconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (ledColor, haloColor, statusText) = when (status) {
        ConnectionStatus.CONNECTED -> Triple(Color(0xFF4ADE80), Color(0x6622C55E), "CONNECTED")
        ConnectionStatus.CONNECTING -> Triple(Color(0xFFFBBF24), Color(0x66F59E0B), "CONNECTING")
        ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> Triple(Color(0xFFF87171), Color(0x66EF4444), "OFFLINE")
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(style.buttonBgTop.copy(alpha = 0.85f))
            .border(1.dp, style.buttonBorder, RoundedCornerShape(16.dp))
            .clickable { onReconnect() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("led_status_indicator"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // Glowing LED circle
        Box(
            modifier = Modifier.size(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(haloColor)
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .shadow(4.dp, CircleShape, spotColor = ledColor)
                    .clip(CircleShape)
                    .background(ledColor)
            )
        }

        // Text displayed right after the light
        Text(
            text = statusText,
            color = ledColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun PageIndicatorDots(
    currentPage: Int,
    onToggle: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggle() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot 1 (Page 0)
        Box(
            modifier = Modifier
                .size(if (currentPage == 0) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(if (currentPage == 0) accentColor else Color(0xFF5A5D64))
        )

        // Dot 2 (Page 1)
        Box(
            modifier = Modifier
                .size(if (currentPage == 1) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(if (currentPage == 1) accentColor else Color(0xFF5A5D64))
        )
    }
}
