package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.RogerBeepGenerator
import com.example.hardware.KeycodeHelper
import com.example.model.PttMode
import com.example.model.RadioSettings
import com.example.model.RadioState
import com.example.model.ScreenDensityMode
import com.example.ui.theme.RadioBorder
import com.example.ui.theme.RadioDarkBg
import com.example.ui.theme.RadioFocusRing
import com.example.ui.theme.RadioLcdAmber
import com.example.ui.theme.RadioLcdBg
import com.example.ui.theme.RadioLcdCyan
import com.example.ui.theme.RadioLcdText
import com.example.ui.theme.RadioPttOrange
import com.example.ui.theme.RadioRxGreen
import com.example.ui.theme.RadioSurface
import com.example.ui.theme.RadioSurfaceVariant
import com.example.ui.theme.RadioTxRed

@Composable
fun KeypadHardwareSettingsScreen(
    state: RadioState,
    settings: RadioSettings,
    onUpdateSettings: (RadioSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RadioDarkBg)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title
        Text(
            text = "HARDWARE & KEYPAD (HT OSAKA Q8)",
            color = RadioLcdAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // 1. LIVE HARDWARE KEY DETECTOR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(RadioLcdBg)
                .border(1.5.dp, RadioFocusRing, RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Keyboard,
                            contentDescription = "Key Detector",
                            tint = RadioFocusRing,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "LIVE KEY DETECTOR",
                            color = RadioFocusRing,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "TEKAN TOMBOL FISIK",
                        color = RadioLcdAmber,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                val lastKey = state.lastHardwareKey
                if (lastKey != null) {
                    Text(
                        text = "KeyCode: ${lastKey.keyCode} (${lastKey.action})",
                        color = RadioLcdText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Label: ${lastKey.keyName}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    if (lastKey.keyCode != settings.pttKeyCode) {
                        Button(
                            onClick = {
                                onUpdateSettings(settings.copy(pttKeyCode = lastKey.keyCode))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .testTag("set_detected_key_as_ptt"),
                            colors = ButtonDefaults.buttonColors(containerColor = RadioPttOrange),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "JADIKAN TOMBOL INI SEBAGAI PTT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Silakan tekan tombol samping PTT, knob, atau keypad pada Osaka Q8...",
                        color = Color(0xFF90A4AE),
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 2. TOMBOL PTT HARDWARE PRESET
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(RadioSurface)
                .border(1.dp, RadioBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "PILIHAN TOMBOL PTT (PUSH-TO-TALK)",
                    color = RadioLcdAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                KeycodeHelper.AVAILABLE_PRESETS.forEach { preset ->
                    val isSelected = settings.pttKeyCode == preset.keyCode

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color(0xFF2E2419) else RadioSurfaceVariant)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) RadioPttOrange else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                onUpdateSettings(settings.copy(pttKeyCode = preset.keyCode))
                            }
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Select",
                            tint = if (isSelected) RadioPttOrange else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.title,
                                color = if (isSelected) RadioFocusRing else Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = preset.description,
                                color = Color(0xFF90A4AE),
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. PTT MODE & ROGER BEEP
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(RadioSurface)
                .border(1.dp, RadioBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "MODE PTT & ROGER BEEP",
                    color = RadioLcdAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Momentary vs Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode PTT Transmisi",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (settings.pttMode == PttMode.MOMENTARY) "Tahan tombol untuk bicara (Momentary)" else "Tekan sekali mulai, tekan lagi lepas (Toggle)",
                            color = Color(0xFF90A4AE),
                            fontSize = 8.5.sp
                        )
                    }

                    Button(
                        onClick = {
                            val nextMode = if (settings.pttMode == PttMode.MOMENTARY) PttMode.TOGGLE else PttMode.MOMENTARY
                            onUpdateSettings(settings.copy(pttMode = nextMode))
                        },
                        modifier = Modifier.height(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RadioSurfaceVariant),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (settings.pttMode == PttMode.MOMENTARY) "MOMENTARY" else "TOGGLE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = RadioFocusRing
                        )
                    }
                }

                // Roger Beep Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Roger Beep Tone",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Bunyi nada saat PTT dilepas",
                            color = Color(0xFF90A4AE),
                            fontSize = 8.5.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { RogerBeepGenerator.playRogerBeep(settings.rogerBeepToneHz) },
                            modifier = Modifier.height(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RadioSurfaceVariant),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("TES BEEP", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = RadioLcdText)
                        }

                        Switch(
                            checked = settings.rogerBeepEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(rogerBeepEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RadioLcdText,
                                checkedTrackColor = Color(0xFF1E3A24)
                            )
                        )
                    }
                }

                // Vibration Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Getar PTT (Haptic)",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Getaran saat tombol PTT ditekan / dilepas",
                            color = Color(0xFF90A4AE),
                            fontSize = 8.5.sp
                        )
                    }

                    Switch(
                        checked = settings.vibrationFeedback,
                        onCheckedChange = { onUpdateSettings(settings.copy(vibrationFeedback = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RadioFocusRing,
                            checkedTrackColor = Color(0xFF3E3514)
                        )
                    )
                }
            }
        }

        // 4. VOLUME KNOB & RESOLUSI LAYAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(RadioSurface)
                .border(1.dp, RadioBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "KNOB VOLUME & RESOLUSI LAYAR",
                    color = RadioLcdAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Volume Knob Info
                Text(
                    text = "Rotary Knob Osaka Q8: Putar knob atas untuk menaikkan/menurunkan volume suara. Tampilan HUD Volume akan muncul otomatis di layar.",
                    color = Color(0xFFB0BEC5),
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Screen density
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Resolusi Layar",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (settings.screenDensityMode == ScreenDensityMode.QVGA_COMPACT) "QVGA (240 x 320 px) - Osaka Q8" else "HVGA (320 x 480 px)",
                            color = RadioLcdCyan,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = {
                            val nextDensity = if (settings.screenDensityMode == ScreenDensityMode.QVGA_COMPACT) ScreenDensityMode.HVGA_STANDARD else ScreenDensityMode.QVGA_COMPACT
                            onUpdateSettings(settings.copy(screenDensityMode = nextDensity))
                        },
                        modifier = Modifier.height(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RadioSurfaceVariant),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (settings.screenDensityMode == ScreenDensityMode.QVGA_COMPACT) "QVGA 240x320" else "HVGA 320x480",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RadioFocusRing
                        )
                    }
                }
            }
        }
    }
}
