package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dose_schedules")
data class DoseSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicationName: String,
    val quantity: Int,
    val timeHour: Int,
    val timeMinute: Int,
    val reminderOffsetMinutes: Int = 15,
    val colorHex: String = "#E53935",
    val instructions: String = "Tomar con agua",
    val isActive: Boolean = true
) {
    val formattedTime: String
        get() = String.format("%02d:%02d", timeHour, timeMinute)

    val reminderTimeFormatted: String
        get() {
            var totalMinutes = timeHour * 60 + timeMinute - reminderOffsetMinutes
            if (totalMinutes < 0) totalMinutes += 24 * 60
            val remHour = totalMinutes / 60
            val remMin = totalMinutes % 60
            return String.format("%02d:%02d", remHour, remMin)
        }
}
