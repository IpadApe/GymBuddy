package com.gymtracker.ui.screens.routines

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
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
import java.io.File

class RoutinesViewModel(private val app: GymTrackerApp) : ViewModel() {
    private val repo = app.repository
    val allRoutines = repo.getAllRoutines()
    val prebuiltRoutines = repo.getPrebuiltRoutines()

    private val _importError = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = _importError

    private val _exportedJson = MutableStateFlow<Pair<String, String>?>(null) // name to json
    val exportedJson: StateFlow<Pair<String, String>?> = _exportedJson

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

    fun importFromJson(jsonString: String) {
        viewModelScope.launch {
            try {
                repo.importRoutineFromJson(jsonString)
            } catch (e: Exception) {
                _importError.value = e.message ?: "Failed to import template"
            }
        }
    }

    fun exportToJson(routineId: Long) {
        viewModelScope.launch {
            try {
                val json = repo.exportRoutineToJson(routineId)
                val allRoutines = repo.getAllRoutines().first()
                val name = allRoutines.find { it.id == routineId }?.name ?: "routine"
                _exportedJson.value = name to json
            } catch (e: Exception) {
                _importError.value = e.message ?: "Failed to export routine"
            }
        }
    }

    fun clearError() { _importError.value = null }
    fun clearExport() { _exportedJson.value = null }
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
    val importError by viewModel.importError.collectAsState()
    val exportedJson by viewModel.exportedJson.collectAsState()
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val json = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText() ?: return@let
            viewModel.importFromJson(json)
        }
    }

    // Trigger share sheet when export is ready
    LaunchedEffect(exportedJson) {
        exportedJson?.let { (name, json) ->
            shareRoutineJson(context, name, json)
            viewModel.clearExport()
        }
    }

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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, "Help", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(
                        onClick = { filePicker.launch("application/json") },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.FileUpload, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import")
                    }
                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Filled.Add, "Create")
                    }
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
                    onDelete = { viewModel.deleteRoutine(routine) },
                    onExport = { viewModel.exportToJson(routine.id) }
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
                    onDelete = null,
                    onExport = null
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

    if (showHelpDialog) {
        TemplateHelpDialog(onDismiss = { showHelpDialog = false })
    }

    importError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Import Failed") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } }
        )
    }
}

private fun shareRoutineJson(context: Context, routineName: String, json: String) {
    val fileName = routineName.replace(" ", "_").lowercase() + ".json"
    val file = File(context.cacheDir, fileName)
    file.writeText(json)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "GymTracker routine: $routineName")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share routine"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineCard(
    routine: RoutineEntity,
    onClick: () -> Unit,
    onClone: () -> Unit,
    onDelete: (() -> Unit)?,
    onExport: (() -> Unit)?
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
                    ChipLabel("Template", BlueTrust)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChipLabel("${routine.daysPerWeek}x/week", MaterialTheme.colorScheme.primary)
                ChipLabel(routine.goal, TealSuccess)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onClone, shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clone", style = MaterialTheme.typography.labelSmall)
                }
                if (onExport != null) {
                    OutlinedButton(onClick = onExport, shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Filled.FileDownload, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
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
fun TemplateHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Templates") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("You can import your own workout routines as JSON files.", style = MaterialTheme.typography.bodyMedium)

                Text("JSON Format", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        """
{
  "version": 1,
  "name": "My PPL",
  "description": "Push Pull Legs",
  "goal": "Hypertrophy",
  "daysPerWeek": 6,
  "days": [
    {
      "name": "Push A",
      "splitType": "PUSH",
      "exercises": [
        {
          "exercise": "Bench Press",
          "sets": 4,
          "reps": "6-10",
          "restSeconds": 120
        }
      ]
    }
  ]
}""".trim(),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Text("Fields", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                HelpRow("goal", "Hypertrophy, Strength, Endurance, Fat Loss")
                HelpRow("splitType", "PUSH, PULL, LEGS, UPPER, LOWER, FULL_BODY, CUSTOM")
                HelpRow("exercise", "Must match an exercise name in the app (case-insensitive)")
                HelpRow("reps", "Any string: \"8\", \"8-12\", \"AMRAP\"")

                Text("How to Import", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "1. Create a .json file on your phone or PC\n" +
                    "2. Transfer via USB, Google Drive, email, etc.\n" +
                    "3. Tap Import on the Routines screen\n" +
                    "4. Select your .json file",
                    style = MaterialTheme.typography.bodySmall
                )

                Text("How to Export", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Tap Export on any of your custom routines. The JSON file will be shared via your device's share sheet.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } }
    )
}

@Composable
private fun HelpRow(field: String, description: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Text(
            "\"$field\"",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(100.dp)
        )
        Text(description, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
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
