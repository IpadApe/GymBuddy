package com.gymtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.repository.GymRepository
import com.gymtracker.data.sync.WatchSyncServer
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

    private lateinit var watchSyncServer: WatchSyncServer

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
        seedDatabase()
        startWatchSyncServer()
    }

    private fun startWatchSyncServer() {
        watchSyncServer = WatchSyncServer(this, applicationScope)
        try {
            watchSyncServer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        if (::watchSyncServer.isInitialized) watchSyncServer.stop()
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

            val reminderChannel = NotificationChannel(
                CHANNEL_WORKOUT_REMINDER,
                "Workout Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Rest timer warnings and inactivity reminders"
                enableVibration(true)
            }

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(restTimerChannel)
            nm.createNotificationChannel(workoutChannel)
            nm.createNotificationChannel(prChannel)
            nm.createNotificationChannel(reminderChannel)
        }
    }

    private fun seedDatabase() {
        applicationScope.launch {
            repository.seedExercisesIfEmpty()
            repository.ensureNewExercises()
            repository.seedRoutinesIfEmpty()
        }
    }

    companion object {
        lateinit var instance: GymTrackerApp
            private set

        const val CHANNEL_REST_TIMER = "rest_timer"
        const val CHANNEL_WORKOUT = "workout"
        const val CHANNEL_PR = "personal_record"
        const val CHANNEL_WORKOUT_REMINDER = "workout_reminder"
    }
}
