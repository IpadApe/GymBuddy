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
import androidx.compose.ui.geometry.Rect
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
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    FilterChip(selected = state.view7Days, onClick = { viewModel.toggle7or30() }, label = { Text("7 Days") })
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = !state.view7Days, onClick = { viewModel.toggle7or30() }, label = { Text("30 Days") })
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    LegendItem(MuscleAdequate, "Adequate")
                    LegendItem(MuscleOvertrained, "Overtrained")
                    LegendItem(MuscleUndertrained, "Undertrained")
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E0E1A)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(500.dp).padding(8.dp),
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
            item { SectionHeader(title = "Muscle Details") }
            items(state.muscleStatuses) { status ->
                MuscleStatusCard(status = status, show7Days = state.view7Days)
            }
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
                                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                                )
                                Text(advisory.recommendation, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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
// BODY MAP CANVAS
// Virtual coordinate space per panel: 100 wide × 270 tall
// Modelled after classic front/back anatomy chart style
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
            MuscleStatus.ADEQUATE    -> MuscleAdequate
            MuscleStatus.OVERTRAINED -> MuscleOvertrained
            MuscleStatus.UNDERTRAINED -> MuscleUndertrained
        }
    }

    fun muscleAlpha(m: MuscleGroup): Float {
        val s = statusMap[m] ?: return 0.30f
        val sets = if (show7Days) s.totalSets7Days else s.totalSets30Days
        return (0.40f + (sets.coerceAtMost(20).toFloat() / 20f) * 0.50f)
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(500.dp)) {
        val panelW = size.width / 2f
        val panelH = size.height
        val sx = panelW / 100f
        val sy = panelH / 270f

        // Front panel
        drawBodyPanel(sx, sy, isFront = true,  isFemale = isFemale, ::muscleColor, ::muscleAlpha)
        // Back panel — translate right
        translate(left = panelW) {
            drawBodyPanel(sx, sy, isFront = false, isFemale = isFemale, ::muscleColor, ::muscleAlpha)
        }

        // Divider line
        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(panelW, 0f),
            end = Offset(panelW, panelH),
            strokeWidth = 1f
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Draw a single body panel (front or back)
// Body is drawn as separate shapes: head, torso, arms, legs
// ─────────────────────────────────────────────────────────────
private fun DrawScope.drawBodyPanel(
    sx: Float, sy: Float,
    isFront: Boolean, isFemale: Boolean,
    muscleColor: (MuscleGroup) -> Color,
    muscleAlpha: (MuscleGroup) -> Float
) {
    val skin     = Color(0xFF5C3E2C)   // warm dark skin (reads as human on dark bg)
    val skinEdge = Color(0xFF3A2518)   // darker outline

    val hipW = if (isFemale) 1.13f else 1.00f
    val wstW = if (isFemale) 0.91f else 1.00f

    fun part(block: Path.() -> Unit) {
        val p = Path().apply(block)
        drawPath(p, skin)
        drawPath(p, skinEdge, style = Stroke(width = 1.4f))
    }

    // ── HEAD ──
    drawOval(skin, topLeft = Offset(39*sx, 0f), size = Size(22*sx, 22*sy))
    drawOval(skinEdge, topLeft = Offset(39*sx, 0f), size = Size(22*sx, 22*sy), style = Stroke(1.4f))

    // ── NECK ──
    part {
        moveTo(45*sx, 21*sy); lineTo(55*sx, 21*sy)
        lineTo(56*sx, 30*sy); lineTo(44*sx, 30*sy); close()
    }

    // ── TORSO (shoulders → crotch, arms excluded) ──
    part {
        moveTo(44*sx, 29*sy)
        // Left collarbone → shoulder
        cubicTo(36*sx, 30*sy, 24*sx, 31*sy, 15*sx, 36*sy)
        // Shoulder outer curve
        cubicTo(11*sx, 39*sy, 11*sx, 46*sy, 15*sx, 50*sy)
        // Armpit → chest wall
        cubicTo(18*sx, 52*sy, 22*sx, 53*sy, 25*sx, 54*sy)
        // Left chest wall → waist
        cubicTo(24*sx, 68*sy, 24*sx, 86*sy, (27f*wstW)*sx, 102*sy)
        // Left hip flare
        cubicTo((25f*wstW)*sx, 112*sy, (21f*hipW)*sx, 122*sy, (21f*hipW)*sx, 132*sy)
        // Left hip bottom → crotch
        cubicTo((21f*hipW)*sx, 140*sy, (23f*hipW)*sx, 145*sy, (28f*hipW)*sx, 148*sy)
        lineTo(36*sx, 152*sy)
        // Crotch curve
        cubicTo(41*sx, 155*sy, 59*sx, 155*sy, 64*sx, 152*sy)
        lineTo((72f*hipW)*sx, 148*sy)
        // Right hip
        cubicTo((77f*hipW)*sx, 145*sy, (79f*hipW)*sx, 140*sy, (79f*hipW)*sx, 132*sy)
        cubicTo((79f*hipW)*sx, 122*sy, (75f*hipW)*sx, 112*sy, (73f*wstW)*sx, 102*sy)
        // Right chest wall → armpit
        cubicTo(76*sx, 86*sy, 76*sx, 68*sy, 75*sx, 54*sy)
        cubicTo(78*sx, 53*sy, 82*sx, 52*sy, 85*sx, 50*sy)
        // Right shoulder
        cubicTo(89*sx, 46*sy, 89*sx, 39*sy, 85*sx, 36*sy)
        cubicTo(76*sx, 31*sy, 64*sx, 30*sy, 56*sx, 29*sy)
        close()
    }

    // ── LEFT ARM ──
    part {
        moveTo(13*sx, 37*sy)
        cubicTo(9*sx, 41*sy, 7*sx, 56*sy, 7*sx, 70*sy)
        cubicTo(7*sx, 83*sy, 9*sx, 94*sy, 11*sx, 108*sy)
        cubicTo(12*sx, 117*sy, 12*sx, 124*sy, 13*sx, 130*sy)
        // Hand
        cubicTo(12*sx, 136*sy, 13*sx, 141*sy, 18*sx, 142*sy)
        cubicTo(23*sx, 142*sy, 24*sx, 136*sy, 23*sx, 130*sy)
        // Inner arm up
        cubicTo(22*sx, 124*sy, 22*sx, 117*sy, 22*sx, 108*sy)
        cubicTo(22*sx, 94*sy, 21*sx, 83*sy, 21*sx, 70*sy)
        cubicTo(21*sx, 56*sy, 20*sx, 41*sy, 17*sx, 37*sy)
        close()
    }

    // ── RIGHT ARM (mirror) ──
    part {
        moveTo(87*sx, 37*sy)
        cubicTo(91*sx, 41*sy, 93*sx, 56*sy, 93*sx, 70*sy)
        cubicTo(93*sx, 83*sy, 91*sx, 94*sy, 89*sx, 108*sy)
        cubicTo(88*sx, 117*sy, 88*sx, 124*sy, 87*sx, 130*sy)
        cubicTo(88*sx, 136*sy, 87*sx, 141*sy, 82*sx, 142*sy)
        cubicTo(77*sx, 142*sy, 76*sx, 136*sy, 77*sx, 130*sy)
        cubicTo(78*sx, 124*sy, 78*sx, 117*sy, 78*sx, 108*sy)
        cubicTo(79*sx, 94*sy, 79*sx, 83*sy, 79*sx, 70*sy)
        cubicTo(79*sx, 56*sy, 80*sx, 41*sy, 83*sx, 37*sy)
        close()
    }

    // ── LEFT LEG ──
    part {
        moveTo((21f*hipW)*sx, 150*sy)
        cubicTo((20f*hipW)*sx, 164*sy, 19*sx, 182*sy, 20*sx, 200*sy)
        cubicTo(21*sx, 210*sy, 22*sx, 218*sy, 22*sx, 224*sy)
        cubicTo(21*sx, 236*sy, 21*sx, 244*sy, 20*sx, 252*sy)
        cubicTo(19*sx, 255*sy, 18*sx, 258*sy, 16*sx, 260*sy)
        lineTo(38*sx, 260*sy)
        cubicTo(38*sx, 257*sy, 38*sx, 254*sy, 38*sx, 250*sy)
        cubicTo(38*sx, 242*sy, 38*sx, 234*sy, 37*sx, 224*sy)
        cubicTo(38*sx, 218*sy, 39*sx, 210*sy, 39*sx, 200*sy)
        cubicTo(40*sx, 182*sy, 40*sx, 164*sy, 37*sx, 150*sy)
        close()
    }

    // ── RIGHT LEG (mirror) ──
    part {
        moveTo((79f*hipW)*sx, 150*sy)
        cubicTo((80f*hipW)*sx, 164*sy, 81*sx, 182*sy, 80*sx, 200*sy)
        cubicTo(79*sx, 210*sy, 78*sx, 218*sy, 78*sx, 224*sy)
        cubicTo(79*sx, 236*sy, 79*sx, 244*sy, 80*sx, 252*sy)
        cubicTo(81*sx, 255*sy, 82*sx, 258*sy, 84*sx, 260*sy)
        lineTo(62*sx, 260*sy)
        cubicTo(62*sx, 257*sy, 62*sx, 254*sy, 62*sx, 250*sy)
        cubicTo(62*sx, 242*sy, 62*sx, 234*sy, 63*sx, 224*sy)
        cubicTo(62*sx, 218*sy, 61*sx, 210*sy, 61*sx, 200*sy)
        cubicTo(60*sx, 182*sy, 60*sx, 164*sy, 63*sx, 150*sy)
        close()
    }

    // ── Muscles on top ──
    if (isFront) drawFrontMuscles(sx, sy, isFemale, hipW, muscleColor, muscleAlpha)
    else         drawBackMuscles(sx, sy, isFemale, hipW, muscleColor, muscleAlpha)
}

// muscle fill + dark outline (like reference image style)
private fun DrawScope.m(path: Path, color: Color, alpha: Float) {
    drawPath(path, color.copy(alpha = alpha))
    drawPath(path, Color.Black.copy(alpha = (alpha * 0.6f + 0.25f).coerceAtMost(0.75f)),
        style = Stroke(width = 1.5f))
}

// ─────────────────────────────────────────────────────────────
// FRONT MUSCLES
// ─────────────────────────────────────────────────────────────
private fun DrawScope.drawFrontMuscles(
    sx: Float, sy: Float, isFemale: Boolean, hipW: Float,
    color: (MuscleGroup) -> Color, alpha: (MuscleGroup) -> Float
) {
    // ── DELTOIDS ──
    m(Path().apply {
        moveTo(13*sx, 37*sy)
        cubicTo(9*sx, 40*sy, 8*sx, 48*sy, 10*sx, 55*sy)
        cubicTo(12*sx, 60*sy, 18*sx, 61*sy, 23*sx, 56*sy)
        cubicTo(27*sx, 50*sy, 25*sx, 40*sy, 20*sx, 36*sy)
        cubicTo(17*sx, 34*sy, 14*sx, 35*sy, 13*sx, 37*sy); close()
    }, color(MuscleGroup.SHOULDERS), alpha(MuscleGroup.SHOULDERS))

    m(Path().apply {
        moveTo(87*sx, 37*sy)
        cubicTo(91*sx, 40*sy, 92*sx, 48*sy, 90*sx, 55*sy)
        cubicTo(88*sx, 60*sy, 82*sx, 61*sy, 77*sx, 56*sy)
        cubicTo(73*sx, 50*sy, 75*sx, 40*sy, 80*sx, 36*sy)
        cubicTo(83*sx, 34*sy, 86*sx, 35*sy, 87*sx, 37*sy); close()
    }, color(MuscleGroup.SHOULDERS), alpha(MuscleGroup.SHOULDERS))

    // ── CHEST / PECS ──
    if (!isFemale) {
        m(Path().apply {
            moveTo(44*sx, 37*sy)
            cubicTo(35*sx, 36*sy, 23*sx, 41*sy, 21*sx, 51*sy)
            cubicTo(20*sx, 58*sy, 24*sx, 66*sy, 33*sx, 68*sy)
            cubicTo(39*sx, 69*sy, 45*sx, 65*sy, 46*sx, 58*sy)
            cubicTo(47*sx, 50*sy, 46*sx, 42*sy, 44*sx, 37*sy); close()
        }, color(MuscleGroup.CHEST), alpha(MuscleGroup.CHEST))
        m(Path().apply {
            moveTo(56*sx, 37*sy)
            cubicTo(65*sx, 36*sy, 77*sx, 41*sy, 79*sx, 51*sy)
            cubicTo(80*sx, 58*sy, 76*sx, 66*sy, 67*sx, 68*sy)
            cubicTo(61*sx, 69*sy, 55*sx, 65*sy, 54*sx, 58*sy)
            cubicTo(53*sx, 50*sy, 54*sx, 42*sy, 56*sx, 37*sy); close()
        }, color(MuscleGroup.CHEST), alpha(MuscleGroup.CHEST))
    } else {
        m(Path().apply {
            moveTo(44*sx, 39*sy)
            cubicTo(36*sx, 39*sy, 24*sx, 44*sy, 22*sx, 53*sy)
            cubicTo(21*sx, 60*sy, 26*sx, 66*sy, 34*sx, 67*sy)
            cubicTo(40*sx, 68*sy, 46*sx, 63*sy, 46*sx, 56*sy)
            cubicTo(47*sx, 48*sy, 46*sx, 41*sy, 44*sx, 39*sy); close()
        }, color(MuscleGroup.CHEST), alpha(MuscleGroup.CHEST))
        m(Path().apply {
            moveTo(56*sx, 39*sy)
            cubicTo(64*sx, 39*sy, 76*sx, 44*sy, 78*sx, 53*sy)
            cubicTo(79*sx, 60*sy, 74*sx, 66*sy, 66*sx, 67*sy)
            cubicTo(60*sx, 68*sy, 54*sx, 63*sy, 54*sx, 56*sy)
            cubicTo(53*sx, 48*sy, 54*sx, 41*sy, 56*sx, 39*sy); close()
        }, color(MuscleGroup.CHEST), alpha(MuscleGroup.CHEST))
    }

    // ── BICEPS ──
    m(Path().apply {
        moveTo(10*sx, 53*sy)
        cubicTo(8*sx, 58*sy, 7*sx, 70*sy, 9*sx, 80*sy)
        cubicTo(11*sx, 86*sy, 16*sx, 87*sy, 19*sx, 82*sy)
        cubicTo(22*sx, 75*sy, 21*sx, 62*sy, 19*sx, 55*sy)
        cubicTo(17*sx, 50*sy, 12*sx, 49*sy, 10*sx, 53*sy); close()
    }, color(MuscleGroup.BICEPS), alpha(MuscleGroup.BICEPS))

    m(Path().apply {
        moveTo(90*sx, 53*sy)
        cubicTo(92*sx, 58*sy, 93*sx, 70*sy, 91*sx, 80*sy)
        cubicTo(89*sx, 86*sy, 84*sx, 87*sy, 81*sx, 82*sy)
        cubicTo(78*sx, 75*sy, 79*sx, 62*sy, 81*sx, 55*sy)
        cubicTo(83*sx, 50*sy, 88*sx, 49*sy, 90*sx, 53*sy); close()
    }, color(MuscleGroup.BICEPS), alpha(MuscleGroup.BICEPS))

    // ── ABS (3 rows × 2 cols) ──
    val ac = color(MuscleGroup.ABS); val aa = alpha(MuscleGroup.ABS)
    for ((x0, x1) in listOf(34f to 44f, 56f to 66f)) {
        for ((y0, y1) in listOf(72f to 84f, 86f to 97f, 99f to 110f)) {
            val rx = 2f * sx; val ry = 2f * sy
            m(Path().apply {
                moveTo(x0 * sx + rx, y0 * sy)
                lineTo(x1 * sx - rx, y0 * sy)
                cubicTo(x1*sx, y0*sy, x1*sx, y0*sy, x1*sx, y0*sy + ry)
                lineTo(x1*sx, y1*sy - ry)
                cubicTo(x1*sx, y1*sy, x1*sx, y1*sy, x1*sx - rx, y1*sy)
                lineTo(x0*sx + rx, y1*sy)
                cubicTo(x0*sx, y1*sy, x0*sx, y1*sy, x0*sx, y1*sy - ry)
                lineTo(x0*sx, y0*sy + ry)
                cubicTo(x0*sx, y0*sy, x0*sx, y0*sy, x0*sx + rx, y0*sy)
                close()
            }, ac, aa)
        }
    }

    // ── OBLIQUES ──
    m(Path().apply {
        moveTo(25*sx, 80*sy)
        cubicTo(22*sx, 86*sy, 22*sx, 98*sy, 24*sx, 108*sy)
        cubicTo(26*sx, 113*sy, 32*sx, 113*sy, 35*sx, 108*sy)
        cubicTo(37*sx, 100*sy, 36*sx, 87*sy, 33*sx, 81*sy)
        cubicTo(31*sx, 76*sy, 27*sx, 76*sy, 25*sx, 80*sy); close()
    }, color(MuscleGroup.ABS), alpha(MuscleGroup.ABS))

    m(Path().apply {
        moveTo(75*sx, 80*sy)
        cubicTo(78*sx, 86*sy, 78*sx, 98*sy, 76*sx, 108*sy)
        cubicTo(74*sx, 113*sy, 68*sx, 113*sy, 65*sx, 108*sy)
        cubicTo(63*sx, 100*sy, 64*sx, 87*sy, 67*sx, 81*sy)
        cubicTo(69*sx, 76*sy, 73*sx, 76*sy, 75*sx, 80*sy); close()
    }, color(MuscleGroup.ABS), alpha(MuscleGroup.ABS))

    // ── QUADS (two heads each side) ──
    // Left outer (vastus lateralis)
    m(Path().apply {
        moveTo(21*sx, 160*sy)
        cubicTo(19*sx, 171*sy, 19*sx, 188*sy, 21*sx, 202*sy)
        cubicTo(23*sx, 210*sy, 28*sx, 213*sy, 32*sx, 210*sy)
        cubicTo(34*sx, 199*sy, 34*sx, 174*sy, 32*sx, 161*sy)
        cubicTo(29*sx, 155*sy, 23*sx, 155*sy, 21*sx, 160*sy); close()
    }, color(MuscleGroup.QUADS), alpha(MuscleGroup.QUADS))
    // Left inner (rectus femoris + vastus medialis)
    m(Path().apply {
        moveTo(33*sx, 160*sy)
        cubicTo(34*sx, 170*sy, 35*sx, 186*sy, 36*sx, 200*sy)
        cubicTo(36*sx, 208*sy, 38*sx, 212*sy, 39*sx, 210*sy)
        cubicTo(40*sx, 199*sy, 40*sx, 172*sy, 38*sx, 160*sy)
        cubicTo(37*sx, 154*sy, 34*sx, 154*sy, 33*sx, 160*sy); close()
    }, color(MuscleGroup.QUADS), alpha(MuscleGroup.QUADS))
    // Right outer
    m(Path().apply {
        moveTo(79*sx, 160*sy)
        cubicTo(81*sx, 171*sy, 81*sx, 188*sy, 79*sx, 202*sy)
        cubicTo(77*sx, 210*sy, 72*sx, 213*sy, 68*sx, 210*sy)
        cubicTo(66*sx, 199*sy, 66*sx, 174*sy, 68*sx, 161*sy)
        cubicTo(71*sx, 155*sy, 77*sx, 155*sy, 79*sx, 160*sy); close()
    }, color(MuscleGroup.QUADS), alpha(MuscleGroup.QUADS))
    // Right inner
    m(Path().apply {
        moveTo(67*sx, 160*sy)
        cubicTo(66*sx, 170*sy, 65*sx, 186*sy, 64*sx, 200*sy)
        cubicTo(64*sx, 208*sy, 62*sx, 212*sy, 61*sx, 210*sy)
        cubicTo(60*sx, 199*sy, 60*sx, 172*sy, 62*sx, 160*sy)
        cubicTo(63*sx, 154*sy, 66*sx, 154*sy, 67*sx, 160*sy); close()
    }, color(MuscleGroup.QUADS), alpha(MuscleGroup.QUADS))

    // ── CALVES (front / tibialis anterior) ──
    m(Path().apply {
        moveTo(23*sx, 224*sy)
        cubicTo(22*sx, 230*sy, 22*sx, 242*sy, 24*sx, 250*sy)
        cubicTo(26*sx, 254*sy, 30*sx, 254*sy, 32*sx, 250*sy)
        cubicTo(34*sx, 242*sy, 34*sx, 230*sy, 32*sx, 224*sy)
        cubicTo(30*sx, 220*sy, 25*sx, 220*sy, 23*sx, 224*sy); close()
    }, color(MuscleGroup.CALVES), alpha(MuscleGroup.CALVES))

    m(Path().apply {
        moveTo(77*sx, 224*sy)
        cubicTo(78*sx, 230*sy, 78*sx, 242*sy, 76*sx, 250*sy)
        cubicTo(74*sx, 254*sy, 70*sx, 254*sy, 68*sx, 250*sy)
        cubicTo(66*sx, 242*sy, 66*sx, 230*sy, 68*sx, 224*sy)
        cubicTo(70*sx, 220*sy, 75*sx, 220*sy, 77*sx, 224*sy); close()
    }, color(MuscleGroup.CALVES), alpha(MuscleGroup.CALVES))
}

// ─────────────────────────────────────────────────────────────
// BACK MUSCLES
// ─────────────────────────────────────────────────────────────
private fun DrawScope.drawBackMuscles(
    sx: Float, sy: Float, isFemale: Boolean, hipW: Float,
    color: (MuscleGroup) -> Color, alpha: (MuscleGroup) -> Float
) {
    // ── REAR DELTOIDS ──
    m(Path().apply {
        moveTo(13*sx, 37*sy)
        cubicTo(9*sx, 40*sy, 8*sx, 48*sy, 10*sx, 55*sy)
        cubicTo(12*sx, 60*sy, 18*sx, 61*sy, 23*sx, 56*sy)
        cubicTo(27*sx, 50*sy, 25*sx, 40*sy, 20*sx, 36*sy)
        cubicTo(17*sx, 34*sy, 14*sx, 35*sy, 13*sx, 37*sy); close()
    }, color(MuscleGroup.SHOULDERS), alpha(MuscleGroup.SHOULDERS))

    m(Path().apply {
        moveTo(87*sx, 37*sy)
        cubicTo(91*sx, 40*sy, 92*sx, 48*sy, 90*sx, 55*sy)
        cubicTo(88*sx, 60*sy, 82*sx, 61*sy, 77*sx, 56*sy)
        cubicTo(73*sx, 50*sy, 75*sx, 40*sy, 80*sx, 36*sy)
        cubicTo(83*sx, 34*sy, 86*sx, 35*sy, 87*sx, 37*sy); close()
    }, color(MuscleGroup.SHOULDERS), alpha(MuscleGroup.SHOULDERS))

    // ── TRAPEZIUS (diamond: neck→shoulders→mid-back) ──
    m(Path().apply {
        moveTo(50*sx, 28*sy)
        cubicTo(62*sx, 29*sy, 76*sx, 37*sy, 82*sx, 48*sy)
        cubicTo(78*sx, 60*sy, 64*sx, 67*sy, 50*sx, 70*sy)
        cubicTo(36*sx, 67*sy, 22*sx, 60*sy, 18*sx, 48*sy)
        cubicTo(24*sx, 37*sy, 38*sx, 29*sy, 50*sx, 28*sy); close()
    }, color(MuscleGroup.BACK), alpha(MuscleGroup.BACK))

    // ── LATS (left wing) ──
    m(Path().apply {
        moveTo(24*sx, 50*sy)
        cubicTo(19*sx, 58*sy, 17*sx, 72*sy, 19*sx, 86*sy)
        cubicTo(21*sx, 95*sy, 28*sx, 102*sy, 40*sx, 100*sy)
        cubicTo(46*sx, 98*sy, 49*sx, 92*sy, 48*sx, 83*sy)
        cubicTo(47*sx, 70*sy, 39*sx, 55*sy, 33*sx, 48*sy)
        cubicTo(29*sx, 45*sy, 26*sx, 47*sy, 24*sx, 50*sy); close()
    }, color(MuscleGroup.BACK), alpha(MuscleGroup.BACK))

    // ── LATS (right wing) ──
    m(Path().apply {
        moveTo(76*sx, 50*sy)
        cubicTo(81*sx, 58*sy, 83*sx, 72*sy, 81*sx, 86*sy)
        cubicTo(79*sx, 95*sy, 72*sx, 102*sy, 60*sx, 100*sy)
        cubicTo(54*sx, 98*sy, 51*sx, 92*sy, 52*sx, 83*sy)
        cubicTo(53*sx, 70*sy, 61*sx, 55*sy, 67*sx, 48*sy)
        cubicTo(71*sx, 45*sy, 74*sx, 47*sy, 76*sx, 50*sy); close()
    }, color(MuscleGroup.BACK), alpha(MuscleGroup.BACK))

    // ── TRICEPS ──
    m(Path().apply {
        moveTo(10*sx, 50*sy)
        cubicTo(8*sx, 56*sy, 8*sx, 70*sy, 10*sx, 80*sy)
        cubicTo(12*sx, 86*sy, 17*sx, 87*sy, 20*sx, 82*sy)
        cubicTo(22*sx, 74*sy, 22*sx, 60*sy, 20*sx, 52*sy)
        cubicTo(18*sx, 47*sy, 12*sx, 47*sy, 10*sx, 50*sy); close()
    }, color(MuscleGroup.TRICEPS), alpha(MuscleGroup.TRICEPS))

    m(Path().apply {
        moveTo(90*sx, 50*sy)
        cubicTo(92*sx, 56*sy, 92*sx, 70*sy, 90*sx, 80*sy)
        cubicTo(88*sx, 86*sy, 83*sx, 87*sy, 80*sx, 82*sy)
        cubicTo(78*sx, 74*sy, 78*sx, 60*sy, 80*sx, 52*sy)
        cubicTo(82*sx, 47*sy, 88*sx, 47*sy, 90*sx, 50*sy); close()
    }, color(MuscleGroup.TRICEPS), alpha(MuscleGroup.TRICEPS))

    // ── GLUTES ──
    m(Path().apply {
        moveTo((21f*hipW)*sx, 134*sy)
        cubicTo((19f*hipW)*sx, 143*sy, (19f*hipW)*sx, 155*sy, (21f*hipW)*sx, 164*sy)
        cubicTo((23f*hipW)*sx, 170*sy, 33*sx, 172*sy, 42*sx, 168*sy)
        cubicTo(48*sx, 163*sy, 50*sx, 155*sy, 49*sx, 146*sy)
        cubicTo(48*sx, 138*sy, 42*sx, 132*sy, 35*sx, 131*sy)
        cubicTo(29*sx, 130*sy, (24f*hipW)*sx, 131*sy, (21f*hipW)*sx, 134*sy); close()
    }, color(MuscleGroup.GLUTES), alpha(MuscleGroup.GLUTES))

    m(Path().apply {
        moveTo((79f*hipW)*sx, 134*sy)
        cubicTo((81f*hipW)*sx, 143*sy, (81f*hipW)*sx, 155*sy, (79f*hipW)*sx, 164*sy)
        cubicTo((77f*hipW)*sx, 170*sy, 67*sx, 172*sy, 58*sx, 168*sy)
        cubicTo(52*sx, 163*sy, 50*sx, 155*sy, 51*sx, 146*sy)
        cubicTo(52*sx, 138*sy, 58*sx, 132*sy, 65*sx, 131*sy)
        cubicTo(71*sx, 130*sy, (76f*hipW)*sx, 131*sy, (79f*hipW)*sx, 134*sy); close()
    }, color(MuscleGroup.GLUTES), alpha(MuscleGroup.GLUTES))

    // ── HAMSTRINGS ──
    m(Path().apply {
        moveTo(21*sx, 164*sy)
        cubicTo(19*sx, 174*sy, 19*sx, 192*sy, 21*sx, 206*sy)
        cubicTo(23*sx, 214*sy, 29*sx, 217*sy, 35*sx, 214*sy)
        cubicTo(38*sx, 204*sy, 39*sx, 182*sy, 38*sx, 168*sy)
        cubicTo(36*sx, 158*sy, 24*sx, 157*sy, 21*sx, 164*sy); close()
    }, color(MuscleGroup.HAMSTRINGS), alpha(MuscleGroup.HAMSTRINGS))

    m(Path().apply {
        moveTo(79*sx, 164*sy)
        cubicTo(81*sx, 174*sy, 81*sx, 192*sy, 79*sx, 206*sy)
        cubicTo(77*sx, 214*sy, 71*sx, 217*sy, 65*sx, 214*sy)
        cubicTo(62*sx, 204*sy, 61*sx, 182*sy, 62*sx, 168*sy)
        cubicTo(64*sx, 158*sy, 76*sx, 157*sy, 79*sx, 164*sy); close()
    }, color(MuscleGroup.HAMSTRINGS), alpha(MuscleGroup.HAMSTRINGS))

    // ── CALVES (back — gastrocnemius diamond shape) ──
    m(Path().apply {
        moveTo(30*sx, 216*sy)
        cubicTo(23*sx, 220*sy, 21*sx, 232*sy, 23*sx, 244*sy)
        cubicTo(25*sx, 252*sy, 30*sx, 256*sy, 35*sx, 252*sy)
        cubicTo(38*sx, 244*sy, 38*sx, 230*sy, 36*sx, 220*sy)
        cubicTo(34*sx, 214*sy, 30*sx, 213*sy, 30*sx, 216*sy); close()
    }, color(MuscleGroup.CALVES), alpha(MuscleGroup.CALVES))

    m(Path().apply {
        moveTo(70*sx, 216*sy)
        cubicTo(77*sx, 220*sy, 79*sx, 232*sy, 77*sx, 244*sy)
        cubicTo(75*sx, 252*sy, 70*sx, 256*sy, 65*sx, 252*sy)
        cubicTo(62*sx, 244*sy, 62*sx, 230*sy, 64*sx, 220*sy)
        cubicTo(66*sx, 214*sy, 70*sx, 213*sy, 70*sx, 216*sy); close()
    }, color(MuscleGroup.CALVES), alpha(MuscleGroup.CALVES))
}

@Composable
fun MuscleStatusCard(status: MuscleGroupStatus, show7Days: Boolean) {
    val color = when (status.status) {
        MuscleStatus.ADEQUATE    -> MuscleAdequate
        MuscleStatus.OVERTRAINED -> MuscleOvertrained
        MuscleStatus.UNDERTRAINED -> MuscleUndertrained
    }
    val sets = if (show7Days) status.totalSets7Days else status.totalSets30Days

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(status.muscleGroup.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "$sets sets (${if (show7Days) "7d" else "30d"}) • ${status.status.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                status.daysSinceLastTrained?.let {
                    Text("${it}d ago", style = MaterialTheme.typography.labelMedium,
                        color = if (it > 7) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant)
                } ?: Text("Never", style = MaterialTheme.typography.labelMedium, color = ErrorRed)
                Text("~${status.estimatedRecoveryHours}h recovery",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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
