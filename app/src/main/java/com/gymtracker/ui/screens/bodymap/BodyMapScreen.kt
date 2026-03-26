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

class BodyMapViewModel(private val app: GymTrackerApp) : ViewModel() {
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
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
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
        // Multiply: white image pixels × overlay colour = overlay colour (shows through)
        //           black image lines  × overlay colour = black        (lines stay sharp)
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.32f)   // matches the uploaded body_map image proportions
        ) {
            val W = size.width; val H = size.height

            // 1 – white background so Multiply has a base to work against
            drawRect(Color.White)

            // 2 – coloured muscle overlays
            drawFrontOverlays(W, H, ::mColor, ::mAlpha)
            drawBackOverlays(W, H, ::mColor, ::mAlpha)

            // 3 – line-art image, Multiply blend:
            //     white × colour = colour  →  overlays show in muscle areas
            //     black × colour = black   →  anatomy lines stay crisp
            imageBitmap?.let { bmp ->
                drawImage(
                    image     = bmp,
                    dstOffset = IntOffset.Zero,
                    dstSize   = IntSize(W.toInt(), H.toInt()),
                    blendMode = BlendMode.Multiply
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// FRONT overlays  (coordinates as fraction of canvas W × H)
// Image layout: front figure occupies x ≈ 3%–47%, y ≈ 1%–97%
//               figure center ≈ x=25%
// ─────────────────────────────────────────────────────────────
private fun DrawScope.drawFrontOverlays(
    W: Float, H: Float,
    color: (MuscleGroup) -> Color,
    alpha: (MuscleGroup) -> Float
) {
    fun o(cx: Float, cy: Float, rx: Float, ry: Float, g: MuscleGroup) {
        drawOval(color(g).copy(alpha(g)),
            topLeft = Offset((cx - rx) * W, (cy - ry) * H),
            size    = Size(2 * rx * W, 2 * ry * H))
    }

    // Deltoids / Shoulders
    o(0.095f, 0.195f, 0.038f, 0.060f, MuscleGroup.SHOULDERS)   // left
    o(0.405f, 0.195f, 0.038f, 0.060f, MuscleGroup.SHOULDERS)   // right

    // Chest
    o(0.175f, 0.240f, 0.058f, 0.073f, MuscleGroup.CHEST)       // left pec
    o(0.325f, 0.240f, 0.058f, 0.073f, MuscleGroup.CHEST)       // right pec

    // Biceps
    o(0.068f, 0.360f, 0.030f, 0.083f, MuscleGroup.BICEPS)      // left
    o(0.432f, 0.360f, 0.030f, 0.083f, MuscleGroup.BICEPS)      // right

    // Forearms
    o(0.057f, 0.510f, 0.025f, 0.068f, MuscleGroup.FOREARMS)    // left
    o(0.443f, 0.510f, 0.025f, 0.068f, MuscleGroup.FOREARMS)    // right

    // Abs — 2 columns × 3 rows
    val ac = color(MuscleGroup.ABS); val aa = alpha(MuscleGroup.ABS)
    for (cx in listOf(0.218f, 0.282f)) {
        for (cy in listOf(0.340f, 0.405f, 0.468f)) {
            drawOval(ac.copy(aa), Offset((cx - 0.027f) * W, (cy - 0.030f) * H),
                Size(0.054f * W, 0.060f * H))
        }
    }
    // Obliques
    o(0.155f, 0.435f, 0.025f, 0.068f, MuscleGroup.ABS)         // left
    o(0.345f, 0.435f, 0.025f, 0.068f, MuscleGroup.ABS)         // right

    // Quads
    o(0.170f, 0.670f, 0.048f, 0.100f, MuscleGroup.QUADS)       // left
    o(0.330f, 0.670f, 0.048f, 0.100f, MuscleGroup.QUADS)       // right

    // Tibialis anterior (front of shin)
    o(0.163f, 0.855f, 0.020f, 0.058f, MuscleGroup.CALVES)      // left
    o(0.337f, 0.855f, 0.020f, 0.058f, MuscleGroup.CALVES)      // right
}

// ─────────────────────────────────────────────────────────────
// BACK overlays  (back figure: x ≈ 53%–97%, center ≈ x=75%)
// ─────────────────────────────────────────────────────────────
private fun DrawScope.drawBackOverlays(
    W: Float, H: Float,
    color: (MuscleGroup) -> Color,
    alpha: (MuscleGroup) -> Float
) {
    fun o(cx: Float, cy: Float, rx: Float, ry: Float, g: MuscleGroup) {
        drawOval(color(g).copy(alpha(g)),
            topLeft = Offset((cx - rx) * W, (cy - ry) * H),
            size    = Size(2 * rx * W, 2 * ry * H))
    }

    // Rear deltoids
    o(0.595f, 0.195f, 0.038f, 0.060f, MuscleGroup.SHOULDERS)   // left (viewer's)
    o(0.905f, 0.195f, 0.038f, 0.060f, MuscleGroup.SHOULDERS)   // right

    // Trapezius (wide diamond, upper back)
    o(0.750f, 0.230f, 0.112f, 0.083f, MuscleGroup.BACK)

    // Lats
    o(0.622f, 0.385f, 0.053f, 0.103f, MuscleGroup.BACK)        // left lat
    o(0.878f, 0.385f, 0.053f, 0.103f, MuscleGroup.BACK)        // right lat

    // Triceps (back of upper arm)
    o(0.574f, 0.360f, 0.030f, 0.083f, MuscleGroup.TRICEPS)     // left
    o(0.926f, 0.360f, 0.030f, 0.083f, MuscleGroup.TRICEPS)     // right

    // Forearms (back)
    o(0.557f, 0.510f, 0.025f, 0.068f, MuscleGroup.FOREARMS)    // left
    o(0.943f, 0.510f, 0.025f, 0.068f, MuscleGroup.FOREARMS)    // right

    // Glutes
    o(0.685f, 0.630f, 0.063f, 0.077f, MuscleGroup.GLUTES)      // left
    o(0.815f, 0.630f, 0.063f, 0.077f, MuscleGroup.GLUTES)      // right

    // Hamstrings
    o(0.665f, 0.685f, 0.052f, 0.100f, MuscleGroup.HAMSTRINGS)  // left
    o(0.835f, 0.685f, 0.052f, 0.100f, MuscleGroup.HAMSTRINGS)  // right

    // Calves (gastrocnemius diamond)
    o(0.655f, 0.860f, 0.042f, 0.068f, MuscleGroup.CALVES)      // left
    o(0.845f, 0.860f, 0.042f, 0.068f, MuscleGroup.CALVES)      // right
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
    }
}

private fun muscleIcon(group: MuscleGroup): ImageVector = when (group) {
    MuscleGroup.ABS                                                          -> Icons.Filled.ViewModule
    MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS,
    MuscleGroup.GLUTES, MuscleGroup.CALVES                                  -> Icons.Filled.DirectionsRun
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
