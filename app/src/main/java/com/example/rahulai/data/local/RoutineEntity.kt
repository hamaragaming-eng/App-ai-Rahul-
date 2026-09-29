package com.example.rahulai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val title: String,
    val triggerPhrase: String,
    val description: String,
    val iconKey: String, // NIGHT, MORNING, PARTY, LEAVING, MOVIE
    val actionJson: String,
    val isEnabled: Boolean = true
)
