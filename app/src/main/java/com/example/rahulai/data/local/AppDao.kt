package com.example.rahulai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM smart_devices ORDER BY room ASC, name ASC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM smart_devices WHERE id = :id")
    suspend fun getDeviceById(id: String): DeviceEntity?

    @Query("SELECT * FROM smart_devices")
    suspend fun getDeviceListSnapshot(): List<DeviceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(device: DeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<DeviceEntity>)

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Query("UPDATE smart_devices SET isPowered = :isPowered WHERE id = :id")
    suspend fun setPower(id: String, isPowered: Boolean)

    @Query("UPDATE smart_devices SET brightness = :brightness WHERE id = :id")
    suspend fun setBrightness(id: String, brightness: Int)

    @Query("UPDATE smart_devices SET temperature = :temp WHERE id = :id")
    suspend fun setTemperature(id: String, temp: Int)

    @Query("UPDATE smart_devices SET colorHex = :colorHex WHERE id = :id")
    suspend fun setColor(id: String, colorHex: String)

    @Query("UPDATE smart_devices SET mode = :mode WHERE id = :id")
    suspend fun setMode(id: String, mode: String)

    @Query("UPDATE smart_devices SET isPowered = :isPowered")
    suspend fun setAllDevicesPower(isPowered: Boolean)

    @Query("DELETE FROM smart_devices WHERE id = :id")
    suspend fun deleteDevice(id: String)

    // Logs
    @Query("SELECT * FROM command_logs ORDER BY timestamp DESC LIMIT 60")
    fun getRecentLogs(): Flow<List<CommandLogEntity>>

    @Insert
    suspend fun insertLog(log: CommandLogEntity)

    @Query("DELETE FROM command_logs")
    suspend fun clearLogs()

    // Routines
    @Query("SELECT * FROM routines")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: String): RoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<RoutineEntity>)

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)
}
