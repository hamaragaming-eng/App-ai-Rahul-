package com.example.rahulai.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.rahulai.data.ai.ChatMessage
import com.example.rahulai.data.ai.MessageSender
import com.example.rahulai.ui.components.AiVoiceWaveOrb
import com.example.rahulai.ui.viewmodel.RahulAiViewModel
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
fun AssistantScreen(
    viewModel: RahulAiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()
    val speechRecognitionAvailable by viewModel.speechManager.speechRecognitionAvailable.collectAsState()
    val screenTorchColor by viewModel.screenTorchColor.collectAsState()

    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var voiceOutputEnabled by remember { mutableStateOf(viewModel.speechManager.isVoiceOutputEnabled) }
    var showVoiceChooserDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    fun handleMicClick() {
        if (isListening) {
            viewModel.stopListening()
        } else {
            if (!speechRecognitionAvailable) {
                showVoiceChooserDialog = true
                return
            }
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                viewModel.startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestionChips = listOf(
        "माया, मुझसे मीठी-मीठी बातें करो ❤️",
        "एक प्यारी सी शायरी सुनाओ 🌹",
        "फोन की टॉर्च जला दो माया ✨",
        "फोन की रैम क्लीन करो 🚀",
        "रोमांटिक डेट मोड लगाओ 🕯️",
        "सेल्फी के लिए कैमरा खोलो 📸",
        "फोन साइलेंट कर दो 🤫",
        "बैटरी कितनी बची है माया? 🔋",
        "व्हाट्सऐप खोलो 💬",
        "एक प्यारा सा जोक सुनाओ 😉"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFFF2A6D), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Maya AI • आपकी प्यारी साथी 💖",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            voiceOutputEnabled = viewModel.toggleSpeechOutput()
                        },
                        modifier = Modifier.testTag("toggle_voice_output_btn")
                    ) {
                        Icon(
                            imageVector = if (voiceOutputEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Toggle Voice Output",
                            tint = if (voiceOutputEnabled) Color(0xFFFF2A6D) else TextMuted
                        )
                    }
                    IconButton(
                        onClick = { viewModel.clearHistory() },
                        modifier = Modifier.testTag("clear_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )

            // Dynamic Romantic Voice Orb & Waveform
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                SurfaceDark,
                                DarkNavy.copy(alpha = 0.6f)
                            )
                        )
                    )
            ) {
                AiVoiceWaveOrb(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    isProcessing = isProcessing,
                    audioRms = audioRms,
                    onClick = { handleMicClick() }
                )
            }

            // Chat Messages History
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(messages) { message ->
                    ChatMessageBubble(msg = message)
                }
            }

            // Quick romantic suggestions row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFF2A6D).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF2A6D)),
                        modifier = Modifier
                            .clickable { showVoiceChooserDialog = true }
                            .testTag("voice_chooser_suggestion_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = "Phone Commands",
                                tint = Color(0xFFFF2A6D),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Phone Commands 🎙️",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                items(suggestionChips) { chipText ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceVariantDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, VioletPulse.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable {
                            viewModel.processVoiceOrTextInput(chipText)
                        }
                    ) {
                        Text(
                            text = chipText,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Bottom Input Controls & Microphone Bar
            Surface(
                color = SurfaceDark,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Voice Mic Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (isListening) listOf(Color(0xFFFF2A6D), Color(0xFFFF5277))
                                    else listOf(Color(0xFFFF2A6D), VioletPulse)
                                )
                            )
                            .clickable { handleMicClick() }
                            .testTag("mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Text Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (isListening) "आपकी माया सुन रही है... बोलिए ❤️" else "माया से प्यार से बोलिए या लिखिए...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF2A6D),
                            unfocusedBorderColor = VioletPulse.copy(alpha = 0.3f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = Color(0xFFFF2A6D)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("user_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val textToSend = inputText
                                inputText = ""
                                viewModel.processVoiceOrTextInput(textToSend)
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) Color(0xFFFF2A6D) else TextMuted
                        )
                    }
                }
            }
        }

        // Ambient Screen Torch Overlay if activated
        AnimatedVisibility(
            visible = screenTorchColor != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val torchColor = screenTorchColor ?: Color.Transparent
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(torchColor)
                    .clickable { viewModel.setScreenTorch(null) }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Romantic Screen Torch Active 🕯️",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Screen par tap karein band karne ke liye",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    if (showVoiceChooserDialog) {
        VoiceCommandChooserDialog(
            onDismiss = { showVoiceChooserDialog = false },
            onCommandSelected = { cmd ->
                showVoiceChooserDialog = false
                viewModel.processVoiceOrTextInput(cmd)
            }
        )
    }
}

@Composable
fun VoiceCommandChooserDialog(
    onDismiss: () -> Unit,
    onCommandSelected: (String) -> Unit
) {
    val commandGroups = listOf(
        "❤️ Romantic & Dil Ki Baatein" to listOf(
            "माया, मुझसे अच्छी-अच्छी बातें करो",
            "एक प्यारी सी शायरी सुनाओ",
            "I love you Maya ❤️",
            "तुम कौन हो माया?",
            "कैसी हो मेरी जान?",
            "Aap mere liye kya kar sakte ho?",
            "Ek meetha sa joke sunao"
        ),
        "🔦 Flashlight & Screen Torch" to listOf(
            "Torch jalao meri jaan",
            "Torch band kar do",
            "Emergency SOS flashlight on karo",
            "Phone flashlight toggle karo"
        ),
        "🚀 Phone Speed & Performance" to listOf(
            "Phone boost karo",
            "RAM clean karo",
            "Battery status check karo",
            "Phone ki health batao"
        ),
        "🔊 Volume & Sound Profiles" to listOf(
            "Volume full 100 percent karo",
            "Volume kam karo",
            "Phone vibrate mode pe daalo",
            "Phone silent kar do",
            "Ringtone normal mode karo"
        ),
        "📱 Mobile Apps & Launchers" to listOf(
            "Camera open karo selfie ke liye",
            "WhatsApp kholo",
            "YouTube chalao",
            "Phone dialer open karo",
            "Calculator open karo",
            "Clock aur alarm open karo"
        ),
        "💖 Romantic Modes & Routines" to listOf(
            "Romantic Date Mode active karo",
            "Gaming Turbo Mode on karo",
            "Super Power Saver mode laga do",
            "Good Night mode on karo"
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Maya AI • Voice Commands 📱💖",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                commandGroups.forEach { (header, cmds) ->
                    item {
                        Text(
                            text = header,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF2A6D),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    items(cmds) { cmd ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCommandSelected(cmd) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    Icons.Default.RecordVoiceOver,
                                    contentDescription = "Speak",
                                    tint = Color(0xFFFF2A6D),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = cmd,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
fun ChatMessageBubble(msg: ChatMessage) {
    val isUser = msg.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF2A6D).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFFFF2A6D), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Maya AI",
                    tint = Color(0xFFFF2A6D),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) ElectricBlue.copy(alpha = 0.25f) else SurfaceVariantDark
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) ElectricBlue.copy(alpha = 0.5f) else Color(0xFFFF2A6D).copy(alpha = 0.35f)
            ),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!isUser && msg.actionDetail != null) {
                    Text(
                        text = "📱 Phone: ${msg.actionDetail}",
                        color = Color(0xFFFF2A6D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = msg.text,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
