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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
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
            _state.update { it.copy(
                muscleStatuses = statuses,
                imbalances = imbalances,
                isLoading = false,
                isFemale = prefs?.isFemale ?: false
            ) }
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
                            .height(480.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BodyMapCanvas(
                            muscleStatuses = state.muscleStatuses,
                            show7Days = state.view7Days,
                            isFemale = state.isFemale
                        )
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

// ═══════════════════════════════════════════════════════════════
// REALISTIC BODY MAP CANVAS
// Virtual coordinate space: 100 × 260 per body panel
// ═══════════════════════════════════════════════════════════════

@Composable
fun BodyMapCanvas(
    muscleStatuses: List<MuscleGroupStatus>,
    show7Days: Boolean,
    isFemale: Boolean
) {
    val statusMap = muscleStatuses.associateBy { it.muscleGroup }

    fun muscleColor(m: MuscleGroup): Color {
        val s = statusMap[m] ?: return MuscleUndertrained
        return when (s.status) {
            MuscleStatus.ADEQUATE -> MuscleAdequate
            MuscleStatus.OVERTRAINED -> MuscleOvertrained
            MuscleStatus.UNDERTRAINED -> MuscleUndertrained
        }
    }

    fun muscleAlpha(m: MuscleGroup): Float {
        val s = statusMap[m] ?: return 0.28f
        val sets = if (show7Days) s.totalSets7Days else s.totalSets30Days
        return (0.38f + (sets.coerceAtMost(20).toFloat() / 20f) * 0.55f)
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(460.dp)) {
        val panelW = size.width / 2f
        val panelH = size.height

        // Scale factors: virtual 100×260 → actual pixels
        val sx = panelW / 100f
        val sy = panelH / 260f

        // Front view
        translate(left = 0f) {
            drawBodyPanel(sx, sy, isFront = true, isFemale = isFemale, ::muscleColor, ::muscleAlpha)
        }
        // Back view
        translate(left = panelW) {
            drawBodyPanel(sx, sy, isFront = false, isFemale = isFemale, ::muscleColor, ::muscleAlpha)
        }
    }
}

private fun DrawScope.drawBodyPanel(
    sx: Float, sy: Float,
    isFront: Boolean,
    isFemale: Boolean,
    muscleColor: (MuscleGroup) -> Color,
    muscleAlpha: (MuscleGroup) -> Float
) {
    val silhouette = Color(0xFF2A3550)
    val outline = Color(0xFF3D4F6E)

    // ── Head ──
    drawOval(
        color = silhouette,
        topLeft = Offset(37 * sx, 1 * sy),
        size = Size(26 * sx, 28 * sy)
    )

    // ── Neck ──
    val neckPath = Path().apply {
        moveTo(44 * sx, 27 * sy); lineTo(56 * sx, 27 * sy)
        lineTo(57 * sx, 37 * sy); lineTo(43 * sx, 37 * sy); close()
    }
    drawPath(neckPath, silhouette)

    // ── Main body silhouette ──
    val hipMult = if (isFemale) 1.12f else 1f
    val waistMult = if (isFemale) 0.88f else 1f

    val body = Path().apply {
        // Start neck-left → clockwise
        moveTo(43 * sx, 30 * sy)
        // Left shoulder slope
        cubicTo(38 * sx, 32 * sy, 22 * sx, 32 * sy, 14 * sx, 40 * sy)
        // Left upper arm outer
        cubicTo(10 * sx, 47 * sy, 8 * sx, 60 * sy, 10 * sx, 72 * sy)
        // Left forearm outer
        cubicTo(12 * sx, 82 * sy, 14 * sx, 93 * sy, 16 * sx, 100 * sy)
        // Left hand
        cubicTo(17 * sx, 104 * sy, 18 * sx, 108 * sy, 19 * sx, 112 * sy)
        lineTo(24 * sx, 112 * sy)
        // Left hip/waist
        cubicTo(26 * sx, 108 * sy, 27 * sx * waistMult + 1, 102 * sy, 28 * sx * waistMult, 102 * sy)
        // Left waist inward
        cubicTo(27 * sx, 110 * sy, (22 * sx * hipMult), 118 * sy, (21 * sx * hipMult), 126 * sy)
        // Left outer thigh
        cubicTo((20 * sx * hipMult), 136 * sy, 22 * sx, 152 * sy, 23 * sx, 163 * sy)
        // Left knee outer
        cubicTo(24 * sx, 170 * sy, 24 * sx, 175 * sy, 25 * sx, 179 * sy)
        // Left shin outer
        cubicTo(26 * sx, 192 * sy, 27 * sx, 206 * sy, 27 * sx, 216 * sy)
        // Left ankle/foot
        cubicTo(26 * sx, 222 * sy, 24 * sx, 226 * sy, 22 * sx, 228 * sy)
        lineTo(38 * sx, 228 * sy)
        lineTo(38 * sx, 224 * sy)
        // Inner left leg going up
        cubicTo(37 * sx, 214 * sy, 37 * sx, 175 * sy, 39 * sx, 160 * sy)
        // Crotch
        cubicTo(40 * sx, 155 * sy, 60 * sx, 155 * sy, 61 * sx, 160 * sy)
        // Inner right leg going down
        cubicTo(63 * sx, 175 * sy, 63 * sx, 214 * sy, 62 * sx, 224 * sy)
        lineTo(62 * sx, 228 * sy)
        lineTo(78 * sx, 228 * sy)
        // Right ankle/foot
        cubicTo(76 * sx, 226 * sy, 74 * sx, 222 * sy, 73 * sx, 216 * sy)
        // Right shin outer
        cubicTo(73 * sx, 206 * sy, 74 * sx, 192 * sy, 75 * sx, 179 * sy)
        // Right knee outer
        cubicTo(76 * sx, 175 * sy, 76 * sx, 170 * sy, 77 * sx, 163 * sy)
        // Right outer thigh
        cubicTo(78 * sx, 152 * sy, (80 * sx * hipMult), 136 * sy, (79 * sx * hipMult), 126 * sy)
        // Right hip
        cubicTo((78 * sx * hipMult), 118 * sy, (73 * sx * waistMult), 110 * sy, 72 * sx, 102 * sy)
        cubicTo(73 * sx * waistMult, 102 * sy, 74 * sx, 108 * sy, 76 * sx, 112 * sy)
        lineTo(81 * sx, 112 * sy)
        // Right hand
        cubicTo(82 * sx, 108 * sy, 83 * sx, 104 * sy, 84 * sx, 100 * sy)
        // Right forearm outer
        cubicTo(86 * sx, 93 * sy, 88 * sx, 82 * sy, 90 * sx, 72 * sy)
        // Right upper arm outer
        cubicTo(92 * sx, 60 * sy, 90 * sx, 47 * sy, 86 * sx, 40 * sy)
        // Right shoulder slope
        cubicTo(78 * sx, 32 * sy, 62 * sx, 32 * sy, 57 * sx, 30 * sy)
        close()
    }
    drawPath(body, silhouette)
    drawPath(body, outline, style = Stroke(width = 1f * sx))

    // ── Muscle regions ──
    if (isFront) drawFrontMuscles(sx, sy, isFemale, muscleColor, muscleAlpha)
    else drawBackMuscles(sx, sy, isFemale, muscleColor, muscleAlpha)
}

private fun DrawScope.drawMuscle(path: Path, color: Color, alpha: Float) {
    drawPath(path, color.copy(alpha = alpha), style = Fill)
    drawPath(path, color.copy(alpha = (alpha * 0.6f + 0.3f).coerceAtMost(0.85f)),
        style = Stroke(width = 1.2f))
}

// ─────────────────────────────────────────────────
// FRONT muscles
// ─────────────────────────────────────────────────
private fun DrawScope.drawFrontMuscles(
    sx: Float, sy: Float,
    isFemale: Boolean,
    color: (MuscleGroup) -> Color,
    alpha: (MuscleGroup) -> Float
) {
    // Left deltoid
    drawMuscle(Path().apply {
        moveTo(15 * sx, 38 * sy)
        cubicTo(10 * sx, 36 * sy, 9 * sx, 47 * sy, 11 * sx, 56 * sy)
        cubicTo(13 * sx, 60 * sy, 18 * sx, 60 * sy, 22 * sx, 54 * sy)
        cubicTo(26 * sx, 48 * sy, 24 * sx, 38 * sy, 20 * sx, 35 * sy)
        cubicTo(18 * sx, 34 * sy, 16 * sx, 35 * sy, 15 * sx, 38 * sy); close()
    }, color(MuscleGroup.SHOULDERS), alpha(MuscleGroup.SHOULDERS))

    // Right deltoid (mirror)
    drawMuscle(Path().apply {
        moveTo(85 * sx, 38 * sy)
        cubicTo(90 * sx, 36 * sy, 91 * sx, 47 * sy, 89 * sx, 56 * sy)
        cubicTo(87 * sx, 60 * sy, 82 * sx, 60 * sy, 78 * sx, 54 * sy)
        cubicTo(74 * sx, 48 * sy, 76 * sx, 38 * sy, 80 * sx, 35 * sy)
        cubicTo(82 * sx, 34 * sy, 84 * sx, 35 * sy, 85 * sx, 38 * sy); close()
    }, color(MuscleGroup.SHOULDERS), alpha(MuscleGroup.SHOULDERS))

    if (!isFemale) {
        // Left pec
        drawMuscle(Path().apply {
            moveTo(43 * sx, 38 * sy)
            cubicTo(34 * sx, 37 * sy, 22 * sx, 42 * sy, 20 * sx, 52 * sy)
            cubicTo(19 * sx, 58 * sy, 24 * sx, 66 * sy, 33 * sx, 67 * sy)
            cubicTo(39 * sx, 68 * sy, 44 * sx, 65 * sy, 45 * sx, 60 * sy)
            cubicTo(46 * sx, 55 * sy, 45 * sx, 46 * sy, 43 * sx, 38 * sy); close()
        }, color(MuscleGroup.CHEST), alpha(MuscleGroup.CHEST))

        // Right pec
        drawMuscle(Path().apply {
            moveTo(57 * sx, 38 * sy)
            cubicTo(66 * sx, 37 * sy, 78 * sx, 42 * sy, 80 * sx, 52 * sy)
            cubicTo(81 * sx, 58 * sy, 76 * sx, 66 * sy, 67 * sx, 67 * sy)
            cubicTo(61 * sx, 68 * sy, 56 * sx, 65 * sy, 55 * sx, 60 * sy)
            cubicTo(54 * sx, 55 * sy, 55 * sx, 46 * sy, 57 * sx, 38 * sy); close()
        }, color(MuscleGroup.CHEST), alpha(MuscleGroup.CHEST))
    } else {
        // Female chest — smaller indication at upper chest
        drawMuscle(Path().apply {
            moveTo(38 * sx, 42 * sy)
            cubicTo(30 * sx, 44 * sy, 24 * sx, 50 * sy, 26 * sx, 58 * sy)
            cubicTo(28 * sx, 63 * sy, 35 * sx, 65 * sy, 42 * sx, 62 * sy)
            cubicTo(46 * sx, 60 * sy, 47 * sx, 55 * sy, 46 * sx, 48 * sy)
            cubicTo(44 * sx, 43 * sy, 41 * sx, 41 * sy, 38 * sx, 42 * sy); close()
        }, color(MuscleGroup.CHEST), alpha(MuscleGroup.CHEST))
        drawMuscle(Path().apply {
            moveTo(62 * sx, 42 * sy)
            cubicTo(70 * sx, 44 * sy, 76 * sx, 50 * sy, 74 * sx, 58 * sy)
            cubicTo(72 * sx, 63 * sy, 65 * sx, 65 * sy, 58 * sx, 62 * sy)
            cubicTo(54 * sx, 60 * sy, 53 * sx, 55 * sy, 54 * sx, 48 * sy)
            cubicTo(56 * sx, 43 * sy, 59 * sx, 41 * sy, 62 * sx, 42 * sy); close()
        }, color(MuscleGroup.CHEST), alpha(MuscleGroup.CHEST))
    }

    // Left bicep
    drawMuscle(Path().apply {
        moveTo(11 * sx, 58 * sy)
        cubicTo(9 * sx, 62 * sy, 9 * sx, 74 * sy, 11 * sx, 79 * sy)
        cubicTo(13 * sx, 82 * sy, 17 * sx, 82 * sy, 19 * sx, 78 * sy)
        cubicTo(21 * sx, 73 * sy, 21 * sx, 62 * sy, 19 * sx, 58 * sy)
        cubicTo(17 * sx, 55 * sy, 13 * sx, 55 * sy, 11 * sx, 58 * sy); close()
    }, color(MuscleGroup.BICEPS), alpha(MuscleGroup.BICEPS))

    // Right bicep
    drawMuscle(Path().apply {
        moveTo(89 * sx, 58 * sy)
        cubicTo(91 * sx, 62 * sy, 91 * sx, 74 * sy, 89 * sx, 79 * sy)
        cubicTo(87 * sx, 82 * sy, 83 * sx, 82 * sy, 81 * sx, 78 * sy)
        cubicTo(79 * sx, 73 * sy, 79 * sx, 62 * sy, 81 * sx, 58 * sy)
        cubicTo(83 * sx, 55 * sy, 87 * sx, 55 * sy, 89 * sx, 58 * sy); close()
    }, color(MuscleGroup.BICEPS), alpha(MuscleGroup.BICEPS))

    // Abs — 3 rows × 2 cols of rounded boxes
    val absColor = color(MuscleGroup.ABS)
    val absAlpha = alpha(MuscleGroup.ABS)
    val absCols = listOf(38f to 47f, 53f to 62f)
    val absRows = listOf(72f to 83f, 84f to 93f, 94f to 104f)
    for ((x0, x1) in absCols) for ((y0, y1) in absRows) {
        drawMuscle(Path().apply {
            val r = 3 * sx
            moveTo((x0 + r / sx) * sx, y0 * sy)
            lineTo((x1 - r / sx) * sx, y0 * sy)
            cubicTo(x1 * sx, y0 * sy, x1 * sx, y0 * sy, x1 * sx, (y0 + r / sy) * sy)
            lineTo(x1 * sx, (y1 - r / sy) * sy)
            cubicTo(x1 * sx, y1 * sy, x1 * sx, y1 * sy, (x1 - r / sx) * sx, y1 * sy)
            lineTo((x0 + r / sx) * sx, y1 * sy)
            cubicTo(x0 * sx, y1 * sy, x0 * sx, y1 * sy, x0 * sx, (y1 - r / sy) * sy)
            lineTo(x0 * sx, (y0 + r / sy) * sy)
            cubicTo(x0 * sx, y0 * sy, x0 * sx, y0 * sy, (x0 + r / sx) * sx, y0 * sy)
            close()
        }, absColor, absAlpha)
    }

    // Left oblique
    drawMuscle(Path().apply {
        moveTo(27 * sx, 75 * sy)
        cubicTo(23 * sx, 80 * sy, 22 * sx, 93 * sy, 25 * sx, 100 * sy)
        cubicTo(27 * sx, 104 * sy, 33 * sx, 104 * sy, 36 * sx, 100 * sy)
        cubicTo(38 * sx, 95 * sy, 38 * sx, 82 * sy, 36 * sx, 75 * sy)
        cubicTo(34 * sx, 72 * sy, 29 * sx, 72 * sy, 27 * sx, 75 * sy); close()
    }, color(MuscleGroup.ABS), alpha(MuscleGroup.ABS))

    // Right oblique
    drawMuscle(Path().apply {
        moveTo(73 * sx, 75 * sy)
        cubicTo(77 * sx, 80 * sy, 78 * sx, 93 * sy, 75 * sx, 100 * sy)
        cubicTo(73 * sx, 104 * sy, 67 * sx, 104 * sy, 64 * sx, 100 * sy)
        cubicTo(62 * sx, 95 * sy, 62 * sx, 82 * sy, 64 * sx, 75 * sy)
        cubicTo(66 * sx, 72 * sy, 71 * sx, 72 * sy, 73 * sx, 75 * sy); close()
    }, color(MuscleGroup.ABS), alpha(MuscleGroup.ABS))

    // Left quad
    drawMuscle(Path().apply {
        moveTo(23 * sx, 160 * sy)
        cubicTo(20 * sx, 168 * sy, 20 * sx, 185 * sy, 22 * sx, 197 * sy)
        cubicTo(24 * sx, 200 * sy, 32 * sx, 200 * sy, 37 * sx, 197 * sy)
        cubicTo(39 * sx, 185 * sy, 39 * sx, 165 * sy, 37 * sx, 158 * sy)
        cubicTo(34 * sx, 155 * sy, 26 * sx, 156 * sy, 23 * sx, 160 * sy); close()
    }, color(MuscleGroup.QUADS), alpha(MuscleGroup.QUADS))

    // Right quad
    drawMuscle(Path().apply {
        moveTo(77 * sx, 160 * sy)
        cubicTo(80 * sx, 168 * sy, 80 * sx, 185 * sy, 78 * sx, 197 * sy)
        cubicTo(76 * sx, 200 * sy, 68 * sx, 200 * sy, 63 * sx, 197 * sy)
        cubicTo(61 * sx, 185 * sy, 61 * sx, 165 * sy, 63 * sx, 158 * sy)
        cubicTo(66 * sx, 155 * sy, 74 * sx, 156 * sy, 77 * sx, 160 * sy); close()
    }, color(MuscleGroup.QUADS), alpha(MuscleGroup.QUADS))

    // Left calf (front)
    drawMuscle(Path().apply {
        moveTo(25 * sx, 202 * sy)
        cubicTo(23 * sx, 208 * sy, 23 * sx, 220 * sy, 25 * sx, 226 * sy)
        cubicTo(27 * sx, 228 * sy, 34 * sx, 228 * sy, 36 * sx, 226 * sy)
        cubicTo(38 * sx, 220 * sy, 38 * sx, 208 * sy, 36 * sx, 202 * sy)
        cubicTo(34 * sx, 200 * sy, 27 * sx, 200 * sy, 25 * sx, 202 * sy); close()
    }, color(MuscleGroup.CALVES), alpha(MuscleGroup.CALVES))

    // Right calf (front)
    drawMuscle(Path().apply {
        moveTo(75 * sx, 202 * sy)
        cubicTo(77 * sx, 208 * sy, 77 * sx, 220 * sy, 75 * sx, 226 * sy)
        cubicTo(73 * sx, 228 * sy, 66 * sx, 228 * sy, 64 * sx, 226 * sy)
        cubicTo(62 * sx, 220 * sy, 62 * sx, 208 * sy, 64 * sx, 202 * sy)
        cubicTo(66 * sx, 200 * sy, 73 * sx, 200 * sy, 75 * sx, 202 * sy); close()
    }, color(MuscleGroup.CALVES), alpha(MuscleGroup.CALVES))
}

// ─────────────────────────────────────────────────
// BACK muscles
// ─────────────────────────────────────────────────
private fun DrawScope.drawBackMuscles(
    sx: Float, sy: Float,
    isFemale: Boolean,
    color: (MuscleGroup) -> Color,
    alpha: (MuscleGroup) -> Float
) {
    // Left rear deltoid
    drawMuscle(Path().apply {
        moveTo(15 * sx, 38 * sy)
        cubicTo(10 * sx, 36 * sy, 9 * sx, 47 * sy, 11 * sx, 56 * sy)
        cubicTo(13 * sx, 60 * sy, 18 * sx, 60 * sy, 22 * sx, 54 * sy)
        cubicTo(26 * sx, 48 * sy, 24 * sx, 38 * sy, 20 * sx, 35 * sy)
        cubicTo(18 * sx, 34 * sy, 16 * sx, 35 * sy, 15 * sx, 38 * sy); close()
    }, color(MuscleGroup.SHOULDERS), alpha(MuscleGroup.SHOULDERS))
    drawMuscle(Path().apply {
        moveTo(85 * sx, 38 * sy)
        cubicTo(90 * sx, 36 * sy, 91 * sx, 47 * sy, 89 * sx, 56 * sy)
        cubicTo(87 * sx, 60 * sy, 82 * sx, 60 * sy, 78 * sx, 54 * sy)
        cubicTo(74 * sx, 48 * sy, 76 * sx, 38 * sy, 80 * sx, 35 * sy)
        cubicTo(82 * sx, 34 * sy, 84 * sx, 35 * sy, 85 * sx, 38 * sy); close()
    }, color(MuscleGroup.SHOULDERS), alpha(MuscleGroup.SHOULDERS))

    // Trapezius (upper back diamond)
    drawMuscle(Path().apply {
        moveTo(50 * sx, 30 * sy)
        cubicTo(58 * sx, 31 * sy, 70 * sx, 38 * sy, 72 * sx, 48 * sy)
        cubicTo(70 * sx, 57 * sy, 60 * sx, 63 * sy, 50 * sx, 65 * sy)
        cubicTo(40 * sx, 63 * sy, 30 * sx, 57 * sy, 28 * sx, 48 * sy)
        cubicTo(30 * sx, 38 * sy, 42 * sx, 31 * sy, 50 * sx, 30 * sy); close()
    }, color(MuscleGroup.SHOULDERS), alpha(MuscleGroup.SHOULDERS))

    // Left lat
    drawMuscle(Path().apply {
        moveTo(28 * sx, 50 * sy)
        cubicTo(22 * sx, 56 * sy, 18 * sx, 70 * sy, 20 * sx, 82 * sy)
        cubicTo(22 * sx, 90 * sy, 30 * sx, 96 * sy, 40 * sx, 94 * sy)
        cubicTo(46 * sx, 92 * sy, 48 * sx, 87 * sy, 47 * sx, 78 * sy)
        cubicTo(46 * sx, 66 * sy, 38 * sx, 51 * sy, 32 * sx, 48 * sy)
        cubicTo(30 * sx, 47 * sy, 29 * sx, 48 * sy, 28 * sx, 50 * sy); close()
    }, color(MuscleGroup.BACK), alpha(MuscleGroup.BACK))

    // Right lat
    drawMuscle(Path().apply {
        moveTo(72 * sx, 50 * sy)
        cubicTo(78 * sx, 56 * sy, 82 * sx, 70 * sy, 80 * sx, 82 * sy)
        cubicTo(78 * sx, 90 * sy, 70 * sx, 96 * sy, 60 * sx, 94 * sy)
        cubicTo(54 * sx, 92 * sy, 52 * sx, 87 * sy, 53 * sx, 78 * sy)
        cubicTo(54 * sx, 66 * sy, 62 * sx, 51 * sy, 68 * sx, 48 * sy)
        cubicTo(70 * sx, 47 * sy, 71 * sx, 48 * sy, 72 * sx, 50 * sy); close()
    }, color(MuscleGroup.BACK), alpha(MuscleGroup.BACK))

    // Left tricep
    drawMuscle(Path().apply {
        moveTo(11 * sx, 58 * sy)
        cubicTo(9 * sx, 63 * sy, 9 * sx, 75 * sy, 11 * sx, 80 * sy)
        cubicTo(13 * sx, 83 * sy, 17 * sx, 83 * sy, 19 * sx, 79 * sy)
        cubicTo(21 * sx, 74 * sy, 21 * sx, 63 * sy, 19 * sx, 58 * sy)
        cubicTo(17 * sx, 55 * sy, 13 * sx, 55 * sy, 11 * sx, 58 * sy); close()
    }, color(MuscleGroup.TRICEPS), alpha(MuscleGroup.TRICEPS))

    // Right tricep
    drawMuscle(Path().apply {
        moveTo(89 * sx, 58 * sy)
        cubicTo(91 * sx, 63 * sy, 91 * sx, 75 * sy, 89 * sx, 80 * sy)
        cubicTo(87 * sx, 83 * sy, 83 * sx, 83 * sy, 81 * sx, 79 * sy)
        cubicTo(79 * sx, 74 * sy, 79 * sx, 63 * sy, 81 * sx, 58 * sy)
        cubicTo(83 * sx, 55 * sy, 87 * sx, 55 * sy, 89 * sx, 58 * sy); close()
    }, color(MuscleGroup.TRICEPS), alpha(MuscleGroup.TRICEPS))

    // Left glute
    val gluteW = if (isFemale) 1.1f else 1f
    drawMuscle(Path().apply {
        moveTo((28 * gluteW) * sx, 126 * sy)
        cubicTo((22 * gluteW) * sx, 130 * sy, (20 * gluteW) * sx, 142 * sy, (22 * gluteW) * sx, 152 * sy)
        cubicTo((24 * gluteW) * sx, 158 * sy, 34 * sx, 160 * sy, 42 * sx, 157 * sy)
        cubicTo(47 * sx, 154 * sy, 49 * sx, 147 * sy, 48 * sx, 138 * sy)
        cubicTo(47 * sx, 130 * sy, 42 * sx, 124 * sy, 36 * sx, 123 * sy)
        cubicTo(32 * sx, 122 * sy, (30 * gluteW) * sx, 123 * sy, (28 * gluteW) * sx, 126 * sy); close()
    }, color(MuscleGroup.GLUTES), alpha(MuscleGroup.GLUTES))

    // Right glute
    drawMuscle(Path().apply {
        moveTo((72 * gluteW) * sx, 126 * sy)
        cubicTo((78 * gluteW) * sx, 130 * sy, (80 * gluteW) * sx, 142 * sy, (78 * gluteW) * sx, 152 * sy)
        cubicTo((76 * gluteW) * sx, 158 * sy, 66 * sx, 160 * sy, 58 * sx, 157 * sy)
        cubicTo(53 * sx, 154 * sy, 51 * sx, 147 * sy, 52 * sx, 138 * sy)
        cubicTo(53 * sx, 130 * sy, 58 * sx, 124 * sy, 64 * sx, 123 * sy)
        cubicTo(68 * sx, 122 * sy, (70 * gluteW) * sx, 123 * sy, (72 * gluteW) * sx, 126 * sy); close()
    }, color(MuscleGroup.GLUTES), alpha(MuscleGroup.GLUTES))

    // Left hamstring
    drawMuscle(Path().apply {
        moveTo(23 * sx, 162 * sy)
        cubicTo(20 * sx, 170 * sy, 20 * sx, 186 * sy, 22 * sx, 198 * sy)
        cubicTo(24 * sx, 201 * sy, 32 * sx, 201 * sy, 37 * sx, 198 * sy)
        cubicTo(39 * sx, 186 * sy, 39 * sx, 168 * sy, 37 * sx, 160 * sy)
        cubicTo(34 * sx, 157 * sy, 26 * sx, 158 * sy, 23 * sx, 162 * sy); close()
    }, color(MuscleGroup.HAMSTRINGS), alpha(MuscleGroup.HAMSTRINGS))

    // Right hamstring
    drawMuscle(Path().apply {
        moveTo(77 * sx, 162 * sy)
        cubicTo(80 * sx, 170 * sy, 80 * sx, 186 * sy, 78 * sx, 198 * sy)
        cubicTo(76 * sx, 201 * sy, 68 * sx, 201 * sy, 63 * sx, 198 * sy)
        cubicTo(61 * sx, 186 * sy, 61 * sx, 168 * sy, 63 * sx, 160 * sy)
        cubicTo(66 * sx, 157 * sy, 74 * sx, 158 * sy, 77 * sx, 162 * sy); close()
    }, color(MuscleGroup.HAMSTRINGS), alpha(MuscleGroup.HAMSTRINGS))

    // Left calf (back - wider gastrocnemius shape)
    drawMuscle(Path().apply {
        moveTo(24 * sx, 204 * sy)
        cubicTo(22 * sx, 210 * sy, 22 * sx, 220 * sy, 24 * sx, 226 * sy)
        cubicTo(26 * sx, 229 * sy, 31 * sx, 230 * sy, 36 * sx, 226 * sy)
        cubicTo(38 * sx, 220 * sy, 38 * sx, 210 * sy, 36 * sx, 204 * sy)
        cubicTo(34 * sx, 200 * sy, 27 * sx, 200 * sy, 24 * sx, 204 * sy); close()
    }, color(MuscleGroup.CALVES), alpha(MuscleGroup.CALVES))

    // Right calf (back)
    drawMuscle(Path().apply {
        moveTo(76 * sx, 204 * sy)
        cubicTo(78 * sx, 210 * sy, 78 * sx, 220 * sy, 76 * sx, 226 * sy)
        cubicTo(74 * sx, 229 * sy, 69 * sx, 230 * sy, 64 * sx, 226 * sy)
        cubicTo(62 * sx, 220 * sy, 62 * sx, 210 * sy, 64 * sx, 204 * sy)
        cubicTo(66 * sx, 200 * sy, 73 * sx, 200 * sy, 76 * sx, 204 * sy); close()
    }, color(MuscleGroup.CALVES), alpha(MuscleGroup.CALVES))
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
