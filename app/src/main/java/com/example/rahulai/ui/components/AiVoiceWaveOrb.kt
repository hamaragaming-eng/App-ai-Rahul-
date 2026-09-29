package com.example.rahulai.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.VioletPulse

@Composable
fun AiVoiceWaveOrb(
    isListening: Boolean,
    isSpeaking: Boolean,
    isProcessing: Boolean,
    audioRms: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val waveRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_rotation"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    val activeColor = when {
        isListening -> Color(0xFFEF4444) // Live listening red/coral
        isSpeaking -> NeonCyan          // Rahul Speaking cyan
        isProcessing -> VioletPulse     // AI Neural processing violet
        else -> ElectricBlue            // Idle ready blue
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .clickable { onClick() }
                .testTag("ai_voice_orb")
        ) {
            // Background Canvas animated rings & waves
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = (size.minDimension / 2f) * 0.65f

                // Outer animated aura rings
                val dynamicRadius = if (isListening) {
                    baseRadius + (audioRms.coerceIn(0f, 15f) * 2.5f)
                } else if (isSpeaking || isProcessing) {
                    baseRadius * pulseScale
                } else {
                    baseRadius
                }

                // Ambient glow circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            activeColor.copy(alpha = if (isListening || isSpeaking) 0.35f else 0.15f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.width / 2f
                    ),
                    radius = size.width / 2f,
                    center = center
                )

                // Outer pulsing stroke ring
                drawCircle(
                    color = activeColor.copy(alpha = ringAlpha * 0.6f),
                    radius = dynamicRadius + 14f,
                    center = center,
                    style = Stroke(width = 2.5f)
                )

                // Secondary ring
                drawCircle(
                    color = activeColor.copy(alpha = ringAlpha * 0.35f),
                    radius = dynamicRadius + 28f,
                    center = center,
                    style = Stroke(width = 1.5f)
                )

                // Core orb gradient
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            activeColor,
                            Color(0xFF0F172A)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    radius = baseRadius,
                    center = center
                )
            }

            // Center Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(68.dp)
                    .background(Color(0xFF070D1B).copy(alpha = 0.85f), CircleShape)
                    .border(2.dp, activeColor.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    imageVector = when {
                        isListening -> Icons.Default.Mic
                        isSpeaking -> Icons.Default.RecordVoiceOver
                        isProcessing -> Icons.Default.GraphicEq
                        else -> Icons.Default.SmartToy
                    },
                    contentDescription = "Voice Assistant State",
                    tint = activeColor,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // State Pill
        val statusText = when {
            isListening -> "माया सुन रही है... बोलिए ❤️"
            isSpeaking -> "माया बोल रही है... 💖"
            isProcessing -> "माया सोच रही है... ✨"
            else -> "Tap Orb / माया से बात करें"
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(SurfaceVariantDark, RoundedCornerShape(20.dp))
                .border(1.dp, activeColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(activeColor, CircleShape)
            )
            Text(
                text = statusText,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
