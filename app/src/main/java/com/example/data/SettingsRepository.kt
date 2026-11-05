package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.RemoteSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ytremote_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val SERVER_IP = stringPreferencesKey("server_ip")
        val SERVER_PORT = intPreferencesKey("server_port")
        val SERVER_PIN = stringPreferencesKey("server_pin")
        val MOUSE_SENSITIVITY = floatPreferencesKey("mouse_sensitivity")
        val SHOW_TOUCHPAD = booleanPreferencesKey("show_touchpad")
        val TOUCHPAD_TAP_TO_CLICK = booleanPreferencesKey("touchpad_tap_to_click")
        val SHOW_SCROLL_BAR = booleanPreferencesKey("show_scroll_bar")
        val SCROLL_SENSITIVITY = floatPreferencesKey("scroll_sensitivity")
        val INVERT_SCROLL = booleanPreferencesKey("invert_scroll")
        val GYRO_ENABLED = booleanPreferencesKey("gyro_enabled")
        val GYRO_SENSITIVITY = floatPreferencesKey("gyro_sensitivity")
        val GYRO_SMOOTHING = floatPreferencesKey("gyro_smoothing")
        val PAUSE_AIR_MOUSE_WHILE_TYPING = booleanPreferencesKey("pause_air_mouse_while_typing")
        val SHOW_QUICK_AIR_MOUSE_TOGGLE = booleanPreferencesKey("show_quick_air_mouse_toggle")
        val AUTO_ENTER_ON_TYPING = booleanPreferencesKey("auto_enter_on_typing")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SHOW_UP_NEXT = booleanPreferencesKey("show_up_next")
        val SHOW_VOICE_SEARCH = booleanPreferencesKey("show_voice_search")
        val SHOW_PLAYBACK_CONTROLS = booleanPreferencesKey("show_playback_controls")
        val SHOW_VOLUME_CONTROLS = booleanPreferencesKey("show_volume_controls")
        val SHOW_ADDITIONAL_CONTROLS = booleanPreferencesKey("show_additional_controls")
        val COMPACT_TOUCHPAD = booleanPreferencesKey("compact_touchpad")
        val REMOTE_DESIGN_STYLE = stringPreferencesKey("remote_design_style")
        val DPAD_SCALE = floatPreferencesKey("dpad_scale")
        val TOUCHPAD_HEIGHT_SCALE = floatPreferencesKey("touchpad_height_scale")
        val NOW_PLAYING_TEXT = stringPreferencesKey("now_playing_text")
        val USE_JOYSTICK = booleanPreferencesKey("use_joystick")
        val JOYSTICK_SENSITIVITY = floatPreferencesKey("joystick_sensitivity")
    }

    val settingsFlow: Flow<RemoteSettings> = context.dataStore.data.map { preferences ->
        val styleId = preferences[PreferencesKeys.REMOTE_DESIGN_STYLE] ?: com.example.ui.theme.RemoteDesignStyle.OBSIDIAN_PRO.id
        RemoteSettings(
            serverIp = preferences[PreferencesKeys.SERVER_IP] ?: "192.168.1.100",
            serverPort = preferences[PreferencesKeys.SERVER_PORT] ?: 8765,
            pin = preferences[PreferencesKeys.SERVER_PIN] ?: "2468",
            mouseSensitivity = preferences[PreferencesKeys.MOUSE_SENSITIVITY] ?: 1.5f,
            joystickSensitivity = preferences[PreferencesKeys.JOYSTICK_SENSITIVITY] ?: 1.0f,
            showTouchpad = preferences[PreferencesKeys.SHOW_TOUCHPAD] ?: true,
            touchpadTapToClick = preferences[PreferencesKeys.TOUCHPAD_TAP_TO_CLICK] ?: true,
            showScrollBar = preferences[PreferencesKeys.SHOW_SCROLL_BAR] ?: true,
            scrollSensitivity = preferences[PreferencesKeys.SCROLL_SENSITIVITY] ?: 1.5f,
            invertScroll = preferences[PreferencesKeys.INVERT_SCROLL] ?: false,
            gyroEnabled = preferences[PreferencesKeys.GYRO_ENABLED] ?: false,
            gyroSensitivity = preferences[PreferencesKeys.GYRO_SENSITIVITY] ?: 2.0f,
            gyroSmoothing = preferences[PreferencesKeys.GYRO_SMOOTHING] ?: 0.85f,
            pauseAirMouseWhileTyping = preferences[PreferencesKeys.PAUSE_AIR_MOUSE_WHILE_TYPING] ?: true,
            showQuickAirMouseToggle = preferences[PreferencesKeys.SHOW_QUICK_AIR_MOUSE_TOGGLE] ?: true,
            autoEnterOnTyping = preferences[PreferencesKeys.AUTO_ENTER_ON_TYPING] ?: true,
            keepScreenOn = preferences[PreferencesKeys.KEEP_SCREEN_ON] ?: true,
            vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
            showUpNext = preferences[PreferencesKeys.SHOW_UP_NEXT] ?: true,
            showVoiceSearch = preferences[PreferencesKeys.SHOW_VOICE_SEARCH] ?: true,
            showPlaybackControls = preferences[PreferencesKeys.SHOW_PLAYBACK_CONTROLS] ?: true,
            showVolumeControls = preferences[PreferencesKeys.SHOW_VOLUME_CONTROLS] ?: true,
            showAdditionalControls = preferences[PreferencesKeys.SHOW_ADDITIONAL_CONTROLS] ?: true,
            compactTouchpad = preferences[PreferencesKeys.COMPACT_TOUCHPAD] ?: false,
            remoteDesignStyle = com.example.ui.theme.RemoteDesignStyle.fromId(styleId),
            dpadScale = preferences[PreferencesKeys.DPAD_SCALE] ?: 1.18f,
            touchpadHeightScale = preferences[PreferencesKeys.TOUCHPAD_HEIGHT_SCALE] ?: 1.25f,
            nowPlayingText = preferences[PreferencesKeys.NOW_PLAYING_TEXT]
                ?: "YouTube: Lofi Hip Hop Radio ☕ - Beats to Relax/Study to • Lofi Girl [LIVE]",
            useJoystick = preferences[PreferencesKeys.USE_JOYSTICK] ?: true
        )
    }

    suspend fun updateConnectionSettings(ip: String, port: Int, pin: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_IP] = ip
            preferences[PreferencesKeys.SERVER_PORT] = port
            preferences[PreferencesKeys.SERVER_PIN] = pin
        }
    }

    suspend fun updateMouseSensitivity(sensitivity: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MOUSE_SENSITIVITY] = sensitivity
        }
    }

    suspend fun updateGyroEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GYRO_ENABLED] = enabled
        }
    }

    suspend fun updateGyroSensitivity(sensitivity: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GYRO_SENSITIVITY] = sensitivity
        }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun updateDisplaySettings(
        showUpNext: Boolean? = null,
        showVoiceSearch: Boolean? = null,
        showPlaybackControls: Boolean? = null,
        showVolumeControls: Boolean? = null,
        showAdditionalControls: Boolean? = null,
        compactTouchpad: Boolean? = null
    ) {
        context.dataStore.edit { preferences ->
            showUpNext?.let { preferences[PreferencesKeys.SHOW_UP_NEXT] = it }
            showVoiceSearch?.let { preferences[PreferencesKeys.SHOW_VOICE_SEARCH] = it }
            showPlaybackControls?.let { preferences[PreferencesKeys.SHOW_PLAYBACK_CONTROLS] = it }
            showVolumeControls?.let { preferences[PreferencesKeys.SHOW_VOLUME_CONTROLS] = it }
            showAdditionalControls?.let { preferences[PreferencesKeys.SHOW_ADDITIONAL_CONTROLS] = it }
            compactTouchpad?.let { preferences[PreferencesKeys.COMPACT_TOUCHPAD] = it }
        }
    }

    suspend fun updateDesignStyle(style: com.example.ui.theme.RemoteDesignStyle) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMOTE_DESIGN_STYLE] = style.id
        }
    }

    suspend fun updateControllerSizes(dpadScale: Float? = null, touchpadHeightScale: Float? = null) {
        context.dataStore.edit { preferences ->
            dpadScale?.let { preferences[PreferencesKeys.DPAD_SCALE] = it }
            touchpadHeightScale?.let { preferences[PreferencesKeys.TOUCHPAD_HEIGHT_SCALE] = it }
        }
    }

    suspend fun updateNowPlayingText(text: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOW_PLAYING_TEXT] = text
        }
    }

    suspend fun updateUseJoystick(useJoystick: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_JOYSTICK] = useJoystick
        }
    }

    suspend fun updateJoystickSensitivity(sensitivity: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.JOYSTICK_SENSITIVITY] = sensitivity
        }
    }

    suspend fun updateTouchpadSettings(
        showTouchpad: Boolean? = null,
        tapToClick: Boolean? = null,
        sensitivity: Float? = null,
        heightScale: Float? = null
    ) {
        context.dataStore.edit { preferences ->
            showTouchpad?.let { preferences[PreferencesKeys.SHOW_TOUCHPAD] = it }
            tapToClick?.let { preferences[PreferencesKeys.TOUCHPAD_TAP_TO_CLICK] = it }
            sensitivity?.let { preferences[PreferencesKeys.MOUSE_SENSITIVITY] = it }
            heightScale?.let { preferences[PreferencesKeys.TOUCHPAD_HEIGHT_SCALE] = it }
        }
    }

    suspend fun updateScrollSettings(
        showScrollBar: Boolean? = null,
        sensitivity: Float? = null,
        invertScroll: Boolean? = null
    ) {
        context.dataStore.edit { preferences ->
            showScrollBar?.let { preferences[PreferencesKeys.SHOW_SCROLL_BAR] = it }
            sensitivity?.let { preferences[PreferencesKeys.SCROLL_SENSITIVITY] = it }
            invertScroll?.let { preferences[PreferencesKeys.INVERT_SCROLL] = it }
        }
    }

    suspend fun updateAirMouseSettings(
        enabled: Boolean? = null,
        sensitivity: Float? = null,
        smoothing: Float? = null,
        pauseWhileTyping: Boolean? = null,
        showQuickToggle: Boolean? = null
    ) {
        context.dataStore.edit { preferences ->
            enabled?.let { preferences[PreferencesKeys.GYRO_ENABLED] = it }
            sensitivity?.let { preferences[PreferencesKeys.GYRO_SENSITIVITY] = it }
            smoothing?.let { preferences[PreferencesKeys.GYRO_SMOOTHING] = it }
            pauseWhileTyping?.let { preferences[PreferencesKeys.PAUSE_AIR_MOUSE_WHILE_TYPING] = it }
            showQuickToggle?.let { preferences[PreferencesKeys.SHOW_QUICK_AIR_MOUSE_TOGGLE] = it }
        }
    }

    suspend fun updateFeatureSettings(
        keepScreenOn: Boolean? = null,
        autoEnterOnTyping: Boolean? = null,
        vibrationEnabled: Boolean? = null,
        useJoystick: Boolean? = null
    ) {
        context.dataStore.edit { preferences ->
            keepScreenOn?.let { preferences[PreferencesKeys.KEEP_SCREEN_ON] = it }
            autoEnterOnTyping?.let { preferences[PreferencesKeys.AUTO_ENTER_ON_TYPING] = it }
            vibrationEnabled?.let { preferences[PreferencesKeys.VIBRATION_ENABLED] = it }
            useJoystick?.let { preferences[PreferencesKeys.USE_JOYSTICK] = it }
        }
    }
}

