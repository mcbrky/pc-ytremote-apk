package com.example.network

import com.example.model.RemoteCommand
import com.example.model.RemoteKeyNames
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class RemoteApiClient {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val commandAdapter = moshi.adapter(RemoteCommand::class.java)

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // Disable stale connection pooling because Python HTTP servers on Windows close
    // sockets when idle. Always create fresh sockets and retry on connection drops.
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(2500, TimeUnit.MILLISECONDS)
        .readTimeout(2500, TimeUnit.MILLISECONDS)
        .writeTimeout(2500, TimeUnit.MILLISECONDS)
        .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .retryOnConnectionFailure(true)
        .build()

    // High performance mouse delta aggregator to avoid TCP congestion
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var pendingMouseDx = 0f
    private var pendingMouseDy = 0f
    private val mouseLock = Any()
    private val isMouseSending = AtomicBoolean(false)

    suspend fun checkHealth(ip: String, port: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        val sanitizedIp = sanitizeHost(ip)
        val url = "http://$sanitizedIp:$port/health"

        var lastException: Exception? = null
        for (attempt in 1..2) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("Connection", "close")
                    .get()
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        return@withContext Result.success(true)
                    } else {
                        lastException = IOException("Server returned HTTP ${response.code}")
                    }
                }
            } catch (e: Exception) {
                lastException = e
                if (attempt < 2) {
                    delay(50)
                }
            }
        }
        Result.failure(lastException ?: IOException("Health check failed"))
    }

    suspend fun fetchNowPlaying(ip: String, port: Int): Result<String?> = withContext(Dispatchers.IO) {
        val sanitizedIp = sanitizeHost(ip)
        val endpoints = listOf("now_playing", "status", "media", "youtube", "active_window")
        for (endpoint in endpoints) {
            try {
                val url = "http://$sanitizedIp:$port/$endpoint"
                val request = Request.Builder()
                    .url(url)
                    .header("Connection", "close")
                    .get()
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()?.trim()
                        if (!body.isNullOrBlank()) {
                            var title: String? = null
                            if (body.startsWith("{") && body.endsWith("}")) {
                                try {
                                    val json = org.json.JSONObject(body)
                                    title = when {
                                        json.has("title") -> json.optString("title")
                                        json.has("now_playing") -> json.optString("now_playing")
                                        json.has("media_title") -> json.optString("media_title")
                                        json.has("youtube_title") -> json.optString("youtube_title")
                                        json.has("window") -> json.optString("window")
                                        else -> null
                                    }
                                } catch (e: Exception) {
                                    title = body
                                }
                            } else {
                                title = body
                            }
                            if (!title.isNullOrBlank() && title != "null" && title != "undefined") {
                                val cleanedTitle = title.replace(" - YouTube", "").replace("YouTube - ", "").trim()
                                val formatted = if (cleanedTitle.startsWith("YouTube", ignoreCase = true)) {
                                    cleanedTitle
                                } else {
                                    "YouTube: $cleanedTitle"
                                }
                                return@withContext Result.success(formatted)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Continue to next endpoint
            }
        }
        Result.success(null)
    }

    suspend fun fetchVolume(ip: String, port: Int): Result<Int?> = withContext(Dispatchers.IO) {
        val sanitizedIp = sanitizeHost(ip)
        try {
            val url = "http://$sanitizedIp:$port/volume"
            val request = Request.Builder()
                .url(url)
                .header("Connection", "close")
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()?.trim()
                    if (!body.isNullOrBlank()) {
                        val level = if (body.startsWith("{")) {
                            try {
                                val json = org.json.JSONObject(body)
                                json.optInt("volume", json.optInt("level", -1))
                            } catch (e: Exception) {
                                -1
                            }
                        } else {
                            body.toIntOrNull() ?: -1
                        }
                        if (level in 0..100) {
                            return@withContext Result.success(level)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore failure
        }
        Result.success(null)
    }

    suspend fun sendVolume(ip: String, port: Int, pin: String, volumePercent: Int): Result<Unit> {
        val command = RemoteCommand(
            pin = pin,
            action = "set_volume",
            value = volumePercent.coerceIn(0, 100)
        )
        return sendCommand(ip, port, command)
    }

    suspend fun sendKey(ip: String, port: Int, pin: String, key: String): Result<Unit> {
        val command = RemoteCommand(
            pin = pin,
            action = "key",
            key = key
        )
        return sendCommand(ip, port, command)
    }

    suspend fun sendType(ip: String, port: Int, pin: String, text: String, enter: Boolean = false): Result<Unit> {
        val command = RemoteCommand(
            pin = pin,
            action = "type",
            text = text,
            query = text,
            enter = if (enter) true else null
        )
        return sendCommand(ip, port, command)
    }

    suspend fun sendTypeAndEnter(ip: String, port: Int, pin: String, text: String): Result<Unit> {
        val typeResult = sendType(ip, port, pin, text, enter = true)
        delay(70)
        sendKey(ip, port, pin, RemoteKeyNames.ENTER)
        return typeResult
    }

    suspend fun sendInstantYouTubeSearch(ip: String, port: Int, pin: String, query: String): Result<Unit> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return Result.success(Unit)
        // Universal typing: write directly to active search bar or input field and submit with Enter
        return sendTypeAndEnter(ip, port, pin, trimmedQuery)
    }

    suspend fun sendScroll(ip: String, port: Int, pin: String, dy: Float): Result<Unit> {
        // 1. Send scroll action (wheel delta)
        val scrollCommand = RemoteCommand(
            pin = pin,
            action = "scroll",
            dy = dy
        )
        sendCommand(ip, port, scrollCommand)

        // 2. ALWAYS also send corresponding page and arrow keys:
        // dy > 0 (Scroll UP): PAGE_UP + UP arrow
        // dy < 0 (Scroll DOWN): PAGE_DOWN + DOWN arrow
        return try {
            val primaryKey = if (dy > 0) RemoteKeyNames.PAGE_UP else RemoteKeyNames.PAGE_DOWN
            val secondaryKey = if (dy > 0) RemoteKeyNames.UP else RemoteKeyNames.DOWN
            sendKey(ip, port, pin, primaryKey)
            delay(35)
            sendKey(ip, port, pin, secondaryKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendClick(ip: String, port: Int, pin: String): Result<Unit> {
        val command = RemoteCommand(
            pin = pin,
            action = "click"
        )
        return sendCommand(ip, port, command)
    }

    /**
     * Queues mouse delta movement with smooth batching to ensure fluid 60fps tracking
     * without flooding the Windows HTTP server with thousands of concurrent sockets.
     */
    fun enqueueMouseMove(ip: String, port: Int, pin: String, dx: Float, dy: Float) {
        synchronized(mouseLock) {
            pendingMouseDx += dx
            pendingMouseDy += dy
        }

        if (isMouseSending.compareAndSet(false, true)) {
            scope.launch {
                processMouseQueue(ip, port, pin)
            }
        }
    }

    private suspend fun processMouseQueue(ip: String, port: Int, pin: String) {
        try {
            while (scope.isActive) {
                var dxToSend = 0f
                var dyToSend = 0f

                synchronized(mouseLock) {
                    if (pendingMouseDx == 0f && pendingMouseDy == 0f) {
                        return
                    }
                    dxToSend = pendingMouseDx
                    dyToSend = pendingMouseDy
                    pendingMouseDx = 0f
                    pendingMouseDy = 0f
                }

                if (dxToSend != 0f || dyToSend != 0f) {
                    val command = RemoteCommand(
                        pin = pin,
                        action = "mouse",
                        dx = dxToSend,
                        dy = dyToSend
                    )
                    sendCommand(ip, port, command)
                }

                // Tiny pacing pause to match display refresh rate (~16ms)
                delay(16)
            }
        } finally {
            isMouseSending.set(false)
            // If more mouse events arrived while loop was exiting, restart
            synchronized(mouseLock) {
                if ((pendingMouseDx != 0f || pendingMouseDy != 0f) && isMouseSending.compareAndSet(false, true)) {
                    scope.launch { processMouseQueue(ip, port, pin) }
                }
            }
        }
    }

    suspend fun sendCommand(ip: String, port: Int, command: RemoteCommand): Result<Unit> = withContext(Dispatchers.IO) {
        val sanitizedIp = sanitizeHost(ip)
        val url = "http://$sanitizedIp:$port/control"
        val json = commandAdapter.toJson(command)

        var lastException: Exception? = null
        for (attempt in 1..2) {
            try {
                val body = json.toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url(url)
                    .header("Connection", "close")
                    .post(body)
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        return@withContext Result.success(Unit)
                    } else {
                        lastException = IOException("HTTP ${response.code}: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                lastException = e
                if (attempt < 2) {
                    delay(50)
                }
            }
        }
        Result.failure(lastException ?: IOException("Failed to send command to $sanitizedIp:$port"))
    }

    private fun sanitizeHost(host: String): String {
        return host.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .trimEnd('/')
    }
}
