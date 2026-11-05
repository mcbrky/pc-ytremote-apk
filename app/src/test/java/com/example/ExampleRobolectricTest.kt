package com.example

import com.example.model.RemoteCommand
import com.example.model.RemoteKeyNames
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun `verify RemoteCommand json serialization matches python server schema`() {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(RemoteCommand::class.java)

    // Key command
    val keyCmd = RemoteCommand(pin = "2468", action = "key", key = RemoteKeyNames.UP)
    val keyJson = adapter.toJson(keyCmd)
    assertTrue(keyJson.contains("\"pin\":\"2468\""))
    assertTrue(keyJson.contains("\"action\":\"key\""))
    assertTrue(keyJson.contains("\"key\":\"UP\""))

    // Mouse command
    val mouseCmd = RemoteCommand(pin = "2468", action = "mouse", dx = 20f, dy = 10f)
    val mouseJson = adapter.toJson(mouseCmd)
    assertTrue(mouseJson.contains("\"dx\":20.0") || mouseJson.contains("\"dx\":20"))

    // Click command
    val clickCmd = RemoteCommand(pin = "2468", action = "click")
    val clickJson = adapter.toJson(clickCmd)
    assertTrue(clickJson.contains("\"action\":\"click\""))

    // Set Volume command
    val volumeCmd = RemoteCommand(pin = "2468", action = "set_volume", value = 75)
    val volJson = adapter.toJson(volumeCmd)
    assertTrue(volJson.contains("\"action\":\"set_volume\""))
    assertTrue(volJson.contains("\"value\":75"))

    // Search YouTube command
    val searchCmd = RemoteCommand(pin = "2468", action = "search_youtube", text = "sports", query = "sports")
    val searchJson = adapter.toJson(searchCmd)
    assertTrue(searchJson.contains("\"action\":\"search_youtube\""))
    assertTrue(searchJson.contains("\"text\":\"sports\""))
    assertTrue(searchJson.contains("\"query\":\"sports\""))

    // Scroll command
    val scrollCmd = RemoteCommand(pin = "2468", action = "scroll", dy = -120f)
    val scrollJson = adapter.toJson(scrollCmd)
    assertTrue(scrollJson.contains("\"action\":\"scroll\""))
    assertTrue(scrollJson.contains("\"dy\":-120.0") || scrollJson.contains("\"dy\":-120"))
  }

  @Test
  fun `verify RemoteSettings includes joystick sensitivity default`() {
    val settings = com.example.model.RemoteSettings()
    assertEquals(1.0f, settings.joystickSensitivity, 0.01f)
    assertTrue(settings.useJoystick)
  }
}
