package com.gymtracker.ui.screens.bodymap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.model.*
import com.gymtracker.ui.components.*
import com.gymtracker.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BodyMapState(
    val muscleStatuses: List<MuscleGroupStatus> = emptyList(),
    val imbalances: List<ImbalanceAdvisory> = emptyList(),
    val isLoading: Boolean = true,
    val view7Days: Boolean = true,
    val isFemale: Boolean = false
)

class BodyMapViewModel(private val app: GymTrackerApp) : ViewModel() {
    private val repo = app.repository
    private val _state = MutableStateFlow(BodyMapState())
    val state: StateFlow<BodyMapState> = _state.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            val statuses = repo.getMuscleGroupStatuses()
            val imbalances = repo.getImbalanceAdvisories()
            val prefs = repo.getPreferencesSync()
            _state.update {
                it.copy(
                    muscleStatuses = statuses,
                    imbalances = imbalances,
                    isLoading = false,
                    isFemale = prefs?.isFemale ?: false
                )
            }
        }
    }

    fun toggle7or30() { _state.update { it.copy(view7Days = !it.view7Days) } }
}

class BodyMapViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BodyMapViewModel(GymTrackerApp.instance) as T
    }
}

// ─────────────────────────────────────────────────────────────
// Muscle gradient palettes — each group gets a unique gradient
// ─────────────────────────────────────────────────────────────
private fun muscleGradient(group: MuscleGroup, status: MuscleStatus): Brush {
    // Undertrained = always muted dark
    if (status == MuscleStatus.UNDERTRAINED) {
        return Brush.linearGradient(listOf(Color(0xFF1A1A2C), Color(0xFF2A2A44)))
    }
    // Overtrained = always red
    if (status == MuscleStatus.OVERTRAINED) {
        return Brush.linearGradient(listOf(Color(0xFF6B0000), ErrorRed))
    }
    // Adequate — unique colour per group, dark→light
    return when (group) {
        MuscleGroup.CHEST      -> Brush.linearGradient(listOf(Color(0xFF1A3A6B), Color(0xFF2979FF)))
        MuscleGroup.BACK       -> Brush.linearGradient(listOf(Color(0xFF004D40), TealSuccess))
        MuscleGroup.SHOULDERS  -> Brush.linearGradient(listOf(Color(0xFF4A148C), Color(0xFF9C27B0)))
        MuscleGroup.BICEPS     -> Brush.linearGradient(listOf(Color(0xFF01579B), BlueTrust))
        MuscleGroup.TRICEPS    -> Brush.linearGradient(listOf(Color(0xFF1B5E20), Color(0xFF43A047)))
        MuscleGroup.FOREARMS   -> Brush.linearGradient(listOf(Color(0xFF004D40), Color(0xFF26A69A)))
        MuscleGroup.ABS        -> Brush.linearGradient(listOf(Color(0xFF8B3A00), OrangePrimary))
        MuscleGroup.QUADS      -> Brush.linearGradient(listOf(Color(0xFF880E4F), Color(0xFFE91E63)))
        MuscleGroup.HAMSTRINGS -> Brush.linearGradient(listOf(Color(0xFF4A148C), Periwinkle))
        MuscleGroup.GLUTES     -> Brush.linearGradient(listOf(Color(0xFF5D1A00), Color(0xFFBF360C)))
        MuscleGroup.CALVES     -> Brush.linearGradient(listOf(Color(0xFF006064), Color(0xFF00BCD4)))
    }
}

private fun muscleIcon(group: MuscleGroup): ImageVector = when (group) {
    MuscleGroup.CHEST, MuscleGroup.BACK,
    MuscleGroup.BICEPS, MuscleGroup.TRICEPS,
    MuscleGroup.FOREARMS, MuscleGroup.SHOULDERS -> Icons.Filled.FitnessCenter
    MuscleGroup.ABS                              -> Icons.Filled.ViewModule
    MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS,
    MuscleGroup.GLUTES, MuscleGroup.CALVES       -> Icons.Filled.DirectionsRun
}

// ─────────────────────────────────────────────────────────────
// SCREEN
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMapScreen(
    onBack: () -> Unit,
    viewModel: BodyMapViewModel = viewModel(factory = BodyMapViewModelFactory())
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Muscle Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Time filter + legend ──────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = state.view7Days,
                            onClick = { viewModel.toggle7or30() },
                            label = { Text("7 Days") }
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = !state.view7Days,
                            onClick = { viewModel.toggle7or30() },
                            label = { Text("30 Days") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendItem(TealSuccess, "Adequate")
                        LegendItem(ErrorRed,    "Overtrained")
                        LegendItem(Color(0xFF2A2A44), "Undertrained")
                    }
                }
            }

            // ── Muscle tile grid ──────────────────────────────────
            if (state.muscleStatuses.isNotEmpty()) {
                val rows = state.muscleStatuses.chunked(3)
                items(rows) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { status ->
                            MuscleTile(
                                status = status,
                                show7Days = state.view7Days,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill empty slots in last row
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // ── Imbalance warnings ────────────────────────────────
            if (state.imbalances.isNotEmpty()) {
                item { SectionHeader(title = "Imbalance Warnings") }
                items(state.imbalances) { advisory ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Filled.Warning, null, tint = WarningOrange, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "${advisory.overtrainedMuscle.displayName} vs ${advisory.undertrainedMuscle.displayName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    advisory.recommendation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// MUSCLE TILE — same visual style as ExploreCard
// ─────────────────────────────────────────────────────────────
@Composable
fun MuscleTile(
    status: MuscleGroupStatus,
    show7Days: Boolean,
    modifier: Modifier = Modifier
) {
    val sets = if (show7Days) status.totalSets7Days else status.totalSets30Days
    val daysAgo = status.daysSinceLastTrained
    val gradient = muscleGradient(status.muscleGroup, status.status)
    val icon = muscleIcon(status.muscleGroup)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(14.dp)
    ) {
        Column {
            // Icon
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(10.dp))
            // Muscle name
            Text(
                status.muscleGroup.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(2.dp))
            // Sets count
            Text(
                "$sets sets",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.80f)
            )
            // Last trained
            Text(
                if (daysAgo != null) "${daysAgo}d ago" else "Never",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.60f)
            )
        }

        // Status dot (top-right corner)
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    when (status.status) {
                        MuscleStatus.ADEQUATE    -> Color.White.copy(alpha = 0.9f)
                        MuscleStatus.OVERTRAINED -> Color.White.copy(alpha = 0.9f)
                        MuscleStatus.UNDERTRAINED -> Color.White.copy(alpha = 0.30f)
                    }
                )
                .align(Alignment.TopEnd)
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
