package com.gymtracker.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.database.entities.WorkoutSessionEntity
import com.gymtracker.ui.components.*
import com.gymtracker.ui.theme.*
import com.gymtracker.util.AppInstaller
import com.gymtracker.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit,
    onViewProgress: () -> Unit,
    onViewBodyMap: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onResumeWorkout: (Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeWorkoutSessionId by GymTrackerApp.instance.activeWorkoutSessionId.collectAsState()
    val context = LocalContext.current

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    val todayDate = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── HERO HEADER ──────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), Color.Transparent)
                        )
                    )
                    .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 8.dp)
            ) {
                Column {
                    Text(
                        "Hi, athlete! 👋",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    if (uiState.weeklySessionCount > 0) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", fontSize = 12.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${uiState.weeklySessionCount} workout${if (uiState.weeklySessionCount != 1) "s" else ""} this week",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ── UPDATE BANNER ─────────────────────────────────────────
        val update = uiState.availableUpdate
        if (update != null && !uiState.updateDismissed) {
            item {
                Spacer(Modifier.height(4.dp))
                UpdateBanner(
                    versionName = update.versionName,
                    releaseNotes = update.releaseNotes,
                    mandatory = update.mandatory,
                    onUpdate = {
                        AppInstaller.downloadAndInstall(context, update.downloadUrl, update.versionName)
                    },
                    onDismiss = { viewModel.dismissUpdate() }
                )
            }
        }

        // ── RESUME BANNER (when workout in background) ──────────
        if (activeWorkoutSessionId != null) {
            item {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF3730A3))))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                        .clickable { onResumeWorkout(activeWorkoutSessionId!!) }
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("NOW ACTIVE", fontSize = 11.sp, fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = 0.8f), letterSpacing = 2.sp)
                            Text("Workout in Progress",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black, color = Color.White)
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.White))
                                Text(activeWorkoutSessionId.toString(), fontSize = 12.sp,
                                    fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                        Box(
                            modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.PlayArrow, null, tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(28.dp))
                        }
                    }
                    Text("💪", fontSize = 80.sp,
                        modifier = Modifier.align(Alignment.BottomEnd).offset(x = 20.dp, y = 24.dp).alpha(0.1f))
                }
            }
        }

        // ── START WORKOUT ────────────────────────────────────────
        item {
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                GradientButton(
                    text = "▶  Start Workout",
                    onClick = onStartWorkout,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ── THIS WEEK STATS ───────────────────────────────────────
        item {
            Spacer(Modifier.height(28.dp))
            Text(
                "THIS WEEK",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BentoLargeStatCard(
                    value = "${uiState.weeklySessionCount}",
                    goalText = "/ 7",
                    label = "Workouts",
                    emoji = "🏋",
                    accent = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BentoSmallStatCard(
                        value = FormatUtils.formatVolume(uiState.weeklyVolume, uiState.useMetric),
                        label = "Volume",
                        accent = MaterialTheme.colorScheme.secondary,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                    BentoSmallStatCard(
                        value = if (uiState.avgDuration > 0) FormatUtils.formatDuration(uiState.avgDuration) else "--",
                        label = "Avg Time",
                        accent = MaterialTheme.colorScheme.tertiary,
                        icon = Icons.Filled.Timer,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }
            }
        }

        // ── QUICK ACCESS ──────────────────────────────────────────
        item {
            Spacer(Modifier.height(28.dp))
            Text(
                "EXPLORE",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExploreCard(
                    emoji = "💪",
                    label = "Body Map",
                    sublabel = "Muscle status",
                    onClick = onViewBodyMap,
                    gradient = Brush.linearGradient(listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primary
                    )),
                    modifier = Modifier.weight(1f)
                )
                ExploreCard(
                    emoji = "📊",
                    label = "Progress",
                    sublabel = "PRs & charts",
                    onClick = onViewProgress,
                    gradient = Brush.linearGradient(listOf(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.tertiary
                    )),
                    modifier = Modifier.weight(1f)
                )
                ExploreCard(
                    emoji = "🔍",
                    label = "Exercises",
                    sublabel = "Browse all",
                    onClick = onNavigateToExercises,
                    gradient = Brush.linearGradient(listOf(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.secondary
                    )),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── RECOMMENDATIONS ───────────────────────────────────────
        if (uiState.recommendedMuscles.isNotEmpty()) {
            item {
                Spacer(Modifier.height(28.dp))
                Text(
                    "RECOMMENDED TODAY",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 16.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Train these today",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Based on your training history",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.recommendedMuscles) { muscle ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(getMuscleColor(muscle.displayName).copy(alpha = 0.15f))
                                        .border(1.dp, getMuscleColor(muscle.displayName).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        muscle.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = getMuscleColor(muscle.displayName),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        if (uiState.deloadRecommended) {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(WarningOrange.copy(alpha = 0.10f))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Warning, null, tint = WarningOrange, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Deload week recommended — 4+ weeks of consistent training",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WarningOrange
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── RECENT WORKOUTS ───────────────────────────────────────
        item {
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RECENT WORKOUTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "See All ›",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onViewProgress() }
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        if (uiState.recentSessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.FitnessCenter, null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No workouts yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Start your first workout!", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(uiState.recentSessions.take(5)) { session ->
                Spacer(Modifier.height(8.dp))
                RecentWorkoutCard(
                    session = session,
                    useMetric = uiState.useMetric,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// BENTO LARGE STAT CARD
// ─────────────────────────────────────────────────────────────────
@Composable
fun BentoLargeStatCard(
    value: String,
    goalText: String,
    label: String,
    emoji: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) { Text(emoji, fontSize = 20.sp) }
                Spacer(Modifier.height(8.dp))
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 40.sp, fontWeight = FontWeight.Black, color = accent, lineHeight = 40.sp)
                Spacer(Modifier.width(4.dp))
                Text(goalText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// BENTO SMALL STAT CARD
// ─────────────────────────────────────────────────────────────────
@Composable
fun BentoSmallStatCard(
    value: String,
    label: String,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
                Spacer(Modifier.height(4.dp))
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = accent, letterSpacing = (-0.5).sp)
            }
            Icon(icon, null, tint = accent.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// EXPLORE CARD
// ─────────────────────────────────────────────────────────────────
@Composable
fun ExploreCard(
    emoji: String,
    label: String,
    sublabel: String,
    onClick: () -> Unit,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(10.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(sublabel, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// RECENT WORKOUT CARD
// ─────────────────────────────────────────────────────────────────
@Composable
fun RecentWorkoutCard(
    session: WorkoutSessionEntity,
    useMetric: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)),
                    RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                )
                .align(Alignment.CenterStart)
        )
        Column(modifier = Modifier.padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        session.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        FormatUtils.formatDate(session.startTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (session.splitType.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            session.splitType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                WorkoutMetricChip(
                    emoji = "⏱",
                    value = FormatUtils.formatDuration(session.durationSeconds),
                    label = "Duration",
                    color = MaterialTheme.colorScheme.secondary
                )
                WorkoutMetricChip(
                    emoji = "🏋",
                    value = FormatUtils.formatVolume(session.totalVolumeKg, useMetric),
                    label = "Volume",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun WorkoutMetricChip(emoji: String, value: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 12.sp, color = color)
        Spacer(Modifier.width(5.dp))
        Column {
            Text(value, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// Legacy kept for EmptyState usage in ActiveWorkoutScreen
@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier, onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// UPDATE BANNER
// ═══════════════════════════════════════════════════════════════

@Composable
fun UpdateBanner(
    versionName: String,
    releaseNotes: String,
    mandatory: Boolean,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF1565C0).copy(alpha = 0.20f),
                        Color(0xFF0D47A1).copy(alpha = 0.12f)
                    )
                )
            )
            .border(1.dp, Color(0xFF1976D2).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1976D2).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.SystemUpdate,
                        contentDescription = null,
                        tint = Color(0xFF42A5F5),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Update available — v$versionName",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF90CAF9)
                    )
                    Text(
                        if (mandatory) "Required update" else "Tap to update the app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!mandatory) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (releaseNotes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        if (expanded) "Hide what's new ▲" else "What's new ▼",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64B5F6)
                    )
                }
                AnimatedVisibility(visible = expanded) {
                    Text(
                        releaseNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onUpdate,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Download, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Download & Install", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
