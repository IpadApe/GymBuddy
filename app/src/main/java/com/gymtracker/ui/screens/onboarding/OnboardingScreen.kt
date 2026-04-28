package com.gymtracker.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.database.entities.UserPreferencesEntity
import com.gymtracker.ui.theme.*
import kotlinx.coroutines.launch

// ─── ViewModel ───────────────────────────────────────────────────────────────

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

// ─── Step model ──────────────────────────────────────────────────────────────

data class OnboardingOption(
    val id: String,
    val emoji: String,
    val label: String,
    val sub: String
)

private val GOAL_OPTIONS = listOf(
    OnboardingOption("Strength",    "💪", "Build Strength",    "Heavy compounds, low reps"),
    OnboardingOption("Hypertrophy", "📈", "Gain Muscle",       "Volume-focused training"),
    OnboardingOption("Endurance",   "🏃", "Build Endurance",   "Higher reps, circuits"),
    OnboardingOption("Fat_Loss",    "🔥", "Lose Fat",          "Cardio + resistance"),
    OnboardingOption("General",     "⚡", "Stay Active",       "General fitness"),
)

private val FREQ_OPTIONS = listOf(
    OnboardingOption("2", "2️⃣", "2 days / week", "Light schedule"),
    OnboardingOption("3", "3️⃣", "3 days / week", "Balanced approach"),
    OnboardingOption("4", "4️⃣", "4 days / week", "Intermediate"),
    OnboardingOption("5", "5️⃣", "5 days / week", "Advanced"),
    OnboardingOption("6", "6️⃣", "6 days / week", "Dedicated athlete"),
)

private val UNIT_OPTIONS = listOf(
    OnboardingOption("metric",   "🇪🇺", "Metric",   "Kilograms & centimetres"),
    OnboardingOption("imperial", "🇺🇸", "Imperial", "Pounds & inches"),
)

private val SPLIT_OPTIONS = listOf(
    OnboardingOption("PPL",    "🔄", "Push Pull Legs",    "6 days — popular choice"),
    OnboardingOption("UL",     "↕️", "Upper / Lower",     "4 days — balanced"),
    OnboardingOption("SL55",   "🏋️", "StrongLifts 5×5",   "3 days — beginner"),
    OnboardingOption("FB",     "⚡", "Full Body 3×/Week", "3 days — flexible"),
    OnboardingOption("CUSTOM", "✏️", "I'll set it up",    "Custom routine"),
)

private val STEP_TITLES = listOf(
    "What's your goal?",
    "How often do you train?",
    "Preferred units?",
    "Preferred split?",
)
private val STEP_SUBTITLES = listOf(
    "We'll personalise your experience.",
    "Choose your weekly training days.",
    "You can change this later in Settings.",
    "We'll set up a routine for you.",
)

// ─── Helpers ─────────────────────────────────────────────────────────────────

/** Darkens a colour by ~15 % — equivalent to T.primaryDark in the design tokens. */
private fun Color.darken(factor: Float = 0.85f) = Color(
    red   = red   * factor,
    green = green * factor,
    blue  = blue  * factor,
    alpha = alpha
)

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModelFactory())
) {
    var step  by remember { mutableIntStateOf(0) }
    // Empty = not yet picked (matches JSX `picks = {}` initial state)
    var goal  by remember { mutableStateOf("") }
    var freq  by remember { mutableStateOf("") }
    var units by remember { mutableStateOf("") }
    var split by remember { mutableStateOf("") }

    val totalSteps  = 4
    val selections  = listOf(goal, freq, units, split)
    val progress    = (step + 1).toFloat() / totalSteps

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top progress bar ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(MaterialTheme.colorScheme.outline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                    )
            )
        }

        // ── Step dots + counter ───────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(totalSteps) { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == step) 20.dp else 6.dp, 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (i <= step) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline
                            )
                    )
                }
            }
            Text(
                "${step + 1} of $totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }

        // ── Content (animated per step) ───────────────────────────
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState)
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                else
                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            },
            modifier = Modifier.weight(1f),
            label = "step"
        ) { s ->
            val options  = listOf(GOAL_OPTIONS, FREQ_OPTIONS, UNIT_OPTIONS, SPLIT_OPTIONS)[s]
            val selected = selections[s]
            val onSelect: (String) -> Unit = when (s) {
                0    -> { v -> goal  = v }
                1    -> { v -> freq  = v }
                2    -> { v -> units = v }
                else -> { v -> split = v }
            }

            val primary     = MaterialTheme.colorScheme.primary
            val primaryDark = primary.darken()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 28.dp, bottom = 24.dp)
            ) {
                // Logo mark — emoji icon, primary→primaryDark gradient, coloured glow shadow
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation    = 12.dp,
                            shape        = RoundedCornerShape(14.dp),
                            ambientColor = primary.copy(alpha = 0.35f),
                            spotColor    = primary.copy(alpha = 0.35f)
                        )
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(listOf(primary, primaryDark))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏋️", fontSize = 22.sp)
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    STEP_TITLES[s],
                    fontSize     = 26.sp,
                    fontWeight   = FontWeight.Black,
                    letterSpacing = (-0.3).sp,
                    lineHeight   = (26 * 1.2f).sp,
                    color        = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    STEP_SUBTITLES[s],
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(28.dp))

                options.forEach { opt ->
                    OnboardingOptionCard(
                        opt        = opt,
                        isSelected = selected == opt.id,
                        onClick    = { onSelect(opt.id) }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        // ── Footer ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isLast      = step == totalSteps - 1
            val chosen      = selections[step].isNotEmpty()
            val primary     = MaterialTheme.colorScheme.primary
            val primaryDark = primary.darken()

            Button(
                onClick = {
                    if (!chosen) return@Button
                    if (isLast) {
                        viewModel.completeOnboarding(
                            goal.ifEmpty { "Hypertrophy" },
                            freq.toIntOrNull() ?: 4,
                            units != "imperial",
                            split.ifEmpty { "PPL" },
                            onComplete
                        )
                    } else {
                        step++
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape          = RoundedCornerShape(16.dp),
                colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (chosen)
                                Brush.horizontalGradient(listOf(primary, primaryDark))
                            else
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.outline,
                                        MaterialTheme.colorScheme.outline
                                    )
                                ),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isLast) "Get Started 🚀" else "Continue →",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = if (chosen) Color.White
                                     else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (step > 0) {
                TextButton(
                    onClick  = { step-- },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "← Back",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── Option card ─────────────────────────────────────────────────────────────

@Composable
fun OnboardingOptionCard(opt: OnboardingOption, isSelected: Boolean, onClick: () -> Unit) {
    val primary     = MaterialTheme.colorScheme.primary
    val borderColor = if (isSelected) primary else MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Emoji icon box
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.background
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(opt.emoji, fontSize = 20.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    opt.label,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (isSelected) primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    opt.sub,
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            // Radio circle
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) primary else Color.Transparent)
                    .border(
                        2.dp,
                        if (isSelected) primary else MaterialTheme.colorScheme.outline,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Text(
                        "✓",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                }
            }
        }
    }
}
