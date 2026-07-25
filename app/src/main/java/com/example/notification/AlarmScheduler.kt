package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.DoseSchedule
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarmsForSchedules(schedules: List<DoseSchedule>) {
        schedules.forEach { schedule ->
            if (schedule.isActive) {
                scheduleAlarmForDose(schedule)
            } else {
                cancelAlarmForDose(schedule)
            }
        }
    }

    fun scheduleAlarmForDose(schedule: DoseSchedule) {
        val intent = Intent(context, PillReminderReceiver::class.java).apply {
            action = PillReminderReceiver.ACTION_PILL_REMINDER
            putExtra(PillReminderReceiver.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(PillReminderReceiver.EXTRA_MED_NAME, schedule.medicationName)
            putExtra(PillReminderReceiver.EXTRA_QUANTITY, schedule.quantity)
            putExtra(PillReminderReceiver.EXTRA_SCHEDULED_TIME, schedule.formattedTime)
            putExtra(PillReminderReceiver.EXTRA_OFFSET_MINS, schedule.reminderOffsetMinutes)
            putExtra(PillReminderReceiver.EXTRA_INSTRUCTIONS, schedule.instructions)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.timeHour)
            set(Calendar.MINUTE, schedule.timeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -schedule.reminderOffsetMinutes)

            // If time is already past today, set for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Scheduled alarm for ${schedule.medicationName} at ${calendar.time}")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error scheduling alarm", e)
        }
    }

    fun cancelAlarmForDose(schedule: DoseSchedule) {
        val intent = Intent(context, PillReminderReceiver::class.java).apply {
            action = PillReminderReceiver.ACTION_PILL_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun scheduleTestNotification(medicationName: String, quantity: Int) {
        val intent = Intent(context, PillReminderReceiver::class.java).apply {
            action = PillReminderReceiver.ACTION_PILL_REMINDER
            putExtra(PillReminderReceiver.EXTRA_SCHEDULE_ID, 9999)
            putExtra(PillReminderReceiver.EXTRA_MED_NAME, medicationName)
            putExtra(PillReminderReceiver.EXTRA_QUANTITY, quantity)
            putExtra(PillReminderReceiver.EXTRA_SCHEDULED_TIME, "Prueba")
            putExtra(PillReminderReceiver.EXTRA_OFFSET_MINS, 15)
            putExtra(PillReminderReceiver.EXTRA_INSTRUCTIONS, "Esta es una notificación de prueba.")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 3000 // Trigger in 3 seconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}
