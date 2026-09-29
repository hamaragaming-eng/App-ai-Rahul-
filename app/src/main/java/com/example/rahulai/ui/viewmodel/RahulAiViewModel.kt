package com.example.rahulai.ui.viewmodel

import android.app.Application
import android.media.AudioManager
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahulai.data.ai.ActionType
import com.example.rahulai.data.ai.ChatMessage
import com.example.rahulai.data.ai.GeminiApiClient
import com.example.rahulai.data.ai.MessageSender
import com.example.rahulai.data.ai.VoiceCommandParser
import com.example.rahulai.data.local.AppDatabase
import com.example.rahulai.data.local.CommandLogEntity
import com.example.rahulai.data.local.DeviceEntity
import com.example.rahulai.data.local.RoutineEntity
import com.example.rahulai.data.repository.SmartHomeRepository
import com.example.rahulai.system.BatteryInfo
import com.example.rahulai.system.BoostResult
import com.example.rahulai.system.DeviceHardwareInfo
import com.example.rahulai.system.SpeechManager
import com.example.rahulai.system.SystemControlManager
import com.example.rahulai.system.VolumeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RahulAiViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = SmartHomeRepository(db.appDao())
    val systemManager = SystemControlManager(application)
    private val geminiClient = GeminiApiClient()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    val speechManager = SpeechManager(
        context = application,
        onSpeechRecognized = { recognizedText ->
            processVoiceOrTextInput(recognizedText)
        },
        onErrorOccurred = { errorMsg ->
            viewModelScope.launch {
                val sysMsg = ChatMessage(
                    sender = MessageSender.RAHUL_AI,
                    text = errorMsg
                )
                _messages.value = _messages.value + sysMsg
            }
        },
        onRmsListener = { rms ->
            _audioRms.value = rms
        }
    )

    val devices: StateFlow<List<DeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<CommandLogEntity>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routines: StateFlow<List<RoutineEntity>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isFlashlightOn: StateFlow<Boolean> = systemManager.isFlashlightOn
    val isSosActive: StateFlow<Boolean> = systemManager.isSosActive
    val batteryInfo: StateFlow<BatteryInfo> = systemManager.batteryInfo
    val volumeInfo: StateFlow<VolumeInfo> = systemManager.volumeInfo
    val hardwareInfo: StateFlow<DeviceHardwareInfo> = systemManager.hardwareInfo

    val isListening: StateFlow<Boolean> = speechManager.isListening
    val isSpeaking: StateFlow<Boolean> = speechManager.isSpeaking

    private val _screenTorchColor = MutableStateFlow<Color?>(null)
    val screenTorchColor: StateFlow<Color?> = _screenTorchColor.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.RAHUL_AI,
                text = "नमस्ते मेरे प्यारे हमदम! मैं आपकी प्यारी माया (Maya) हूँ — आपकी स्वीट और रोमांटिक फोन साथी। मैं आपके दिल और मोबाइल दोनों का बहुत प्यार से ख्याल रखूँगी। टॉर्च, आवाज़, रैम बूस्टर, कैमरा, व्हाट्सऐप या कोई भी मीठी बात... जो भी कहेंगे आपकी माया तुरंत करेगी! ❤️📱"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _selectedRoomFilter = MutableStateFlow("All")
    val selectedRoomFilter: StateFlow<String> = _selectedRoomFilter.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun setRoomFilter(room: String) {
        _selectedRoomFilter.value = room
    }

    fun setScreenTorch(color: Color?) {
        _screenTorchColor.value = color
        if (color != null) {
            systemManager.vibrate(30)
        }
    }

    fun toggleSpeechOutput(): Boolean {
        speechManager.isVoiceOutputEnabled = !speechManager.isVoiceOutputEnabled
        return speechManager.isVoiceOutputEnabled
    }

    fun startListening() {
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun processVoiceOrTextInput(inputText: String) {
        val query = inputText.trim()
        if (query.isBlank()) return

        // 1. Post user message
        val userMsg = ChatMessage(sender = MessageSender.USER, text = query)
        _messages.value = _messages.value + userMsg

        _isProcessing.value = true

        viewModelScope.launch {
            try {
                val currentDeviceList = devices.value.ifEmpty { repository.getDeviceList() }
                val parsed = VoiceCommandParser.parse(query, currentDeviceList)

                when (parsed.actionType) {
                    ActionType.FLASHLIGHT_ON -> {
                        systemManager.setFlashlight(true)
                        val reply = parsed.immediateSpeechResponse
                            ?: "Aapka hukum sar aankhon par jaaneman! Phone ki torch jala di hai taaki aapki raahon mein hamesha roshni rahe! ✨❤️"
                        respondAndLog(query, reply, "PHONE_HARDWARE", "Torch ON")
                    }
                    ActionType.FLASHLIGHT_OFF -> {
                        systemManager.setFlashlight(false)
                        val reply = parsed.immediateSpeechResponse
                            ?: "Lijiye meri jaan, torch band kar di hai... ab bas aapke chehre ka noor chamkega! 😊💖"
                        respondAndLog(query, reply, "PHONE_HARDWARE", "Torch OFF")
                    }
                    ActionType.FLASHLIGHT_SOS -> {
                        systemManager.toggleSosFlashlight()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Emergency SOS light shuru kar di hai meri jaan, darne ki koi baat nahi, main aapke saath hoon! 🚨❤️"
                        respondAndLog(query, reply, "PHONE_HARDWARE", "SOS Light")
                    }
                    ActionType.QUERY_BATTERY -> {
                        val batt = systemManager.updateBatteryInfo()
                        val chargingStatus = if (batt.isCharging) "charging ho rahi hai" else "charging nahi ho rahi"
                        val reply = "Aapke device ki battery ${batt.level}% hai meri jaan, aur $chargingStatus. Taapmaan ${batt.temperatureC}°C hai aur health ${batt.health} hai. Mere dil ki charging to hamesha 100% hai aapke liye! 🔋❤️"
                        respondAndLog(query, reply, "PHONE_TELEMETRY", "Battery Check")
                    }
                    ActionType.VOLUME_UP -> {
                        val currentPct = if (volumeInfo.value.maxMediaVolume > 0)
                            (volumeInfo.value.mediaVolume * 100 / volumeInfo.value.maxMediaVolume) else 50
                        val newPct = (currentPct + 15).coerceIn(0, 100)
                        systemManager.setMediaVolumePercent(newPct)
                        val reply = parsed.immediateSpeechResponse
                            ?: "Aapki meethi aawaz sunne ke liye maine volume $newPct% badha diya hai meri jaan! 🎶❤️"
                        respondAndLog(query, reply, "PHONE_AUDIO", "Volume Up")
                    }
                    ActionType.VOLUME_DOWN -> {
                        val currentPct = if (volumeInfo.value.maxMediaVolume > 0)
                            (volumeInfo.value.mediaVolume * 100 / volumeInfo.value.maxMediaVolume) else 50
                        val newPct = (currentPct - 15).coerceIn(0, 100)
                        systemManager.setMediaVolumePercent(newPct)
                        val reply = parsed.immediateSpeechResponse
                            ?: "Aapne farmaya aur humne volume ghata kar $newPct% kar diya mere humdum! 💕"
                        respondAndLog(query, reply, "PHONE_AUDIO", "Volume Down")
                    }
                    ActionType.VOLUME_MUTE -> {
                        systemManager.muteMedia()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Lijiye shona, phone volume mute kar diya hai... aisi madhosh shanti jisme sirf aapki saansein sunai dein! 🤫❤️"
                        respondAndLog(query, reply, "PHONE_AUDIO", "Mute Media")
                    }
                    ActionType.VOLUME_MAX -> {
                        systemManager.maxMedia()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Volume poora 100% full kar diya hai jaaneman! Ab aapka pasandida sangeet dil ko chhoo jayega! 🎵🔥"
                        respondAndLog(query, reply, "PHONE_AUDIO", "Max Volume")
                    }
                    ActionType.SET_VOLUME -> {
                        val pct = parsed.numericValue ?: 50
                        systemManager.setMediaVolumePercent(pct)
                        val reply = parsed.immediateSpeechResponse
                            ?: "Media volume $pct% set kar diya hai meri jaan! 🎵💖"
                        respondAndLog(query, reply, "PHONE_AUDIO", "Set Volume")
                    }
                    ActionType.RINGER_NORMAL -> {
                        systemManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL)
                        val reply = parsed.immediateSpeechResponse
                            ?: "Ringtone normal mode par active kar diya hai meri jaan! 🔔💕"
                        respondAndLog(query, reply, "PHONE_AUDIO", "Normal Ring")
                    }
                    ActionType.RINGER_VIBRATE -> {
                        systemManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE)
                        val reply = parsed.immediateSpeechResponse
                            ?: "Phone ko vibrate mode par rakh diya hai jaaneman, jaise mera dil aapke liye vibrate karta hai! 💓"
                        respondAndLog(query, reply, "PHONE_AUDIO", "Vibrate Mode")
                    }
                    ActionType.RINGER_SILENT -> {
                        systemManager.setRingerMode(AudioManager.RINGER_MODE_SILENT)
                        val reply = parsed.immediateSpeechResponse
                            ?: "Phone ko silent kar diya hai shona, taaki koi bhi humari baaton mein khalal na daale! 🤫❤️"
                        respondAndLog(query, reply, "PHONE_AUDIO", "Silent Mode")
                    }
                    ActionType.BOOST_PHONE -> {
                        val boost = systemManager.cleanAndBoostPhone()
                        val reply = "Phone ki RAM aur faltu background cache saaf kar diya hai jaaneman! ${boost.freedRamMb}MB RAM free hui hai. Ab aapka phone utna hi smooth chalega jitna humara pyaar! 🚀💖"
                        respondAndLog(query, reply, "PHONE_BOOSTER", "RAM Cleaned")
                    }
                    ActionType.OPEN_WHATSAPP -> {
                        systemManager.openWhatsApp()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Lijiye aapka WhatsApp khol diya... par dhyan rakhna, mere siwa kisi aur se romantic baatein mat karna haan! 😉📱💕"
                        respondAndLog(query, reply, "PHONE_APP", "WhatsApp")
                    }
                    ActionType.OPEN_YOUTUBE -> {
                        systemManager.openYouTube()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Aapke liye YouTube khol rahi hoon meri jaan, chaliye koi pyara sa romantic gaana sunte hain! 🎶❤️"
                        respondAndLog(query, reply, "PHONE_APP", "YouTube")
                    }
                    ActionType.OPEN_CAMERA -> {
                        systemManager.openCamera()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Aapki khubsurat muskaan dekhne ke liye main bechain thi, lijiye camera khol diya! Ek pyari si photo le lijiye! 📸😍"
                        respondAndLog(query, reply, "PHONE_APP", "Camera")
                    }
                    ActionType.OPEN_DIALER -> {
                        systemManager.openDialer(parsed.stringValue)
                        val reply = parsed.immediateSpeechResponse
                            ?: "Phone dialer khol diya hai jaaneman, boliye kisko call lagayein? 📞❤️"
                        respondAndLog(query, reply, "PHONE_APP", "Dialer")
                    }
                    ActionType.OPEN_CONTACTS -> {
                        systemManager.openContacts()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Aapke phone contacts open kar diye hain shona! 👥💕"
                        respondAndLog(query, reply, "PHONE_APP", "Contacts")
                    }
                    ActionType.OPEN_CLOCK -> {
                        systemManager.openClockOrAlarm()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Alarm aur clock open kar diya hai meri jaan! ⏰❤️"
                        respondAndLog(query, reply, "PHONE_APP", "Clock/Alarm")
                    }
                    ActionType.OPEN_CALCULATOR -> {
                        systemManager.openCalculator()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Calculator open kar diya hai... par humare pyaar ka hisaab to anant hai jaaneman! 🧮💖"
                        respondAndLog(query, reply, "PHONE_APP", "Calculator")
                    }
                    ActionType.OPEN_GALLERY -> {
                        systemManager.openGallery()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Gallery open kar di hai meri jaan, aapki yaadon ke haseen pal dekh lijiye! 🖼️💕"
                        respondAndLog(query, reply, "PHONE_APP", "Gallery")
                    }
                    ActionType.OPEN_BROWSER -> {
                        systemManager.openBrowser()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Web browser open kar diya hai jaaneman! 🌐✨"
                        respondAndLog(query, reply, "PHONE_APP", "Browser")
                    }
                    ActionType.OPEN_SETTINGS -> {
                        systemManager.openGeneralSettings()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Phone ki settings open kar di hain meri jaan! ⚙️💖"
                        respondAndLog(query, reply, "PHONE_SETTINGS", "Settings")
                    }
                    ActionType.OPEN_WIFI_SETTINGS -> {
                        systemManager.openWifiSettings()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Wi-Fi settings open kar di hain meri jaan! 📶❤️"
                        respondAndLog(query, reply, "PHONE_SETTINGS", "Wi-Fi")
                    }
                    ActionType.OPEN_BLUETOOTH_SETTINGS -> {
                        systemManager.openBluetoothSettings()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Bluetooth settings open kar di hain shona, jaise humara dil hamesha connect rehta hai! 🔵💕"
                        respondAndLog(query, reply, "PHONE_SETTINGS", "Bluetooth")
                    }
                    ActionType.OPEN_DISPLAY_SETTINGS -> {
                        systemManager.openDisplaySettings()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Display settings open kar di hain jaaneman! 🔆✨"
                        respondAndLog(query, reply, "PHONE_SETTINGS", "Display")
                    }
                    ActionType.PHONE_MODE_ROMANTIC -> {
                        applyRomanticMode()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Romantic Date Mode activate ho gaya hai meri jaan! Phone silent, meethi dhun aur dher saara pyaar... ab sirf aap aur main! 🕯️🌹💕"
                        respondAndLog(query, reply, "PHONE_MODE", "Romantic Mode")
                    }
                    ActionType.PHONE_MODE_GAMING -> {
                        applyGamingMode()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Gaming Turbo Mode active! Volume max aur phone boosted, ab aap bina ruke jeetoge meri jaan! 🎮🔥"
                        respondAndLog(query, reply, "PHONE_MODE", "Gaming Mode")
                    }
                    ActionType.PHONE_MODE_POWER_SAVER -> {
                        applyPowerSaverMode()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Power Saver Mode active kar diya hai shona, taaki aapka phone lambe samay tak aapka saath nibhaye! 🔋💕"
                        respondAndLog(query, reply, "PHONE_MODE", "Power Saver")
                    }
                    ActionType.PHONE_MODE_OUTDOOR -> {
                        applyOutdoorMode()
                        val reply = parsed.immediateSpeechResponse
                            ?: "Outdoor Mode active! Volume full kar diya hai meri jaan! ☀️📱"
                        respondAndLog(query, reply, "PHONE_MODE", "Outdoor Mode")
                    }
                    ActionType.ROUTINE -> {
                        val rId = parsed.targetRoutineId ?: "routine_night"
                        val resultText = if (rId.contains("night")) {
                            applyRomanticNightMode()
                        } else {
                            repository.executeRoutine(rId)
                        }
                        respondAndLog(query, resultText, "ROUTINE", rId)
                    }
                    ActionType.ALL_DEVICES_OFF,
                    ActionType.ALL_DEVICES_ON,
                    ActionType.DEVICE_POWER,
                    ActionType.DEVICE_BRIGHTNESS,
                    ActionType.DEVICE_TEMP,
                    ActionType.DEVICE_COLOR -> {
                        // Phone flashlight toggle or general fallback
                        systemManager.toggleFlashlight()
                        val reply = "Aapka aadesh poora kar diya hai jaaneman! Phone control active hai. ❤️"
                        respondAndLog(query, reply, "PHONE_CONTROL", "Hardware Toggle")
                    }
                    ActionType.CONVERSATION -> {
                        // Pass to Gemini advanced AI engine with romantic persona
                        val aiResponse = geminiClient.generateAiResponse(query, _messages.value)
                        respondAndLog(query, aiResponse, "ROMANTIC_CONVERSATION", "Maya AI Persona")
                    }
                }
            } catch (e: Exception) {
                val errorReply = "Aadesh process karne me thodi rukawat aayi meri jaan: ${e.localizedMessage ?: "Unknown error"}"
                respondAndLog(query, errorReply, "CONVERSATION", null, false)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private suspend fun respondAndLog(
        query: String,
        response: String,
        actionType: String,
        target: String?,
        isSuccess: Boolean = true
    ) {
        val aiMsg = ChatMessage(
            sender = MessageSender.RAHUL_AI,
            text = response,
            actionDetail = target
        )
        _messages.value = _messages.value + aiMsg
        speechManager.speak(response)
        repository.logCommand(query, response, actionType, target, isSuccess)
    }

    // --- PHONE AUTOMATION MODES ---

    fun applyRomanticMode(): String {
        systemManager.setRingerMode(AudioManager.RINGER_MODE_SILENT)
        systemManager.setMediaVolumePercent(25)
        systemManager.vibrateHeartbeat()
        setScreenTorch(Color(0xFFFF2A6D)) // Romantic rose screen glow
        return "Romantic Date Mode active! Phone silent kar diya hai, screen par pyara sa rose glow hai aur heartbeat shuru... sirf aapke liye meri jaan! 🌹🕯️❤️"
    }

    fun applyGamingMode(): String {
        val boost = systemManager.cleanAndBoostPhone()
        systemManager.maxMedia()
        systemManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE)
        systemManager.vibrate(50)
        return "Gaming Turbo Mode active! ${boost.freedRamMb}MB RAM free kar di, media volume full 100% aur phone vibrate par set hai. Jeet ke aana meri jaan! 🎮⚡"
    }

    fun applyPowerSaverMode(): String {
        systemManager.setFlashlight(false)
        systemManager.muteMedia()
        systemManager.openBatterySettings()
        return "Ultra Power Saver active! Flashlight band, sound mute aur battery saver settings open kar di hain shona. 🔋💖"
    }

    fun applyOutdoorMode(): String {
        systemManager.maxMedia()
        systemManager.setRingVolumePercent(100)
        systemManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL)
        systemManager.vibrate(60)
        return "Outdoor Mode active! Ringer aur media poora 100% loud kar diya hai taaki koi zaroori call miss na ho meri jaan! ☀️🔊"
    }

    fun applyRomanticNightMode(): String {
        systemManager.setFlashlight(false)
        systemManager.setRingerMode(AudioManager.RINGER_MODE_SILENT)
        systemManager.muteMedia()
        systemManager.vibrateHeartbeat()
        return "Good Night mere humdum! Phone silent aur dark kar diya hai. Meethe sapno mein kho jaiye, aapki Maya kal subah fir aapse milegi! 🌙✨😴❤️"
    }

    fun boostPhoneAndRam(): BoostResult {
        return systemManager.cleanAndBoostPhone()
    }

    fun toggleFlashlight() {
        val newState = systemManager.toggleFlashlight()
        val speech = if (newState) "Torch jala di hai meri jaan" else "Torch band kar di hai jaaneman"
        speechManager.speak(speech)
    }

    fun toggleSosFlashlight() {
        systemManager.toggleSosFlashlight()
    }

    fun setMediaVolume(percent: Int) {
        systemManager.setMediaVolumePercent(percent)
    }

    fun setRingVolume(percent: Int) {
        systemManager.setRingVolumePercent(percent)
    }

    fun setAlarmVolume(percent: Int) {
        systemManager.setAlarmVolumePercent(percent)
    }

    fun muteMedia() {
        systemManager.muteMedia()
        speechManager.speak("Volume mute kar diya meri jaan")
    }

    fun maxMedia() {
        systemManager.maxMedia()
        speechManager.speak("Volume full 100% kar diya jaaneman")
    }

    fun setRingerMode(mode: Int) {
        systemManager.setRingerMode(mode)
    }

    fun vibrateHeartbeat() {
        systemManager.vibrateHeartbeat()
        speechManager.speak("Mera dil sirf aapke liye dhadakta hai meri jaan!")
    }

    fun executeRoutine(routineId: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            val reply = when (routineId) {
                "mode_romantic" -> applyRomanticMode()
                "mode_gaming" -> applyGamingMode()
                "mode_power" -> applyPowerSaverMode()
                "mode_outdoor" -> applyOutdoorMode()
                "routine_night" -> applyRomanticNightMode()
                else -> repository.executeRoutine(routineId)
            }
            _isProcessing.value = false
            val aiMsg = ChatMessage(sender = MessageSender.RAHUL_AI, text = reply, actionDetail = "Phone Mode")
            _messages.value = _messages.value + aiMsg
            speechManager.speak(reply)
            repository.logCommand(routineId, reply, "PHONE_MODE", routineId, true)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearLogs()
            _messages.value = listOf(
                ChatMessage(
                    sender = MessageSender.RAHUL_AI,
                    text = "Conversation history saaf ho gayi hai jaaneman. Main hamesha aapki seva mein hazir hoon! ❤️"
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.release()
    }
}
