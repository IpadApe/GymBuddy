package com.gymtracker.ui.screens.bodymap

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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

class BodyMapViewModel(app: GymTrackerApp) : ViewModel() {
    private val repo = app.repository
    private val _state = MutableStateFlow(BodyMapState())
    val state: StateFlow<BodyMapState> = _state.asStateFlow()
    init { loadData() }
    private fun loadData() {
        viewModelScope.launch {
            val statuses  = repo.getMuscleGroupStatuses()
            val imbalances = repo.getImbalanceAdvisories()
            val prefs     = repo.getPreferencesSync()
            _state.update { it.copy(muscleStatuses = statuses, imbalances = imbalances,
                isLoading = false, isFemale = prefs?.isFemale ?: false) }
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
// SCREEN
// ─────────────────────────────────────────────────────────────
@SuppressLint("DiscouragedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMapScreen(
    onBack: () -> Unit,
    viewModel: BodyMapViewModel = viewModel(factory = BodyMapViewModelFactory())
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    // Runtime check — build succeeds even before image is placed
    val bodyMapResId = remember {
        context.resources.getIdentifier("body_map", "drawable", context.packageName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Muscle Map") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Time filter ───────────────────────────────────────
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    FilterChip(selected = state.view7Days,  onClick = { viewModel.toggle7or30() }, label = { Text("7 Days") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = !state.view7Days, onClick = { viewModel.toggle7or30() }, label = { Text("30 Days") })
                }
            }

            // ── Legend ────────────────────────────────────────────
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    LegendItem(MuscleAdequate,    "Adequate")
                    LegendItem(MuscleOvertrained, "Overtrained")
                    LegendItem(Color(0xFF2A2A44), "Undertrained")
                    LegendItem(MuscleRecovering,  "Recovering")
                }
            }

            // ── Body diagram ──────────────────────────────────────
            item {
                if (bodyMapResId != 0) {
                    BodyMapDiagram(
                        resId = bodyMapResId,
                        muscleStatuses = state.muscleStatuses,
                        show7Days = state.view7Days
                    )
                } else {
                    // Placeholder until body_map.png is added to res/drawable/
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Save the body image as app/src/main/res/drawable/body_map.png",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Muscle status tiles ───────────────────────────────
            if (state.muscleStatuses.isNotEmpty()) {
                val rows = state.muscleStatuses.chunked(3)
                items(rows) { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { status ->
                            MuscleTile(status = status, show7Days = state.view7Days, modifier = Modifier.weight(1f))
                        }
                        repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
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
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${advisory.overtrainedMuscle.displayName} vs ${advisory.undertrainedMuscle.displayName}",
                                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(advisory.recommendation, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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
// BODY MAP DIAGRAM
// Image (line art) sits on top via BlendMode.Multiply:
//   • White pixels → transparent (overlays show through)
//   • Black lines  → remain black (anatomy lines visible)
// ─────────────────────────────────────────────────────────────
@Composable
fun BodyMapDiagram(
    resId: Int,
    muscleStatuses: List<MuscleGroupStatus>,
    show7Days: Boolean
) {
    val context   = LocalContext.current
    val statusMap = muscleStatuses.associateBy { it.muscleGroup }

    // Load bitmap once — used inside Canvas.drawImage with BlendMode.Multiply
    val imageBitmap: ImageBitmap? = remember(resId) {
        try {
            BitmapFactory.decodeResource(context.resources, resId)?.asImageBitmap()
        } catch (_: Exception) { null }
    }

    fun mColor(g: MuscleGroup): Color = when (statusMap[g]?.status) {
        MuscleStatus.ADEQUATE    -> MuscleAdequate
        MuscleStatus.OVERTRAINED -> MuscleOvertrained
        else                     -> MuscleUndertrained
    }

    fun mAlpha(g: MuscleGroup): Float {
        val s = statusMap[g] ?: return 0f
        val sets = if (show7Days) s.totalSets7Days else s.totalSets30Days
        if (sets == 0) return 0.20f
        return (0.45f + (sets.coerceAtMost(20).toFloat() / 20f) * 0.40f)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F8)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Single Canvas: overlays first, then image on top with Multiply blend
        // Multiply: white image pixels × overlay color = overlay color (shows through)
        //           black image lines  × overlay color = black        (lines stay sharp)
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(601f / 525f)  // exact body_map.png dimensions (601×525)
        ) {
            val w = size.width; val h = size.height

            // 1 – white background so Multiply has a base to work against
            drawRect(Color.White)

            // 2 – colored muscle overlays
            drawFrontOverlays(w, h, ::mColor, ::mAlpha)
            drawBackOverlays(w, h, ::mColor, ::mAlpha)

            // 3 – line-art image, Multiply blend:
            //     white × color = color  →  overlays show in muscle areas
            //     black × color = black  →  anatomy lines stay crisp
            imageBitmap?.let { bmp ->
                drawImage(
                    image     = bmp,
                    dstOffset = IntOffset.Zero,
                    dstSize   = IntSize(w.toInt(), h.toInt()),
                    blendMode = BlendMode.Multiply
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// FRONT overlays
// Measured from body_map.png (601×525):
//   Front figure: x=0.030–0.474, center x=0.252
//   Content y:    0.048–0.950
// ─────────────────────────────────────────────────────────────
private fun DrawScope.drawFrontOverlays(
    w: Float, h: Float,
    color: (MuscleGroup) -> Color,
    alpha: (MuscleGroup) -> Float
) {
    fun o(cx: Float, cy: Float, rx: Float, ry: Float, g: MuscleGroup) {
        drawOval(color(g).copy(alpha(g)),
            topLeft = Offset((cx - rx) * w, (cy - ry) * h),
            size    = Size(2 * rx * w, 2 * ry * h))
    }

    // Deltoids — shoulder caps appear at y≈0.21 (left=0.163, right=0.343)
    o(0.173f, 0.215f, 0.024f, 0.036f, MuscleGroup.SHOULDERS)   // left
    o(0.331f, 0.215f, 0.024f, 0.036f, MuscleGroup.SHOULDERS)   // right

    // Pectorals — torso centre x=0.252, pecs sit left/right of sternum
    o(0.210f, 0.265f, 0.038f, 0.050f, MuscleGroup.CHEST)       // left pec
    o(0.294f, 0.265f, 0.038f, 0.050f, MuscleGroup.CHEST)       // right pec

    // Biceps — front of upper arm, arm outer edge at y=0.25–0.35 is x=0.120/0.388
    o(0.136f, 0.310f, 0.020f, 0.056f, MuscleGroup.BICEPS)      // left
    o(0.368f, 0.310f, 0.020f, 0.056f, MuscleGroup.BICEPS)      // right

    // Forearms — arm extends to x=0.078/0.428 at y=0.35–0.45
    o(0.096f, 0.455f, 0.018f, 0.050f, MuscleGroup.FOREARMS)    // left
    o(0.406f, 0.455f, 0.018f, 0.050f, MuscleGroup.FOREARMS)    // right

    // Abs — 2 columns × 3 rows (6-pack grid), centred on x=0.252
    val ac = color(MuscleGroup.ABS); val aa = alpha(MuscleGroup.ABS)
    for (cx in listOf(0.228f, 0.276f)) {
        for (cy in listOf(0.325f, 0.375f, 0.425f)) {
            drawOval(ac.copy(aa),
                Offset((cx - 0.022f) * w, (cy - 0.030f) * h),
                Size(0.044f * w, 0.060f * h))
        }
    }
    // Obliques
    o(0.183f, 0.390f, 0.017f, 0.048f, MuscleGroup.ABS)         // left
    o(0.321f, 0.390f, 0.017f, 0.048f, MuscleGroup.ABS)         // right

    // Quads — thighs at y=0.65–0.75: left=0.160, right=0.346, gap≈0.240–0.265
    o(0.200f, 0.678f, 0.035f, 0.062f, MuscleGroup.QUADS)       // left thigh
    o(0.305f, 0.678f, 0.035f, 0.062f, MuscleGroup.QUADS)       // right thigh

    // Tibialis anterior — front of shin
    o(0.192f, 0.815f, 0.014f, 0.038f, MuscleGroup.CALVES)      // left
    o(0.313f, 0.815f, 0.014f, 0.038f, MuscleGroup.CALVES)      // right
}

// ─────────────────────────────────────────────────────────────
// BACK overlays
// Measured from body_map.png (601×525):
//   Back figure: x=0.532–0.980, center x=0.756
// ─────────────────────────────────────────────────────────────
private fun DrawScope.drawBackOverlays(
    w: Float, h: Float,
    color: (MuscleGroup) -> Color,
    alpha: (MuscleGroup) -> Float
) {
    fun o(cx: Float, cy: Float, rx: Float, ry: Float, g: MuscleGroup) {
        drawOval(color(g).copy(alpha(g)),
            topLeft = Offset((cx - rx) * w, (cy - ry) * h),
            size    = Size(2 * rx * w, 2 * ry * h))
    }

    // Rear deltoids — shoulder span at y=0.15–0.25: left=0.637, right=0.875
    o(0.648f, 0.215f, 0.024f, 0.036f, MuscleGroup.SHOULDERS)   // left
    o(0.862f, 0.215f, 0.024f, 0.036f, MuscleGroup.SHOULDERS)   // right

    // Trapezius — upper back, centred on spine x=0.756
    o(0.756f, 0.235f, 0.088f, 0.060f, MuscleGroup.BACK)

    // Lats — sweep down from armpit
    o(0.672f, 0.360f, 0.040f, 0.075f, MuscleGroup.BACK)        // left
    o(0.840f, 0.360f, 0.040f, 0.075f, MuscleGroup.BACK)        // right

    // Triceps — back of upper arm, arm outer edge at y=0.25–0.35: x=0.616/0.897
    o(0.628f, 0.320f, 0.022f, 0.058f, MuscleGroup.TRICEPS)     // left
    o(0.882f, 0.320f, 0.022f, 0.058f, MuscleGroup.TRICEPS)     // right

    // Forearms (back) — arm extends to x=0.579/0.933 at y=0.35–0.45
    o(0.596f, 0.450f, 0.018f, 0.048f, MuscleGroup.FOREARMS)    // left
    o(0.916f, 0.450f, 0.018f, 0.048f, MuscleGroup.FOREARMS)    // right

    // Glutes — large rounded muscles at y≈0.56–0.67
    o(0.697f, 0.600f, 0.047f, 0.057f, MuscleGroup.GLUTES)      // left
    o(0.815f, 0.600f, 0.047f, 0.057f, MuscleGroup.GLUTES)      // right

    // Hamstrings — back of thigh, y=0.65–0.75: left=0.651, right=0.864
    o(0.697f, 0.678f, 0.040f, 0.062f, MuscleGroup.HAMSTRINGS)  // left
    o(0.815f, 0.678f, 0.040f, 0.062f, MuscleGroup.HAMSTRINGS)  // right

    // Calves — gastrocnemius
    o(0.690f, 0.815f, 0.028f, 0.050f, MuscleGroup.CALVES)      // left
    o(0.820f, 0.815f, 0.028f, 0.050f, MuscleGroup.CALVES)      // right
}

// ─────────────────────────────────────────────────────────────
// MUSCLE STATUS TILE (Explore-card style)
// ─────────────────────────────────────────────────────────────
private fun muscleGradient(group: MuscleGroup, status: MuscleStatus): Brush {
    if (status == MuscleStatus.UNDERTRAINED)
        return Brush.linearGradient(listOf(Color(0xFF1A1A2C), Color(0xFF2A2A44)))
    if (status == MuscleStatus.OVERTRAINED)
        return Brush.linearGradient(listOf(Color(0xFF6B0000), ErrorRed))
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
        MuscleGroup.CARDIO     -> Brush.linearGradient(listOf(Color(0xFF7F0000), Color(0xFFEF5350)))
    }
}

private fun muscleIcon(group: MuscleGroup): ImageVector = when (group) {
    MuscleGroup.ABS                                                          -> Icons.Filled.ViewModule
    MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS,
    MuscleGroup.GLUTES, MuscleGroup.CALVES                                  -> Icons.AutoMirrored.Filled.DirectionsRun
    else                                                                     -> Icons.Filled.FitnessCenter
}

@Composable
fun MuscleTile(status: MuscleGroupStatus, show7Days: Boolean, modifier: Modifier = Modifier) {
    val sets   = if (show7Days) status.totalSets7Days else status.totalSets30Days
    val daysAgo = status.daysSinceLastTrained

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(muscleGradient(status.muscleGroup, status.status))
            .padding(14.dp)
    ) {
        Column {
            Icon(muscleIcon(status.muscleGroup), null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(status.muscleGroup.displayName, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(2.dp))
            Text("$sets sets", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.80f))
            Text(if (daysAgo != null) "${daysAgo}d ago" else "Never",
                style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.60f))
        }
        Box(modifier = Modifier.size(8.dp).clip(CircleShape)
            .background(Color.White.copy(alpha = if (status.status == MuscleStatus.UNDERTRAINED) 0.25f else 0.9f))
            .align(Alignment.TopEnd))
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
