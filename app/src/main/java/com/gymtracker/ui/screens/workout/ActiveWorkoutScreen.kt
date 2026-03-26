package com.gymtracker.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.database.entities.*
import com.gymtracker.data.model.MuscleGroup
import com.gymtracker.data.model.SetType
import com.gymtracker.ui.components.*
import com.gymtracker.util.FormatUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ═══════════════════════════════════════════════════════════════
// DATA CLASSES
// ═══════════════════════════════════════════════════════════════

data class ExerciseWithSetsAndInfo(
    val workoutExercise: WorkoutExerciseEntity,
    val exercise: ExerciseEntity,
    val sets: List<WorkoutSetEntity>
)

data class SetEditState(
    val weightText: String = "",
    val repsText: String = "",
    val rpe: Float? = null,
    val isPrefilled: Boolean = false  // true = values copied from history, shown in grey
)

enum class ExerciseInputMode { WEIGHT_AND_REPS, REPS_ONLY, TIME_ONLY }

private val cardioKeywords = listOf("run", "jog", "sprint", "cardio", "cycling", "bike", "swim", "walk", "treadmill", "elliptical", "stair")

fun getExerciseInputMode(exercise: ExerciseEntity): ExerciseInputMode {
    if (cardioKeywords.any { exercise.name.lowercase().contains(it) }) return ExerciseInputMode.TIME_ONLY
    if (exercise.equipmentType.equals("Bodyweight", ignoreCase = true)) return ExerciseInputMode.REPS_ONLY
    return ExerciseInputMode.WEIGHT_AND_REPS
}

// ═══════════════════════════════════════════════════════════════
// VIEW MODEL
// ═══════════════════════════════════════════════════════════════

class ActiveWorkoutViewModel(
    private val app: GymTrackerApp,
    private val sessionId: Long
) : ViewModel() {

    private val repo = app.repository

    val session = flow { emit(repo.getSessionById(sessionId)) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val exercises = combine(
        repo.getExerciseDetails(sessionId),
        repo.getExercisesWithSets(sessionId)
    ) { details, withSets ->
        details.map { detail ->
            val sets = withSets
                .find { it.workoutExercise.id == detail.workoutExercise.id }
                ?.sets ?: emptyList()
            ExerciseWithSetsAndInfo(detail.workoutExercise, detail.exercise, sets)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()

    private val _restSecondsRemaining = MutableStateFlow(0)
    val restSecondsRemaining = _restSecondsRemaining.asStateFlow()
    private val _restTimerTotal = MutableStateFlow(90)
    val restTimerTotal = _restTimerTotal.asStateFlow()
    private val _restTimerActive = MutableStateFlow(false)
    val restTimerActive = _restTimerActive.asStateFlow()
    private var restTimerJob: Job? = null

    private val _setEditStates = MutableStateFlow<Map<Long, SetEditState>>(emptyMap())
    val setEditStates = _setEditStates.asStateFlow()

    // Map<exerciseId, previous session's sets> for PREVIOUS column
    private val _previousSetsMap = MutableStateFlow<Map<Long, List<WorkoutSetEntity>>>(emptyMap())
    val previousSetsMap = _previousSetsMap.asStateFlow()

    // Exercise history for graph sheet
    private val _historyExerciseId = MutableStateFlow<Long?>(null)
    val exerciseHistory = _historyExerciseId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.getSetHistory(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises = repo.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var elapsedTimerJob: Job? = null

    init {
        elapsedTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value++
            }
        }
        // Auto-init edit states and load previous sets when exercises update
        viewModelScope.launch {
            exercises.collect { exList ->
                val current = _setEditStates.value.toMutableMap()
                val prevMap = mutableMapOf<Long, List<WorkoutSetEntity>>()
                exList.forEach { ex ->
                    ex.sets.forEach { set ->
                        if (!current.containsKey(set.id)) {
                            current[set.id] = SetEditState(
                                weightText = formatWeightInput(set.weight),
                                repsText = if (set.reps > 0) set.reps.toString() else "",
                                rpe = set.rpe
                            )
                        }
                    }
                    prevMap[ex.exercise.id] = repo.getPreviousSetsForExercise(ex.exercise.id, sessionId)
                }
                _setEditStates.value = current
                _previousSetsMap.value = prevMap
            }
        }
    }

    private fun formatWeightInput(weight: Double): String {
        if (weight <= 0.0) return ""
        return if (weight == weight.toLong().toDouble()) weight.toLong().toString()
        else weight.toString()
    }

    fun updateSetWeight(setId: Long, weight: String) {
        _setEditStates.value = _setEditStates.value +
                (setId to (_setEditStates.value[setId] ?: SetEditState()).copy(weightText = weight, isPrefilled = false))
    }

    fun updateSetReps(setId: Long, reps: String) {
        _setEditStates.value = _setEditStates.value +
                (setId to (_setEditStates.value[setId] ?: SetEditState()).copy(repsText = reps, isPrefilled = false))
    }

    fun updateSetRpe(setId: Long, rpe: Float?) {
        _setEditStates.value = _setEditStates.value +
                (setId to (_setEditStates.value[setId] ?: SetEditState()).copy(rpe = rpe))
    }

    fun changeSetType(set: WorkoutSetEntity) {
        viewModelScope.launch {
            val types = SetType.entries
            val currentIndex = types.indexOfFirst { it.name == set.setType }.coerceAtLeast(0)
            val nextType = types[(currentIndex + 1) % types.size]
            repo.updateSet(set.copy(setType = nextType.name))
        }
    }

    fun completeSet(set: WorkoutSetEntity, restSeconds: Int) {
        viewModelScope.launch {
            val editState = _setEditStates.value[set.id] ?: SetEditState()
            val weight = editState.weightText.toDoubleOrNull() ?: 0.0
            val reps = editState.repsText.toIntOrNull() ?: 0
            val updated = set.copy(weight = weight, reps = reps, rpe = editState.rpe)
            repo.completeSet(updated)
            startRestTimer(restSeconds)
        }
    }

    fun addSet(workoutExerciseId: Long, exerciseId: Long, currentSets: List<WorkoutSetEntity>) {
        viewModelScope.launch {
            val newSetId = repo.addSet(workoutExerciseId, currentSets.size + 1)
            val lastSet = currentSets.lastOrNull { it.isCompleted }
                ?: repo.getSetHistory(exerciseId).first().firstOrNull { it.isCompleted }
            if (lastSet != null && (lastSet.weight > 0 || lastSet.reps > 0)) {
                _setEditStates.value = _setEditStates.value + (newSetId to SetEditState(
                    weightText = formatWeightInput(lastSet.weight),
                    repsText = if (lastSet.reps > 0) lastSet.reps.toString() else "",
                    isPrefilled = true
                ))
            }
        }
    }

    fun deleteSet(set: WorkoutSetEntity) {
        viewModelScope.launch {
            repo.deleteSet(set)
            _setEditStates.value = _setEditStates.value - set.id
        }
    }

    fun addExercise(exerciseId: Long) {
        viewModelScope.launch {
            val workoutExId = repo.addExerciseToWorkout(sessionId, exerciseId, exercises.value.size)
            repo.addSet(workoutExId, 1)
        }
    }

    fun removeExercise(workoutExercise: WorkoutExerciseEntity) {
        viewModelScope.launch {
            repo.removeExerciseFromWorkout(workoutExercise)
        }
    }

    fun changeRestTime(workoutExercise: WorkoutExerciseEntity, seconds: Int) {
        viewModelScope.launch {
            repo.updateWorkoutExercise(workoutExercise.copy(restTimeSeconds = seconds))
        }
    }

    fun addWarmUpSets(workoutExercise: WorkoutExerciseEntity, currentSets: List<WorkoutSetEntity>) {
        viewModelScope.launch {
            val startNum = currentSets.size + 1
            repo.addSet(workoutExercise.id, startNum, SetType.WARM_UP.name)
            repo.addSet(workoutExercise.id, startNum + 1, SetType.WARM_UP.name)
        }
    }

    fun updateExerciseNote(workoutExercise: WorkoutExerciseEntity, note: String) {
        viewModelScope.launch {
            repo.updateWorkoutExercise(workoutExercise.copy(notes = note))
        }
    }

    fun replaceExercise(workoutExercise: WorkoutExerciseEntity, newExerciseId: Long) {
        viewModelScope.launch {
            repo.updateWorkoutExercise(workoutExercise.copy(exerciseId = newExerciseId))
        }
    }

    fun showHistoryFor(exerciseId: Long?) {
        _historyExerciseId.value = exerciseId
    }

    private fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _restTimerTotal.value = seconds
        _restSecondsRemaining.value = seconds
        _restTimerActive.value = true
        restTimerJob = viewModelScope.launch {
            while (_restSecondsRemaining.value > 0) {
                delay(1000)
                _restSecondsRemaining.value--
            }
            _restTimerActive.value = false
        }
    }

    fun cancelRestTimer() {
        restTimerJob?.cancel()
        _restTimerActive.value = false
        _restSecondsRemaining.value = 0
    }

    private val _workoutCount = MutableStateFlow(0)
    val workoutCount = _workoutCount.asStateFlow()
    private val _streak = MutableStateFlow(0)
    val streak = _streak.asStateFlow()

    // Frozen snapshot of elapsed time set the moment the workout is finished
    private val _finalElapsedSeconds = MutableStateFlow(0)
    val finalElapsedSeconds = _finalElapsedSeconds.asStateFlow()

    fun finishAndLoadStats() {
        // Snapshot + cancel SYNCHRONOUSLY before launching the suspend work
        _finalElapsedSeconds.value = _elapsedSeconds.value
        elapsedTimerJob?.cancel()
        cancelRestTimer()
        viewModelScope.launch {
            repo.finishWorkoutSession(sessionId)
            _workoutCount.value = repo.getCompletedWorkoutCount()
            _streak.value = repo.getWorkoutStreak()
        }
    }
}

class ActiveWorkoutViewModelFactory(
    private val app: GymTrackerApp,
    private val sessionId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ActiveWorkoutViewModel(app, sessionId) as T
    }
}

// ═══════════════════════════════════════════════════════════════
// ACTIVE WORKOUT SCREEN
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    sessionId: Long,
    onFinish: () -> Unit,
    app: GymTrackerApp
) {
    val viewModel: ActiveWorkoutViewModel = viewModel(
        factory = ActiveWorkoutViewModelFactory(app, sessionId)
    )
    val session by viewModel.session.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val restActive by viewModel.restTimerActive.collectAsState()
    val restRemaining by viewModel.restSecondsRemaining.collectAsState()
    val restTotal by viewModel.restTimerTotal.collectAsState()
    val setEditStates by viewModel.setEditStates.collectAsState()
    val previousSetsMap by viewModel.previousSetsMap.collectAsState()
    val exerciseHistory by viewModel.exerciseHistory.collectAsState()
    val workoutCount by viewModel.workoutCount.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val finalElapsedSeconds by viewModel.finalElapsedSeconds.collectAsState()

    var showExercisePicker by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(false) }
    var replaceTargetExercise by remember { mutableStateOf<WorkoutExerciseEntity?>(null) }
    var historyExercise by remember { mutableStateOf<ExerciseWithSetsAndInfo?>(null) }
    // Multi-add: track which exercises were added in this picker session
    var addedInPicker by remember { mutableStateOf(setOf<Long>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            session?.name ?: "Workout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            FormatUtils.formatDuration(elapsedSeconds),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showFinishDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Finish", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showExercisePicker = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "Add Exercise")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Sticky rest timer — lives outside the scroll area
            if (restActive) {
                RestTimerDisplay(
                    secondsRemaining = restRemaining,
                    totalSeconds = restTotal,
                    onCancel = { viewModel.cancelRestTimer() },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            // Empty state
            if (exercises.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Filled.FitnessCenter,
                        title = "No Exercises Yet",
                        subtitle = "Tap + to add exercises to your workout",
                        modifier = Modifier.fillParentMaxHeight(0.6f)
                    )
                }
            } else {
                items(exercises, key = { it.workoutExercise.id }) { ex ->
                    val prevSets = previousSetsMap[ex.exercise.id] ?: emptyList()
                    ExerciseSessionCard(
                        exerciseData = ex,
                        setEditStates = setEditStates,
                        previousSets = prevSets,
                        onUpdateWeight = { setId, w -> viewModel.updateSetWeight(setId, w) },
                        onUpdateReps = { setId, r -> viewModel.updateSetReps(setId, r) },
                        onUpdateRpe = { setId, rpe -> viewModel.updateSetRpe(setId, rpe) },
                        onChangeSetType = { set -> viewModel.changeSetType(set) },
                        onCompleteSet = { set ->
                            viewModel.completeSet(set, ex.workoutExercise.restTimeSeconds)
                        },
                        onDeleteSet = { set -> viewModel.deleteSet(set) },
                        onAddSet = { viewModel.addSet(ex.workoutExercise.id, ex.exercise.id, ex.sets) },
                        onRemoveExercise = { viewModel.removeExercise(ex.workoutExercise) },
                        onChangeRestTime = { seconds -> viewModel.changeRestTime(ex.workoutExercise, seconds) },
                        onAddWarmUpSets = { viewModel.addWarmUpSets(ex.workoutExercise, ex.sets) },
                        onUpdateNote = { note -> viewModel.updateExerciseNote(ex.workoutExercise, note) },
                        onReplaceExercise = { replaceTargetExercise = ex.workoutExercise },
                        onViewHistory = {
                            viewModel.showHistoryFor(ex.exercise.id)
                            historyExercise = ex
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
            } // end LazyColumn
        } // end Column
    }

    // Add / Replace Exercise picker
    if (showExercisePicker || replaceTargetExercise != null) {
        val allExercises by viewModel.allExercises.collectAsState()
        val isReplaceMode = replaceTargetExercise != null
        ExercisePickerBottomSheet(
            exercises = allExercises,
            addedExerciseIds = if (isReplaceMode) emptySet() else addedInPicker,
            onExerciseSelected = { exercise ->
                if (isReplaceMode) {
                    viewModel.replaceExercise(replaceTargetExercise!!, exercise.id)
                    replaceTargetExercise = null
                } else {
                    viewModel.addExercise(exercise.id)
                    // Add to tracking set — sheet stays open
                    addedInPicker = addedInPicker + exercise.id
                }
            },
            onDismiss = {
                showExercisePicker = false
                replaceTargetExercise = null
                addedInPicker = emptySet()  // reset for next open
            }
        )
    }

    // Exercise history bottom sheet
    if (historyExercise != null) {
        ExerciseHistorySheet(
            exercise = historyExercise!!.exercise,
            history = exerciseHistory,
            inputMode = getExerciseInputMode(historyExercise!!.exercise),
            onDismiss = {
                historyExercise = null
                viewModel.showHistoryFor(null)
            }
        )
    }

    // Finish confirmation dialog
    if (showFinishDialog) {
        val totalSets = exercises.sumOf { it.sets.size }
        val completedSets = exercises.sumOf { it.sets.count { s -> s.isCompleted } }
        val hasUnfinished = completedSets < totalSets && totalSets > 0

        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = {
                Text(if (hasUnfinished) "Unfinished Workout" else "Finish Workout?")
            },
            text = {
                if (hasUnfinished) {
                    Text("You have ${totalSets - completedSets} unfinished set${if (totalSets - completedSets != 1) "s" else ""}. Are you sure you want to finish?")
                } else {
                    Text("$completedSets / $totalSets sets completed · ${FormatUtils.formatDuration(elapsedSeconds)}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finishAndLoadStats()
                        showFinishDialog = false
                        showSummary = true
                    },
                    colors = if (hasUnfinished)
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    else ButtonDefaults.buttonColors()
                ) {
                    Text(if (hasUnfinished) "Yes, Finish" else "Finish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text(if (hasUnfinished) "No, Go Back" else "Cancel")
                }
            }
        )
    }

    // Full-screen summary shown after saving — uses frozen elapsed time
    if (showSummary) {
        WorkoutCompleteScreen(
            session = session,
            exercises = exercises,
            elapsedSeconds = finalElapsedSeconds,
            workoutCount = workoutCount,
            streak = streak,
            onClose = onFinish
        )
    }
}

private fun formatWeightDisplay(weight: Double): String {
    return if (weight == weight.toLong().toDouble()) weight.toLong().toString()
    else "%.1f".format(weight)
}

// ═══════════════════════════════════════════════════════════════
// EXERCISE SESSION CARD
// ═══════════════════════════════════════════════════════════════

@Composable
fun ExerciseSessionCard(
    exerciseData: ExerciseWithSetsAndInfo,
    setEditStates: Map<Long, SetEditState>,
    previousSets: List<WorkoutSetEntity> = emptyList(),
    onUpdateWeight: (Long, String) -> Unit,
    onUpdateReps: (Long, String) -> Unit,
    onUpdateRpe: (Long, Float?) -> Unit,
    onChangeSetType: (WorkoutSetEntity) -> Unit,
    onCompleteSet: (WorkoutSetEntity) -> Unit,
    onDeleteSet: (WorkoutSetEntity) -> Unit,
    onAddSet: () -> Unit,
    onRemoveExercise: () -> Unit,
    onChangeRestTime: (Int) -> Unit = {},
    onAddWarmUpSets: () -> Unit = {},
    onUpdateNote: (String) -> Unit = {},
    onReplaceExercise: () -> Unit = {},
    onViewHistory: () -> Unit = {}
) {
    var rpeExpandedForSetId by remember { mutableStateOf<Long?>(null) }
    var showRemoveConfirm by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showRestTimeDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

    val completedCount = exerciseData.sets.count { it.isCompleted }
    val totalCount = exerciseData.sets.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exerciseData.exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ChipLabel(
                            exerciseData.exercise.primaryMuscleGroup,
                            getMuscleColor(exerciseData.exercise.primaryMuscleGroup)
                        )
                        ChipLabel(
                            exerciseData.exercise.equipmentType,
                            MaterialTheme.colorScheme.secondary
                        )
                        // Rest time chip
                        ChipLabel(
                            "⏱ ${exerciseData.workoutExercise.restTimeSeconds}s",
                            MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (totalCount > 0) {
                        Text(
                            "$completedCount/$totalCount",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (completedCount == totalCount && totalCount > 0)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    // ⋯ Options menu
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                Icons.Filled.MoreVert,
                                "Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add Note") },
                                leadingIcon = { Icon(Icons.Filled.Edit, null, modifier = Modifier.size(18.dp)) },
                                onClick = { showOptionsMenu = false; showNoteDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Warm-up Sets") },
                                leadingIcon = { Icon(Icons.Filled.LocalFireDepartment, null, modifier = Modifier.size(18.dp)) },
                                onClick = { showOptionsMenu = false; onAddWarmUpSets() }
                            )
                            DropdownMenuItem(
                                text = { Text("Change Rest Time (${exerciseData.workoutExercise.restTimeSeconds}s)") },
                                leadingIcon = { Icon(Icons.Filled.Timer, null, modifier = Modifier.size(18.dp)) },
                                onClick = { showOptionsMenu = false; showRestTimeDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Exercise History") },
                                leadingIcon = { Icon(Icons.Filled.ShowChart, null, modifier = Modifier.size(18.dp)) },
                                onClick = { showOptionsMenu = false; onViewHistory() }
                            )
                            DropdownMenuItem(
                                text = { Text("Replace Exercise") },
                                leadingIcon = { Icon(Icons.Filled.SwapHoriz, null, modifier = Modifier.size(18.dp)) },
                                onClick = { showOptionsMenu = false; onReplaceExercise() }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Remove Exercise", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Delete, null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = { showOptionsMenu = false; showRemoveConfirm = true }
                            )
                        }
                    }
                }
            }

            // Note display
            if (exerciseData.workoutExercise.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    exerciseData.workoutExercise.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Column headers ───────────────────────────────────
            val inputMode = getExerciseInputMode(exerciseData.exercise)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SET",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "PREVIOUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.width(62.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.width(4.dp))
                if (inputMode != ExerciseInputMode.REPS_ONLY) {
                    Text(
                        if (inputMode == ExerciseInputMode.TIME_ONLY) "TIME" else "KG",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
                if (inputMode != ExerciseInputMode.TIME_ONLY) {
                    Text(
                        "REPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = if (inputMode == ExerciseInputMode.REPS_ONLY) Modifier.weight(2f) else Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
                // Space for check button + delete button
                Spacer(modifier = Modifier.width(74.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── Sets ─────────────────────────────────────────────
            exerciseData.sets.forEach { set ->
                val editState = setEditStates[set.id] ?: SetEditState()
                val prevSet = previousSets.getOrNull(set.setNumber - 1)
                SetRow(
                    set = set,
                    editState = editState,
                    inputMode = inputMode,
                    previousSet = prevSet,
                    rpeExpanded = rpeExpandedForSetId == set.id,
                    onWeightChange = { onUpdateWeight(set.id, it) },
                    onRepsChange = { onUpdateReps(set.id, it) },
                    onRpeChange = { onUpdateRpe(set.id, it) },
                    onToggleRpe = {
                        rpeExpandedForSetId = if (rpeExpandedForSetId == set.id) null else set.id
                    },
                    onChangeSetType = { onChangeSetType(set) },
                    onComplete = { onCompleteSet(set) },
                    onDelete = { onDeleteSet(set) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Add Set button ────────────────────────────────────
            OutlinedButton(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Set", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    // Remove exercise confirm dialog
    if (showRemoveConfirm) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirm = false },
            title = { Text("Remove Exercise?") },
            text = { Text("Remove ${exerciseData.exercise.name} and all its sets from this workout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveExercise()
                        showRemoveConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // Change rest time dialog
    if (showRestTimeDialog) {
        var sliderValue by remember {
            mutableFloatStateOf(exerciseData.workoutExercise.restTimeSeconds.toFloat())
        }
        AlertDialog(
            onDismissRequest = { showRestTimeDialog = false },
            title = { Text("Rest Time") },
            text = {
                Column {
                    Text(
                        "${sliderValue.toInt()} seconds",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 15f..300f,
                        steps = 18  // 15-second increments
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("15s", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("5 min", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onChangeRestTime(sliderValue.toInt())
                    showRestTimeDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRestTimeDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add/edit note dialog
    if (showNoteDialog) {
        var noteText by remember { mutableStateOf(exerciseData.workoutExercise.notes) }
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Note") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add a note for this exercise…") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(8.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateNote(noteText)
                    showNoteDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// SET ROW  (Strong-style: SET | PREVIOUS | KG | REPS | ✓)
// ═══════════════════════════════════════════════════════════════

@Composable
fun SetRow(
    set: WorkoutSetEntity,
    editState: SetEditState,
    inputMode: ExerciseInputMode = ExerciseInputMode.WEIGHT_AND_REPS,
    previousSet: WorkoutSetEntity? = null,
    rpeExpanded: Boolean,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onRpeChange: (Float?) -> Unit,
    onToggleRpe: () -> Unit,
    onChangeSetType: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val setTypeColor = when (set.setType) {
        SetType.WARM_UP.name  -> Color(0xFFFF9100)
        SetType.DROP_SET.name -> Color(0xFF448AFF)
        SetType.FAILURE.name  -> Color(0xFFE94560)
        else                  -> MaterialTheme.colorScheme.primary
    }
    val setTypeLabel = when (set.setType) {
        SetType.WARM_UP.name  -> "W"
        SetType.DROP_SET.name -> "D"
        SetType.FAILURE.name  -> "F"
        else                  -> "${set.setNumber}"
    }

    // Previous set display text
    val prevText = previousSet?.let { prev ->
        when (inputMode) {
            ExerciseInputMode.TIME_ONLY -> if (prev.weight > 0) "${formatWeightDisplay(prev.weight)} m" else "—"
            ExerciseInputMode.REPS_ONLY -> if (prev.reps > 0) "${prev.reps} reps" else "—"
            else -> if (prev.weight > 0 || prev.reps > 0)
                "${formatWeightDisplay(prev.weight)} kg\n× ${prev.reps}" else "—"
        }
    } ?: "—"

    // TextFieldValue with select-all when prefilled — so first keystroke replaces, not appends
    val weightSource = remember(set.id) { MutableInteractionSource() }
    val weightFocused by weightSource.collectIsFocusedAsState()
    var weightTFV by remember(set.id) {
        mutableStateOf(
            TextFieldValue(
                text = editState.weightText,
                selection = if (editState.isPrefilled) TextRange(0, editState.weightText.length)
                else TextRange(editState.weightText.length)
            )
        )
    }
    val repsSource = remember(set.id) { MutableInteractionSource() }
    val repsFocused by repsSource.collectIsFocusedAsState()
    var repsTFV by remember(set.id) {
        mutableStateOf(
            TextFieldValue(
                text = editState.repsText,
                selection = if (editState.isPrefilled) TextRange(0, editState.repsText.length)
                else TextRange(editState.repsText.length)
            )
        )
    }

    // Sync external state changes (new prefill arrived, etc.) but never override while focused
    LaunchedEffect(editState.weightText, editState.isPrefilled) {
        if (!weightFocused && weightTFV.text != editState.weightText) {
            weightTFV = TextFieldValue(
                text = editState.weightText,
                selection = if (editState.isPrefilled) TextRange(0, editState.weightText.length)
                else TextRange(editState.weightText.length)
            )
        }
    }
    LaunchedEffect(editState.repsText, editState.isPrefilled) {
        if (!repsFocused && repsTFV.text != editState.repsText) {
            repsTFV = TextFieldValue(
                text = editState.repsText,
                selection = if (editState.isPrefilled) TextRange(0, editState.repsText.length)
                else TextRange(editState.repsText.length)
            )
        }
    }

    val prefilledColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f)
    val normalColor = MaterialTheme.colorScheme.onSurface
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = normalColor,
        unfocusedTextColor = if (editState.isPrefilled) prefilledColor else normalColor,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = if (set.isCompleted)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.outline,
        disabledBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
    )
    val fieldTextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = if (editState.isPrefilled) FontWeight.Normal else FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (set.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                else Color.Transparent
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ── SET badge (tap to cycle type) ─────────────────
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(setTypeColor.copy(alpha = if (set.isCompleted) 0.20f else 0.10f))
                    .clickable(onClick = onChangeSetType),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    setTypeLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = setTypeColor
                )
            }

            // ── PREVIOUS column ───────────────────────────────
            Box(
                modifier = Modifier.width(62.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    prevText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
            }

            // ── WEIGHT / TIME field ───────────────────────────
            if (inputMode != ExerciseInputMode.REPS_ONLY) {
                val weightBorderColor = when {
                    weightFocused -> MaterialTheme.colorScheme.primary
                    set.isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    else -> MaterialTheme.colorScheme.outline
                }
                val weightTextColor = when {
                    set.isCompleted -> normalColor.copy(alpha = 0.80f)
                    editState.isPrefilled -> prefilledColor
                    else -> normalColor
                }
                BasicTextField(
                    value = weightTFV,
                    onValueChange = { nv: TextFieldValue ->
                        if (nv.text.isEmpty() || nv.text.matches(Regex("^\\d*\\.?\\d*$"))) {
                            weightTFV = nv
                            onWeightChange(nv.text)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .border(
                            width = if (weightFocused) 2.dp else 1.dp,
                            color = weightBorderColor,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    enabled = !set.isCompleted,
                    textStyle = fieldTextStyle.copy(color = weightTextColor),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    interactionSource = weightSource,
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (weightTFV.text.isEmpty()) {
                                Text(
                                    if (inputMode == ExerciseInputMode.TIME_ONLY) "min" else "kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            // ── REPS field ────────────────────────────────────
            if (inputMode != ExerciseInputMode.TIME_ONLY) {
                val repsBorderColor = when {
                    repsFocused -> MaterialTheme.colorScheme.primary
                    set.isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    else -> MaterialTheme.colorScheme.outline
                }
                val repsTextColor = when {
                    set.isCompleted -> normalColor.copy(alpha = 0.80f)
                    editState.isPrefilled -> prefilledColor
                    else -> normalColor
                }
                BasicTextField(
                    value = repsTFV,
                    onValueChange = { nv: TextFieldValue ->
                        if (nv.text.isEmpty() || nv.text.matches(Regex("^\\d+$"))) {
                            repsTFV = nv
                            onRepsChange(nv.text)
                        }
                    },
                    modifier = (if (inputMode == ExerciseInputMode.REPS_ONLY) Modifier.weight(2f) else Modifier.weight(1f))
                        .height(44.dp)
                        .border(
                            width = if (repsFocused) 2.dp else 1.dp,
                            color = repsBorderColor,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    enabled = !set.isCompleted,
                    textStyle = fieldTextStyle.copy(color = repsTextColor),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    interactionSource = repsSource,
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (repsTFV.text.isEmpty()) {
                                Text(
                                    "reps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            // ── RPE mini-button ───────────────────────────────
            if (!set.isCompleted) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (editState.rpe != null) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .clickable(onClick = onToggleRpe),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = editState.rpe?.let {
                            if (it == it.toInt().toFloat()) it.toInt().toString()
                            else "%.0f".format(it)
                        } ?: "RPE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = if (editState.rpe != null) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Check / Completed button ──────────────────────
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (set.isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    )
                    .clickable(enabled = !set.isCompleted, onClick = onComplete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = if (set.isCompleted) "Done" else "Complete",
                    tint = if (set.isCompleted) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // ── Delete (small, only while not completed) ──────
            if (!set.isCompleted) {
                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(
                        Icons.Filled.Close,
                        "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // ── RPE chips (expandable) ────────────────────────────
        AnimatedVisibility(
            visible = rpeExpanded && !set.isCompleted,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val rpeOptions = listOf(6f, 7f, 7.5f, 8f, 8.5f, 9f, 9.5f, 10f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 104.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rpeOptions.forEach { rpeVal ->
                    val isSelected = editState.rpe == rpeVal
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                            )
                            .clickable { onRpeChange(if (isSelected) null else rpeVal) }
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (rpeVal == rpeVal.toInt().toFloat()) rpeVal.toInt().toString()
                            else rpeVal.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// WORKOUT COMPLETE SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
fun WorkoutCompleteScreen(
    session: WorkoutSessionEntity?,
    exercises: List<ExerciseWithSetsAndInfo>,
    elapsedSeconds: Int,
    workoutCount: Int,
    streak: Int,
    onClose: () -> Unit
) {
    val completedExercises = exercises.filter { it.sets.any { s -> s.isCompleted } }
    val totalVolume = completedExercises.sumOf { ex -> ex.sets.filter { it.isCompleted }.sumOf { it.weight * it.reps } }
    val totalPRs = completedExercises.sumOf { ex -> ex.sets.count { it.isCompleted && it.isPersonalRecord } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            // Stars
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(44.dp))
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Congratulations!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (workoutCount > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "That's workout #$workoutCount!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (streak > 1) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        null,
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "$streak day streak!",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFFF6B35),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Workout card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        session?.name ?: "Workout",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        FormatUtils.formatDateTime(session?.startTime ?: System.currentTimeMillis()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (completedExercises.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        // Header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Sets",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "Best set",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(6.dp))

                        completedExercises.forEach { ex ->
                            val done = ex.sets.filter { it.isCompleted }
                            val inputMode = getExerciseInputMode(ex.exercise)
                            val bestSet = when (inputMode) {
                                ExerciseInputMode.TIME_ONLY -> done.maxByOrNull { it.weight }
                                ExerciseInputMode.REPS_ONLY -> done.maxByOrNull { it.reps }
                                else -> done.maxByOrNull { it.weight * it.reps }
                            }
                            val bestSetText = bestSet?.let {
                                when (inputMode) {
                                    ExerciseInputMode.TIME_ONLY -> "${formatWeightDisplay(it.weight)} min"
                                    ExerciseInputMode.REPS_ONLY -> "${it.reps} reps"
                                    else -> "${formatWeightDisplay(it.weight)} kg × ${it.reps}"
                                }
                            } ?: "—"
                            val hasPR = done.any { it.isPersonalRecord }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "${done.size} × ${ex.exercise.name}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (hasPR) {
                                        Text(
                                            "PR",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                    RoundedCornerShape(3.dp)
                                                )
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(bestSetText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(10.dp))

                        // Bottom stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.Timer, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FormatUtils.formatDuration(elapsedSeconds), style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.FitnessCenter, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (totalVolume >= 1000) "${"%.1f".format(totalVolume / 1000)}t kg"
                                    else "${"%.0f".format(totalVolume)} kg",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (totalPRs > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Filled.EmojiEvents, null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFD700))
                                    Text("$totalPRs PR${if (totalPRs > 1) "s" else ""}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }

        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(Icons.Filled.Close, "Close")
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// EXERCISE PICKER BOTTOM SHEET
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerBottomSheet(
    exercises: List<ExerciseEntity>,
    onExerciseSelected: (ExerciseEntity) -> Unit,
    onDismiss: () -> Unit,
    addedExerciseIds: Set<Long> = emptySet()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf<String?>(null) }

    val filtered = remember(searchQuery, selectedMuscle, exercises) {
        exercises.filter { ex ->
            (searchQuery.isEmpty() ||
                    ex.name.contains(searchQuery, ignoreCase = true) ||
                    ex.primaryMuscleGroup.contains(searchQuery, ignoreCase = true) ||
                    ex.equipmentType.contains(searchQuery, ignoreCase = true)) &&
                    (selectedMuscle == null || ex.primaryMuscleGroup == selectedMuscle)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Add Exercise",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (addedExerciseIds.isNotEmpty()) {
                        Text(
                            "${addedExerciseIds.size} added",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (addedExerciseIds.isNotEmpty()) {
                        TextButton(onClick = onDismiss) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, "Close")
                        }
                    }
                }
            }

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search exercises…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, "Clear")
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Muscle group filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedMuscle == null,
                        onClick = { selectedMuscle = null },
                        label = { Text("All") }
                    )
                }
                items(MuscleGroup.entries.toList()) { mg ->
                    FilterChip(
                        selected = selectedMuscle == mg.displayName,
                        onClick = {
                            selectedMuscle =
                                if (selectedMuscle == mg.displayName) null else mg.displayName
                        },
                        label = { Text(mg.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = getMuscleColor(mg.displayName).copy(alpha = 0.2f),
                            selectedLabelColor = getMuscleColor(mg.displayName)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Results count
            Text(
                "${filtered.size} exercise${if (filtered.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Exercise list
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.SearchOff,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No exercises found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { exercise ->
                        val isAdded = exercise.id in addedExerciseIds
                        ExerciseCard(
                            name = exercise.name,
                            muscleGroup = exercise.primaryMuscleGroup,
                            equipment = exercise.equipmentType,
                            difficulty = exercise.difficulty,
                            onClick = { if (!isAdded) onExerciseSelected(exercise) },
                            trailing = {
                                if (isAdded) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            "Added",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            "Added",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                } else {
                                    Icon(
                                        Icons.Filled.AddCircle,
                                        "Add",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// EXERCISE HISTORY SHEET
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseHistorySheet(
    exercise: ExerciseEntity,
    history: List<WorkoutSetEntity>,
    inputMode: ExerciseInputMode,
    onDismiss: () -> Unit
) {
    // Group completed sets by workoutExerciseId → one data point per session
    val chartPoints = remember(history, inputMode) {
        history
            .filter { it.isCompleted && it.completedAt != null }
            .groupBy { it.workoutExerciseId }
            .mapNotNull { (_, sets) ->
                val ts = sets.minOf { it.completedAt!! }
                val value = when (inputMode) {
                    ExerciseInputMode.TIME_ONLY -> sets.maxOf { it.weight }
                    ExerciseInputMode.REPS_ONLY -> sets.maxOf { it.reps }.toDouble()
                    else -> sets.maxByOrNull { s -> s.weight }?.weight ?: 0.0
                }
                if (value > 0) Pair(ts, value) else null
            }
            .sortedBy { it.first }
            .takeLast(12)
    }

    val dateFormat = remember { SimpleDateFormat("d MMM", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        exercise.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when (inputMode) {
                            ExerciseInputMode.TIME_ONLY -> "Duration history"
                            ExerciseInputMode.REPS_ONLY -> "Reps history"
                            else -> "Weight history"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "Close")
                }
            }

            Spacer(Modifier.height(16.dp))

            if (chartPoints.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.ShowChart,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Not enough data yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Complete more workouts to see your progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                val yLabel = when (inputMode) {
                    ExerciseInputMode.TIME_ONLY -> "min"
                    ExerciseInputMode.REPS_ONLY -> "reps"
                    else -> "kg"
                }
                SimpleLineChart(
                    dataPoints = chartPoints,
                    yLabel = yLabel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Recent sessions table
                Text(
                    "Recent sessions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                chartPoints.reversed().take(5).forEach { (ts, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            dateFormat.format(Date(ts)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${formatWeightDisplay(value)} $yLabel",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SIMPLE LINE CHART
// ═══════════════════════════════════════════════════════════════

@Composable
fun SimpleLineChart(
    dataPoints: List<Pair<Long, Double>>,
    yLabel: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    val minY = dataPoints.minOf { it.second }
    val maxY = dataPoints.maxOf { it.second }
    val yRange = (maxY - minY).coerceAtLeast(1.0)

    Canvas(modifier = modifier) {
        val padLeft = 16f
        val padRight = 16f
        val padTop = 16f
        val padBottom = 16f
        val chartW = size.width - padLeft - padRight
        val chartH = size.height - padTop - padBottom
        val n = dataPoints.size

        // Grid lines (3 horizontal)
        repeat(4) { i ->
            val y = padTop + chartH * i / 3f
            drawLine(gridColor, Offset(padLeft, y), Offset(size.width - padRight, y), strokeWidth = 1f)
        }

        fun toX(index: Int): Float = padLeft + (index.toFloat() / (n - 1)) * chartW
        fun toY(value: Double): Float = padTop + chartH - ((value - minY) / yRange * chartH).toFloat()

        // Draw line
        val linePath = Path()
        dataPoints.forEachIndexed { i, (_, v) ->
            val x = toX(i)
            val y = toY(v)
            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }
        drawPath(
            linePath,
            color = primaryColor.copy(alpha = 0.8f),
            style = Stroke(width = 2.5f)
        )

        // Fill under line
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(toX(n - 1), padTop + chartH)
            lineTo(toX(0), padTop + chartH)
            close()
        }
        drawPath(fillPath, color = primaryColor.copy(alpha = 0.08f))

        // Dots
        dataPoints.forEachIndexed { i, (_, v) ->
            val x = toX(i)
            val y = toY(v)
            drawCircle(color = primaryColor, radius = 5f, center = Offset(x, y))
            drawCircle(color = Color.White, radius = 2.5f, center = Offset(x, y))
        }
    }
}
