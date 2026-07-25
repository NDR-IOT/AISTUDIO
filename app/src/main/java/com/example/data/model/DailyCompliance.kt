package com.example.data.model

data class DailyCompliance(
    val dateString: String,
    val dayLabel: String,
    val dayNumber: String,
    val totalDoses: Int,
    val takenDoses: Int,
    val percentage: Float,
    val isToday: Boolean
)
