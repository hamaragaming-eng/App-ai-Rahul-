package com.example.rahulai.ui.screens

import android.media.AudioManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rahulai.ui.viewmodel.RahulAiViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPulse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemControlScreen(
    viewModel: RahulAiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val isSosActive by viewModel.isSosActive.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val volumeInfo by viewModel.volumeInfo.collectAsState()
    val hardwareInfo by viewModel.hardwareInfo.collectAsState()
    val screenTorchColor by viewModel.screenTorchColor.collectAsState()

    var boostNotice by remember { mutableStateOf<String?>(null) }

    val mediaPct = if (volumeInfo.maxMediaVolume > 0) {
        (volumeInfo.mediaVolume * 100 / volumeInfo.maxMediaVolume)
    } else 50

    val ringPct = if (volumeInfo.maxRingVolume > 0) {
        (volumeInfo.ringVolume * 100 / volumeInfo.maxRingVolume)
    } else 50

    val alarmPct = if (volumeInfo.maxAlarmVolume > 0) {
        (volumeInfo.alarmVolume * 100 / volumeInfo.maxAlarmVolume)
    } else 50

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "Phone Hardware & Control Center",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Flashlight, SOS & Romantic Screen Torch
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFlashlightOn || isSosActive) SurfaceVariantDark else SurfaceDark
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isFlashlightOn) AccentAmber else if (isSosActive) AccentRose else Color(0xFF1F2B42)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("flashlight_control_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isFlashlightOn) AccentAmber
                                        else if (isSosActive) AccentRose
                                        else Color(0xFF1E293B)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isFlashlightOn || isSosActive) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                    contentDescription = "Flashlight",
                                    tint = if (isFlashlightOn || isSosActive) DarkNavy else TextMuted,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isSosActive) "SOS Emergency Strobe" else "Camera LED Torch",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isSosActive) "Morse SOS pulse active" else if (isFlashlightOn) "Torch ON • Roshni Chalu" else "Torch OFF",
                                    color = if (isSosActive) AccentRose else if (isFlashlightOn) AccentAmber else TextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.toggleFlashlight() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFlashlightOn) AccentAmber else ElectricBlue,
                                    contentColor = DarkNavy
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("toggle_flashlight_btn")
                            ) {
                                Text(
                                    text = if (isFlashlightOn) "OFF" else "ON",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // SOS and Screen Torch Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.toggleSosFlashlight() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isSosActive) AccentRose else TextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "SOS", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isSosActive) "Stop SOS" else "SOS Strobe", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.setScreenTorch(Color(0xFFFF2A6D))
                                    Toast.makeText(context, "Romantic Screen Torch Active ❤️", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF2A6D)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = "Screen Glow", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Screen Torch 🌹", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 2. RAM Cleaner & Phone Performance Booster
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2B42)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(NeonCyan.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "RAM Speed",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Phone RAM & Performance",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Used: ${hardwareInfo.usedRamMb}MB / Total: ${hardwareInfo.totalRamMb}MB",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val res = viewModel.boostPhoneAndRam()
                                    boostNotice = "🚀 Boosted! ${res.freedRamMb}MB RAM aur ${res.freedCacheMb}MB cache saaf kiya gaya!"
                                    Toast.makeText(context, boostNotice, Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = DarkNavy
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("boost_phone_btn")
                            ) {
                                Icon(Icons.Default.CleaningServices, contentDescription = "Clean", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Boost 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val ramPct = if (hardwareInfo.totalRamMb > 0) {
                            hardwareInfo.usedRamMb.toFloat() / hardwareInfo.totalRamMb
                        } else 0.5f

                        LinearProgressIndicator(
                            progress = { ramPct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (ramPct > 0.8f) AccentRose else NeonCyan,
                            trackColor = Color(0xFF1E293B)
                        )

                        if (boostNotice != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = boostNotice!!,
                                color = AccentEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 3. Audio, Volume & Ringer Profiles
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2B42)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ElectricBlue.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Volume",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Audio & Sound Profiles",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            }

                            Row {
                                OutlinedButton(
                                    onClick = { viewModel.muteMedia() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("mute_button")
                                ) {
                                    Text("Mute", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { viewModel.maxMedia() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricBlue,
                                        contentColor = DarkNavy
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Max 100%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Media Volume
                        Text(text = "Media Volume: $mediaPct%", fontSize = 12.sp, color = TextSecondary)
                        Slider(
                            value = mediaPct.toFloat(),
                            onValueChange = { viewModel.setMediaVolume(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricBlue,
                                activeTrackColor = ElectricBlue,
                                inactiveTrackColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Ring Volume
                        Text(text = "Ringtone Volume: $ringPct%", fontSize = 12.sp, color = TextSecondary)
                        Slider(
                            value = ringPct.toFloat(),
                            onValueChange = { viewModel.setRingVolume(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sound Mode Selector Buttons
                        Text(text = "Ringer Mode:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.setRingerMode(AudioManager.RINGER_MODE_NORMAL) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (volumeInfo.ringerMode == AudioManager.RINGER_MODE_NORMAL) ElectricBlue else Color(0xFF1E293B),
                                    contentColor = if (volumeInfo.ringerMode == AudioManager.RINGER_MODE_NORMAL) DarkNavy else TextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Normal", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Normal", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.setRingerMode(AudioManager.RINGER_MODE_VIBRATE) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (volumeInfo.ringerMode == AudioManager.RINGER_MODE_VIBRATE) VioletPulse else Color(0xFF1E293B),
                                    contentColor = if (volumeInfo.ringerMode == AudioManager.RINGER_MODE_VIBRATE) Color.White else TextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Vibration, contentDescription = "Vibrate", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Vibrate", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.setRingerMode(AudioManager.RINGER_MODE_SILENT) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (volumeInfo.ringerMode == AudioManager.RINGER_MODE_SILENT) AccentRose else Color(0xFF1E293B),
                                    contentColor = if (volumeInfo.ringerMode == AudioManager.RINGER_MODE_SILENT) Color.White else TextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.NotificationsOff, contentDescription = "Silent", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Silent", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 4. Romantic Heartbeat Haptics & Love Touch
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF2A6D).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF2A6D).copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Heartbeat",
                                tint = Color(0xFFFF2A6D),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Romantic Heartbeat Haptic",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Phone par dil ki dhadkan mehsus karein ❤️",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.vibrateHeartbeat() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF2A6D),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dhadkan 💓", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 5. Battery & Hardware Telemetry Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2B42)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("battery_info_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AccentEmerald.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                    contentDescription = "Battery",
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Battery: ${batteryInfo.level}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (batteryInfo.isCharging) "Charging active • ${batteryInfo.health}" else "Discharging • Health: ${batteryInfo.health}",
                                    fontSize = 12.sp,
                                    color = if (batteryInfo.isCharging) AccentEmerald else TextMuted
                                )
                            }

                            Text(
                                text = "${batteryInfo.temperatureC}°C",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = AccentAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Device Details Grid
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Device Model:", fontSize = 11.sp, color = TextMuted)
                                Text("${hardwareInfo.brand} ${hardwareInfo.model}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            Column {
                                Text("Android OS:", fontSize = 11.sp, color = TextMuted)
                                Text("v${hardwareInfo.androidVersion} (API ${hardwareInfo.sdkInt})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            Column {
                                Text("Free Storage:", fontSize = 11.sp, color = TextMuted)
                                Text("${hardwareInfo.freeStorageGb}GB / ${hardwareInfo.totalStorageGb}GB", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NeonCyan)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}
