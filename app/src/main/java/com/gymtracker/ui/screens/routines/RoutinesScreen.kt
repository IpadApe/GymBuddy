package com.gymtracker.ui.screens.routines

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
import com.gymtracker.data.database.entities.*
import com.gymtracker.ui.components.*
import com.gymtracker.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RoutinesViewModel(private val app: GymTrackerApp) : ViewModel() {
    private val repo = app.repository
    val allRoutines = repo.getAllRoutines()
    val prebuiltRoutines = repo.getPrebuiltRoutines()

    fun createRoutine(name: String, description: String, goal: String, daysPerWeek: Int, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repo.insertRoutine(RoutineEntity(name = name, description = description, goal = goal, daysPerWeek = daysPerWeek))
            onCreated(id)
        }
    }

    fun cloneRoutine(routineId: Long, onCloned: (Long) -> Unit) {
        viewModelScope.launch {
            val newId = repo.cloneRoutine(routineId)
            if (newId > 0) onCloned(newId)
        }
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch { repo.deleteRoutine(routine) }
    }
}

class RoutinesViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RoutinesViewModel(GymTrackerApp.instance) as T
    }
}

@Composable
fun RoutinesScreen(
    onRoutineClick: (Long) -> Unit,
    onStartWorkout: (Long) -> Unit,
    viewModel: RoutinesViewModel = viewModel(factory = RoutinesViewModelFactory())
) {
    val allRoutines by viewModel.allRoutines.collectAsState(initial = emptyList())
    val prebuilt by viewModel.prebuiltRoutines.collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Routines", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Filled.Add, "Create")
                }
            }
        }

        // My Routines
        val custom = allRoutines.filter { !it.isPrebuilt }
        if (custom.isNotEmpty()) {
            item { SectionHeader(title = "My Routines") }
            items(custom) { routine ->
                RoutineCard(
                    routine = routine,
                    onClick = { onRoutineClick(routine.id) },
                    onClone = { viewModel.cloneRoutine(routine.id) { onRoutineClick(it) } },
                    onDelete = { viewModel.deleteRoutine(routine) }
                )
            }
        }

        // Prebuilt Templates
        if (prebuilt.isNotEmpty()) {
            item { SectionHeader(title = "Templates") }
            items(prebuilt) { routine ->
                RoutineCard(
                    routine = routine,
                    onClick = { onRoutineClick(routine.id) },
                    onClone = { viewModel.cloneRoutine(routine.id) { onRoutineClick(it) } },
                    onDelete = null
                )
            }
        }

        if (allRoutines.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.CalendarMonth,
                    title = "No routines yet",
                    subtitle = "Create a custom routine or use one of the prebuilt templates to get started",
                    actionLabel = "Create Routine",
                    onAction = { showCreateDialog = true }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showCreateDialog) {
        CreateRoutineDialog(
            onConfirm = { name, desc, goal, days ->
                viewModel.createRoutine(name, desc, goal, days) { onRoutineClick(it) }
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineCard(
    routine: RoutineEntity,
    onClick: () -> Unit,
    onClone: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(routine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (routine.description.isNotBlank()) {
                        Text(routine.description, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                }
                if (routine.isPrebuilt) {
                    ChipLabel("Template", NeonBlue)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChipLabel("${routine.daysPerWeek}x/week", MaterialTheme.colorScheme.primary)
                ChipLabel(routine.goal, NeonGreen)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onClone, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clone", style = MaterialTheme.typography.labelSmall)
                }
                if (onDelete != null) {
                    OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)) {
                        Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateRoutineDialog(
    onConfirm: (String, String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("Hypertrophy") }
    var daysPerWeek by remember { mutableStateOf("4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Routine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, shape = RoundedCornerShape(10.dp))
                NumberInputField(daysPerWeek, { daysPerWeek = it }, "Days per Week", suffix = "days")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onConfirm(name, description, goal, daysPerWeek.toIntOrNull() ?: 4)
            }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
