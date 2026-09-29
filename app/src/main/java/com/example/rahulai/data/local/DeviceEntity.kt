package com.example.rahulai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val room: String,
    val type: String, // LIGHT, AC, LOCK, TV, FAN, PLUG, CAMERA
    val isPowered: Boolean = false,
    val brightness: Int = 80, // 0 - 100
    val temperature: Int = 22, // 16 - 30 Celsius
    val colorHex: String = "#00F5D4",
    val mode: String = "AUTO", // AUTO, COOL, HEAT, ECO, FAN, etc.
    val isOnline: Boolean = true
)
