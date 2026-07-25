package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dose_logs")
data class DoseLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduleId: Int,
    val dateString: String, // Format: YYYY-MM-DD
    val scheduledTime: String, // e.g. "07:30"
    val reminderTime: String, // e.g. "07:15"
    val medicationName: String,
    val quantity: Int,
    val colorHex: String,
    val instructions: String = "Tomar con agua",
    val isTaken: Boolean = false,
    val takenTimestamp: Long? = null
)
