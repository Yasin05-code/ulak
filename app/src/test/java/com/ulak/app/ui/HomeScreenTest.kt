package com.ulak.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.os.Looper
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.ulak.app.MainActivity
import com.ulak.app.permission.PermissionHelper
import com.ulak.app.service.CommunicationService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Runs the real MainActivity on the JVM and walks through the main flow.
 * Screenshots are written to app/build/screenshots/.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xhdpi")
class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val app: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun mainFlow() {
        // 1. Home screen: mode is off, nothing sent yet.
        composeRule.onNodeWithText("ULAK").assertExists()
        composeRule.onNodeWithText("Communication Mode: Off").assertExists()
        composeRule.onNodeWithText("No messages yet").assertExists()
        screenshot("1_home")

        // 2. Start Communication with all permissions granted.
        shadowOf(app).grantPermissions(*PermissionHelper.permissionsToRequest())
        composeRule.onNodeWithText("Start Communication").performClick()
        // Android delivers the permission result on the next main-thread loop.
        shadowOf(Looper.getMainLooper()).idle()

        val startIntent = shadowOf(app).nextStartedService
        assertEquals(CommunicationService::class.java.name, startIntent.component?.className)

        // Robolectric records the start request; run the service ourselves.
        val service = Robolectric.buildService(CommunicationService::class.java, startIntent)
            .create()
            .startCommand(0, 1)
        assertTrue(CommunicationService.isRunning.value)

        val notification = shadowOf(service.get()).lastForegroundNotification
        assertEquals(
            "Ulak is relaying messages",
            shadowOf(notification).contentText.toString()
        )

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Communication Mode: Active").assertExists()
        composeRule.onNodeWithText("Stop Communication").assertExists()
        screenshot("2_communication_mode")

        // 3. I'M SAFE creates a message from this phone.
        composeRule.onNodeWithText("I'M SAFE").performClick()
        composeRule.onNodeWithText("I'M SAFE · ", substring = true).assertExists()
        composeRule.onNodeWithText("No messages yet").assertDoesNotExist()
        screenshot("3_im_safe")

        // 4. SOS creates a second message.
        composeRule.onNodeWithText("SOS").performClick()
        composeRule.onNodeWithText("SOS · ", substring = true).assertExists()
        screenshot("4_sos")

        // 5. Stop Communication stops the service.
        composeRule.onNodeWithText("Stop Communication").performClick()
        shadowOf(Looper.getMainLooper()).idle()
        val stopIntent = shadowOf(app).nextStoppedService
        assertEquals(CommunicationService::class.java.name, stopIntent.component?.className)

        service.destroy()
        assertFalse(CommunicationService.isRunning.value)

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Communication Mode: Off").assertExists()
        composeRule.onNodeWithText("Start Communication").assertExists()
        screenshot("5_communication_stopped")
    }

    private fun screenshot(name: String) {
        // Let the button press ripple finish so the image shows the settled screen.
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        val dir = File("build/screenshots").apply { mkdirs() }
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        File(dir, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
