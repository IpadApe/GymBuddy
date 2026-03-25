package com.gymtracker.ui.screens.bodymap

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val view7Days: Boolean = true
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
            _state.update { it.copy(muscleStatuses = statuses, imbalances = imbalances, isLoading = false) }
        }
    }

    fun toggle7or30() {
        _state.update { it.copy(view7Days = !it.view7Days) }
    }
}

class BodyMapViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BodyMapViewModel(GymTrackerApp.instance) as T
    }
}

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
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = state.view7Days,
                        onClick = { viewModel.toggle7or30() },
                        label = { Text("7 Days") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = !state.view7Days,
                        onClick = { viewModel.toggle7or30() },
                        label = { Text("30 Days") }
                    )
                }
            }

            // Legend
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(MuscleAdequate, "Adequate")
                    LegendItem(MuscleOvertrained, "Overtrained")
                    LegendItem(MuscleUndertrained, "Undertrained")
                }
            }

            // Body Map visualization
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BodyMapCanvas(muscleStatuses = state.muscleStatuses, show7Days = state.view7Days)
                    }
                }
            }

            // Muscle details list
            item { SectionHeader(title = "Muscle Details") }
            items(state.muscleStatuses) { status ->
                MuscleStatusCard(status = status, show7Days = state.view7Days)
            }

            // Imbalance Advisories
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

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun BodyMapCanvas(muscleStatuses: List<MuscleGroupStatus>, show7Days: Boolean) {
    val statusMap = muscleStatuses.associateBy { it.muscleGroup }

    fun getColor(muscle: MuscleGroup): Color {
        val s = statusMap[muscle] ?: return MuscleUndertrained
        return when(s.status) {
            MuscleStatus.ADEQUATE -> MuscleAdequate
            MuscleStatus.OVERTRAINED -> MuscleOvertrained
            MuscleStatus.UNDERTRAINED -> MuscleUndertrained
        }
    }

    fun getAlpha(muscle: MuscleGroup): Float {
        val s = statusMap[muscle] ?: return 0.3f
        val sets = if(show7Days) s.totalSets7Days else s.totalSets30Days
        return (0.3f + (sets.coerceAtMost(30).toFloat() / 30f) * 0.7f)
    }

    // Simplified body representation using labeled rounded rects
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Front body
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("FRONT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            MuscleBlock("Shoulders", getColor(MuscleGroup.SHOULDERS), getAlpha(MuscleGroup.SHOULDERS), 120.dp, 28.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MuscleBlock("Biceps", getColor(MuscleGroup.BICEPS), getAlpha(MuscleGroup.BICEPS), 28.dp, 55.dp)
                MuscleBlock("Chest", getColor(MuscleGroup.CHEST), getAlpha(MuscleGroup.CHEST), 80.dp, 55.dp)
                MuscleBlock("Biceps", getColor(MuscleGroup.BICEPS), getAlpha(MuscleGroup.BICEPS), 28.dp, 55.dp)
            }
            MuscleBlock("Abs/Core", getColor(MuscleGroup.ABS), getAlpha(MuscleGroup.ABS), 80.dp, 50.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MuscleBlock("Quads", getColor(MuscleGroup.QUADS), getAlpha(MuscleGroup.QUADS), 45.dp, 70.dp)
                MuscleBlock("Quads", getColor(MuscleGroup.QUADS), getAlpha(MuscleGroup.QUADS), 45.dp, 70.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MuscleBlock("Calves", getColor(MuscleGroup.CALVES), getAlpha(MuscleGroup.CALVES), 35.dp, 45.dp)
                MuscleBlock("Calves", getColor(MuscleGroup.CALVES), getAlpha(MuscleGroup.CALVES), 35.dp, 45.dp)
            }
        }

        // Back body
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("BACK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            MuscleBlock("Shoulders", getColor(MuscleGroup.SHOULDERS), getAlpha(MuscleGroup.SHOULDERS), 120.dp, 28.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MuscleBlock("Triceps", getColor(MuscleGroup.TRICEPS), getAlpha(MuscleGroup.TRICEPS), 28.dp, 55.dp)
                MuscleBlock("Back", getColor(MuscleGroup.BACK), getAlpha(MuscleGroup.BACK), 80.dp, 55.dp)
                MuscleBlock("Triceps", getColor(MuscleGroup.TRICEPS), getAlpha(MuscleGroup.TRICEPS), 28.dp, 55.dp)
            }
            MuscleBlock("Glutes", getColor(MuscleGroup.GLUTES), getAlpha(MuscleGroup.GLUTES), 80.dp, 50.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MuscleBlock("Hams", getColor(MuscleGroup.HAMSTRINGS), getAlpha(MuscleGroup.HAMSTRINGS), 45.dp, 70.dp)
                MuscleBlock("Hams", getColor(MuscleGroup.HAMSTRINGS), getAlpha(MuscleGroup.HAMSTRINGS), 45.dp, 70.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MuscleBlock("Calves", getColor(MuscleGroup.CALVES), getAlpha(MuscleGroup.CALVES), 35.dp, 45.dp)
                MuscleBlock("Calves", getColor(MuscleGroup.CALVES), getAlpha(MuscleGroup.CALVES), 35.dp, 45.dp)
            }
        }
    }
}

@Composable
fun MuscleBlock(label: String, color: Color, alpha: Float, width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
    }
}

@Composable
fun MuscleStatusCard(status: MuscleGroupStatus, show7Days: Boolean) {
    val color = when(status.status) {
        MuscleStatus.ADEQUATE -> MuscleAdequate
        MuscleStatus.OVERTRAINED -> MuscleOvertrained
        MuscleStatus.UNDERTRAINED -> MuscleUndertrained
    }
    val sets = if(show7Days) status.totalSets7Days else status.totalSets30Days

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(12.dp).clip(CircleShape).background(color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(status.muscleGroup.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "$sets sets (${if(show7Days) "7d" else "30d"}) • ${status.status.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                status.daysSinceLastTrained?.let {
                    Text(
                        "${it}d ago",
                        style = MaterialTheme.typography.labelMedium,
                        color = if(it > 7) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } ?: Text("Never", style = MaterialTheme.typography.labelMedium, color = ErrorRed)
                Text("~${status.estimatedRecoveryHours}h recovery", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
