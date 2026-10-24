package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RadioBorder
import com.example.ui.theme.RadioLcdAmber
import com.example.ui.theme.RadioLcdBg
import com.example.ui.theme.RadioRxGreen
import com.example.ui.theme.RadioTxRed

@Composable
fun VuMeterBar(
    audioLevel: Float, // 0..100
    isTx: Boolean,
    label: String = if (isTx) "MIC VU" else "AUDIO RX",
    modifier: Modifier = Modifier
) {
    val totalSegments = 12
    val activeSegments = ((audioLevel / 100f) * totalSegments).toInt().coerceIn(0, totalSegments)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(RadioLcdBg)
            .border(1.dp, RadioBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (isTx) RadioTxRed else RadioRxGreen
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until totalSegments) {
                val isActive = i < activeSegments
                val segmentColor = when {
                    !isActive -> Color(0xFF1E2620)
                    i < 7 -> RadioRxGreen
                    i < 10 -> RadioLcdAmber
                    else -> RadioTxRed
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(segmentColor)
                )
            }
        }

        Text(
            text = "${audioLevel.toInt()}%",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.LightGray
        )
    }
}
