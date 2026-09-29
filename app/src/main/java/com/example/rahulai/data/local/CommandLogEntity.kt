package com.example.rahulai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_logs")
data class CommandLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val query: String,
    val response: String,
    val actionType: String, // SMART_HOME, SYSTEM_CONTROL, CONVERSATION, ROUTINE
    val targetDeviceOrFeature: String? = null,
    val isSuccess: Boolean = true
)
