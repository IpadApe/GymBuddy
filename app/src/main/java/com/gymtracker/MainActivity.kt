package com.gymtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.gymtracker.ui.navigation.MainNavigation
import com.gymtracker.ui.theme.AppTheme
import com.gymtracker.ui.theme.GymTrackerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as GymTrackerApp

        setContent {
            var darkMode by remember { mutableStateOf(true) }
            var appTheme by remember { mutableStateOf(AppTheme.ORANGE) }
            var onboardingNeeded by remember { mutableStateOf<Boolean?>(null) }

            LaunchedEffect(Unit) {
                val prefs = app.repository.getPreferencesSync()
                darkMode = prefs?.darkMode ?: true
                appTheme = AppTheme.fromString(prefs?.colorTheme ?: "ORANGE")
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
