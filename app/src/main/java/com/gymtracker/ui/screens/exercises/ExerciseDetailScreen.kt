package com.gymtracker.ui.screens.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.gymtracker.data.database.entities.ExerciseEntity
import com.gymtracker.data.database.entities.PersonalRecordEntity
import com.gymtracker.data.database.entities.WorkoutSetEntity
import com.gymtracker.ui.components.*
import com.gymtracker.ui.theme.*
import com.gymtracker.util.FormatUtils
import com.gymtracker.util.OneRepMaxCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExerciseDetailState(
    val exercise: ExerciseEntity? = null,
    val setHistory: List<WorkoutSetEntity> = emptyList(),
    val personalRecords: List<PersonalRecordEntity> = emptyList(),
    val estimated1RM: Double = 0.0,
    val useMetric: Boolean = true
)

class ExerciseDetailViewModel(app: GymTrackerApp, private val exerciseId: Long) : ViewModel() {
    private val repo = app.repository
    private val _state = MutableStateFlow(ExerciseDetailState())
    val state: StateFlow<ExerciseDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val exercise = repo.getExerciseById(exerciseId)
            _state.update { it.copy(exercise = exercise) }
        }
        viewModelScope.launch {
            repo.getSetHistory(exerciseId).collect { history ->
                val best = history.filter { it.isCompleted }.maxByOrNull { it.weight * (1 + it.reps / 30.0) }
                val orm = best?.let { OneRepMaxCalculator.epley(it.weight, it.reps) } ?: 0.0
                _state.update { it.copy(setHistory = history.take(50), estimated1RM = orm) }
            }
        }
        viewModelScope.launch {
            repo.getPRsForExercise(exerciseId).collect { prs ->
                _state.update { it.copy(personalRecords = prs) }
            }
        }
        viewModelScope.launch {
            repo.getPreferences().collect { p ->
                _state.update { it.copy(useMetric = p?.useMetric ?: true) }
            }
        }
    }
}

class ExerciseDetailViewModelFactory(private val exerciseId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ExerciseDetailViewModel(GymTrackerApp.instance, exerciseId) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: Long,
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = viewModel(factory = ExerciseDetailViewModelFactory(exerciseId))
) {
    val state by viewModel.state.collectAsState()
    val exercise = state.exercise

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Exercise") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (exercise == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exercise Info Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChipLabel(exercise.primaryMuscleGroup, getMuscleColor(exercise.primaryMuscleGroup))
                            ChipLabel(exercise.equipmentType, MaterialTheme.colorScheme.secondary)
                            ChipLabel(exercise.difficulty, when(exercise.difficulty) {
                                "Beginner" -> SuccessGreen
                                "Intermediate" -> WarningOrange
                                else -> ErrorRed
                            })
                            ChipLabel(exercise.movementType, NeonPurple)
                        }
                        if (exercise.secondaryMuscleGroups.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Secondary: ${exercise.secondaryMuscleGroups}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 1RM & Records
            item {
                SectionHeader(title = "Performance")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Est. 1RM",
                        value = if (state.estimated1RM > 0) FormatUtils.formatWeight(state.estimated1RM, state.useMetric) else "--",
                        icon = Icons.Filled.EmojiEvents,
                        color = NeonYellow,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "PRs",
                        value = "${state.personalRecords.size}",
                        icon = Icons.Filled.Star,
                        color = NeonGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 1RM Calculator
            item {
                OneRepMaxCalculatorCard(useMetric = state.useMetric)
            }

            // Instructions
            item {
                SectionHeader(title = "Instructions")
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        exercise.instructions,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // History
            if (state.setHistory.isNotEmpty()) {
                item { SectionHeader(title = "Recent Sets") }
                items(state.setHistory.take(20)) { set ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (set.isPersonalRecord) {
                                    Icon(Icons.Filled.Star, null, tint = NeonYellow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    "${FormatUtils.formatWeight(set.weight, state.useMetric)} × ${set.reps}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(set.setType, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                set.rpe?.let {
                                    Text("RPE $it", style = MaterialTheme.typography.labelSmall, color = NeonOrange)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun OneRepMaxCalculatorCard(useMetric: Boolean) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Double?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("1RM Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberInputField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = "Weight",
                    suffix = if (useMetric) "kg" else "lbs",
                    modifier = Modifier.weight(1f)
                )
                NumberInputField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = "Reps",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val w = weight.toDoubleOrNull() ?: return@Button
                    val r = reps.toIntOrNull() ?: return@Button
                    result = OneRepMaxCalculator.epley(w, r)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Calculate")
            }
            result?.let { orm ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Estimated 1RM: ${String.format("%.1f", orm)} ${if (useMetric) "kg" else "lbs"}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Percentage table
                OneRepMaxCalculator.percentages(orm).take(6).forEach { (pct, wt) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$pct%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format("%.1f", wt)} ${if (useMetric) "kg" else "lbs"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
