package com.example.rahulai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DeviceEntity::class, CommandLogEntity::class, RoutineEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rahul_ai_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                scope.launch(Dispatchers.IO) {
                    ensureSeedData(instance.appDao())
                }
                instance
            }
        }

        suspend fun ensureSeedData(dao: AppDao) {
            val existing = dao.getDeviceListSnapshot()
            if (existing.isEmpty()) {
                populateInitialData(dao)
            }
        }

        suspend fun populateInitialData(dao: AppDao) {
            val initialPhoneComponents = listOf(
                DeviceEntity(
                    id = "phone_torch",
                    name = "LED Camera Flashlight",
                    room = "Mobile Hardware",
                    type = "LIGHT",
                    isPowered = false,
                    brightness = 100,
                    colorHex = "#00F5D4"
                ),
                DeviceEntity(
                    id = "phone_audio",
                    name = "Media & Ring Audio",
                    room = "Mobile Hardware",
                    type = "SPEAKER",
                    isPowered = true,
                    brightness = 75
                ),
                DeviceEntity(
                    id = "phone_battery",
                    name = "Power & Battery Module",
                    room = "Mobile Telemetry",
                    type = "BATTERY",
                    isPowered = true,
                    brightness = 100
                ),
                DeviceEntity(
                    id = "phone_ram",
                    name = "RAM Memory Engine",
                    room = "Mobile System",
                    type = "MEMORY",
                    isPowered = true,
                    brightness = 45
                ),
                DeviceEntity(
                    id = "phone_haptics",
                    name = "Romantic Heartbeat Haptics",
                    room = "Mobile Hardware",
                    type = "VIBRATOR",
                    isPowered = true
                )
            )
            dao.insertDevices(initialPhoneComponents)

            val initialPhoneModes = listOf(
                RoutineEntity(
                    id = "mode_romantic",
                    title = "Romantic Date Mode 🌹",
                    triggerPhrase = "romantic mode",
                    description = "Phone silent, soft romantic 20% media volume, love heartbeat vibration",
                    iconKey = "ROMANTIC",
                    actionJson = "MODE_ROMANTIC"
                ),
                RoutineEntity(
                    id = "mode_gaming",
                    title = "Gaming Turbo Mode 🎮",
                    triggerPhrase = "gaming mode",
                    description = "Boosts RAM, sets media volume to 100%, high performance haptics",
                    iconKey = "GAMING",
                    actionJson = "MODE_GAMING"
                ),
                RoutineEntity(
                    id = "mode_power",
                    title = "Super Power Saver 🔋",
                    triggerPhrase = "power saver",
                    description = "Mutes volume, switches off flashlight, opens battery saver",
                    iconKey = "POWER",
                    actionJson = "MODE_POWER"
                ),
                RoutineEntity(
                    id = "mode_outdoor",
                    title = "Outdoor Bright Mode ☀️",
                    triggerPhrase = "outdoor mode",
                    description = "Max 100% volume, display settings, flashlight ready",
                    iconKey = "OUTDOOR",
                    actionJson = "MODE_OUTDOOR"
                ),
                RoutineEntity(
                    id = "mode_sos",
                    title = "Emergency SOS Flasher 🚨",
                    triggerPhrase = "sos mode",
                    description = "Pulses camera flashlight in SOS morse pattern (... --- ...)",
                    iconKey = "SOS",
                    actionJson = "MODE_SOS"
                ),
                RoutineEntity(
                    id = "routine_night",
                    title = "Good Night / Meethe Sapne 🌙",
                    triggerPhrase = "good night",
                    description = "Mutes sound, turns off torch, and whispers sweet romantic dreams",
                    iconKey = "NIGHT",
                    actionJson = "NIGHT_ALL_OFF"
                )
            )
            dao.insertRoutines(initialPhoneModes)

            dao.insertLog(
                CommandLogEntity(
                    query = "System Online",
                    response = "नमस्ते मेरे प्यारे हमदम! ❤️ आपकी प्यारी माया (Maya AI) ऑनलाइन है। मैं आपके फोन के सभी फीचर्स और ऐप्स को बड़े प्यार से कंट्रोल कर सकती हूँ!",
                    actionType = "SYSTEM_CONTROL",
                    targetDeviceOrFeature = "Phone Engine",
                    isSuccess = true
                )
            )
        }
    }
}
