package com.gymtracker.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.database.dao.PRWithExerciseName
import com.gymtracker.data.database.entities.*
import com.gymtracker.data.model.OverloadSuggestion
import com.gymtracker.ui.components.*
import com.gymtracker.ui.theme.*
import com.gymtracker.util.FormatUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DailyVolumePoint(val label: String, val volumeKg: Double)

data class SessionExerciseItem(val exerciseName: String, val sets: List<WorkoutSetEntity>)

data class ProgressState(
    val sessions: List<WorkoutSessionEntity> = emptyList(),
    val monthlyVolume: Double = 0.0,
    val weeklyVolume: Double = 0.0,
    val weeklyCount: Int = 0,
    val monthlyCount: Int = 0,
    val avgDuration: Int = 0,
    val recentPRs: List<PRWithExerciseName> = emptyList(),
    val overloadSuggestions: List<OverloadSuggestion> = emptyList(),
    val measurements: List<BodyMeasurementEntity> = emptyList(),
    val calendarDays: Map<Int, Boolean> = emptyMap(), // dayOfMonth -> hasWorkout
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val useMetric: Boolean = true,
    val showMeasurementDialog: Boolean = false,
    val dailyVolume: List<DailyVolumePoint> = emptyList(),
    val selectedSession: WorkoutSessionEntity? = null,
    val selectedSessionExercises: List<SessionExerciseItem> = emptyList(),
    val isLoadingDetail: Boolean = false,
    val savedFromHistoryConfirm: Boolean = false
)

class ProgressViewModel(private val app: GymTrackerApp) : ViewModel() {
    private val repo = app.repository
    private val _state = MutableStateFlow(ProgressState())
    val state: StateFlow<ProgressState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val weekStart = FormatUtils.getWeekStart()
        val monthStart = FormatUtils.getMonthStart()
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            repo.getAllSessions().collect { _state.update { s -> s.copy(sessions = it) } }
        }
        viewModelScope.launch {
            repo.getTotalVolume(weekStart, now).collect { _state.update { s -> s.copy(weeklyVolume = it) } }
        }
        viewModelScope.launch {
            repo.getTotalVolume(monthStart, now).collect { _state.update { s -> s.copy(monthlyVolume = it) } }
        }
        viewModelScope.launch {
            repo.getSessionCount(weekStart, now).collect { _state.update { s -> s.copy(weeklyCount = it) } }
        }
        viewModelScope.launch {
            repo.getSessionCount(monthStart, now).collect { _state.update { s -> s.copy(monthlyCount = it) } }
        }
        viewModelScope.launch {
            repo.getAvgDuration(monthStart, now).collect { _state.update { s -> s.copy(avgDuration = it) } }
        }
        viewModelScope.launch {
            repo.getRecentPRsWithExercises(10).collect { _state.update { s -> s.copy(recentPRs = it) } }
        }
        viewModelScope.launch {
            repo.getAllSessions().collect { sessions ->
                val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
                val grouped = sessions
                    .filter { it.endTime != null && it.totalVolumeKg > 0 }
                    .groupBy { session ->
                        val cal = Calendar.getInstance().apply { timeInMillis = session.startTime }
                        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                        cal.timeInMillis
                    }
                    .entries
                    .sortedBy { it.key }
                    .takeLast(14)
                    .map { (dayMs, daySessions) ->
                        DailyVolumePoint(
                            label = fmt.format(Date(dayMs)),
                            volumeKg = daySessions.sumOf { it.totalVolumeKg }
                        )
                    }
                _state.update { s -> s.copy(dailyVolume = grouped) }
            }
        }
        viewModelScope.launch {
            repo.getAllMeasurements().collect { _state.update { s -> s.copy(measurements = it) } }
        }
        viewModelScope.launch {
            repo.getPreferences().collect { p -> _state.update { s -> s.copy(useMetric = p?.useMetric ?: true) } }
        }
        viewModelScope.launch {
            val suggestions = repo.getProgressiveOverloadSuggestions()
            _state.update { it.copy(overloadSuggestions = suggestions) }
        }
        loadCalendar()
    }

    private fun loadCalendar() {
        viewModelScope.launch {
            val s = _state.value
            val start = FormatUtils.getMonthStartMillis(s.selectedYear, s.selectedMonth)
            val end = FormatUtils.getMonthEndMillis(s.selectedYear, s.selectedMonth)
            repo.getSessionsBetween(start, end).collect { sessions ->
                val days = mutableMapOf<Int, Boolean>()
                sessions.forEach {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = it.startTime
                    days[cal.get(Calendar.DAY_OF_MONTH)] = true
                }
                _state.update { it.copy(calendarDays = days) }
            }
        }
    }

    fun changeMonth(delta: Int) {
        val cal = Calendar.getInstance()
        cal.set(_state.value.selectedYear, _state.value.selectedMonth, 1)
        cal.add(Calendar.MONTH, delta)
        _state.update { it.copy(selectedMonth = cal.get(Calendar.MONTH), selectedYear = cal.get(Calendar.YEAR)) }
        loadCalendar()
    }

    fun showMeasurementDialog() { _state.update { it.copy(showMeasurementDialog = true) } }
    fun hideMeasurementDialog() { _state.update { it.copy(showMeasurementDialog = false) } }

    fun openSessionDetail(session: WorkoutSessionEntity) {
        _state.update { it.copy(selectedSession = session, isLoadingDetail = true, savedFromHistoryConfirm = false) }
        viewModelScope.launch {
            val wxsList = repo.getExercisesWithSets(session.id).first()
            val items = wxsList.sortedBy { it.workoutExercise.orderIndex }.map { wxs ->
                val exercise = repo.getExerciseById(wxs.workoutExercise.exerciseId)
                SessionExerciseItem(
                    exerciseName = exercise?.name ?: "Unknown Exercise",
                    sets = wxs.sets.filter { it.isCompleted }.sortedBy { it.setNumber }
                )
            }.filter { it.sets.isNotEmpty() }
            _state.update { it.copy(selectedSessionExercises = items, isLoadingDetail = false) }
        }
    }

    fun closeSessionDetail() {
        _state.update { it.copy(selectedSession = null, selectedSessionExercises = emptyList(), savedFromHistoryConfirm = false) }
    }

    fun saveSessionAsRoutine(name: String) {
        val session = _state.value.selectedSession ?: return
        viewModelScope.launch {
            repo.saveWorkoutAsRoutine(session.id, name)
            _state.update { it.copy(savedFromHistoryConfirm = true) }
        }
    }

    fun saveMeasurement(bodyWeight: Double?, bodyFat: Double?, chest: Double?, waist: Double?,
                        leftArm: Double?, rightArm: Double?) {
        viewModelScope.launch {
            repo.insertMeasurement(
                BodyMeasurementEntity(
                    date = System.currentTimeMillis(),
                    bodyWeight = bodyWeight,
                    bodyFatPercentage = bodyFat,
                    chest = chest,
                    waist = waist,
                    leftArm = leftArm,
                    rightArm = rightArm
                )
            )
            hideMeasurementDialog()
        }
    }
}

class ProgressViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProgressViewModel(GymTrackerApp.instance) as T
    }
}

@Composable
fun ProgressScreen(
    onExerciseClick: (Long) -> Unit,
    viewModel: ProgressViewModel = viewModel(factory = ProgressViewModelFactory())
) {
    val state by viewModel.state.collectAsState()
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    var selectedTab by remember { mutableStateOf("overview") }
    val tabs = listOf("overview", "PRs", "measurements")
    val completedSessions = state.sessions.filter { it.endTime != null }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── HEADER + TABS ─────────────────────────────────────────
        item {
            Text(
                "Progress",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)
            )
            Spacer(Modifier.height(14.dp))
            // Segmented tab bar — matching design spec pill tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                tabs.forEach { tab ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                if (tab == selectedTab) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { selectedTab = tab }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            tab.replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tab == selectedTab) Color.White
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ══════════════════════════════════════════════════════════
        // OVERVIEW TAB
        // ══════════════════════════════════════════════════════════
        if (selectedTab == "overview") {
            // Stats cards row 1
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Week Volume", FormatUtils.formatVolume(state.weeklyVolume, state.useMetric),
                        Icons.Filled.FitnessCenter, BlueTrust, Modifier.weight(1f))
                    StatCard("Month Volume", FormatUtils.formatVolume(state.monthlyVolume, state.useMetric),
                        Icons.AutoMirrored.Filled.TrendingUp, TealSuccess, Modifier.weight(1f))
                }
            }
            // Stats cards row 2
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Sessions/Week", "${state.weeklyCount}", Icons.Filled.CalendarMonth, OrangePrimary, Modifier.weight(1f))
                    StatCard("Avg Duration",
                        if (state.avgDuration > 0) FormatUtils.formatDuration(state.avgDuration) else "--",
                        Icons.Filled.Timer, Periwinkle, Modifier.weight(1f))
                }
            }

            // Calendar
            item {
                Text(
                    "WORKOUT CALENDAR",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.changeMonth(-1) }) {
                                Icon(Icons.Filled.ChevronLeft, "Previous")
                            }
                            Text(
                                "${months[state.selectedMonth]} ${state.selectedYear}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.changeMonth(1) }) {
                                Icon(Icons.Filled.ChevronRight, "Next")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("M","T","W","T","F","S","S").forEach {
                                Text(
                                    it, modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val daysInMonth = FormatUtils.getDaysInMonth(state.selectedYear, state.selectedMonth)
                        val cal = Calendar.getInstance().apply { set(state.selectedYear, state.selectedMonth, 1) }
                        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                        val rows = ((firstDayOfWeek + daysInMonth) + 6) / 7
                        for (row in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (col in 0 until 7) {
                                    val day = row * 7 + col - firstDayOfWeek + 1
                                    if (day in 1..daysInMonth) {
                                        val hasWorkout = state.calendarDays.containsKey(day)
                                        val isToday = day == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                                            state.selectedMonth == Calendar.getInstance().get(Calendar.MONTH) &&
                                            state.selectedYear == Calendar.getInstance().get(Calendar.YEAR)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f).aspectRatio(1f).padding(2.dp).clip(CircleShape)
                                                .background(when {
                                                    hasWorkout -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                    isToday   -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                    else      -> Color.Transparent
                                                }),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "$day",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (hasWorkout) Color.White else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 11.sp
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Progressive Overload
            if (state.overloadSuggestions.isNotEmpty()) {
                item {
                    Text(
                        "PROGRESSIVE OVERLOAD",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 2.sp, fontWeight = FontWeight.Bold
                    )
                }
                items(state.overloadSuggestions.take(5)) { suggestion ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = TealSuccess.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = TealSuccess, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(suggestion.exerciseName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(suggestion.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Volume over time chart
            if (state.dailyVolume.size >= 2) {
                item {
                    Text(
                        "VOLUME OVER TIME",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 2.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            VolumeLineChart(data = state.dailyVolume, useMetric = state.useMetric, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            // Workout History
            item {
                Text(
                    "WORKOUT HISTORY",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp, fontWeight = FontWeight.Bold
                )
            }
            if (completedSessions.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.FitnessCenter,
                        title = "No workouts yet",
                        subtitle = "Complete a workout to see your history here"
                    )
                }
            }
            items(completedSessions.take(20)) { session ->
                WorkoutSessionCard(
                    name = session.name,
                    date = FormatUtils.formatDate(session.startTime),
                    duration = FormatUtils.formatDuration(session.durationSeconds),
                    volume = FormatUtils.formatVolume(session.totalVolumeKg, state.useMetric),
                    splitType = session.splitType,
                    onClick = { viewModel.openSessionDetail(session) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // ══════════════════════════════════════════════════════════
        // PRs TAB
        // ══════════════════════════════════════════════════════════
        if (selectedTab == "PRs") {
            item {
                Text(
                    "PERSONAL RECORDS",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp, fontWeight = FontWeight.Bold
                )
            }
            if (state.recentPRs.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.EmojiEvents,
                        title = "No PRs yet",
                        subtitle = "Complete workouts to set personal records"
                    )
                }
            }
            items(state.recentPRs) { pr ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp).clip(RoundedCornerShape(10.dp))
                                .background(WarningOrange.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pr.exerciseName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(FormatUtils.formatDate(pr.achievedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            FormatUtils.formatWeight(pr.value, state.useMetric),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarningOrange
                        )
                    }
                }
            }
        }

        // ══════════════════════════════════════════════════════════
        // MEASUREMENTS TAB
        // ══════════════════════════════════════════════════════════
        if (selectedTab == "measurements") {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "BODY MEASUREMENTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 2.sp, fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.showMeasurementDialog() }) {
                        Text("+ Add", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (state.measurements.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Straighten,
                        title = "No measurements yet",
                        subtitle = "Track your body measurements to see progress over time",
                        actionLabel = "Add Measurement",
                        onAction = { viewModel.showMeasurementDialog() }
                    )
                }
            } else {
                items(state.measurements) { m ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(FormatUtils.formatDate(m.date), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                m.bodyWeight?.let { MiniStat(Icons.Filled.MonitorWeight, FormatUtils.formatWeight(it, state.useMetric), "Weight") }
                                m.bodyFatPercentage?.let { MiniStat(Icons.Filled.Percent, "${it}%", "Body Fat") }
                                m.chest?.let { MiniStat(Icons.Filled.Straighten, FormatUtils.formatDistance(it, state.useMetric), "Chest") }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // Measurement Dialog
    if (state.showMeasurementDialog) {
        MeasurementDialog(
            useMetric = state.useMetric,
            onSave = { bw, bf, c, w, la, ra -> viewModel.saveMeasurement(bw, bf, c, w, la, ra) },
            onDismiss = { viewModel.hideMeasurementDialog() }
        )
    }

    // Workout History Detail Sheet
    val selectedSession = state.selectedSession
    if (selectedSession != null) {
        WorkoutHistoryDetailSheet(
            session = selectedSession,
            exercises = state.selectedSessionExercises,
            isLoading = state.isLoadingDetail,
            savedConfirm = state.savedFromHistoryConfirm,
            useMetric = state.useMetric,
            onSaveAsRoutine = { name -> viewModel.saveSessionAsRoutine(name) },
            onDismiss = { viewModel.closeSessionDetail() }
        )
    }
}

@Composable
fun VolumeLineChart(
    data: List<DailyVolumePoint>,
    useMetric: Boolean,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) return
    val maxVal = data.maxOf { it.volumeKg }.coerceAtLeast(1.0)
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val n = data.size
            val chartH = size.height - 4.dp.toPx()
            val points = data.mapIndexed { i, pt ->
                Offset(
                    x = i * (size.width / (n - 1).toFloat()),
                    y = chartH - (pt.volumeKg / maxVal * chartH).toFloat()
                )
            }

            // Filled area under line
            val fillPath = Path().apply {
                moveTo(points.first().x, chartH)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, chartH)
                close()
            }
            drawPath(fillPath, color = primaryColor.copy(alpha = 0.12f))

            // Line
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(
                linePath,
                color = primaryColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Dots
            points.forEach { p ->
                drawCircle(primaryColor, radius = 4.dp.toPx(), center = p)
                drawCircle(surfaceColor, radius = 2.dp.toPx(), center = p)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                data.first().label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = labelColor,
                modifier = Modifier.weight(1f)
            )
            if (data.size > 2) {
                Text(
                    data[data.size / 2].label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = labelColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                data.last().label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = labelColor,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Max: ${FormatUtils.formatVolume(maxVal, useMetric)}",
            style = MaterialTheme.typography.labelSmall,
            color = labelColor
        )
    }
}

@Composable
fun MeasurementDialog(
    useMetric: Boolean,
    onSave: (Double?, Double?, Double?, Double?, Double?, Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var bodyWeight by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var leftArm by remember { mutableStateOf("") }
    var rightArm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Measurement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberInputField(bodyWeight, { bodyWeight = it }, "Body Weight",
                        suffix = if(useMetric) "kg" else "lbs", modifier = Modifier.weight(1f))
                    NumberInputField(bodyFat, { bodyFat = it }, "Body Fat", suffix = "%", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberInputField(chest, { chest = it }, "Chest",
                        suffix = if(useMetric) "cm" else "in", modifier = Modifier.weight(1f))
                    NumberInputField(waist, { waist = it }, "Waist",
                        suffix = if(useMetric) "cm" else "in", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberInputField(leftArm, { leftArm = it }, "L Arm",
                        suffix = if(useMetric) "cm" else "in", modifier = Modifier.weight(1f))
                    NumberInputField(rightArm, { rightArm = it }, "R Arm",
                        suffix = if(useMetric) "cm" else "in", modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    bodyWeight.toDoubleOrNull(), bodyFat.toDoubleOrNull(),
                    chest.toDoubleOrNull(), waist.toDoubleOrNull(),
                    leftArm.toDoubleOrNull(), rightArm.toDoubleOrNull()
                )
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ═══════════════════════════════════════════════════════════════
// WORKOUT HISTORY DETAIL SHEET
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryDetailSheet(
    session: WorkoutSessionEntity,
    exercises: List<SessionExerciseItem>,
    isLoading: Boolean,
    savedConfirm: Boolean,
    useMetric: Boolean,
    onSaveAsRoutine: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }

    if (showSaveDialog) {
        var routineName by remember { mutableStateOf(session.name) }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save as Routine", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = routineName,
                    onValueChange = { routineName = it },
                    label = { Text("Routine name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveAsRoutine(routineName)
                        showSaveDialog = false
                    },
                    enabled = routineName.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        session.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        FormatUtils.formatDateTime(session.startTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "Close")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (session.durationSeconds > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Timer, null, modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Text(FormatUtils.formatDuration(session.durationSeconds),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (session.totalVolumeKg > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.FitnessCenter, null, modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary)
                        Text(FormatUtils.formatVolume(session.totalVolumeKg, useMetric),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else if (exercises.isEmpty()) {
                Text(
                    "No exercises recorded for this session.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // Exercise list — scrollable
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    exercises.forEach { item ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    item.exerciseName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(6.dp))

                                // Column headers
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("SET", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(32.dp))
                                    Text("WEIGHT", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f))
                                    Text("REPS", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
                                }
                                Spacer(Modifier.height(4.dp))

                                item.sets.forEachIndexed { idx, set ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${idx + 1}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.width(32.dp)
                                        )
                                        val weightText = if (set.weight > 0) {
                                            val w = if (set.weight == set.weight.toLong().toDouble())
                                                set.weight.toLong().toString()
                                            else "%.1f".format(set.weight)
                                            "$w kg"
                                        } else "—"
                                        Text(
                                            weightText,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        val repsText = if (set.reps > 0) "${set.reps} reps" else "—"
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.width(48.dp)
                                        ) {
                                            if (set.isPersonalRecord) {
                                                Text(
                                                    "PR",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .background(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                            RoundedCornerShape(3.dp)
                                                        )
                                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                            }
                                            Text(repsText, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Save as Routine button / confirmation
            if (savedConfirm) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20).copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                        Text("Saved to Routines!", style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.BookmarkAdd, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save as Routine")
                }
            }
        }
    }
}
