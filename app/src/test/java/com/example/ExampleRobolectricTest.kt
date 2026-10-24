package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.hardware.KeycodeHelper
import com.example.model.PttMode
import com.example.model.RadioSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Osaka PTT", appName)
    }

    @Test
    fun `verify Osaka Q8 PTT keycode mapping`() {
        val label = KeycodeHelper.getKeyLabel(KeycodeHelper.KEYCODE_POC_PTT_DEFAULT)
        assertTrue(label.contains("261"))
        assertTrue(label.contains("Osaka Q8"))
    }

    @Test
    fun `verify default radio settings`() {
        val settings = RadioSettings()
        assertEquals(261, settings.pttKeyCode)
        assertEquals(PttMode.MOMENTARY, settings.pttMode)
        assertTrue(settings.rogerBeepEnabled)
        assertTrue(settings.vibrationFeedback)
    }
}
