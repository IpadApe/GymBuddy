package com.gymtracker.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.database.entities.WorkoutSessionEntity
import com.gymtracker.ui.components.*
import com.gymtracker.ui.theme.*
import com.gymtracker.util.FormatUtils
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit,
    onViewProgress: () -> Unit,
    onViewBodyMap: () -> Unit,
    onNavigateToExercises: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    "GymTracker",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Let's crush it today 💪",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Start Workout CTA
        item {
            GradientButton(
                text = "Start Workout",
                onClick = onStartWorkout,
                icon = Icons.Filled.PlayArrow,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Quick Stats Row
        item {
            SectionHeader(title = "This Week")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Workouts",
                    value = "${uiState.weeklySessionCount}",
                    icon = Icons.Filled.FitnessCenter,
                    color = NeonBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Volume",
                    value = FormatUtils.formatVolume(uiState.weeklyVolume, uiState.useMetric),
                    icon = Icons.Filled.TrendingUp,
                    color = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Avg Time",
                    value = if (uiState.avgDuration > 0) FormatUtils.formatDuration(uiState.avgDuration) else "--",
                    icon = Icons.Filled.Timer,
                    color = NeonOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Filled.Accessibility,
                    label = "Body Map",
                    onClick = onViewBodyMap,
                    color = NeonPurple,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Filled.BarChart,
                    label = "Progress",
                    onClick = onViewProgress,
                    color = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Filled.Search,
                    label = "Exercises",
                    onClick = onNavigateToExercises,
                    color = NeonBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recommendations
        if (uiState.recommendedMuscles.isNotEmpty()) {
            item {
                SectionHeader(title = "Recommended Today")
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Based on your history, consider training:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.recommendedMuscles.forEach { muscle ->
                                ChipLabel(muscle.displayName, getMuscleColor(muscle.displayName))
                            }
                        }
                        if (uiState.deloadRecommended) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = WarningOrange.copy(alpha = 0.15f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Warning, null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Deload recommended — you've been training hard for 4+ weeks!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WarningOrange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Workouts
        item {
            SectionHeader(
                title = "Recent Workouts",
                action = "See All",
                onAction = onViewProgress
            )
        }

        if (uiState.recentSessions.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.FitnessCenter,
                    title = "No workouts yet",
                    subtitle = "Start your first workout to begin tracking your progress!",
                    actionLabel = "Start Now",
                    onAction = onStartWorkout
                )
            }
        } else {
            items(uiState.recentSessions.take(5)) { session ->
                WorkoutSessionCard(
                    name = session.name,
                    date = FormatUtils.formatDate(session.startTime),
                    duration = FormatUtils.formatDuration(session.durationSeconds),
                    volume = FormatUtils.formatVolume(session.totalVolumeKg, uiState.useMetric),
                    splitType = session.splitType,
                    onClick = { /* navigate to workout detail */ }
                )
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
