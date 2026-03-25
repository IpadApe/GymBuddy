package com.gymtracker.ui.screens.exercises

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
        _state.update { it.copy(selectedMuscle = if (it.selectedMuscle == muscle) null else muscle) }
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

    Column(modifier = Modifier.fillMaxSize()) {
        // Title
        Text(
            "Exercise Library",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Search
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search exercises...") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
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

        Spacer(modifier = Modifier.height(12.dp))

        // Muscle group filter chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MuscleGroup.entries.toList()) { muscle ->
                FilterChip(
                    selected = state.selectedMuscle == muscle.displayName,
                    onClick = { viewModel.onMuscleFilter(muscle.displayName) },
                    label = { Text(muscle.displayName, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = getMuscleColor(muscle.displayName).copy(alpha = 0.2f),
                        selectedLabelColor = getMuscleColor(muscle.displayName)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Equipment filter chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(EquipmentType.entries.toList()) { equip ->
                FilterChip(
                    selected = state.selectedEquipment == equip.displayName,
                    onClick = { viewModel.onEquipmentFilter(equip.displayName) },
                    label = { Text(equip.displayName, style = MaterialTheme.typography.labelSmall) }
                )
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
    }
}
