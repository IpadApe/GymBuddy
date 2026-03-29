package com.gymtracker.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.database.entities.WorkoutSessionEntity
import com.gymtracker.data.model.SplitType
import com.gymtracker.ui.components.*
import com.gymtracker.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkoutSetupViewModel(private val app: GymTrackerApp) : ViewModel() {
    private val repo = app.repository
    val templates = repo.getTemplates()
    val recentSessions = repo.getRecentSessions(5)

    fun startNewWorkout(name: String, splitType: String, onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val sessionId = repo.startWorkoutSession(name, splitType)
            onStarted(sessionId)
        }
    }

    fun startFromTemplate(templateId: Long, onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val template = repo.getSessionById(templateId) ?: return@launch
            val sessionId = repo.startWorkoutSession(template.name, template.splitType)
            // Copy exercises from template
            val templateExercises = repo.getExercisesWithSets(templateId).first()
            templateExercises.forEach { wxs ->
                val newExId = repo.addExerciseToWorkout(
                    sessionId, wxs.workoutExercise.exerciseId,
                    wxs.workoutExercise.orderIndex, wxs.workoutExercise.restTimeSeconds
                )
                wxs.sets.forEach { set ->
                    repo.addSet(newExId, set.setNumber, set.setType)
                }
            }
            onStarted(sessionId)
        }
    }
}

class WorkoutSetupViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WorkoutSetupViewModel(GymTrackerApp.instance) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSetupScreen(
    onStartSession: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: WorkoutSetupViewModel = viewModel(factory = WorkoutSetupViewModelFactory())
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    val templates by viewModel.templates.collectAsState(initial = emptyList())
    val recentSessions by viewModel.recentSessions.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Start Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick Start Splits
            item {
                SectionHeader(title = "Quick Start")
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(SplitType.entries.toList()) { split ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.startNewWorkout(split.displayName, split.name, onStartSession)
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (split) {
                                SplitType.PUSH -> Icons.AutoMirrored.Filled.ArrowForward
                                SplitType.PULL -> Icons.AutoMirrored.Filled.ArrowBack
                                SplitType.LEGS -> Icons.Filled.DirectionsWalk
                                SplitType.UPPER -> Icons.Filled.North
                                SplitType.LOWER -> Icons.Filled.South
                                SplitType.FULL_BODY -> Icons.Filled.AccessibilityNew
                                SplitType.CUSTOM -> Icons.Filled.Edit
                            },
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            split.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Custom workout
            item {
                OutlinedButton(
                    onClick = { showCustomDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Custom Workout Name")
                }
            }

            // Templates
            if (templates.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(title = "Templates")
                }
                items(templates) { template ->
                    WorkoutSessionCard(
                        name = template.name,
                        date = "Template",
                        duration = "--",
                        volume = "--",
                        splitType = template.splitType,
                        onClick = { viewModel.startFromTemplate(template.id, onStartSession) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Custom name dialog
    if (showCustomDialog) {
        var customName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Custom Workout") },
            text = {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Workout Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (customName.isNotBlank()) {
                        viewModel.startNewWorkout(customName, "CUSTOM", onStartSession)
                        showCustomDialog = false
                    }
                }) { Text("Start") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) { Text("Cancel") }
            }
        )
    }
}
