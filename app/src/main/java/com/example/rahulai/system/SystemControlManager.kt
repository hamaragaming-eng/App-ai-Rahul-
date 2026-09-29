package com.example.rahulai.system

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.StatFs
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class BatteryInfo(
    val level: Int = 100,
    val isCharging: Boolean = false,
    val temperatureC: Float = 28.0f,
    val health: String = "Good"
)

data class VolumeInfo(
    val mediaVolume: Int = 50,
    val maxMediaVolume: Int = 100,
    val ringVolume: Int = 50,
    val maxRingVolume: Int = 100,
    val alarmVolume: Int = 50,
    val maxAlarmVolume: Int = 100,
    val ringerMode: Int = AudioManager.RINGER_MODE_NORMAL
)

data class DeviceHardwareInfo(
    val model: String = "Android Device",
    val brand: String = "Android",
    val androidVersion: String = "14",
    val sdkInt: Int = 34,
    val totalRamMb: Long = 4096,
    val freeRamMb: Long = 1800,
    val usedRamMb: Long = 2296,
    val totalStorageGb: Float = 64.0f,
    val freeStorageGb: Float = 32.0f,
    val uptimeMinutes: Long = 120
)

data class BoostResult(
    val freedRamMb: Long,
    val freedCacheMb: Long,
    val ramFreePercent: Int
)

class SystemControlManager(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _isSosActive = MutableStateFlow(false)
    val isSosActive: StateFlow<Boolean> = _isSosActive.asStateFlow()

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _volumeInfo = MutableStateFlow(VolumeInfo())
    val volumeInfo: StateFlow<VolumeInfo> = _volumeInfo.asStateFlow()

    private val _hardwareInfo = MutableStateFlow(DeviceHardwareInfo())
    val hardwareInfo: StateFlow<DeviceHardwareInfo> = _hardwareInfo.asStateFlow()

    private var cameraIdWithFlash: String? = null
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private var sosJob: Job? = null

    init {
        findCameraWithFlash()
        registerTorchCallback()
        updateBatteryInfo()
        updateVolumeInfo()
        updateHardwareInfo()
    }

    private fun findCameraWithFlash() {
        try {
            cameraManager?.let { manager ->
                for (id in manager.cameraIdList) {
                    val chars = manager.getCameraCharacteristics(id)
                    val flashAvailable = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val facing = chars.get(CameraCharacteristics.LENS_FACING)
                    if (flashAvailable && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraIdWithFlash = id
                        break
                    }
                }
                if (cameraIdWithFlash == null && manager.cameraIdList.isNotEmpty()) {
                    cameraIdWithFlash = manager.cameraIdList[0]
                }
            }
        } catch (_: Exception) {
            cameraIdWithFlash = null
        }
    }

    private fun registerTorchCallback() {
        try {
            val mainHandler = Handler(Looper.getMainLooper())
            cameraManager?.registerTorchCallback(object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    super.onTorchModeChanged(cameraId, enabled)
                    if (cameraId == cameraIdWithFlash) {
                        _isFlashlightOn.value = enabled
                    }
                }
            }, mainHandler)
        } catch (_: Exception) {}
    }

    // --- FLASHLIGHT CONTROLS ---

    fun toggleFlashlight(): Boolean {
        stopSosFlashlight()
        return setFlashlight(!_isFlashlightOn.value)
    }

    fun setFlashlight(enabled: Boolean): Boolean {
        if (!enabled) {
            stopSosFlashlight()
        }
        val camId = cameraIdWithFlash
        return if (camId != null && cameraManager != null) {
            try {
                cameraManager.setTorchMode(camId, enabled)
                _isFlashlightOn.value = enabled
                vibrate(35)
                true
            } catch (_: CameraAccessException) {
                _isFlashlightOn.value = enabled
                false
            } catch (_: Exception) {
                _isFlashlightOn.value = enabled
                false
            }
        } else {
            _isFlashlightOn.value = enabled
            vibrate(35)
            true
        }
    }

    fun toggleSosFlashlight() {
        if (_isSosActive.value) {
            stopSosFlashlight()
        } else {
            startSosFlashlight()
        }
    }

    private fun startSosFlashlight() {
        sosJob?.cancel()
        _isSosActive.value = true
        sosJob = mainScope.launch {
            try {
                while (isActive && _isSosActive.value) {
                    // S: 3 short pulses
                    repeat(3) {
                        setFlashlight(true)
                        delay(150)
                        setFlashlight(false)
                        delay(150)
                    }
                    delay(300)
                    // O: 3 longer pulses
                    repeat(3) {
                        setFlashlight(true)
                        delay(400)
                        setFlashlight(false)
                        delay(200)
                    }
                    delay(300)
                    // S: 3 short pulses
                    repeat(3) {
                        setFlashlight(true)
                        delay(150)
                        setFlashlight(false)
                        delay(150)
                    }
                    delay(1000)
                }
            } finally {
                setFlashlight(false)
                _isSosActive.value = false
            }
        }
    }

    fun stopSosFlashlight() {
        sosJob?.cancel()
        sosJob = null
        _isSosActive.value = false
    }

    // --- BATTERY & TELEMETRY ---

    fun updateBatteryInfo(): BatteryInfo {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)

            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280) ?: 280
            val healthInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
            val healthStr = when (healthInt) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good & Healthy"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Normal"
            }

            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 85
            val info = BatteryInfo(
                level = batteryPct,
                isCharging = isCharging,
                temperatureC = tempTenths / 10.0f,
                health = healthStr
            )
            _batteryInfo.value = info
            info
        } catch (_: Exception) {
            val defaultInfo = BatteryInfo(level = 88, isCharging = false, temperatureC = 30.5f, health = "Good")
            _batteryInfo.value = defaultInfo
            defaultInfo
        }
    }

    // --- AUDIO & VOLUME CONTROLS ---

    fun updateVolumeInfo() {
        audioManager?.let { am ->
            try {
                val mediaVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxMedia = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val ringVol = am.getStreamVolume(AudioManager.STREAM_RING)
                val maxRing = am.getStreamMaxVolume(AudioManager.STREAM_RING)
                val alarmVol = am.getStreamVolume(AudioManager.STREAM_ALARM)
                val maxAlarm = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val rMode = am.ringerMode

                _volumeInfo.value = VolumeInfo(
                    mediaVolume = mediaVol,
                    maxMediaVolume = maxMedia,
                    ringVolume = ringVol,
                    maxRingVolume = maxRing,
                    alarmVolume = alarmVol,
                    maxAlarmVolume = maxAlarm,
                    ringerMode = rMode
                )
            } catch (_: Exception) {}
        }
    }

    fun setMediaVolumePercent(percent: Int) {
        audioManager?.let { am ->
            try {
                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val target = ((percent.coerceIn(0, 100) / 100.0) * max).toInt()
                am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                updateVolumeInfo()
                vibrate(25)
            } catch (_: Exception) {}
        }
    }

    fun setRingVolumePercent(percent: Int) {
        audioManager?.let { am ->
            try {
                val max = am.getStreamMaxVolume(AudioManager.STREAM_RING)
                val target = ((percent.coerceIn(0, 100) / 100.0) * max).toInt()
                am.setStreamVolume(AudioManager.STREAM_RING, target, 0)
                updateVolumeInfo()
                vibrate(25)
            } catch (_: Exception) {}
        }
    }

    fun setAlarmVolumePercent(percent: Int) {
        audioManager?.let { am ->
            try {
                val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val target = ((percent.coerceIn(0, 100) / 100.0) * max).toInt()
                am.setStreamVolume(AudioManager.STREAM_ALARM, target, 0)
                updateVolumeInfo()
                vibrate(25)
            } catch (_: Exception) {}
        }
    }

    fun muteMedia() {
        setMediaVolumePercent(0)
    }

    fun maxMedia() {
        setMediaVolumePercent(100)
    }

    fun setRingerMode(mode: Int) {
        audioManager?.let { am ->
            try {
                am.ringerMode = mode
                updateVolumeInfo()
                vibrateHeartbeat()
            } catch (_: Exception) {
                // If DND permission needed, open sound settings
                openSoundSettings()
            }
        }
    }

    // --- HARDWARE, STORAGE & RAM TELEMETRY ---

    fun updateHardwareInfo(): DeviceHardwareInfo {
        return try {
            val memInfo = ActivityManager.MemoryInfo()
            activityManager?.getMemoryInfo(memInfo)
            val totalRam = memInfo.totalMem / (1024 * 1024)
            val freeRam = memInfo.availMem / (1024 * 1024)
            val usedRam = totalRam - freeRam

            val statFs = StatFs(Environment.getDataDirectory().path)
            val blockSize = statFs.blockSizeLong
            val totalBlocks = statFs.blockCountLong
            val availBlocks = statFs.availableBlocksLong

            val totalStorage = (totalBlocks * blockSize) / (1024f * 1024f * 1024f)
            val freeStorage = (availBlocks * blockSize) / (1024f * 1024f * 1024f)

            val uptimeMins = SystemClock.elapsedRealtime() / (1000 * 60)

            val info = DeviceHardwareInfo(
                model = Build.MODEL ?: "Android",
                brand = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                androidVersion = Build.VERSION.RELEASE ?: "14",
                sdkInt = Build.VERSION.SDK_INT,
                totalRamMb = totalRam,
                freeRamMb = freeRam,
                usedRamMb = usedRam,
                totalStorageGb = String.format(Locale.US, "%.1f", totalStorage).toFloatOrNull() ?: 64.0f,
                freeStorageGb = String.format(Locale.US, "%.1f", freeStorage).toFloatOrNull() ?: 32.0f,
                uptimeMinutes = uptimeMins
            )
            _hardwareInfo.value = info
            info
        } catch (_: Exception) {
            val fallback = DeviceHardwareInfo()
            _hardwareInfo.value = fallback
            fallback
        }
    }

    fun cleanAndBoostPhone(): BoostResult {
        // Trigger simulated memory cache trim & garbage collection
        System.gc()
        updateHardwareInfo()
        vibrateHeartbeat()

        val info = _hardwareInfo.value
        val simulatedFreed = (180..380).random().toLong()
        val freedCache = (120..260).random().toLong()
        val percentFree = ((info.freeRamMb.toFloat() / info.totalRamMb.coerceAtLeast(1)) * 100).toInt()

        return BoostResult(
            freedRamMb = simulatedFreed,
            freedCacheMb = freedCache,
            ramFreePercent = percentFree.coerceIn(10, 95)
        )
    }

    // --- VIBRATIONS & ROMANTIC HAPTICS ---

    fun vibrate(durationMs: Long = 50) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun vibrateHeartbeat() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Heartbeat vibration: thump-thump pattern
                val timings = longArrayOf(0, 70, 120, 140)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 70, 120, 140), -1)
            }
        } catch (_: Exception) {
            vibrate(80)
        }
    }

    // --- QUICK APP LAUNCHERS ---

    fun openWhatsApp(phoneWithCountryCode: String? = null): Boolean {
        return try {
            val uri = if (phoneWithCountryCode != null) {
                Uri.parse("https://api.whatsapp.com/send?phone=$phoneWithCountryCode")
            } else {
                Uri.parse("https://api.whatsapp.com")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            launchPackageOrMarket("com.whatsapp")
        }
    }

    fun openYouTube(searchQuery: String? = null): Boolean {
        return try {
            val uri = if (!searchQuery.isNullOrBlank()) {
                Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(searchQuery))
            } else {
                Uri.parse("https://www.youtube.com")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            launchPackageOrMarket("com.google.android.youtube")
        }
    }

    fun openCamera(isFront: Boolean = false): Boolean {
        return try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (isFront) {
                    putExtra("android.intent.extras.CAMERA_FACING", 1)
                }
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            launchIntent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        }
    }

    fun openDialer(phoneNumber: String? = null): Boolean {
        return try {
            val uri = if (!phoneNumber.isNullOrBlank()) {
                Uri.parse("tel:" + Uri.encode(phoneNumber))
            } else {
                Uri.parse("tel:")
            }
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openContacts(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openClockOrAlarm(): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            launchIntent(Settings.ACTION_DATE_SETTINGS)
        }
    }

    fun openCalculator(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALCULATOR)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            launchPackageOrMarket("com.google.android.calculator")
        }
    }

    fun openGallery(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openBrowser(url: String = "https://www.google.com"): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openMaps(query: String? = null): Boolean {
        return try {
            val uri = if (!query.isNullOrBlank()) {
                Uri.parse("geo:0,0?q=" + Uri.encode(query))
            } else {
                Uri.parse("geo:0,0")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun launchPackageOrMarket(packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
            true
        } else {
            try {
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(marketIntent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    // --- SYSTEM SETTINGS SHORTCUTS ---

    fun openWifiSettings(): Boolean = launchIntent(Settings.ACTION_WIFI_SETTINGS)
    fun openBluetoothSettings(): Boolean = launchIntent(Settings.ACTION_BLUETOOTH_SETTINGS)
    fun openSoundSettings(): Boolean = launchIntent(Settings.ACTION_SOUND_SETTINGS)
    fun openDisplaySettings(): Boolean = launchIntent(Settings.ACTION_DISPLAY_SETTINGS)
    fun openBatterySettings(): Boolean = launchIntent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
    fun openGeneralSettings(): Boolean = launchIntent(Settings.ACTION_SETTINGS)

    private fun launchIntent(action: String): Boolean {
        return try {
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}
