package com.gymtracker.ui.screens.routines

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
import com.gymtracker.data.database.entities.*
import com.gymtracker.ui.components.*
import com.gymtracker.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RoutineDetailState(
    val routine: RoutineEntity? = null,
    val days: List<RoutineDayEntity> = emptyList(),
    val dayExercises: Map<Long, List<RoutineDayExerciseEntity>> = emptyMap(),
    val exerciseNames: Map<Long, String> = emptyMap()
)

class RoutineDetailViewModel(private val app: GymTrackerApp, private val routineId: Long) : ViewModel() {
    private val repo = app.repository
    private val _state = MutableStateFlow(RoutineDetailState())
    val state: StateFlow<RoutineDetailState> = _state.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            val routine = repo.getRoutineById(routineId)
            _state.update { it.copy(routine = routine) }
        }
        viewModelScope.launch {
            repo.getDaysForRoutine(routineId).collect { days ->
                _state.update { it.copy(days = days) }
                // Load exercises for all days — use .first() so the loop isn't blocked by each day's flow
                val dayExMap = mutableMapOf<Long, List<RoutineDayExerciseEntity>>()
                val nameMap = _state.value.exerciseNames.toMutableMap()
                days.forEach { day ->
                    val exercises = repo.getExercisesForRoutineDay(day.id).first()
                    dayExMap[day.id] = exercises
                    exercises.forEach { rde ->
                        if (!nameMap.containsKey(rde.exerciseId)) {
                            val ex = repo.getExerciseById(rde.exerciseId)
                            if (ex != null) nameMap[rde.exerciseId] = ex.name
                        }
                    }
                }
                _state.update { it.copy(dayExercises = dayExMap, exerciseNames = nameMap) }
            }
        }
    }

    fun startWorkoutFromDay(dayId: Long, onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val dayWithExercises = repo.getDayWithExercises(dayId) ?: return@launch
            val routineName = _state.value.routine?.name ?: "Workout"
            val sessionId = repo.startWorkoutSession(
                name = "$routineName - ${dayWithExercises.day.dayName}",
                splitType = dayWithExercises.day.splitType
            )
            dayWithExercises.exercises.sortedBy { it.orderIndex }.forEachIndexed { index, rde ->
                val workoutExId = repo.addExerciseToWorkout(
                    sessionId, rde.exerciseId, index, rde.restTimeSeconds
                )
                repeat(rde.targetSets) { setIndex ->
                    repo.addSet(workoutExId, setIndex + 1)
                }
            }
            onStarted(sessionId)
        }
    }
}

class RoutineDetailViewModelFactory(private val app: GymTrackerApp, private val routineId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RoutineDetailViewModel(app, routineId) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    routineId: Long,
    onBack: () -> Unit,
    onStartWorkout: (Long) -> Unit,
    app: GymTrackerApp
) {
    val viewModel: RoutineDetailViewModel = viewModel(
        factory = RoutineDetailViewModelFactory(app, routineId),
        key = "routine_$routineId"
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.routine?.name ?: "Routine Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.routine != null) {
                item {
                    Text(
                        state.routine!!.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(state.days) { day ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                day.dayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { viewModel.startWorkoutFromDay(day.id, onStartWorkout) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        val exercises = state.dayExercises[day.id] ?: emptyList()
                        exercises.forEach { rde ->
                            val name = state.exerciseNames[rde.exerciseId] ?: "Exercise #${rde.exerciseId}"
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text(
                                    "${rde.targetSets} × ${rde.targetReps}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (rde != exercises.last()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
