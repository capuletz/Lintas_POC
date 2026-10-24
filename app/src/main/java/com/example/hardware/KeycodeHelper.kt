package com.example.hardware

import android.view.KeyEvent

data class PttKeyPreset(
    val keyCode: Int,
    val title: String,
    val description: String
)

object KeycodeHelper {

    // Common POC / Osaka Q8 PTT keycode constants
    const val KEYCODE_POC_PTT_DEFAULT = 261 // Standard Android POC Radio PTT
    const val KEYCODE_POC_PTT_SECONDARY = 262
    const val KEYCODE_POC_CUSTOM_1 = 131 // KEYCODE_F1
    const val KEYCODE_POC_CUSTOM_2 = 132 // KEYCODE_F2
    const val KEYCODE_POC_SOS = 183 // KEYCODE_PROG_RED
    const val KEYCODE_HEADSETHOOK = KeyEvent.KEYCODE_HEADSETHOOK // 79
    const val KEYCODE_VOLUME_DOWN = KeyEvent.KEYCODE_VOLUME_DOWN // 25
    const val KEYCODE_VOLUME_UP = KeyEvent.KEYCODE_VOLUME_UP // 24
    const val KEYCODE_BUTTON_L1 = KeyEvent.KEYCODE_BUTTON_L1 // 102

    val AVAILABLE_PRESETS = listOf(
        PttKeyPreset(
            keyCode = KEYCODE_POC_PTT_DEFAULT,
            title = "Osaka Q8 Hardware PTT (Key 261)",
            description = "Tombol PTT samping standar Osaka Q8 & radio PoC Android"
        ),
        PttKeyPreset(
            keyCode = KEYCODE_POC_CUSTOM_1,
            title = "Side Key F1 (Key 131)",
            description = "Tombol fungsi samping atas / F1"
        ),
        PttKeyPreset(
            keyCode = KEYCODE_VOLUME_DOWN,
            title = "Volume Down (Key 25)",
            description = "Gunakan tombol volume bawah sebagai trigger PTT"
        ),
        PttKeyPreset(
            keyCode = KEYCODE_HEADSETHOOK,
            title = "Headset Hook / Mic PTT (Key 79)",
            description = "PTT eksternal microphone / earpiece K-Type"
        ),
        PttKeyPreset(
            keyCode = KEYCODE_BUTTON_L1,
            title = "Button L1 / Game Key (Key 102)",
            description = "Tombol trigger L1 radio POC"
        ),
        PttKeyPreset(
            keyCode = KEYCODE_POC_SOS,
            title = "Orange Top Key / SOS (Key 183)",
            description = "Tombol orange atas Osaka Q8"
        )
    )

    fun getKeyLabel(keyCode: Int): String {
        return when (keyCode) {
            261 -> "Osaka Q8 PTT (261)"
            262 -> "Osaka Q8 PTT-2 (262)"
            131 -> "Side Key F1 (131)"
            132 -> "Side Key F2 (132)"
            133 -> "Side Key F3 (133)"
            134 -> "Side Key F4 (134)"
            183 -> "Orange/SOS Key (183)"
            KeyEvent.KEYCODE_HEADSETHOOK -> "Headset Hook / K-Type (79)"
            KeyEvent.KEYCODE_VOLUME_UP -> "Volume Up / Rotary Knob CW (24)"
            KeyEvent.KEYCODE_VOLUME_DOWN -> "Volume Down / Rotary Knob CCW (25)"
            KeyEvent.KEYCODE_DPAD_UP -> "D-Pad UP (19)"
            KeyEvent.KEYCODE_DPAD_DOWN -> "D-Pad DOWN (20)"
            KeyEvent.KEYCODE_DPAD_LEFT -> "D-Pad LEFT (21)"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "D-Pad RIGHT (22)"
            KeyEvent.KEYCODE_DPAD_CENTER -> "D-Pad OK/CENTER (23)"
            KeyEvent.KEYCODE_ENTER -> "Keypad ENTER (66)"
            KeyEvent.KEYCODE_BACK -> "Keypad BACK (4)"
            KeyEvent.KEYCODE_MENU -> "Keypad MENU (82)"
            KeyEvent.KEYCODE_0 -> "Keypad 0 (7)"
            KeyEvent.KEYCODE_1 -> "Keypad 1 (8)"
            KeyEvent.KEYCODE_2 -> "Keypad 2 (9)"
            KeyEvent.KEYCODE_3 -> "Keypad 3 (10)"
            KeyEvent.KEYCODE_4 -> "Keypad 4 (11)"
            KeyEvent.KEYCODE_5 -> "Keypad 5 (12)"
            KeyEvent.KEYCODE_6 -> "Keypad 6 (13)"
            KeyEvent.KEYCODE_7 -> "Keypad 7 (14)"
            KeyEvent.KEYCODE_8 -> "Keypad 8 (15)"
            KeyEvent.KEYCODE_9 -> "Keypad 9 (16)"
            KeyEvent.KEYCODE_STAR -> "Keypad * [Star] (17)"
            KeyEvent.KEYCODE_POUND -> "Keypad # [Pound] (18)"
            KeyEvent.KEYCODE_CALL -> "Keypad Call / Hijau (5)"
            KeyEvent.KEYCODE_ENDCALL -> "Keypad End / Merah (6)"
            else -> "KeyCode $keyCode"
        }
    }
}
