package com.gymtracker.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.BuildConfig
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.database.entities.UserPreferencesEntity
import com.gymtracker.ui.components.GradientButton
import com.gymtracker.ui.components.NumberInputField
import com.gymtracker.ui.components.SectionHeader
import com.gymtracker.ui.theme.*
import com.gymtracker.util.AppInstaller
import com.gymtracker.util.FormatUtils
import com.gymtracker.util.UpdateChecker
import com.gymtracker.util.UpdateInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class UpdateCheckState { IDLE, CHECKING, UP_TO_DATE, UPDATE_AVAILABLE, ERROR }

class SettingsViewModel(private val app: GymTrackerApp) : ViewModel() {
    private val repo = app.repository
    val prefs = repo.getPreferences()

    private val _updateCheckState = MutableStateFlow(UpdateCheckState.IDLE)
    val updateCheckState: StateFlow<UpdateCheckState> = _updateCheckState.asStateFlow()

    private val _availableUpdate = MutableStateFlow<UpdateInfo?>(null)
    val availableUpdate: StateFlow<UpdateInfo?> = _availableUpdate.asStateFlow()

    fun updatePrefs(transform: (UserPreferencesEntity) -> UserPreferencesEntity) {
        viewModelScope.launch {
            val current = repo.getPreferencesSync() ?: UserPreferencesEntity()
            repo.updatePreferences(transform(current))
        }
    }

    fun exportData() {
        viewModelScope.launch {
            val data = repo.exportAllData()
            // In production, serialize to JSON and write to file
        }
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            _updateCheckState.value = UpdateCheckState.CHECKING
            val info = UpdateChecker.checkForUpdate(BuildConfig.VERSION_CODE)
            if (info != null) {
                _availableUpdate.value = info
                _updateCheckState.value = UpdateCheckState.UPDATE_AVAILABLE
            } else {
                _updateCheckState.value = UpdateCheckState.UP_TO_DATE
            }
        }
    }
}

class SettingsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SettingsViewModel(GymTrackerApp.instance) as T
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory())
) {
    val prefs by viewModel.prefs.collectAsState(initial = null)
    val p = prefs ?: UserPreferencesEntity()
    var showPlateCalc by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 8.dp))
        }

        // Appearance
        item { SectionHeader(title = "Appearance") }
        item {
            SettingsToggle(
                icon = Icons.Filled.DarkMode,
                title = "Dark Mode",
                subtitle = "Use dark theme",
                checked = p.darkMode,
                onCheckedChange = { viewModel.updatePrefs { it.copy(darkMode = !it.darkMode) } }
            )
        }

        // Profile
        item { SectionHeader(title = "Profile") }
        item {
            SettingsToggle(
                icon = Icons.Filled.Person,
                title = "Female Body",
                subtitle = if (p.isFemale) "Showing female body map" else "Showing male body map",
                checked = p.isFemale,
                onCheckedChange = { viewModel.updatePrefs { it.copy(isFemale = !it.isFemale) } }
            )
        }

        // Units
        item { SectionHeader(title = "Units") }
        item {
            SettingsToggle(
                icon = Icons.Filled.Straighten,
                title = "Metric Units",
                subtitle = if (p.useMetric) "kg / cm" else "lbs / inches",
                checked = p.useMetric,
                onCheckedChange = { viewModel.updatePrefs { it.copy(useMetric = !it.useMetric) } }
            )
        }

        // Workout Defaults
        item { SectionHeader(title = "Workout Defaults") }
        item {
            SettingsValue(
                icon = Icons.Filled.Timer,
                title = "Default Rest Time",
                value = FormatUtils.formatDuration(p.defaultRestTimeSeconds)
            )
        }

        // Notifications
        item { SectionHeader(title = "Notifications") }
        item {
            SettingsToggle(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "Sound Alerts",
                subtitle = "Play sound when rest timer completes",
                checked = p.soundEnabled,
                onCheckedChange = { viewModel.updatePrefs { it.copy(soundEnabled = !it.soundEnabled) } }
            )
        }
        item {
            SettingsToggle(
                icon = Icons.Filled.Vibration,
                title = "Vibration",
                subtitle = "Vibrate on rest timer completion and PRs",
                checked = p.vibrationEnabled,
                onCheckedChange = { viewModel.updatePrefs { it.copy(vibrationEnabled = !it.vibrationEnabled) } }
            )
        }

        // Tools
        item { SectionHeader(title = "Tools") }
        item {
            Card(
                onClick = { showPlateCalc = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Calculate, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Plate Calculator", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Calculate which plates to load", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Data
        item { SectionHeader(title = "Data") }
        item {
            Card(
                onClick = { viewModel.exportData() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Upload, null, tint = BlueTrust)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Export Data", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Backup workout data as JSON", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // About
        item { SectionHeader(title = "About") }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("GymTracker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Free forever. No accounts. No subscriptions. Your data stays on your device.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    if (showPlateCalc) {
        PlateCalculatorDialog(useMetric = p.useMetric, onDismiss = { showPlateCalc = false })
    }
}

@Composable
fun SettingsToggle(
    icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = { onCheckedChange() },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
        }
    }
}

@Composable
fun SettingsValue(icon: ImageVector, title: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PlateCalculatorDialog(useMetric: Boolean, onDismiss: () -> Unit) {
    var targetWeight by remember { mutableStateOf("") }
    var barWeight by remember { mutableStateOf(if (useMetric) "20" else "45") }
    var result by remember { mutableStateOf<com.gymtracker.data.model.PlateResult?>(null) }
    val repo = GymTrackerApp.instance.repository

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plate Calculator") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberInputField(targetWeight, { targetWeight = it }, "Target",
                        suffix = if(useMetric) "kg" else "lbs", modifier = Modifier.weight(1f))
                    NumberInputField(barWeight, { barWeight = it }, "Bar",
                        suffix = if(useMetric) "kg" else "lbs", modifier = Modifier.weight(1f))
                }
                Button(
                    onClick = {
                        val t = targetWeight.toDoubleOrNull() ?: return@Button
                        val b = barWeight.toDoubleOrNull() ?: return@Button
                        result = repo.calculatePlates(t, b, useMetric)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Calculate") }

                result?.let { r ->
                    HorizontalDivider()
                    Text("Per side:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    r.platesPerSide.forEach { (plate, count) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${plate}${if(useMetric) "kg" else "lbs"}", style = MaterialTheme.typography.bodyMedium)
                            Text("× $count", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (!r.isExact) {
                        Text("Closest achievable: ${r.achievableWeight}${if(useMetric) "kg" else "lbs"}",
                            style = MaterialTheme.typography.bodySmall, color = WarningOrange)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}
