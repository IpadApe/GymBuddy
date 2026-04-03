package com.gymtracker.util

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.gymtracker.GymTrackerApp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════════════
// FORMAT HELPERS
// ═══════════════════════════════════════════════════════════════
object FormatUtils {
    fun formatDuration(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
        else String.format("%02d:%02d", m, s)
    }

    fun formatWeight(kg: Double, useMetric: Boolean): String {
        return if (useMetric) {
            "${String.format("%.1f", kg)} kg"
        } else {
            "${String.format("%.1f", kg * 2.20462)} lbs"
        }
    }

    fun formatWeightValue(kg: Double, useMetric: Boolean): Double {
        return if (useMetric) kg else kg * 2.20462
    }

    fun toKg(value: Double, isMetric: Boolean): Double {
        return if (isMetric) value else value / 2.20462
    }

    fun formatDistance(cm: Double, useMetric: Boolean): String {
        return if (useMetric) "${String.format("%.1f", cm)} cm"
        else "${String.format("%.1f", cm / 2.54)} in"
    }

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatDayOfWeek(millis: Long): String {
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatVolume(kg: Double, useMetric: Boolean): String {
        val value = if (useMetric) kg else kg * 2.20462
        return if (value >= 1000) {
            "${String.format("%.1f", value / 1000)}${if (useMetric) "t" else "k lbs"}"
        } else {
            "${String.format("%.0f", value)} ${if (useMetric) "kg" else "lbs"}"
        }
    }

    fun daysAgo(millis: Long): Int {
        val diff = System.currentTimeMillis() - millis
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }

    fun getWeekStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getMonthStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getMonthStartMillis(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getMonthEndMillis(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.MONTH, 1)
        return cal.timeInMillis - 1
    }
}

// ═══════════════════════════════════════════════════════════════
// ONE REP MAX CALCULATOR
// ═══════════════════════════════════════════════════════════════
object OneRepMaxCalculator {
    fun epley(weight: Double, reps: Int): Double {
        if (reps <= 0) return 0.0
        if (reps == 1) return weight
        return weight * (1 + reps / 30.0)
    }

    fun brzycki(weight: Double, reps: Int): Double {
        if (reps <= 0) return 0.0
        if (reps == 1) return weight
        return weight * (36.0 / (37.0 - reps))
    }

    fun lombardi(weight: Double, reps: Int): Double {
        if (reps <= 0) return 0.0
        if (reps == 1) return weight
        return weight * Math.pow(reps.toDouble(), 0.1)
    }

    fun average(weight: Double, reps: Int): Double {
        return (epley(weight, reps) + brzycki(weight, reps) + lombardi(weight, reps)) / 3.0
    }

    fun percentages(oneRepMax: Double): List<Pair<Int, Double>> {
        return listOf(100, 95, 90, 85, 80, 75, 70, 65, 60, 55, 50).map { pct ->
            pct to (oneRepMax * pct / 100.0).roundToRoundedWeight()
        }
    }

    private fun Double.roundToRoundedWeight(): Double {
        return (this * 4).roundToInt() / 4.0 // Round to nearest 0.25
    }
}

// ═══════════════════════════════════════════════════════════════
// REST TIMER SERVICE
// ═══════════════════════════════════════════════════════════════
class RestTimerService : Service() {
    private var timer: CountDownTimer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val seconds = intent?.getIntExtra("seconds", 90) ?: 90

        val notification = NotificationCompat.Builder(this, GymTrackerApp.CHANNEL_WORKOUT)
            .setContentTitle("Rest Timer")
            .setContentText("Resting: ${FormatUtils.formatDuration(seconds)}")
            .setSmallIcon(android.R.drawable.ic_media_pause)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        timer?.cancel()
        timer = object : CountDownTimer(seconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remaining = (millisUntilFinished / 1000).toInt()
                updateNotification(remaining)
            }

            override fun onFinish() {
                onTimerComplete()
            }
        }
        timer?.start()

        return START_REDELIVER_INTENT
    }

    private fun updateNotification(seconds: Int) {
        val notification = NotificationCompat.Builder(this, GymTrackerApp.CHANNEL_WORKOUT)
            .setContentTitle("Rest Timer")
            .setContentText("Time remaining: ${FormatUtils.formatDuration(seconds)}")
            .setSmallIcon(android.R.drawable.ic_media_pause)
            .setOngoing(true)
            .build()

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun onTimerComplete() {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300), -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 300, 200, 300), -1)
            }
        }

        val doneNotification = NotificationCompat.Builder(this, GymTrackerApp.CHANNEL_REST_TIMER)
            .setContentTitle("Rest Complete!")
            .setContentText("Time to hit your next set 💪")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setAutoCancel(true)
            .build()

        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID + 1, doneNotification)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}

// ═══════════════════════════════════════════════════════════════
// WORKOUT WIDGET RECEIVER (STUB)
// ═══════════════════════════════════════════════════════════════
class WorkoutWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Widget update logic - handled by Glance in real implementation
    }
}
