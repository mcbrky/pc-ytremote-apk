package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteCommand(
    @Json(name = "pin") val pin: String,
    @Json(name = "action") val action: String,
    @Json(name = "key") val key: String? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "query") val query: String? = null,
    @Json(name = "enter") val enter: Boolean? = null,
    @Json(name = "dx") val dx: Float? = null,
    @Json(name = "dy") val dy: Float? = null,
    @Json(name = "value") val value: Int? = null
)

enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    ERROR
}

object RemoteKeyNames {
    const val UP = "UP"
    const val DOWN = "DOWN"
    const val LEFT = "LEFT"
    const val RIGHT = "RIGHT"
    const val ENTER = "ENTER"
    const val ESC = "ESC"
    const val BACKSPACE = "BACKSPACE"
    const val SPACE = "SPACE"
    const val HOME = "HOME"
    const val END = "END"
    const val F11 = "F11"
    const val TAB = "TAB"
    const val VOLUME_UP = "VOLUME_UP"
    const val VOLUME_DOWN = "VOLUME_DOWN"
    const val MUTE = "MUTE"
    const val MEDIA_PLAY_PAUSE = "MEDIA_PLAY_PAUSE"
    const val MEDIA_NEXT = "MEDIA_NEXT"
    const val MEDIA_PREV = "MEDIA_PREV"
    const val PAGE_UP = "PAGE_UP"
    const val PAGE_DOWN = "PAGE_DOWN"
}

data class NextVideoInfo(
    val title: String,
    val channelName: String,
    val duration: String,
    val isDemo: Boolean = true
)

data class RemoteSettings(
    val serverIp: String = "192.168.1.100",
    val serverPort: Int = 8765,
    val pin: String = "2468",
    val mouseSensitivity: Float = 1.5f,
    val joystickSensitivity: Float = 1.0f,
    val showTouchpad: Boolean = true,
    val touchpadTapToClick: Boolean = true,
    val showScrollBar: Boolean = true,
    val scrollSensitivity: Float = 1.5f,
    val invertScroll: Boolean = false,
    val gyroEnabled: Boolean = false,
    val gyroSensitivity: Float = 2.0f,
    val gyroSmoothing: Float = 0.85f,
    val pauseAirMouseWhileTyping: Boolean = true,
    val showQuickAirMouseToggle: Boolean = true,
    val autoEnterOnTyping: Boolean = true,
    val keepScreenOn: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val showUpNext: Boolean = true,
    val showVoiceSearch: Boolean = true,
    val showPlaybackControls: Boolean = true,
    val showVolumeControls: Boolean = true,
    val showAdditionalControls: Boolean = true,
    val compactTouchpad: Boolean = false,
    val remoteDesignStyle: com.example.ui.theme.RemoteDesignStyle = com.example.ui.theme.RemoteDesignStyle.OBSIDIAN_PRO,
    val dpadScale: Float = 1.18f, // Optimized large for 6.4" display (~285dp)
    val touchpadHeightScale: Float = 1.25f, // Optimized large for 6.4" display (~210dp)
    val nowPlayingText: String = "YouTube: Lofi Hip Hop Radio ☕ - Beats to Relax/Study to • Lofi Girl [LIVE]",
    val useJoystick: Boolean = true,
    val swapDpadAndTouchpad: Boolean = false
)

