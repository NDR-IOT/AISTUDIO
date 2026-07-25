package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.DoseDao
import com.example.data.model.DoseLog
import com.example.data.model.DoseSchedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DoseSchedule::class, DoseLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun doseDao(): DoseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pill_reminder_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultSchedules(database.doseDao())
                    }
                }
            }

            private suspend fun populateDefaultSchedules(dao: DoseDao) {
                // Pre-populate with user's specific 8 pills daily schedule
                val defaultSchedules = listOf(
                    DoseSchedule(
                        medicationName = "Metimazol",
                        quantity = 2,
                        timeHour = 7,
                        timeMinute = 30,
                        reminderOffsetMinutes = 15,
                        colorHex = "#E53935", // Red/Coral
                        instructions = "2 pastillas en la mañana"
                    ),
                    DoseSchedule(
                        medicationName = "Propranolol",
                        quantity = 1,
                        timeHour = 10,
                        timeMinute = 0,
                        reminderOffsetMinutes = 15,
                        colorHex = "#00897B", // Teal
                        instructions = "1 pastilla a media mañana"
                    ),
                    DoseSchedule(
                        medicationName = "Metimazol",
                        quantity = 2,
                        timeHour = 14,
                        timeMinute = 0,
                        reminderOffsetMinutes = 15,
                        colorHex = "#E53935", // Red/Coral
                        instructions = "2 pastillas al mediodía"
                    ),
                    DoseSchedule(
                        medicationName = "Propranolol",
                        quantity = 1,
                        timeHour = 17,
                        timeMinute = 0,
                        reminderOffsetMinutes = 15,
                        colorHex = "#00897B", // Teal
                        instructions = "1 pastilla en la tarde"
                    ),
                    DoseSchedule(
                        medicationName = "Metimazol",
                        quantity = 2,
                        timeHour = 21,
                        timeMinute = 0,
                        reminderOffsetMinutes = 15,
                        colorHex = "#E53935", // Red/Coral
                        instructions = "2 pastillas en la noche"
                    )
                )

                for (schedule in defaultSchedules) {
                    dao.insertSchedule(schedule)
                }
            }
        }
    }
}
