package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.rahulai.data.ai.ActionType
import com.example.rahulai.data.ai.GeminiApiClient
import com.example.rahulai.data.ai.VoiceCommandParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Maya AI", appName)
    }

    @Test
    fun `test voice command parser flashlight`() {
        val cmdOn = VoiceCommandParser.parse("torch jalao")
        assertEquals(ActionType.FLASHLIGHT_ON, cmdOn.actionType)

        val cmdOff = VoiceCommandParser.parse("torch band karo")
        assertEquals(ActionType.FLASHLIGHT_OFF, cmdOff.actionType)

        val cmdHindi = VoiceCommandParser.parse("फोन की टॉर्च जलाओ")
        assertEquals(ActionType.FLASHLIGHT_ON, cmdHindi.actionType)
    }

    @Test
    fun `test voice command parser mobile phone controls and romantic modes`() {
        val cmdNight = VoiceCommandParser.parse("Maya, good night routine chalao")
        assertEquals(ActionType.ROUTINE, cmdNight.actionType)
        assertEquals("routine_night", cmdNight.targetRoutineId)

        val cmdBattery = VoiceCommandParser.parse("battery kitni hai?")
        assertEquals(ActionType.QUERY_BATTERY, cmdBattery.actionType)

        val cmdBoost = VoiceCommandParser.parse("phone boost karo aur RAM clean karo")
        assertEquals(ActionType.BOOST_PHONE, cmdBoost.actionType)

        val cmdRomantic = VoiceCommandParser.parse("romantic date mode on karo")
        assertEquals(ActionType.PHONE_MODE_ROMANTIC, cmdRomantic.actionType)

        val cmdCamera = VoiceCommandParser.parse("camera open karo selfie ke liye")
        assertEquals(ActionType.OPEN_CAMERA, cmdCamera.actionType)

        val cmdWhatsApp = VoiceCommandParser.parse("WhatsApp kholo")
        assertEquals(ActionType.OPEN_WHATSAPP, cmdWhatsApp.actionType)
    }

    @Test
    fun `test maya sweet hindi conversations offline`() {
        val client = GeminiApiClient()
        val sweetReply = client.getOfflineIntelligentResponse("अच्छी अच्छी बातें करो")
        assertTrue(sweetReply.isNotEmpty())

        val shayariReply = client.getOfflineIntelligentResponse("एक शायरी सुनाओ")
        assertTrue(shayariReply.isNotEmpty())

        val whoReply = client.getOfflineIntelligentResponse("तुम कौन हो")
        assertTrue(whoReply.contains("माया") || whoReply.contains("Maya"))
    }
}
