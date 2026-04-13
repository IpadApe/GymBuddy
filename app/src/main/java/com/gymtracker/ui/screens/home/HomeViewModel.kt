package com.gymtracker.ui.screens.home

import androidx.lifecycle.*
import com.gymtracker.BuildConfig
import com.gymtracker.GymTrackerApp
import com.gymtracker.data.database.entities.WorkoutSessionEntity
import com.gymtracker.data.model.MuscleGroup
import com.gymtracker.util.FormatUtils
import com.gymtracker.util.UpdateChecker
import com.gymtracker.util.UpdateInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val recentSessions: List<WorkoutSessionEntity> = emptyList(),
    val weeklySessionCount: Int = 0,
    val weeklyVolume: Double = 0.0,
    val avgDuration: Int = 0,
    val useMetric: Boolean = true,
    val recommendedMuscles: List<MuscleGroup> = emptyList(),
    val deloadRecommended: Boolean = false,
    val availableUpdate: UpdateInfo? = null,
    val updateDismissed: Boolean = false
)

class HomeViewModel(private val app: GymTrackerApp) : ViewModel() {
    private val repo = app.repository
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        checkForUpdate()
    }

    fun dismissUpdate() {
        _uiState.update { it.copy(updateDismissed = true) }
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            val info = UpdateChecker.checkForUpdate(BuildConfig.VERSION_CODE)
            if (info != null) _uiState.update { it.copy(availableUpdate = info) }
        }
    }

    private fun loadData() {
        val weekStart = FormatUtils.getWeekStart()
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            repo.getRecentSessions(10).collect { sessions ->
                _uiState.update { it.copy(recentSessions = sessions) }
            }
        }
        viewModelScope.launch {
            repo.getSessionCount(weekStart, now).collect { count ->
                _uiState.update { it.copy(weeklySessionCount = count) }
            }
        }
        viewModelScope.launch {
            repo.getTotalVolume(weekStart, now).collect { vol ->
                _uiState.update { it.copy(weeklyVolume = vol) }
            }
        }
        viewModelScope.launch {
            repo.getAvgDuration(weekStart, now).collect { dur ->
                _uiState.update { it.copy(avgDuration = dur) }
            }
        }
        viewModelScope.launch {
            repo.getPreferences().collect { prefs ->
                _uiState.update { it.copy(useMetric = prefs?.useMetric ?: true) }
            }
        }
        viewModelScope.launch {
            val recommended = repo.getNextMuscleGroupRecommendation()
            val deload = repo.shouldRecommendDeload()
            _uiState.update { it.copy(recommendedMuscles = recommended, deloadRecommended = deload) }
        }
    }
}

class HomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(GymTrackerApp.instance) as T
    }
}
