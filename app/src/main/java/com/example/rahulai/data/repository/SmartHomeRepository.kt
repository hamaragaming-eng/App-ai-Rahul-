package com.example.rahulai.data.repository

import com.example.rahulai.data.local.AppDao
import com.example.rahulai.data.local.CommandLogEntity
import com.example.rahulai.data.local.DeviceEntity
import com.example.rahulai.data.local.RoutineEntity
import kotlinx.coroutines.flow.Flow

class SmartHomeRepository(private val dao: AppDao) {
    val allDevices: Flow<List<DeviceEntity>> = dao.getAllDevices()
    val recentLogs: Flow<List<CommandLogEntity>> = dao.getRecentLogs()
    val allRoutines: Flow<List<RoutineEntity>> = dao.getAllRoutines()

    suspend fun getDeviceList(): List<DeviceEntity> = dao.getDeviceListSnapshot()

    suspend fun togglePower(id: String, currentState: Boolean) {
        dao.setPower(id, !currentState)
    }

    suspend fun setPower(id: String, isPowered: Boolean) {
        dao.setPower(id, isPowered)
    }

    suspend fun setBrightness(id: String, brightness: Int) {
        dao.setBrightness(id, brightness.coerceIn(0, 100))
    }

    suspend fun setTemperature(id: String, temp: Int) {
        dao.setTemperature(id, temp.coerceIn(16, 30))
    }

    suspend fun setColor(id: String, colorHex: String) {
        dao.setColor(id, colorHex)
    }

    suspend fun setMode(id: String, mode: String) {
        dao.setMode(id, mode)
    }

    suspend fun setAllPower(isPowered: Boolean) {
        dao.setAllDevicesPower(isPowered)
    }

    suspend fun addDevice(device: DeviceEntity) {
        dao.insertOrUpdateDevice(device)
    }

    suspend fun deleteDevice(id: String) {
        dao.deleteDevice(id)
    }

    suspend fun logCommand(
        query: String,
        response: String,
        actionType: String,
        target: String? = null,
        isSuccess: Boolean = true
    ) {
        dao.insertLog(
            CommandLogEntity(
                query = query,
                response = response,
                actionType = actionType,
                targetDeviceOrFeature = target,
                isSuccess = isSuccess
            )
        )
    }

    suspend fun clearLogs() {
        dao.clearLogs()
    }

    suspend fun executeRoutine(routineId: String): String {
        val routine = dao.getRoutineById(routineId) ?: return "Mode nahi mili meri jaan."
        when (routine.actionJson) {
            "MODE_ROMANTIC" -> {
                logCommand("Mode: ${routine.title}", "Romantic Date Mode: silent ringer, love haptics, soft media volume", "PHONE_MODE", routine.title)
                return "Romantic Date Mode activate ho gaya hai meri jaan! Phone silent, meethi dhun aur dher saara pyaar... ab sirf aap aur main! 🕯️🌹💕"
            }
            "MODE_GAMING" -> {
                logCommand("Mode: ${routine.title}", "Gaming Turbo Mode: max volume, RAM cleared", "PHONE_MODE", routine.title)
                return "Gaming Turbo Mode active! Volume max aur phone boosted, ab aap bina ruke jeetoge meri jaan! 🎮🔥"
            }
            "MODE_POWER" -> {
                logCommand("Mode: ${routine.title}", "Power Saver: muted volume, battery saver", "PHONE_MODE", routine.title)
                return "Power Saver Mode active kar diya hai shona, taaki aapka phone lambe samay tak aapka saath nibhaye! 🔋💕"
            }
            "MODE_OUTDOOR" -> {
                logCommand("Mode: ${routine.title}", "Outdoor Mode: 100% volume, display settings", "PHONE_MODE", routine.title)
                return "Outdoor Mode active! Volume full kar diya hai meri jaan! ☀️📱"
            }
            "MODE_SOS" -> {
                logCommand("Mode: ${routine.title}", "SOS mode triggered", "PHONE_MODE", routine.title)
                return "Emergency SOS light shuru kar di hai meri jaan, darne ki koi baat nahi, main aapke saath hoon! 🚨❤️"
            }
            "NIGHT_ALL_OFF" -> {
                logCommand("Mode: ${routine.title}", "Good night phone mode: muted volume and sweet dreams", "PHONE_MODE", routine.title)
                return "Shubh Ratri meri jaan! Good night mode activate kar diya hai. Meethe sapne dekhna, main yahin aapka khayal rakhne ke liye hoon! 🌙✨❤️"
            }
            else -> {
                logCommand("Mode: ${routine.title}", "Executed mode", "PHONE_MODE", routine.title)
                return "${routine.title} safalta-purvak execute ho gaya hai meri jaan! ❤️"
            }
        }
    }
}
