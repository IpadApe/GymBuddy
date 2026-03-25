package com.gymtracker.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.database.entities.UserPreferencesEntity
import com.gymtracker.data.model.TrainingGoal
import com.gymtracker.ui.components.GradientButton
import com.gymtracker.ui.theme.*
import kotlinx.coroutines.launch

class OnboardingViewModel(private val app: GymTrackerApp) : ViewModel() {
    private val repo = app.repository

    fun completeOnboarding(
        goal: String,
        daysPerWeek: Int,
        useMetric: Boolean,
        preferredSplit: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val prefs = repo.getPreferencesSync() ?: UserPreferencesEntity()
            repo.updatePreferences(
                prefs.copy(
                    trainingGoal = goal,
                    daysPerWeek = daysPerWeek,
                    useMetric = useMetric,
                    preferredSplit = preferredSplit,
                    onboardingCompleted = true
                )
            )
            onComplete()
        }
    }
}

class OnboardingViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return OnboardingViewModel(GymTrackerApp.instance) as T
    }
}

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModelFactory())
) {
    var currentPage by remember { mutableIntStateOf(0) }
    var selectedGoal by remember { mutableStateOf("Hypertrophy") }
    var daysPerWeek by remember { mutableIntStateOf(4) }
    var useMetric by remember { mutableStateOf(true) }
    var selectedSplit by remember { mutableStateOf("PPL") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress dots
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentPage) 24.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pages
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            },
            modifier = Modifier.weight(1f),
            label = "page"
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> GoalPage(selectedGoal) { selectedGoal = it }
                2 -> FrequencyPage(daysPerWeek, selectedSplit, useMetric,
                    onDaysChange = { daysPerWeek = it },
                    onSplitChange = { selectedSplit = it },
                    onMetricChange = { useMetric = it })
                3 -> ReadyPage()
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPage > 0) {
                TextButton(onClick = { currentPage-- }) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            if (currentPage < 3) {
                GradientButton(
                    text = "Next",
                    onClick = { currentPage++ },
                    icon = Icons.Filled.ArrowForward,
                    modifier = Modifier.width(140.dp)
                )
            } else {
                GradientButton(
                    text = "Let's Go!",
                    onClick = {
                        viewModel.completeOnboarding(selectedGoal, daysPerWeek, useMetric, selectedSplit, onComplete)
                    },
                    icon = Icons.Filled.Bolt,
                    modifier = Modifier.width(160.dp)
                )
            }
        }

        // Skip
        if (currentPage < 3) {
            TextButton(onClick = {
                viewModel.completeOnboarding(selectedGoal, daysPerWeek, useMetric, selectedSplit, onComplete)
            }) {
                Text("Skip setup", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun WelcomePage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💪", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "GymTracker",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Your free, offline gym companion.\nNo accounts. No subscriptions.\nJust you and your gains.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GoalPage(selectedGoal: String, onGoalChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("What's your goal?", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text("We'll tailor recommendations for you", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))

        TrainingGoal.entries.forEach { goal ->
            val isSelected = selectedGoal == goal.name
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                onClick = { onGoalChange(goal.name) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(14.dp),
                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Crimson, CrimsonLight))
                ) else null
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (goal) {
                            TrainingGoal.STRENGTH -> Icons.Filled.FitnessCenter
                            TrainingGoal.HYPERTROPHY -> Icons.Filled.AutoAwesome
                            TrainingGoal.ENDURANCE -> Icons.Filled.DirectionsRun
                            TrainingGoal.FAT_LOSS -> Icons.Filled.LocalFireDepartment
                        },
                        null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(goal.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            when (goal) {
                                TrainingGoal.STRENGTH -> "Heavy weights, low reps, long rest"
                                TrainingGoal.HYPERTROPHY -> "Moderate weights, higher reps, muscle growth"
                                TrainingGoal.ENDURANCE -> "Light weights, high reps, short rest"
                                TrainingGoal.FAT_LOSS -> "Circuit-style, supersets, calorie burn"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FrequencyPage(
    daysPerWeek: Int,
    selectedSplit: String,
    useMetric: Boolean,
    onDaysChange: (Int) -> Unit,
    onSplitChange: (String) -> Unit,
    onMetricChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Training Setup", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Days per week", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (2..6).forEach { days ->
                FilterChip(
                    selected = daysPerWeek == days,
                    onClick = { onDaysChange(days) },
                    label = { Text("$days") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Preferred Split", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        val splits = listOf("PPL" to "Push Pull Legs", "UL" to "Upper/Lower", "FB" to "Full Body", "BRO" to "Bro Split")
        splits.forEach { (key, label) ->
            FilterChip(
                selected = selectedSplit == key,
                onClick = { onSplitChange(key) },
                label = { Text(label) },
                modifier = Modifier.padding(vertical = 2.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Units", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = useMetric, onClick = { onMetricChange(true) }, label = { Text("Metric (kg/cm)") })
            FilterChip(selected = !useMetric, onClick = { onMetricChange(false) }, label = { Text("Imperial (lbs/in)") })
        }
    }
}

@Composable
fun ReadyPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎯", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "You're all set!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Start tracking your workouts, crush PRs,\nand build the physique you want.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                FeatureRow("200+ exercises with instructions")
                FeatureRow("Automatic PR detection")
                FeatureRow("Muscle map & imbalance tracking")
                FeatureRow("Progressive overload suggestions")
                FeatureRow("Prebuilt routine templates")
                FeatureRow("100% free, 100% offline")
            }
        }
    }
}

@Composable
fun FeatureRow(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
