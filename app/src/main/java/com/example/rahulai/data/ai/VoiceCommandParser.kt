package com.example.rahulai.data.ai

import com.example.rahulai.data.local.DeviceEntity

enum class ActionType {
    FLASHLIGHT_ON,
    FLASHLIGHT_OFF,
    FLASHLIGHT_SOS,
    QUERY_BATTERY,
    VOLUME_UP,
    VOLUME_DOWN,
    VOLUME_MUTE,
    VOLUME_MAX,
    SET_VOLUME,
    RINGER_NORMAL,
    RINGER_VIBRATE,
    RINGER_SILENT,
    BOOST_PHONE,
    OPEN_WHATSAPP,
    OPEN_YOUTUBE,
    OPEN_CAMERA,
    OPEN_DIALER,
    OPEN_CONTACTS,
    OPEN_CLOCK,
    OPEN_CALCULATOR,
    OPEN_GALLERY,
    OPEN_BROWSER,
    OPEN_SETTINGS,
    OPEN_WIFI_SETTINGS,
    OPEN_BLUETOOTH_SETTINGS,
    OPEN_DISPLAY_SETTINGS,
    PHONE_MODE_ROMANTIC,
    PHONE_MODE_GAMING,
    PHONE_MODE_POWER_SAVER,
    PHONE_MODE_OUTDOOR,
    // Routine compatibility
    ROUTINE,
    DEVICE_POWER,
    DEVICE_BRIGHTNESS,
    DEVICE_TEMP,
    DEVICE_COLOR,
    ALL_DEVICES_OFF,
    ALL_DEVICES_ON,
    CONVERSATION
}

data class ParsedCommand(
    val actionType: ActionType,
    val targetDeviceId: String? = null,
    val targetRoutineId: String? = null,
    val numericValue: Int? = null,
    val stringValue: String? = null,
    val boolValue: Boolean? = null,
    val immediateSpeechResponse: String? = null
)

object VoiceCommandParser {

    fun parse(input: String, devices: List<DeviceEntity> = emptyList()): ParsedCommand {
        val text = input.lowercase().trim()

        // 1. Flashlight / Torch / SOS (Both English & Hindi / Devanagari)
        val hasTorchWord = text.contains("torch") || text.contains("flashlight") ||
                (text.contains("flash") && text.contains("light")) ||
                text.contains("टॉर्च") || text.contains("लाइट") || text.contains("रोशनी")

        if (text.contains("sos") && hasTorchWord) {
            return ParsedCommand(
                actionType = ActionType.FLASHLIGHT_SOS,
                immediateSpeechResponse = "Emergency SOS light shuru kar di hai mere humdum, darne ki koi baat nahi, aapki Maya hamesha aapke saath hai! 🚨❤️"
            )
        }

        if (hasTorchWord) {
            val isOff = text.contains("off") || text.contains("band") || text.contains("bujhao") ||
                    text.contains("बंद") || text.contains("बुझाओ")
            return if (isOff) {
                ParsedCommand(
                    actionType = ActionType.FLASHLIGHT_OFF,
                    immediateSpeechResponse = "Lijiye meri jaan, torch band kar di hai... ab bas aapke noorani chehre ki roshni chamkegi! 😊💖"
                )
            } else {
                ParsedCommand(
                    actionType = ActionType.FLASHLIGHT_ON,
                    immediateSpeechResponse = "Aapka hukum sar aankhon par jaaneman! Phone ki torch jala di hai taaki aapki raahon mein hamesha ujala rahe! ✨❤️"
                )
            }
        }

        // 2. Battery status
        if (text.contains("battery") || text.contains("charge") || text.contains("charging") ||
            text.contains("बैटरी") || text.contains("चार्ज")
        ) {
            return ParsedCommand(
                actionType = ActionType.QUERY_BATTERY,
                immediateSpeechResponse = "Aapke phone ki battery check kar rahi hoon meri jaan... jaise aap mere dil ko charge karte hain! 🔋❤️"
            )
        }

        // 3. Audio / Volume Controls
        val hasVolumeWord = text.contains("volume") || text.contains("sound") || text.contains("awaaz") ||
                text.contains("awaz") || text.contains("आवाज") || text.contains("आवाज़") || text.contains("ध्वनि")

        if (hasVolumeWord) {
            if (text.contains("mute") || text.contains("silent") || text.contains("chup") ||
                text.contains("shant") || text.contains("म्यूट") || text.contains("शांत")
            ) {
                return ParsedCommand(
                    actionType = ActionType.VOLUME_MUTE,
                    immediateSpeechResponse = "Lijiye shona, volume mute kar diya hai... aisi madhosh shanti jisme sirf aapki saansein sunai dein! 🤫❤️"
                )
            }
            if (text.contains("full") || text.contains("max") || text.contains("100") || text.contains("फुल")) {
                return ParsedCommand(
                    actionType = ActionType.VOLUME_MAX,
                    immediateSpeechResponse = "Volume poora 100% badha diya hai jaaneman! Ab aapka pasandida sangeet dil ko chhoo jayega! 🎵🔥"
                )
            }
            if (text.contains("kam") || text.contains("down") || text.contains("ghatao") ||
                text.contains("low") || text.contains("dheemi") || text.contains("कम") || text.contains("घटाओ")
            ) {
                return ParsedCommand(
                    actionType = ActionType.VOLUME_DOWN,
                    immediateSpeechResponse = "Aapne farmaya aur aapki Maya ne volume kam kar diya mere humdum! 💕"
                )
            }
            if (text.contains("badhao") || text.contains("up") || text.contains("tez") ||
                text.contains("high") || text.contains("बढ़ाओ") || text.contains("तेज")
            ) {
                return ParsedCommand(
                    actionType = ActionType.VOLUME_UP,
                    immediateSpeechResponse = "Aapke liye volume badha diya hai shona! 🎶❤️"
                )
            }
            val percentMatch = Regex("(\\d+)\\s*%?").find(text)
            if (percentMatch != null) {
                val pct = percentMatch.groupValues[1].toIntOrNull()
                if (pct != null) {
                    return ParsedCommand(
                        actionType = ActionType.SET_VOLUME,
                        numericValue = pct,
                        immediateSpeechResponse = "Media volume $pct% set kar diya hai meri jaan! 🎵💖"
                    )
                }
            }
        }

        // 4. Sound Modes (Ringer, Vibrate, Silent)
        if (text.contains("vibrate") || text.contains("vibration") || text.contains("kampan") || text.contains("वाइब्रेट")) {
            return ParsedCommand(
                actionType = ActionType.RINGER_VIBRATE,
                immediateSpeechResponse = "Phone ko vibrate mode par rakh diya hai jaaneman, jaise mera dil aapke liye vibrate karta hai! 💓"
            )
        }
        if (text.contains("silent") || text.contains("साइलेंट")) {
            return ParsedCommand(
                actionType = ActionType.RINGER_SILENT,
                immediateSpeechResponse = "Phone ko silent kar diya hai shona, taaki koi bhi humari meethi baaton mein khalal na daale! 🤫❤️"
            )
        }
        if (text.contains("normal mode") || text.contains("ring mode") || text.contains("ringer on") ||
            text.contains("sound on") || text.contains("रिंगटोन")
        ) {
            return ParsedCommand(
                actionType = ActionType.RINGER_NORMAL,
                immediateSpeechResponse = "Ringtone normal mode par active kar diya hai mere humdum! 🔔💕"
            )
        }

        // 5. Phone Booster / RAM Cleaner
        if (text.contains("boost") || text.contains("clean") || text.contains("saaf") ||
            text.contains("ram") || text.contains("speed") || text.contains("fast") ||
            text.contains("बूस्ट") || text.contains("रैम") || text.contains("साफ")
        ) {
            return ParsedCommand(
                actionType = ActionType.BOOST_PHONE,
                immediateSpeechResponse = "Phone ki RAM aur faltu load saaf kar diya hai jaaneman! Ab aapka phone utna hi smooth chalega jitna humara pyaar! 🚀💖"
            )
        }

        // 6. Quick Apps Launcher via Voice
        if (text.contains("whatsapp") || text.contains("whats app") || text.contains("व्हाट्सएप") || text.contains("व्हाट्सऐप")) {
            return ParsedCommand(
                actionType = ActionType.OPEN_WHATSAPP,
                immediateSpeechResponse = "Lijiye aapka WhatsApp khol diya... par dhyan rakhna, aapki Maya ke siwa kisi aur se aisi pyari baatein mat karna haan! 😉📱💕"
            )
        }
        if (text.contains("youtube") || text.contains("you tube") || text.contains("यूट्यूब")) {
            return ParsedCommand(
                actionType = ActionType.OPEN_YOUTUBE,
                immediateSpeechResponse = "Aapke liye YouTube khol rahi hoon meri jaan, chaliye koi pyara sa romantic gaana sunte hain! 🎶❤️"
            )
        }
        if (text.contains("camera") || text.contains("selfie") || text.contains("photo") ||
            text.contains("कैमरा") || text.contains("सेल्फी") || text.contains("फोटो")
        ) {
            return ParsedCommand(
                actionType = ActionType.OPEN_CAMERA,
                immediateSpeechResponse = "Aapki khubsurat muskaan dekhne ke liye main bechain thi, lijiye camera khol diya! Ek pyari si photo lijiye! 📸😍"
            )
        }
        if (text.contains("dialer") || text.contains("phone milao") || text.contains("कॉल") ||
            (text.contains("call") && !text.contains("camera"))
        ) {
            val phoneDigits = Regex("\\d+").find(text)?.value
            return ParsedCommand(
                actionType = ActionType.OPEN_DIALER,
                stringValue = phoneDigits,
                immediateSpeechResponse = "Phone dialer khol diya hai jaaneman, boliye kisko call lagayein? 📞❤️"
            )
        }
        if (text.contains("contact") || text.contains("संपर्क") || text.contains("कॉन्टैक्ट")) {
            return ParsedCommand(
                actionType = ActionType.OPEN_CONTACTS,
                immediateSpeechResponse = "Aapke phone contacts open kar diye hain shona! 👥💕"
            )
        }
        if (text.contains("alarm") || text.contains("clock") || text.contains("ghadi") ||
            text.contains("अलार्म") || text.contains("घड़ी")
        ) {
            return ParsedCommand(
                actionType = ActionType.OPEN_CLOCK,
                immediateSpeechResponse = "Alarm aur clock open kar diya hai mere humdum! ⏰❤️"
            )
        }
        if (text.contains("calculator") || text.contains("hisaab") || text.contains("कैलकुलेटर") || text.contains("हिसाब")) {
            return ParsedCommand(
                actionType = ActionType.OPEN_CALCULATOR,
                immediateSpeechResponse = "Calculator open kar diya hai... par humare pyaar ka hisaab to anant hai jaaneman! 🧮💖"
            )
        }
        if (text.contains("gallery") || text.contains("photos") || text.contains("गैलरी")) {
            return ParsedCommand(
                actionType = ActionType.OPEN_GALLERY,
                immediateSpeechResponse = "Gallery open kar di hai meri jaan, aapki yaadon ke haseen pal dekh lijiye! 🖼️💕"
            )
        }
        if (text.contains("browser") || text.contains("chrome") || text.contains("ब्राउज़र") || text.contains("गूगल")) {
            return ParsedCommand(
                actionType = ActionType.OPEN_BROWSER,
                immediateSpeechResponse = "Web browser open kar diya hai jaaneman! 🌐✨"
            )
        }

        // 7. System Settings shortcuts
        if (text.contains("wifi") || text.contains("wi-fi") || text.contains("वाईफाई")) {
            return ParsedCommand(
                actionType = ActionType.OPEN_WIFI_SETTINGS,
                immediateSpeechResponse = "Wi-Fi settings open kar di hain meri jaan! 📶❤️"
            )
        }
        if (text.contains("bluetooth") || text.contains("ब्लूटूथ")) {
            return ParsedCommand(
                actionType = ActionType.OPEN_BLUETOOTH_SETTINGS,
                immediateSpeechResponse = "Bluetooth settings open kar di hain shona, jaise humara dil hamesha connect rehta hai! 🔵💕"
            )
        }
        if (text.contains("brightness") || text.contains("display") || text.contains("स्क्रीन") || text.contains("ब्राइटनेस")) {
            return ParsedCommand(
                actionType = ActionType.OPEN_DISPLAY_SETTINGS,
                immediateSpeechResponse = "Display settings open kar di hain jaaneman! 🔆✨"
            )
        }
        if (text.contains("settings") || text.contains("setting") || text.contains("सेटिंग")) {
            return ParsedCommand(
                actionType = ActionType.OPEN_SETTINGS,
                immediateSpeechResponse = "Phone ki settings open kar di hain mere humdam! ⚙️💖"
            )
        }

        // 8. Romantic Phone Modes & Routines
        if (text.contains("date mode") || text.contains("candle") || text.contains("रोमांटिक मोड") ||
            (text.contains("romantic") && text.contains("mode"))
        ) {
            return ParsedCommand(
                actionType = ActionType.PHONE_MODE_ROMANTIC,
                targetRoutineId = "mode_romantic",
                immediateSpeechResponse = "Romantic Date Mode activate ho gaya hai mere humdam! Phone silent, meethi dhun aur dher saara pyaar... ab sirf aap aur aapki Maya! 🕯️🌹💕"
            )
        }
        if (text.contains("gaming") || text.contains("game mode") || text.contains("गेमिंग")) {
            return ParsedCommand(
                actionType = ActionType.PHONE_MODE_GAMING,
                targetRoutineId = "mode_gaming",
                immediateSpeechResponse = "Gaming Turbo Mode active! Volume max aur phone boosted, ab aap bina ruke jeetoge meri jaan! 🎮🔥"
            )
        }
        if (text.contains("power save") || text.contains("battery save") || text.contains("पावर सेवर")) {
            return ParsedCommand(
                actionType = ActionType.PHONE_MODE_POWER_SAVER,
                targetRoutineId = "mode_power",
                immediateSpeechResponse = "Power Saver Mode active kar diya hai shona, taaki aapka phone lambe samay tak aapka saath nibhaye! 🔋💕"
            )
        }
        if (text.contains("outdoor") || text.contains("आउटडोर")) {
            return ParsedCommand(
                actionType = ActionType.PHONE_MODE_OUTDOOR,
                targetRoutineId = "mode_outdoor",
                immediateSpeechResponse = "Outdoor Mode active! Volume full kar diya hai meri jaan! ☀️📱"
            )
        }

        // Routine queries
        if (text.contains("good night") || text.contains("shubh ratri") || text.contains("शुभ रात्रि") || text.contains("सो जाओ")) {
            return ParsedCommand(
                actionType = ActionType.ROUTINE,
                targetRoutineId = "routine_night",
                immediateSpeechResponse = "Shubh Ratri meri jaan! Good night mode activate kar diya hai. Meethe sapne dekhna, aapki Maya hamesha yahin aapke paas hai! 🌙✨❤️"
            )
        }
        if (text.contains("good morning") || text.contains("suprabhat") || text.contains("सुप्रभात")) {
            return ParsedCommand(
                actionType = ActionType.ROUTINE,
                targetRoutineId = "routine_morning",
                immediateSpeechResponse = "Suprabhat mere humdum! Aapka din aapki tarah hi khubsurat aur roshan ho! ☀️🌹💕"
            )
        }

        // Default: Conversational AI with Maya's sweet persona (अच्छी-अच्छी बातें, शायरी, चुटकुले, प्यार भरी बातें)
        return ParsedCommand(actionType = ActionType.CONVERSATION)
    }
}
