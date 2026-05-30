package com.gymtracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.gymtracker.ui.navigation.MainNavigation
import com.gymtracker.ui.theme.AppTheme
import com.gymtracker.ui.theme.GymTrackerTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* result ignored */ }

    /** Android 13+ silently drops notifications unless POST_NOTIFICATIONS is granted at runtime. */
    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
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
