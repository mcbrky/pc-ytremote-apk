package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SettingsRepository
import com.example.model.ConnectionStatus
import com.example.model.NextVideoInfo
import com.example.model.RemoteKeyNames
import com.example.model.RemoteSettings
import com.example.network.RemoteApiClient
import com.example.sensor.GyroscopeController
import com.example.voice.VoiceSearchManager
import com.example.voice.VoiceState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val apiClient = RemoteApiClient()
    val voiceManager = VoiceSearchManager(application)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    val settings: StateFlow<RemoteSettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = RemoteSettings()
        )

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _lastStatusMessage = MutableStateFlow<String?>(null)
    val lastStatusMessage: StateFlow<String?> = _lastStatusMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _currentVolume = MutableStateFlow(46)
    val currentVolume: StateFlow<Int> = _currentVolume.asStateFlow()

    private val _isSearchModalOpen = MutableStateFlow(false)
    val isSearchModalOpen: StateFlow<Boolean> = _isSearchModalOpen.asStateFlow()

    private val _currentPage = MutableStateFlow(0) // 0: User Design, 1: Extended Numpad/Playback
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _liveNowPlayingTitle = MutableStateFlow<String?>(null)
    val liveNowPlayingTitle: StateFlow<String?> = _liveNowPlayingTitle.asStateFlow()

    // Up Next Demo Preview
    private val _upNextVideo = MutableStateFlow(
        NextVideoInfo(
            title = "Cinematic 8K Nature • Ultra HD Relaxation",
            channelName = "World In Motion",
            duration = "24:18",
            isDemo = true
        )
    )
    val upNextVideo: StateFlow<NextVideoInfo> = _upNextVideo.asStateFlow()

    // Built-in voice typing state
    val voiceState: StateFlow<VoiceState> = voiceManager.voiceState
    private val _liveVoiceText = MutableStateFlow<String?>(null)
    val liveVoiceText: StateFlow<String?> = _liveVoiceText.asStateFlow()

    // Gyroscope controller
    private val gyroController = GyroscopeController(application) { dx, dy ->
        val currentSettings = settings.value
        if (currentSettings.gyroEnabled && _connectionStatus.value == ConnectionStatus.CONNECTED) {
            apiClient.enqueueMouseMove(
                ip = currentSettings.serverIp,
                port = currentSettings.serverPort,
                pin = currentSettings.pin,
                dx = dx,
                dy = dy
            )
        }
    }

    private var healthCheckJob: Job? = null
    private var consecutiveCommandFailures = 0

    init {
        // Observe gyro settings
        viewModelScope.launch {
            settings.collect { currentSettings ->
                if (currentSettings.gyroEnabled) {
                    gyroController.start(currentSettings.gyroSensitivity)
                } else {
                    gyroController.stop()
                }
            }
        }

        // Observe voice state for inline live speech feedback
        viewModelScope.launch {
            voiceState.collect { state ->
                when (state) {
                    is VoiceState.Listening -> {
                        _liveVoiceText.value = "🎙️ Listening... Speak now"
                    }
                    is VoiceState.PartialResult -> {
                        _liveVoiceText.value = "🎙️ ${state.text}"
                    }
                    is VoiceState.FinalResult -> {
                        _liveVoiceText.value = "✓ Typed: \"${state.text}\" ↵"
                        sendUniversalText(state.text, pressEnter = true)
                    }
                    is VoiceState.Error -> {
                        _liveVoiceText.value = state.message
                        viewModelScope.launch {
                            delay(3000)
                            _liveVoiceText.value = null
                        }
                    }
                    is VoiceState.Idle -> {
                        // Handled after result/error timeout
                    }
                }
            }
        }

        // Start periodic health monitor
        startHealthMonitor()
    }

    fun startDirectVoiceSearch() {
        triggerHapticFeedback()
        _liveVoiceText.value = "🎙️ Listening... Speak now"
        voiceManager.startListening { /* StateFlow handles FinalResult */ }
    }

    fun stopVoiceSearch() {
        triggerHapticFeedback()
        voiceManager.stopListening()
        _liveVoiceText.value = null
    }

    fun toggleBuiltInVoiceTyping() {
        if (voiceState.value is VoiceState.Listening) {
            stopVoiceSearch()
        } else {
            startDirectVoiceSearch()
        }
    }

    fun startHealthMonitor() {
        healthCheckJob?.cancel()
        healthCheckJob = viewModelScope.launch {
            while (isActive) {
                checkConnection()
                delay(6000)
            }
        }
    }

    fun checkConnection() {
        viewModelScope.launch {
            val currentSettings = settings.value
            val result = apiClient.checkHealth(currentSettings.serverIp, currentSettings.serverPort)
            if (result.isSuccess) {
                consecutiveCommandFailures = 0
                _connectionStatus.value = ConnectionStatus.CONNECTED
                _lastStatusMessage.value = "Connected to ${currentSettings.serverIp}"
                // Auto-fetch active YouTube / media title from PC
                try {
                    val mediaRes = apiClient.fetchNowPlaying(currentSettings.serverIp, currentSettings.serverPort)
                    if (mediaRes.isSuccess && !mediaRes.getOrNull().isNullOrBlank()) {
                        _liveNowPlayingTitle.value = mediaRes.getOrNull()
                    }
                } catch (e: Exception) {
                    // Ignore background title poll failures
                }
                // Auto-sync master volume level from Windows PC
                try {
                    val volRes = apiClient.fetchVolume(currentSettings.serverIp, currentSettings.serverPort)
                    val fetchedVol = volRes.getOrNull()
                    if (volRes.isSuccess && fetchedVol != null) {
                        _currentVolume.value = fetchedVol
                    }
                } catch (e: Exception) {
                    // Ignore background volume poll failures
                }
            } else {
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                _lastStatusMessage.value = result.exceptionOrNull()?.message ?: "Disconnected"
            }
        }
    }

    fun refreshNowPlaying() {
        triggerHapticFeedback()
        viewModelScope.launch {
            val currentSettings = settings.value
            val res = apiClient.fetchNowPlaying(currentSettings.serverIp, currentSettings.serverPort)
            val fetchedTitle = res.getOrNull()
            if (res.isSuccess && !fetchedTitle.isNullOrBlank()) {
                _liveNowPlayingTitle.value = fetchedTitle
                settingsRepository.updateNowPlayingText(fetchedTitle)
                _lastStatusMessage.value = "Synced: $fetchedTitle"
            } else {
                _lastStatusMessage.value = "Connected. Tip: Keep YouTube playing in PC browser"
            }

            // Also refresh volume
            val volRes = apiClient.fetchVolume(currentSettings.serverIp, currentSettings.serverPort)
            val fetchedVol = volRes.getOrNull()
            if (volRes.isSuccess && fetchedVol != null) {
                _currentVolume.value = fetchedVol
            }
        }
    }

    fun reconnect() {
        viewModelScope.launch {
            _connectionStatus.value = ConnectionStatus.CONNECTING
            _lastStatusMessage.value = "Connecting..."
            checkConnection()
        }
    }

    fun triggerHapticFeedback() {
        if (!settings.value.vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (e: Exception) {
            // Ignore vibration issues
        }
    }

    fun onKeyClicked(key: String) {
        triggerHapticFeedback()
        val currentSettings = settings.value
        viewModelScope.launch {
            val result = apiClient.sendKey(
                ip = currentSettings.serverIp,
                port = currentSettings.serverPort,
                pin = currentSettings.pin,
                key = key
            )
            if (result.isFailure) {
                consecutiveCommandFailures++
                if (consecutiveCommandFailures >= 2) {
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                    _lastStatusMessage.value = "Failed to send $key: ${result.exceptionOrNull()?.message}"
                } else {
                    checkConnection()
                }
            } else {
                consecutiveCommandFailures = 0
                _connectionStatus.value = ConnectionStatus.CONNECTED
            }
        }
    }

    fun onTouchpadDrag(rawDx: Float, rawDy: Float) {
        val currentSettings = settings.value
        val sensitivity = currentSettings.mouseSensitivity
        apiClient.enqueueMouseMove(
            ip = currentSettings.serverIp,
            port = currentSettings.serverPort,
            pin = currentSettings.pin,
            dx = rawDx * sensitivity,
            dy = rawDy * sensitivity
        )
    }

    fun onTouchpadClick() {
        triggerHapticFeedback()
        val currentSettings = settings.value
        viewModelScope.launch {
            val result = apiClient.sendClick(
                ip = currentSettings.serverIp,
                port = currentSettings.serverPort,
                pin = currentSettings.pin
            )
            if (result.isFailure) {
                consecutiveCommandFailures++
                if (consecutiveCommandFailures >= 2) {
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                } else {
                    checkConnection()
                }
            } else {
                consecutiveCommandFailures = 0
                _connectionStatus.value = ConnectionStatus.CONNECTED
            }
        }
    }

    fun onSearchQueryChanged(text: String) {
        _searchQuery.value = text
    }

    fun sendUniversalText(text: String, pressEnter: Boolean = false) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        triggerHapticFeedback()
        val currentSettings = settings.value
        _searchQuery.value = trimmed
        _liveVoiceText.value = if (pressEnter) "✓ Typed: \"$trimmed\" ↵" else "✓ Typed: \"$trimmed\""

        viewModelScope.launch {
            val result = if (pressEnter) {
                apiClient.sendTypeAndEnter(
                    ip = currentSettings.serverIp,
                    port = currentSettings.serverPort,
                    pin = currentSettings.pin,
                    text = trimmed
                )
            } else {
                apiClient.sendType(
                    ip = currentSettings.serverIp,
                    port = currentSettings.serverPort,
                    pin = currentSettings.pin,
                    text = trimmed
                )
            }
            if (result.isSuccess) {
                consecutiveCommandFailures = 0
                _connectionStatus.value = ConnectionStatus.CONNECTED
            } else {
                consecutiveCommandFailures++
                if (consecutiveCommandFailures >= 2) {
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                    _lastStatusMessage.value = "Failed: ${result.exceptionOrNull()?.message}"
                } else {
                    checkConnection()
                }
            }
            delay(2500)
            _liveVoiceText.value = null
        }
    }

    fun sendSearchText(text: String) {
        sendUniversalText(text, pressEnter = true)
    }

    fun sendInstantYouTubeSearch(query: String) {
        sendUniversalText(query, pressEnter = true)
    }

    fun onScrollDrag(deltaY: Float) {
        val currentSettings = settings.value
        viewModelScope.launch {
            apiClient.sendScroll(
                ip = currentSettings.serverIp,
                port = currentSettings.serverPort,
                pin = currentSettings.pin,
                dy = deltaY
            )
        }
    }

    fun onScrollUp() {
        triggerHapticFeedback()
        onScrollDrag(120f)
    }

    fun onScrollDown() {
        triggerHapticFeedback()
        onScrollDrag(-120f)
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun onVolumeUpClicked() {
        val next = (_currentVolume.value + 2).coerceAtMost(100)
        _currentVolume.value = next
        val currentSettings = settings.value
        viewModelScope.launch {
            apiClient.sendVolume(currentSettings.serverIp, currentSettings.serverPort, currentSettings.pin, next)
        }
        onKeyClicked(RemoteKeyNames.VOLUME_UP)
    }

    fun onVolumeDownClicked() {
        val next = (_currentVolume.value - 2).coerceAtLeast(0)
        _currentVolume.value = next
        val currentSettings = settings.value
        viewModelScope.launch {
            apiClient.sendVolume(currentSettings.serverIp, currentSettings.serverPort, currentSettings.pin, next)
        }
        onKeyClicked(RemoteKeyNames.VOLUME_DOWN)
    }

    fun onVolumeSlide(newVolume: Int) {
        val clamped = newVolume.coerceIn(0, 100)
        val old = _currentVolume.value
        if (clamped != old) {
            _currentVolume.value = clamped
            triggerHapticFeedback()
            val currentSettings = settings.value
            viewModelScope.launch {
                // 1. Try direct volume setter on PC
                val res = apiClient.sendVolume(
                    ip = currentSettings.serverIp,
                    port = currentSettings.serverPort,
                    pin = currentSettings.pin,
                    volumePercent = clamped
                )
                // 2. If server does not support set_volume directly, simulate proportional Volume Up/Down keystrokes
                if (res.isFailure) {
                    val delta = clamped - old
                    val steps = (kotlin.math.abs(delta) / 2).coerceIn(1, 10)
                    val key = if (delta > 0) RemoteKeyNames.VOLUME_UP else RemoteKeyNames.VOLUME_DOWN
                    repeat(steps) {
                        apiClient.sendKey(
                            ip = currentSettings.serverIp,
                            port = currentSettings.serverPort,
                            pin = currentSettings.pin,
                            key = key
                        )
                        delay(25)
                    }
                }
            }
        }
    }

    fun updateUseJoystick(useJoystick: Boolean) {
        triggerHapticFeedback()
        viewModelScope.launch {
            settingsRepository.updateUseJoystick(useJoystick)
        }
    }

    fun onChannelUpClicked() {
        onKeyClicked(RemoteKeyNames.MEDIA_NEXT)
    }

    fun onChannelDownClicked() {
        onKeyClicked(RemoteKeyNames.MEDIA_PREV)
    }

    fun openSearchModal() {
        triggerHapticFeedback()
        _isSearchModalOpen.value = true
    }

    fun closeSearchModal() {
        _isSearchModalOpen.value = false
    }

    fun setCurrentPage(page: Int) {
        triggerHapticFeedback()
        _currentPage.value = page
    }

    fun togglePage() {
        triggerHapticFeedback()
        _currentPage.value = if (_currentPage.value == 0) 1 else 0
    }

    fun updateConnectionSettings(ip: String, port: Int, pin: String) {
        viewModelScope.launch {
            settingsRepository.updateConnectionSettings(ip, port, pin)
            reconnect()
        }
    }

    fun updateMouseSensitivity(sensitivity: Float) {
        viewModelScope.launch {
            settingsRepository.updateMouseSensitivity(sensitivity)
        }
    }

    fun updateGyroEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateGyroEnabled(enabled)
        }
    }

    fun updateGyroSensitivity(sensitivity: Float) {
        viewModelScope.launch {
            settingsRepository.updateGyroSensitivity(sensitivity)
            gyroController.updateSensitivity(sensitivity)
        }
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateVibrationEnabled(enabled)
        }
    }

    fun updateDisplaySettings(
        showUpNext: Boolean? = null,
        showVoiceSearch: Boolean? = null,
        showPlaybackControls: Boolean? = null,
        showVolumeControls: Boolean? = null,
        showAdditionalControls: Boolean? = null,
        compactTouchpad: Boolean? = null
    ) {
        viewModelScope.launch {
            settingsRepository.updateDisplaySettings(
                showUpNext = showUpNext,
                showVoiceSearch = showVoiceSearch,
                showPlaybackControls = showPlaybackControls,
                showVolumeControls = showVolumeControls,
                showAdditionalControls = showAdditionalControls,
                compactTouchpad = compactTouchpad
            )
        }
    }

    fun updateDesignStyle(style: com.example.ui.theme.RemoteDesignStyle) {
        triggerHapticFeedback()
        viewModelScope.launch {
            settingsRepository.updateDesignStyle(style)
        }
    }

    fun cycleNextDesignStyle() {
        triggerHapticFeedback()
        val current = settings.value.remoteDesignStyle
        val allStyles = com.example.ui.theme.RemoteDesignStyle.entries
        val nextIndex = (allStyles.indexOf(current) + 1) % allStyles.size
        updateDesignStyle(allStyles[nextIndex])
    }

    fun updateControllerSizes(dpadScale: Float? = null, touchpadHeightScale: Float? = null) {
        viewModelScope.launch {
            settingsRepository.updateControllerSizes(dpadScale, touchpadHeightScale)
        }
    }

    fun updateNowPlayingText(text: String) {
        _liveNowPlayingTitle.value = text
        viewModelScope.launch {
            settingsRepository.updateNowPlayingText(text)
        }
    }

    fun updateJoystickSensitivity(sensitivity: Float) {
        viewModelScope.launch {
            settingsRepository.updateJoystickSensitivity(sensitivity)
        }
    }

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun togglePlayPauseState() {
        _isPlaying.value = !_isPlaying.value
        onKeyClicked(RemoteKeyNames.MEDIA_PLAY_PAUSE)
    }


    override fun onCleared() {
        super.onCleared()
        gyroController.stop()
        voiceManager.stopListening()
        healthCheckJob?.cancel()
    }
}
