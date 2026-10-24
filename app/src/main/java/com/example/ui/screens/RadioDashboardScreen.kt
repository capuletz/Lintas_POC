package com.example.ui.screens

import android.view.MotionEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioOutputMode
import com.example.model.RadioSettings
import com.example.model.RadioState
import com.example.ui.components.LcdScreenHeader
import com.example.ui.components.VuMeterBar
import com.example.ui.theme.RadioBorder
import com.example.ui.theme.RadioDarkBg
import com.example.ui.theme.RadioFocusRing
import com.example.ui.theme.RadioLcdAmber
import com.example.ui.theme.RadioLcdBg
import com.example.ui.theme.RadioLcdText
import com.example.ui.theme.RadioPttOrange
import com.example.ui.theme.RadioPttPressed
import com.example.ui.theme.RadioRxGreen
import com.example.ui.theme.RadioSurface
import com.example.ui.theme.RadioSurfaceVariant
import com.example.ui.theme.RadioTxRed

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RadioDashboardScreen(
    state: RadioState,
    settings: RadioSettings,
    onStartTransmitting: () -> Unit,
    onStopTransmitting: () -> Unit,
    onToggleMicMute: () -> Unit,
    onToggleAudioDeafen: () -> Unit,
    onToggleAudioOutput: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RadioDarkBg)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // 1. Rugged LCD Status Panel
        LcdScreenHeader(
            state = state,
            audioOutputMode = settings.audioOutput
        )

        // 2. Active Audio VU Meter (Displays TX voice meter or RX audio meter)
        if (state.isTransmitting) {
            VuMeterBar(
                audioLevel = state.txAudioLevel,
                isTx = true,
                label = "TX MIC"
            )
        } else if (state.activeSpeakerName != null) {
            VuMeterBar(
                audioLevel = state.rxAudioLevel,
                isTx = false,
                label = "RX: ${state.activeSpeakerName.take(8)}"
            )
        } else {
            VuMeterBar(
                audioLevel = 0f,
                isTx = false,
                label = "SQUELCH OK"
            )
        }

        // 3. Central PTT Indicator & Large Push-to-Talk Button
        val pttBgColor by animateColorAsState(
            targetValue = when {
                state.isTransmitting -> RadioPttPressed
                state.activeSpeakerName != null -> Color(0xFF1E3A24)
                else -> RadioPttOrange
            },
            label = "ptt_bg"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(pttBgColor)
                .border(2.dp, if (state.isTransmitting) Color.White else RadioBorder, RoundedCornerShape(8.dp))
                .testTag("hardware_ptt_button")
                .pointerInteropFilter { motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            onStartTransmitting()
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            onStopTransmitting()
                            true
                        }
                        else -> false
                    }
                }
                .padding(vertical = 12.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (state.isTransmitting) Icons.Default.RecordVoiceOver else Icons.Default.Mic,
                    contentDescription = "PTT Action",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (state.isTransmitting) "TRANSMITTING (TX)" else "HOLD PTT TO TALK",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Tombol Osaka Q8 Keycode: ${settings.pttKeyCode}",
                        color = Color(0xFFFFE0B2),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 4. Quick Radio Controls Bar (Mute, Deafen, SPK/EAR)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Mic Mute Button [*]
            OutlinedButton(
                onClick = onToggleMicMute,
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .testTag("toggle_mic_mute"),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (state.isMicMuted) RadioTxRed.copy(alpha = 0.25f) else RadioSurfaceVariant
                ),
                shape = RoundedCornerShape(4.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (state.isMicMuted) RadioTxRed else RadioBorder)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (state.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mic Mute",
                    tint = if (state.isMicMuted) RadioTxRed else Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.size(3.dp))
                Text(
                    text = if (state.isMicMuted) "MUTED [*]" else "MIC [*]",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (state.isMicMuted) RadioTxRed else Color.White
                )
            }

            // Audio Deafen Button [#]
            OutlinedButton(
                onClick = onToggleAudioDeafen,
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .testTag("toggle_audio_deafen"),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (state.isAudioDeafened) RadioLcdAmber.copy(alpha = 0.25f) else RadioSurfaceVariant
                ),
                shape = RoundedCornerShape(4.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (state.isAudioDeafened) RadioLcdAmber else RadioBorder)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (state.isAudioDeafened) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                    contentDescription = "Audio Deafen",
                    tint = if (state.isAudioDeafened) RadioLcdAmber else Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.size(3.dp))
                Text(
                    text = if (state.isAudioDeafened) "DEAF [#]" else "SPK [#]",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (state.isAudioDeafened) RadioLcdAmber else Color.White
                )
            }

            // Speaker / Headset Mode Toggle
            OutlinedButton(
                onClick = onToggleAudioOutput,
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .testTag("toggle_audio_output"),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = RadioSurfaceVariant),
                shape = RoundedCornerShape(4.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(RadioBorder)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (settings.audioOutput == AudioOutputMode.SPEAKER) Icons.Default.VolumeUp else Icons.Default.Headphones,
                    contentDescription = "Audio Route",
                    tint = RadioLcdText,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.size(3.dp))
                Text(
                    text = if (settings.audioOutput == AudioOutputMode.SPEAKER) "SPEAKER" else "EARPIECE",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // 5. Active Channel Users List (Compact for QVGA)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(RadioSurface)
                .border(1.dp, RadioBorder, RoundedCornerShape(6.dp))
                .padding(6.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "USERS ON CHANNEL (${state.usersInCurrentChannel.size})",
                        color = RadioLcdAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "PING: 24ms",
                        color = RadioLcdText,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(state.usersInCurrentChannel) { user ->
                        val isUserTalking = user.isTalking || (user.isSelf && state.isTransmitting) || (user.name == state.activeSpeakerName)
                        val rowBg = if (isUserTalking) Color(0x3300E676) else RadioSurfaceVariant

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(rowBg)
                                .border(
                                    width = if (isUserTalking) 1.dp else 0.dp,
                                    color = if (isUserTalking) RadioRxGreen else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Talking indicator bulb
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isUserTalking) RadioRxGreen else Color(0xFF546E7A))
                                )
                                Text(
                                    text = user.name,
                                    color = if (isUserTalking) RadioRxGreen else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isUserTalking) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (isUserTalking) {
                                    Text(
                                        text = "[TALKING]",
                                        color = RadioRxGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = "${user.pingMs}ms",
                                    color = Color(0xFF78909C),
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Keypad Navigation Shortcut Tips for Osaka Q8
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF13171D))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Keypad: [1-8] Jump CH | [*] Mute | [#] Deafen | PTT Samping",
                color = Color(0xFF90A4AE),
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
