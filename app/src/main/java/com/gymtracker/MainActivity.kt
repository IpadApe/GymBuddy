package com.gymtracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gymtracker.ui.navigation.MainNavigation
import com.gymtracker.ui.theme.AppTheme
import com.gymtracker.ui.theme.GymTrackerTheme

private const val REQ_NOTIFICATIONS = 1001

class MainActivity : ComponentActivity() {

    /** Android 13+ silently drops notifications unless POST_NOTIFICATIONS is granted at runtime. */
    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQ_NOTIFICATIONS
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ensureNotificationPermission()

        val app = application as GymTrackerApp

        setContent {
            var darkMode by remember { mutableStateOf(true) }
            var appTheme by remember { mutableStateOf(AppTheme.INDIGO) }
            var onboardingNeeded by remember { mutableStateOf<Boolean?>(null) }

            LaunchedEffect(Unit) {
                val prefs = app.repository.getPreferencesSync()
                darkMode = prefs?.darkMode ?: true
                appTheme = AppTheme.fromString(prefs?.colorTheme ?: "INDIGO")
                onboardingNeeded = prefs?.onboardingCompleted != true

                // Observe preference changes
                app.repository.getPreferences().collect { prefs ->
                    prefs?.let {
                        darkMode = it.darkMode
                        appTheme = AppTheme.fromString(it.colorTheme)
                    }
                }
            }

            GymTrackerTheme(darkTheme = darkMode, appTheme = appTheme) {
                if (onboardingNeeded != null) {
                    MainNavigation(startOnboarding = onboardingNeeded == true)
                }
            }
        }
    }
}
