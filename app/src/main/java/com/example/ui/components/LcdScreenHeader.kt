package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioOutputMode
import com.example.model.ConnectionState
import com.example.model.RadioState
import com.example.ui.theme.RadioBorder
import com.example.ui.theme.RadioLcdAmber
import com.example.ui.theme.RadioLcdBg
import com.example.ui.theme.RadioLcdBorder
import com.example.ui.theme.RadioLcdCyan
import com.example.ui.theme.RadioLcdText
import com.example.ui.theme.RadioRxGreen
import com.example.ui.theme.RadioStandby
import com.example.ui.theme.RadioTxRed

@Composable
fun LcdScreenHeader(
    state: RadioState,
    audioOutputMode: AudioOutputMode,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(RadioLcdBg)
            .border(1.5.dp, RadioLcdBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Top Row: Server Name & Connectivity Status & Battery/Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Connection indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val connIcon = if (state.connectionState == ConnectionState.CONNECTED) Icons.Default.Wifi else Icons.Default.WifiOff
                    val connColor = if (state.connectionState == ConnectionState.CONNECTED) RadioLcdText else RadioTxRed
                    Icon(
                        imageVector = connIcon,
                        contentDescription = "Connection Status",
                        tint = connColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = state.activeServer?.name?.take(16) ?: "MUMBLE NET",
                        color = connColor,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Audio Output Mode & Volume
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (state.isMicMuted) {
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = "Mic Muted",
                            tint = RadioTxRed,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    if (state.isAudioDeafened) {
                        Icon(
                            imageVector = Icons.Default.VolumeMute,
                            contentDescription = "Audio Deafened",
                            tint = RadioLcdAmber,
                            modifier = Modifier.size(11.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Volume",
                            tint = RadioLcdText,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Text(
                        text = "${state.volumeLevel}%",
                        color = RadioLcdText,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (audioOutputMode == AudioOutputMode.SPEAKER) "[SPK]" else "[EAR]",
                        color = RadioLcdCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Main Channel & Transmit Status Row (Bold Osaka Q8 Walkie Talkie Display)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Channel Number and Name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CH-${String.format("%02d", state.currentChannel.number)}: ${state.currentChannel.name}",
                        color = RadioLcdText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = state.currentChannel.description.ifBlank { "Mumble Voice Channel" },
                        color = RadioStandby,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Live TX / RX / STBY Badge
                val (badgeText, badgeBg, badgeTextColor) = when {
                    state.isTransmitting -> Triple("TX ON", RadioTxRed, Color.White)
                    state.activeSpeakerName != null -> Triple("RX ON", RadioRxGreen, Color.Black)
                    else -> Triple("STANDBY", RadioBorder, Color.LightGray)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        color = badgeTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
