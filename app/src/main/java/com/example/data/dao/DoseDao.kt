package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DoseLog
import com.example.data.model.DoseSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseDao {

    @Query("SELECT * FROM dose_schedules WHERE isActive = 1 ORDER BY timeHour ASC, timeMinute ASC")
    fun getSchedulesFlow(): Flow<List<DoseSchedule>>

    @Query("SELECT * FROM dose_schedules WHERE isActive = 1 ORDER BY timeHour ASC, timeMinute ASC")
    suspend fun getActiveSchedulesList(): List<DoseSchedule>

    @Query("SELECT * FROM dose_schedules ORDER BY timeHour ASC, timeMinute ASC")
    suspend fun getAllSchedulesList(): List<DoseSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: DoseSchedule): Long

    @Update
    suspend fun updateSchedule(schedule: DoseSchedule)

    @Query("DELETE FROM dose_schedules WHERE id = :id")
    suspend fun deleteSchedule(id: Int)

    @Query("DELETE FROM dose_logs WHERE id = :logId")
    suspend fun deleteLogById(logId: Long)

    @Query("DELETE FROM dose_logs WHERE scheduleId = :scheduleId AND dateString >= :fromDate AND isTaken = 0")
    suspend fun deleteFutureUntakenLogsByScheduleId(scheduleId: Int, fromDate: String)

    @Query("DELETE FROM dose_logs WHERE scheduleId = :scheduleId AND dateString >= :fromDate")
    suspend fun deleteFutureLogsByScheduleId(scheduleId: Int, fromDate: String)

    @Query("UPDATE dose_logs SET medicationName = :name, quantity = :qty, scheduledTime = :sTime, reminderTime = :rTime, colorHex = :color, instructions = :inst WHERE scheduleId = :scheduleId AND isTaken = 0 AND dateString >= :fromDate")
    suspend fun updateFutureUntakenLogsForSchedule(
        scheduleId: Int,
        name: String,
        qty: Int,
        sTime: String,
        rTime: String,
        color: String,
        inst: String,
        fromDate: String
    )

    @Query("SELECT * FROM dose_logs WHERE dateString >= :startDate AND dateString <= :endDate ORDER BY dateString ASC, scheduledTime ASC")
    fun getLogsForDateRangeFlow(startDate: String, endDate: String): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE dateString = :date ORDER BY scheduledTime ASC")
    fun getLogsForDateFlow(date: String): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE dateString = :date ORDER BY scheduledTime ASC")
    suspend fun getLogsForDateList(date: String): List<DoseLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DoseLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<DoseLog>)

    @Query("UPDATE dose_logs SET isTaken = :isTaken, takenTimestamp = :timestamp WHERE id = :logId")
    suspend fun updateLogTakenStatus(logId: Long, isTaken: Boolean, timestamp: Long?)

    @Query("SELECT * FROM dose_logs WHERE id = :logId")
    suspend fun getLogById(logId: Long): DoseLog?

    @Query("SELECT DISTINCT dateString FROM dose_logs WHERE isTaken = 1")
    suspend fun getDatesWithTakenLogs(): List<String>
}
