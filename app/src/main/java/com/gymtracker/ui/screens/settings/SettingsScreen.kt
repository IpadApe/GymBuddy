package com.gymtracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

    fun saveColorTheme(theme: AppTheme) {
        updatePrefs { it.copy(colorTheme = theme.name) }
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
    val updateState by viewModel.updateCheckState.collectAsState()
    val availableUpdate by viewModel.availableUpdate.collectAsState()
    val context = LocalContext.current
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
        item {
            ThemePickerCard(
                selectedTheme = AppTheme.fromString(p.colorTheme),
                onThemeSelected = { viewModel.saveColorTheme(it) }
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GymBuddy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Green dot = up to date, orange dot = update available
                        if (updateState == UpdateCheckState.UP_TO_DATE) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                        } else if (updateState == UpdateCheckState.UPDATE_AVAILABLE) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFFFA726), CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Free forever. No accounts. No subscriptions. Your data stays on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Update checker
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (updateState) {
                                UpdateCheckState.UPDATE_AVAILABLE -> Icons.Filled.SystemUpdate
                                UpdateCheckState.UP_TO_DATE -> Icons.Filled.CheckCircle
                                UpdateCheckState.ERROR -> Icons.Filled.ErrorOutline
                                else -> Icons.Filled.Sync
                            },
                            contentDescription = null,
                            tint = when (updateState) {
                                UpdateCheckState.UPDATE_AVAILABLE -> Color(0xFFFFA726)
                                UpdateCheckState.UP_TO_DATE -> Color(0xFF4CAF50)
                                UpdateCheckState.ERROR -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                when (updateState) {
                                    UpdateCheckState.IDLE -> "Check for Updates"
                                    UpdateCheckState.CHECKING -> "Checking…"
                                    UpdateCheckState.UP_TO_DATE -> "Up to date"
                                    UpdateCheckState.UPDATE_AVAILABLE -> "Update available — v${availableUpdate?.versionName}"
                                    UpdateCheckState.ERROR -> "Could not check for updates"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                when (updateState) {
                                    UpdateCheckState.IDLE -> "Tap to check for a newer version"
                                    UpdateCheckState.CHECKING -> "Contacting update server…"
                                    UpdateCheckState.UP_TO_DATE -> "You're on the latest version"
                                    UpdateCheckState.UPDATE_AVAILABLE -> availableUpdate?.releaseNotes?.lines()?.firstOrNull() ?: "Tap Download to install"
                                    UpdateCheckState.ERROR -> "Check your internet connection"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (updateState == UpdateCheckState.CHECKING) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }

                    // Release notes (when update available)
                    val notes = availableUpdate?.releaseNotes
                    if (updateState == UpdateCheckState.UPDATE_AVAILABLE && !notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "What's new",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (updateState == UpdateCheckState.UPDATE_AVAILABLE && availableUpdate != null) {
                        Button(
                            onClick = {
                                AppInstaller.downloadAndInstall(
                                    context,
                                    availableUpdate!!.downloadUrl,
                                    availableUpdate!!.versionName
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                        ) {
                            Icon(Icons.Filled.Download, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Download & Install")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.checkForUpdate() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            enabled = updateState != UpdateCheckState.CHECKING
                        ) {
                            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (updateState == UpdateCheckState.UP_TO_DATE) "Check again" else "Check for Updates")
                        }
                    }
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
fun ThemePickerCard(selectedTheme: AppTheme, onThemeSelected: (AppTheme) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Palette, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Accent Color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Current: ${selectedTheme.label}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(AppTheme.entries) { theme ->
                    val isSelected = theme == selectedTheme
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .background(theme.previewColor, CircleShape)
                            .then(
                                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            .clickable { onThemeSelected(theme) }
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = theme.label,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
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
