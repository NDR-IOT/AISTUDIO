package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.DailyCompliance
import com.example.data.model.DoseLog
import com.example.data.model.DoseSchedule
import com.example.data.repository.PillRepository
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PillViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PillRepository
    private val alarmScheduler: AlarmScheduler

    private val prefs = application.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "CLARO") ?: "CLARO")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String = dateFormat.format(Date())

    fun getTomorrowDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return dateFormat.format(cal.time)
    }

    private val _selectedDateString = MutableStateFlow(todayDateString)
    val selectedDateString: StateFlow<String> = _selectedDateString.asStateFlow()

    private val _streakDays = MutableStateFlow(0)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    val allSchedules: StateFlow<List<DoseSchedule>>

    @OptIn(ExperimentalCoroutinesApi::class)
    val logsForSelectedDate: StateFlow<List<DoseLog>>

    val nextUpcomingDose: StateFlow<DoseLog?>

    val logsForWeeklyRange: StateFlow<List<DoseLog>>
    val weeklyCompliance: StateFlow<List<DailyCompliance>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PillRepository(db.doseDao())
        alarmScheduler = AlarmScheduler(application)

        allSchedules = repository.activeSchedules.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        logsForSelectedDate = _selectedDateString.flatMapLatest { date ->
            repository.getLogsForDate(date)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        nextUpcomingDose = logsForSelectedDate.map { logs ->
            if (_selectedDateString.value != todayDateString) return@map null
            
            val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            // First look for untaken doses that are coming up or overdue
            logs.firstOrNull { !it.isTaken }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        // Setup 7-day range for weekly compliance chart
        val cal = Calendar.getInstance()
        val dayLabelFormat = SimpleDateFormat("EEE", Locale("es", "ES"))
        val dayNumFormat = SimpleDateFormat("dd", Locale.getDefault())

        val datesList = (6 downTo 0).map { daysAgo ->
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -daysAgo)
            dateFormat.format(c.time)
        }
        val startDateStr = datesList.first()
        val endDateStr = datesList.last()

        logsForWeeklyRange = repository.getLogsForDateRange(startDateStr, endDateStr).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        weeklyCompliance = logsForWeeklyRange.map { logs ->
            val logsByDate = logs.groupBy { it.dateString }
            datesList.map { dStr ->
                val dateObj = dateFormat.parse(dStr) ?: Date()
                val rawLabel = dayLabelFormat.format(dateObj)
                val label = rawLabel.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }.take(3)
                val dayNum = dayNumFormat.format(dateObj)
                val dayLogs = logsByDate[dStr] ?: emptyList()
                val total = dayLogs.size
                val taken = dayLogs.count { it.isTaken }
                val pct = if (total > 0) (taken.toFloat() / total.toFloat() * 100f) else 0f
                DailyCompliance(
                    dateString = dStr,
                    dayLabel = label,
                    dayNumber = dayNum,
                    totalDoses = total,
                    takenDoses = taken,
                    percentage = pct,
                    isToday = dStr == todayDateString
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.ensureLogsForPastDays(6)
            repository.ensureLogsForDate(todayDateString)
            repository.ensureLogsForDate(getTomorrowDateString())
            refreshStreak()
            // Schedule alarms for active schedules
            val activeSchedules = repository.getActiveSchedules()
            if (activeSchedules.isNotEmpty()) {
                alarmScheduler.scheduleAlarmsForSchedules(activeSchedules)
            }
        }
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun selectDate(dateStr: String) {
        _selectedDateString.value = dateStr
        viewModelScope.launch {
            repository.ensureLogsForDate(dateStr)
        }
    }

    fun toggleDoseTaken(logId: Long, currentTaken: Boolean) {
        viewModelScope.launch {
            repository.markDoseAsTaken(logId, !currentTaken)
            refreshStreak()
        }
    }

    fun saveSchedule(schedule: DoseSchedule) {
        viewModelScope.launch {
            if (schedule.id == 0) {
                repository.insertSchedule(schedule)
            } else {
                repository.updateSchedule(schedule)
            }
            val tomorrow = getTomorrowDateString()
            repository.ensureLogsForDate(todayDateString)
            repository.ensureLogsForDate(tomorrow)
            if (_selectedDateString.value != todayDateString && _selectedDateString.value != tomorrow) {
                repository.ensureLogsForDate(_selectedDateString.value)
            }
            val activeSchedules = repository.getActiveSchedules()
            alarmScheduler.scheduleAlarmsForSchedules(activeSchedules)
        }
    }

    fun deleteSchedule(id: Int) {
        viewModelScope.launch {
            repository.deleteSchedule(id)
            val tomorrow = getTomorrowDateString()
            repository.ensureLogsForDate(todayDateString)
            repository.ensureLogsForDate(tomorrow)
            if (_selectedDateString.value != todayDateString && _selectedDateString.value != tomorrow) {
                repository.ensureLogsForDate(_selectedDateString.value)
            }
            val activeSchedules = repository.getActiveSchedules()
            alarmScheduler.scheduleAlarmsForSchedules(activeSchedules)
        }
    }

    fun sendTestNotification(medicationName: String = "Metimazol", quantity: Int = 2) {
        alarmScheduler.scheduleTestNotification(medicationName, quantity)
    }

    private suspend fun refreshStreak() {
        _streakDays.value = repository.calculateStreakDays()
    }
}
