package com.gymtracker.ui.screens.exercises

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.database.entities.ExerciseEntity
import com.gymtracker.data.model.EquipmentType
import com.gymtracker.data.model.MuscleGroup
import com.gymtracker.data.model.MovementType
import com.gymtracker.ui.components.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════
// VIEWMODEL
// ═══════════════════════════════════════════════════════════════
data class ExerciseLibraryState(
    val exercises: List<ExerciseEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedMuscle: String? = null,
    val selectedEquipment: String? = null,
    val selectedMovement: String? = null,
    val isLoading: Boolean = true
)

class ExerciseLibraryViewModel(app: GymTrackerApp) : ViewModel() {
    private val repo = app.repository
    private val _state = MutableStateFlow(ExerciseLibraryState())
    val state: StateFlow<ExerciseLibraryState> = _state.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            repo.getAllExercises().collect { list ->
                _state.update { it.copy(exercises = list, isLoading = false) }
            }
        }
    }

    fun onSearchChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isNotBlank()) {
            viewModelScope.launch {
                repo.searchExercises(query).collect { results ->
                    _state.update { it.copy(exercises = results) }
                }
            }
        } else {
            loadExercises()
        }
    }

    fun onMuscleFilter(muscle: String?) {
        _state.update { it.copy(selectedMuscle = if (muscle == null || it.selectedMuscle == muscle) null else muscle) }
        applyFilters()
    }

    fun onEquipmentFilter(equipment: String?) {
        _state.update { it.copy(selectedEquipment = if (it.selectedEquipment == equipment) null else equipment) }
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            val s = _state.value
            repo.getAllExercises().collect { all ->
                val filtered = all.filter { ex ->
                    (s.selectedMuscle == null || ex.primaryMuscleGroup == s.selectedMuscle) &&
                    (s.selectedEquipment == null || ex.equipmentType == s.selectedEquipment) &&
                    (s.searchQuery.isBlank() || ex.name.contains(s.searchQuery, ignoreCase = true))
                }
                _state.update { it.copy(exercises = filtered) }
            }
        }
    }

    fun createCustomExercise(
        name: String,
        primaryMuscle: String,
        equipment: String,
        difficulty: String,
        instructions: String
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repo.createExercise(
                com.gymtracker.data.database.entities.ExerciseEntity(
                    name = name.trim(),
                    primaryMuscleGroup = primaryMuscle,
                    secondaryMuscleGroups = "",
                    equipmentType = equipment,
                    movementType = "Isolation",
                    difficulty = difficulty,
                    instructions = instructions.trim(),
                    isCustom = true
                )
            )
        }
    }
}

class ExerciseLibraryViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ExerciseLibraryViewModel(GymTrackerApp.instance) as T
    }
}

// ═══════════════════════════════════════════════════════════════
// SCREEN
// ═══════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    onExerciseClick: (Long) -> Unit,
    viewModel: ExerciseLibraryViewModel = viewModel(factory = ExerciseLibraryViewModelFactory())
) {
    val state by viewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateCustomExerciseDialog(
            onConfirm = { name, muscle, equip, diff, instructions ->
                viewModel.createCustomExercise(name, muscle, equip, diff, instructions)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
        // Title
        Text(
            "Exercise Library",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Search
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f), Color.Transparent)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Text("🔍", fontSize = 16.sp) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchChanged("") }) {
                            Icon(Icons.Filled.Close, null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Muscle group pill filter buttons (with "All" at start)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val isSelected = state.selectedMuscle == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.onMuscleFilter(null) }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        "All",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(MuscleGroup.entries.toList()) { muscle ->
                val isSelected = state.selectedMuscle == muscle.displayName
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.onMuscleFilter(muscle.displayName) }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        muscle.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Results count
        Text(
            "${state.exercises.size} exercises",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Exercise list
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.exercises, key = { it.id }) { exercise ->
                    ExerciseCard(
                        name = exercise.name,
                        muscleGroup = exercise.primaryMuscleGroup,
                        equipment = exercise.equipmentType,
                        difficulty = exercise.difficulty,
                        onClick = { onExerciseClick(exercise.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
        }  // end Column

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Filled.Add, "Create custom exercise")
        }
    }  // end Box
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCustomExerciseDialog(
    onConfirm: (name: String, muscle: String, equip: String, diff: String, instructions: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var muscle by remember { mutableStateOf(MuscleGroup.entries.first().displayName) }
    var equipment by remember { mutableStateOf(EquipmentType.entries.first().displayName) }
    var difficulty by remember { mutableStateOf("Beginner") }
    var instructions by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Exercise", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                // Muscle group dropdown
                var muscleExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = muscleExpanded, onExpandedChange = { muscleExpanded = it }) {
                    OutlinedTextField(
                        value = muscle,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Primary muscle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = muscleExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(expanded = muscleExpanded, onDismissRequest = { muscleExpanded = false }) {
                        MuscleGroup.entries.forEach { m ->
                            DropdownMenuItem(text = { Text(m.displayName) }, onClick = { muscle = m.displayName; muscleExpanded = false })
                        }
                    }
                }
                // Equipment dropdown
                var equipExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = equipExpanded, onExpandedChange = { equipExpanded = it }) {
                    OutlinedTextField(
                        value = equipment,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Equipment") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = equipExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(expanded = equipExpanded, onDismissRequest = { equipExpanded = false }) {
                        EquipmentType.entries.forEach { e ->
                            DropdownMenuItem(text = { Text(e.displayName) }, onClick = { equipment = e.displayName; equipExpanded = false })
                        }
                    }
                }
                // Difficulty
                var diffExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = diffExpanded, onExpandedChange = { diffExpanded = it }) {
                    OutlinedTextField(
                        value = difficulty,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Difficulty") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diffExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(expanded = diffExpanded, onDismissRequest = { diffExpanded = false }) {
                        listOf("Beginner", "Intermediate", "Advanced").forEach { d ->
                            DropdownMenuItem(text = { Text(d) }, onClick = { difficulty = d; diffExpanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, muscle, equipment, difficulty, instructions) },
                enabled = name.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
