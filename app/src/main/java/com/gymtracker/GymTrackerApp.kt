package com.gymtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.repository.GymRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GymTrackerApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { GymRepository(database) }

    // Non-null while a workout is running in background
    val activeWorkoutSessionId = MutableStateFlow<Long?>(null)

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
        seedDatabase()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val restTimerChannel = NotificationChannel(
                CHANNEL_REST_TIMER,
                "Rest Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for rest timer completion"
                enableVibration(true)
            }

            val workoutChannel = NotificationChannel(
                CHANNEL_WORKOUT,
                "Active Workout",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing workout notification"
            }

            val prChannel = NotificationChannel(
                CHANNEL_PR,
                "Personal Records",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Personal record celebrations"
            }

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(restTimerChannel)
            nm.createNotificationChannel(workoutChannel)
            nm.createNotificationChannel(prChannel)
        }
    }

    private fun seedDatabase() {
        applicationScope.launch {
            repository.seedExercisesIfEmpty()
            repository.seedRoutinesIfEmpty()
        }
    }

    companion object {
        lateinit var instance: GymTrackerApp
            private set

        const val CHANNEL_REST_TIMER = "rest_timer"
        const val CHANNEL_WORKOUT = "workout"
        const val CHANNEL_PR = "personal_record"
    }
}
