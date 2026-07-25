package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.repository.PillRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PillReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            ACTION_PILL_REMINDER -> {
                val scheduleId = intent.getIntExtra(EXTRA_SCHEDULE_ID, -1)
                val medName = intent.getStringExtra(EXTRA_MED_NAME) ?: "Medicamento"
                val quantity = intent.getIntExtra(EXTRA_QUANTITY, 1)
                val scheduledTime = intent.getStringExtra(EXTRA_SCHEDULED_TIME) ?: ""
                val offsetMins = intent.getIntExtra(EXTRA_OFFSET_MINS, 15)

                showPillNotification(
                    context = context,
                    scheduleId = scheduleId,
                    medName = medName,
                    quantity = quantity,
                    scheduledTime = scheduledTime,
                    offsetMins = offsetMins
                )

                // Reschedule for next day if it's a real schedule
                if (scheduleId != 9999) {
                    val db = AppDatabase.getDatabase(context)
                    CoroutineScope(Dispatchers.IO).launch {
                        val activeSchedules = db.doseDao().getActiveSchedulesList()
                        val schedule = activeSchedules.find { it.id == scheduleId }
                        schedule?.let {
                            AlarmScheduler(context).scheduleAlarmForDose(it)
                        }
                    }
                }
            }

            ACTION_MARK_TAKEN -> {
                val scheduleId = intent.getIntExtra(EXTRA_SCHEDULE_ID, -1)
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

                if (notificationId != -1) {
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(notificationId)
                }

                if (scheduleId != -1) {
                    val db = AppDatabase.getDatabase(context)
                    val repository = PillRepository(db.doseDao())
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val todayStr = dateFormat.format(Date())

                    CoroutineScope(Dispatchers.IO).launch {
                        repository.ensureLogsForDate(todayStr)
                        db.doseDao().getLogsForDateList(todayStr).find { it.scheduleId == scheduleId }?.let { log ->
                            repository.markDoseAsTaken(log.id, true)
                        }
                    }
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                // Reschedule all alarms on phone reboot
                val db = AppDatabase.getDatabase(context)
                CoroutineScope(Dispatchers.IO).launch {
                    val activeSchedules = db.doseDao().getActiveSchedulesList()
                    AlarmScheduler(context).scheduleAlarmsForSchedules(activeSchedules)
                }
            }
        }
    }

    private fun showPillNotification(
        context: Context,
        scheduleId: Int,
        medName: String,
        quantity: Int,
        scheduledTime: String,
        offsetMins: Int
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "pill_reminders_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = if (scheduleId != -1) scheduleId else System.currentTimeMillis().toInt()

        // Content Intent to open app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Intent to mark as taken directly from notification
        val markTakenIntent = Intent(context, PillReminderReceiver::class.java).apply {
            action = ACTION_MARK_TAKEN
            putExtra(EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val markTakenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 10000,
            markTakenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val title = "⏰ Recordatorio ($offsetMins min antes)"
        val doseText = if (quantity > 1) "$quantity pastillas de $medName" else "$quantity pastilla de $medName"
        val bodyText = "A las $scheduledTime hrs te toca tomar: $doseText"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$bodyText. ¡No olvides tomarla con un vaso de agua!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "¡YA ME LA TOMÉ!",
                markTakenPendingIntent
            )

        notificationManager.notify(notificationId, builder.build())
    }

    companion object {
        const val ACTION_PILL_REMINDER = "com.example.ACTION_PILL_REMINDER"
        const val ACTION_MARK_TAKEN = "com.example.ACTION_MARK_TAKEN"

        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_MED_NAME = "extra_med_name"
        const val EXTRA_QUANTITY = "extra_quantity"
        const val EXTRA_SCHEDULED_TIME = "extra_scheduled_time"
        const val EXTRA_OFFSET_MINS = "extra_offset_mins"
        const val EXTRA_INSTRUCTIONS = "extra_instructions"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
