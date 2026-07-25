package com.example.data.repository

import com.example.data.dao.DoseDao
import com.example.data.model.DoseLog
import com.example.data.model.DoseSchedule
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PillRepository(private val doseDao: DoseDao) {

    val activeSchedules: Flow<List<DoseSchedule>> = doseDao.getSchedulesFlow()

    fun getLogsForDate(dateString: String): Flow<List<DoseLog>> {
        return doseDao.getLogsForDateFlow(dateString)
    }

    fun getLogsForDateRange(startDate: String, endDate: String): Flow<List<DoseLog>> {
        return doseDao.getLogsForDateRangeFlow(startDate, endDate)
    }

    suspend fun ensureLogsForPastDays(daysBack: Int = 6) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        for (i in daysBack downTo 0) {
            val tempCal = cal.clone() as java.util.Calendar
            tempCal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val dateStr = dateFormat.format(tempCal.time)
            ensureLogsForDate(dateStr)
        }
    }

    private fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    suspend fun ensureLogsForDate(dateString: String) {
        val today = getTodayDateString()
        val existingLogs = doseDao.getLogsForDateList(dateString)
        val activeSchedules = doseDao.getActiveSchedulesList()
        val activeScheduleIds = activeSchedules.map { it.id }.toSet()

        // Cleanup untaken logs whose schedule is no longer active or present (only for today or future dates)
        if (dateString >= today) {
            val obsoleteLogs = existingLogs.filter { !it.isTaken && it.scheduleId !in activeScheduleIds }
            obsoleteLogs.forEach { log ->
                doseDao.deleteLogById(log.id)
            }
        }

        if (activeSchedules.isEmpty()) return

        val remainingLogs = doseDao.getLogsForDateList(dateString)
        if (remainingLogs.isEmpty() && dateString >= today) {
            val newLogs = activeSchedules.map { schedule ->
                DoseLog(
                    scheduleId = schedule.id,
                    dateString = dateString,
                    scheduledTime = schedule.formattedTime,
                    reminderTime = schedule.reminderTimeFormatted,
                    medicationName = schedule.medicationName,
                    quantity = schedule.quantity,
                    colorHex = schedule.colorHex,
                    instructions = schedule.instructions,
                    isTaken = false,
                    takenTimestamp = null
                )
            }
            doseDao.insertLogs(newLogs)
        } else if (dateString >= today) {
            // Check if any active schedule isn't in existing logs
            val existingScheduleIds = remainingLogs.map { it.scheduleId }.toSet()
            val missingSchedules = activeSchedules.filter { it.id !in existingScheduleIds }

            if (missingSchedules.isNotEmpty()) {
                val additionalLogs = missingSchedules.map { schedule ->
                    DoseLog(
                        scheduleId = schedule.id,
                        dateString = dateString,
                        scheduledTime = schedule.formattedTime,
                        reminderTime = schedule.reminderTimeFormatted,
                        medicationName = schedule.medicationName,
                        quantity = schedule.quantity,
                        colorHex = schedule.colorHex,
                        instructions = schedule.instructions,
                        isTaken = false,
                        takenTimestamp = null
                    )
                }
                doseDao.insertLogs(additionalLogs)
            }
        }
    }

    suspend fun markDoseAsTaken(logId: Long, isTaken: Boolean) {
        val timestamp = if (isTaken) System.currentTimeMillis() else null
        doseDao.updateLogTakenStatus(logId, isTaken, timestamp)
    }

    suspend fun insertSchedule(schedule: DoseSchedule): Long {
        val newId = doseDao.insertSchedule(schedule)
        return newId
    }

    suspend fun updateSchedule(schedule: DoseSchedule) {
        val today = getTodayDateString()
        doseDao.updateSchedule(schedule)
        doseDao.updateFutureUntakenLogsForSchedule(
            scheduleId = schedule.id,
            name = schedule.medicationName,
            qty = schedule.quantity,
            sTime = schedule.formattedTime,
            rTime = schedule.reminderTimeFormatted,
            color = schedule.colorHex,
            inst = schedule.instructions,
            fromDate = today
        )
    }

    suspend fun deleteSchedule(id: Int) {
        val today = getTodayDateString()
        doseDao.deleteSchedule(id)
        doseDao.deleteFutureLogsByScheduleId(id, today)
    }

    suspend fun getActiveSchedules(): List<DoseSchedule> {
        return doseDao.getActiveSchedulesList()
    }

    suspend fun calculateStreakDays(): Int {
        val datesWithTaken = doseDao.getDatesWithTakenLogs().toSet()
        if (datesWithTaken.isEmpty()) return 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        var streak = 0
        var currentCal = java.util.Calendar.getInstance()

        // If today has taken pills or yesterday had, count backwards
        var dateStr = dateFormat.format(currentCal.time)
        if (!datesWithTaken.contains(dateStr)) {
            // Check yesterday
            currentCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            dateStr = dateFormat.format(currentCal.time)
        }

        while (datesWithTaken.contains(dateStr)) {
            streak++
            currentCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            dateStr = dateFormat.format(currentCal.time)
        }

        return streak
    }
}
