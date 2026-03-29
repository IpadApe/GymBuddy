package com.gymtracker.data.repository

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.gymtracker.data.database.AppDatabase
import com.gymtracker.data.database.ExerciseSeedData
import com.gymtracker.data.database.RoutineSeedData
import com.gymtracker.data.database.entities.*
import com.gymtracker.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

// ─── Template JSON format ────────────────────────────────────────────────────
private data class TemplateJson(
    @SerializedName("version") val version: Int = 1,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String = "",
    @SerializedName("goal") val goal: String = "Hypertrophy",
    @SerializedName("daysPerWeek") val daysPerWeek: Int,
    @SerializedName("days") val days: List<TemplateDayJson>
)

private data class TemplateDayJson(
    @SerializedName("name") val name: String,
    @SerializedName("splitType") val splitType: String = "CUSTOM",
    @SerializedName("exercises") val exercises: List<TemplateExerciseJson>
)

private data class TemplateExerciseJson(
    @SerializedName("exercise") val exercise: String,
    @SerializedName("sets") val sets: Int = 3,
    @SerializedName("reps") val reps: String = "8-12",
    @SerializedName("restSeconds") val restSeconds: Int = 90
)

class GymRepository(private val db: AppDatabase) {

    // ═══════════════════ EXERCISES ═══════════════════
    fun getAllExercises(): Flow<List<ExerciseEntity>> = db.exerciseDao().getAllExercises()
    fun searchExercises(query: String): Flow<List<ExerciseEntity>> = db.exerciseDao().searchExercises(query)
    fun getExercisesByMuscle(muscle: String): Flow<List<ExerciseEntity>> = db.exerciseDao().getExercisesByMuscleGroup(muscle)
    fun getExercisesByEquipment(equipment: String): Flow<List<ExerciseEntity>> = db.exerciseDao().getExercisesByEquipment(equipment)
    fun getExercisesByMovement(movement: String): Flow<List<ExerciseEntity>> = db.exerciseDao().getExercisesByMovement(movement)
    suspend fun getExerciseById(id: Long): ExerciseEntity? = db.exerciseDao().getExerciseById(id)
    suspend fun insertExercise(exercise: ExerciseEntity): Long = db.exerciseDao().insertExercise(exercise)
    suspend fun updateExercise(exercise: ExerciseEntity) = db.exerciseDao().updateExercise(exercise)
    suspend fun deleteExercise(exercise: ExerciseEntity) = db.exerciseDao().deleteExercise(exercise)

    // ═══════════════════ WORKOUT SESSIONS ═══════════════════
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>> = db.workoutSessionDao().getAllSessions()
    fun getRecentSessions(limit: Int = 10): Flow<List<WorkoutSessionEntity>> = db.workoutSessionDao().getRecentSessions(limit)
    fun getSessionsBetween(start: Long, end: Long): Flow<List<WorkoutSessionEntity>> = db.workoutSessionDao().getSessionsBetween(start, end)
    fun getTemplates(): Flow<List<WorkoutSessionEntity>> = db.workoutSessionDao().getTemplates()
    suspend fun getSessionById(id: Long): WorkoutSessionEntity? = db.workoutSessionDao().getSessionById(id)
    suspend fun getSessionWithExercises(id: Long): WorkoutWithExercises? = db.workoutSessionDao().getSessionWithExercises(id)
    fun getSessionCount(start: Long, end: Long): Flow<Int> = db.workoutSessionDao().getSessionCountBetween(start, end)
    fun getTotalVolume(start: Long, end: Long): Flow<Double> = db.workoutSessionDao().getTotalVolumeBetween(start, end)
    fun getAvgDuration(start: Long, end: Long): Flow<Int> = db.workoutSessionDao().getAverageDurationBetween(start, end)

    suspend fun startWorkoutSession(name: String, splitType: String, routineId: Long? = null): Long {
        return db.workoutSessionDao().insertSession(
            WorkoutSessionEntity(
                name = name,
                splitType = splitType,
                startTime = System.currentTimeMillis(),
                routineId = routineId
            )
        )
    }

    suspend fun finishWorkoutSession(sessionId: Long) {
        val session = db.workoutSessionDao().getSessionById(sessionId) ?: return
        val exercises = db.workoutExerciseDao().getExercisesWithSets(sessionId).first()
        var totalVolume = 0.0
        exercises.forEach { wxs ->
            wxs.sets.filter { it.isCompleted }.forEach { set ->
                totalVolume += set.weight * set.reps
            }
        }
        val endTime = System.currentTimeMillis()
        db.workoutSessionDao().updateSession(
            session.copy(
                endTime = endTime,
                durationSeconds = ((endTime - session.startTime) / 1000).toInt(),
                totalVolumeKg = totalVolume
            )
        )
        // Update muscle volume logs
        updateMuscleVolumeLogs(sessionId)
    }

    suspend fun deleteSession(session: WorkoutSessionEntity) = db.workoutSessionDao().deleteSession(session)

    suspend fun duplicateSessionAsTemplate(sessionId: Long): Long {
        val original = db.workoutSessionDao().getSessionById(sessionId) ?: return -1
        val templateId = db.workoutSessionDao().insertSession(
            original.copy(id = 0, isTemplate = true, name = "${original.name} (Template)")
        )
        val exercises = db.workoutExerciseDao().getExercisesWithSets(sessionId).first()
        exercises.forEach { wxs ->
            val newExId = db.workoutExerciseDao().insertWorkoutExercise(
                wxs.workoutExercise.copy(id = 0, sessionId = templateId)
            )
            wxs.sets.forEach { set ->
                db.workoutSetDao().insertSet(
                    set.copy(id = 0, workoutExerciseId = newExId, isCompleted = false, completedAt = null)
                )
            }
        }
        return templateId
    }

    // ═══════════════════ WORKOUT EXERCISES ═══════════════════
    fun getExercisesWithSets(sessionId: Long): Flow<List<WorkoutExerciseWithSets>> =
        db.workoutExerciseDao().getExercisesWithSets(sessionId)

    fun getExerciseDetails(sessionId: Long): Flow<List<WorkoutExerciseDetail>> =
        db.workoutExerciseDao().getExerciseDetails(sessionId)

    suspend fun addExerciseToWorkout(sessionId: Long, exerciseId: Long, order: Int, restTime: Int = 90): Long {
        return db.workoutExerciseDao().insertWorkoutExercise(
            WorkoutExerciseEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                orderIndex = order,
                restTimeSeconds = restTime
            )
        )
    }

    suspend fun removeExerciseFromWorkout(workoutExercise: WorkoutExerciseEntity) {
        db.workoutExerciseDao().deleteWorkoutExercise(workoutExercise)
    }

    suspend fun updateWorkoutExercise(exercise: WorkoutExerciseEntity) =
        db.workoutExerciseDao().updateWorkoutExercise(exercise)

    suspend fun getPreviousSetsForExercise(exerciseId: Long, currentSessionId: Long): List<WorkoutSetEntity> {
        val prev = db.workoutExerciseDao().getLastWorkoutExerciseForExercise(exerciseId, currentSessionId)
            ?: return emptyList()
        return db.workoutSetDao().getSetsForExercise(prev.id).first()
    }

    // ═══════════════════ SETS ═══════════════════
    fun getSetsForExercise(workoutExerciseId: Long): Flow<List<WorkoutSetEntity>> =
        db.workoutSetDao().getSetsForExercise(workoutExerciseId)

    fun getSetHistory(exerciseId: Long): Flow<List<WorkoutSetEntity>> =
        db.workoutSetDao().getSetHistoryForExercise(exerciseId)

    suspend fun addSet(workoutExerciseId: Long, setNumber: Int, setType: String = "Working"): Long {
        return db.workoutSetDao().insertSet(
            WorkoutSetEntity(
                workoutExerciseId = workoutExerciseId,
                setNumber = setNumber,
                setType = setType
            )
        )
    }

    suspend fun updateSet(set: WorkoutSetEntity) = db.workoutSetDao().updateSet(set)

    suspend fun completeSet(set: WorkoutSetEntity): WorkoutSetEntity {
        val completed = set.copy(
            isCompleted = true,
            completedAt = System.currentTimeMillis()
        )
        // Check for PR
        val exercise = db.workoutExerciseDao().getById(set.workoutExerciseId)
        if (exercise != null) {
            val maxWeight = db.workoutSetDao().getMaxWeightForExercise(exercise.exerciseId)
            val isPR = maxWeight == null || set.weight > maxWeight.weight ||
                    (set.weight == maxWeight.weight && set.reps > maxWeight.reps)
            val finalSet = if (isPR) {
                db.personalRecordDao().insertRecord(
                    PersonalRecordEntity(
                        exerciseId = exercise.exerciseId,
                        recordType = RecordType.MAX_WEIGHT.name,
                        value = set.weight,
                        sessionId = exercise.sessionId
                    )
                )
                completed.copy(isPersonalRecord = true)
            } else completed

            db.workoutSetDao().updateSet(finalSet)
            return finalSet
        }
        db.workoutSetDao().updateSet(completed)
        return completed
    }

    suspend fun deleteSet(set: WorkoutSetEntity) = db.workoutSetDao().deleteSet(set)

    // ═══════════════════ PERSONAL RECORDS ═══════════════════
    fun getRecentPRs(limit: Int = 20): Flow<List<PersonalRecordEntity>> = db.personalRecordDao().getRecentRecords(limit)
    fun getRecentPRsWithExercises(limit: Int = 20) = db.personalRecordDao().getRecentRecordsWithExercises(limit)
    fun getPRsForExercise(exerciseId: Long): Flow<List<PersonalRecordEntity>> = db.personalRecordDao().getRecordsForExercise(exerciseId)

    // ═══════════════════ BODY MEASUREMENTS ═══════════════════
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>> = db.bodyMeasurementDao().getAllMeasurements()
    suspend fun getLatestMeasurement(): BodyMeasurementEntity? = db.bodyMeasurementDao().getLatestMeasurement()
    fun getMeasurementsSince(date: Long): Flow<List<BodyMeasurementEntity>> = db.bodyMeasurementDao().getMeasurementsSince(date)
    suspend fun insertMeasurement(m: BodyMeasurementEntity): Long = db.bodyMeasurementDao().insertMeasurement(m)
    suspend fun deleteMeasurement(m: BodyMeasurementEntity) = db.bodyMeasurementDao().deleteMeasurement(m)

    // ═══════════════════ ROUTINES ═══════════════════
    fun getAllRoutines(): Flow<List<RoutineEntity>> = db.routineDao().getAllActiveRoutines()
    fun getPrebuiltRoutines(): Flow<List<RoutineEntity>> = db.routineDao().getPrebuiltRoutines()
    suspend fun getRoutineById(id: Long): RoutineEntity? = db.routineDao().getRoutineById(id)
    suspend fun getRoutineWithDays(id: Long): RoutineWithDays? = db.routineDao().getRoutineWithDays(id)
    suspend fun insertRoutine(routine: RoutineEntity): Long = db.routineDao().insertRoutine(routine)
    suspend fun updateRoutine(routine: RoutineEntity) = db.routineDao().updateRoutine(routine)
    suspend fun deleteRoutine(routine: RoutineEntity) = db.routineDao().deleteRoutine(routine)

    fun getDaysForRoutine(routineId: Long): Flow<List<RoutineDayEntity>> = db.routineDayDao().getDaysForRoutine(routineId)
    suspend fun getDayWithExercises(dayId: Long): RoutineDayWithExercises? = db.routineDayDao().getDayWithExercises(dayId)
    suspend fun insertRoutineDay(day: RoutineDayEntity): Long = db.routineDayDao().insertDay(day)
    fun getExercisesForRoutineDay(dayId: Long): Flow<List<RoutineDayExerciseEntity>> = db.routineDayExerciseDao().getExercisesForDay(dayId)

    suspend fun cloneRoutine(routineId: Long): Long {
        val original = db.routineDao().getRoutineWithDays(routineId) ?: return -1
        val newRoutineId = db.routineDao().insertRoutine(
            original.routine.copy(id = 0, name = "${original.routine.name} (Copy)", isPrebuilt = false)
        )
        original.days.forEach { day ->
            val newDayId = db.routineDayDao().insertDay(day.copy(id = 0, routineId = newRoutineId))
            val exercises = db.routineDayExerciseDao().getExercisesForDay(day.id).first()
            exercises.forEach { ex ->
                db.routineDayExerciseDao().insertExercise(ex.copy(id = 0, routineDayId = newDayId))
            }
        }
        return newRoutineId
    }

    // ═══════════════════ MUSCLE VOLUME ═══════════════════
    suspend fun getMuscleVolumeAggregated(startDate: Long, endDate: Long) =
        db.muscleVolumeDao().getAggregatedVolume(startDate, endDate)

    suspend fun getLastTrainedDates() = db.muscleVolumeDao().getLastTrainedDates()

    private suspend fun updateMuscleVolumeLogs(sessionId: Long) {
        val today = todayStartMillis()
        val exercises = db.workoutExerciseDao().getExercisesWithSets(sessionId).first()
        val volumeByMuscle = mutableMapOf<String, Triple<Int, Double, Int>>() // sets, volume, reps

        exercises.forEach { wxs ->
            val exercise = db.exerciseDao().getExerciseById(wxs.workoutExercise.exerciseId) ?: return@forEach
            val completedSets = wxs.sets.filter { it.isCompleted }
            val muscles = mutableListOf(exercise.primaryMuscleGroup)
            exercise.secondaryMuscleGroups.split(",").filter { it.isNotBlank() }.forEach { muscles.add(it.trim()) }

            muscles.forEach { muscle ->
                val current = volumeByMuscle.getOrDefault(muscle, Triple(0, 0.0, 0))
                val sets = completedSets.size
                val volume = completedSets.sumOf { it.weight * it.reps }
                val reps = completedSets.sumOf { it.reps }
                volumeByMuscle[muscle] = Triple(current.first + sets, current.second + volume, current.third + reps)
            }
        }

        volumeByMuscle.forEach { (muscle, data) ->
            db.muscleVolumeDao().insertVolumeLog(
                MuscleVolumeLogEntity(
                    muscleGroup = muscle,
                    date = today,
                    totalSets = data.first,
                    totalVolume = data.second,
                    totalReps = data.third
                )
            )
        }
    }

    // ═══════════════════ WORKOUT STATS ═══════════════════
    suspend fun getCompletedWorkoutCount(): Int = db.workoutSessionDao().getCompletedWorkoutCount()

    suspend fun getWorkoutStreak(): Int {
        val dates = db.workoutSessionDao().getAllCompletedSessionDates()
        if (dates.isEmpty()) return 0
        val dayMs = 24 * 60 * 60 * 1000L
        val todayDay = System.currentTimeMillis() / dayMs
        val uniqueDays = dates.map { it / dayMs }.distinct().sortedDescending()
        if (uniqueDays.first() < todayDay - 1) return 0
        var streak = 0
        var expected = if (uniqueDays.first() == todayDay) todayDay else todayDay - 1
        for (day in uniqueDays) {
            if (day == expected) { streak++; expected-- }
            else if (day < expected) break
        }
        return streak
    }

    // ═══════════════════ USER PREFERENCES ═══════════════════
    fun getPreferences(): Flow<UserPreferencesEntity?> = db.userPreferencesDao().getPreferences()
    suspend fun getPreferencesSync(): UserPreferencesEntity? = db.userPreferencesDao().getPreferencesSync()
    suspend fun updatePreferences(prefs: UserPreferencesEntity) = db.userPreferencesDao().insertOrUpdate(prefs)

    // ═══════════════════ MUSCLE STATUS & BODY MAP ═══════════════════
    suspend fun getMuscleGroupStatuses(): List<MuscleGroupStatus> {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000
        val thirtyDaysAgo = now - 30L * 24 * 60 * 60 * 1000

        val volume7d = db.muscleVolumeDao().getAggregatedVolume(sevenDaysAgo, now)
        val volume30d = db.muscleVolumeDao().getAggregatedVolume(thirtyDaysAgo, now)
        val lastTrained = db.muscleVolumeDao().getLastTrainedDates()

        val volume7dMap = volume7d.associateBy { it.muscleGroup }
        val volume30dMap = volume30d.associateBy { it.muscleGroup }
        val lastTrainedMap = lastTrained.associateBy { it.muscleGroup }

        return MuscleGroup.entries.map { muscle ->
            val name = muscle.displayName
            val v7 = volume7dMap[name]
            val v30 = volume30dMap[name]
            val last = lastTrainedMap[name]

            val sets7 = v7?.totalSets ?: 0
            val sets30 = v30?.totalSets ?: 0
            val daysSince = last?.let { ((now - it.lastTrainedDate) / (24 * 60 * 60 * 1000)).toInt() }

            // Optimal weekly sets: 10-20 for most muscle groups
            val status = when {
                sets7 > 25 -> MuscleStatus.OVERTRAINED
                sets7 in 10..25 -> MuscleStatus.ADEQUATE
                else -> MuscleStatus.UNDERTRAINED
            }

            // Recovery estimate: ~48-72 hours for most, depends on volume
            val recoveryHours = when {
                sets7 > 20 -> 72
                sets7 > 10 -> 48
                else -> 24
            }

            MuscleGroupStatus(
                muscleGroup = muscle,
                status = status,
                totalSets7Days = sets7,
                totalSets30Days = sets30,
                totalVolume7Days = v7?.totalVolume ?: 0.0,
                daysSinceLastTrained = daysSince,
                estimatedRecoveryHours = recoveryHours
            )
        }
    }

    suspend fun getImbalanceAdvisories(): List<ImbalanceAdvisory> {
        val statuses = getMuscleGroupStatuses().associateBy { it.muscleGroup }
        val advisories = mutableListOf<ImbalanceAdvisory>()

        MuscleGroup.antagonistPairs.forEach { (muscle, antagonist) ->
            val muscleStatus = statuses[muscle] ?: return@forEach
            val antStatus = statuses[antagonist] ?: return@forEach

            if (muscleStatus.totalSets7Days > 0 && antStatus.totalSets7Days > 0) {
                val ratio = muscleStatus.totalSets7Days.toDouble() / antStatus.totalSets7Days.toDouble()
                if (ratio > 1.5) {
                    advisories.add(
                        ImbalanceAdvisory(
                            overtrainedMuscle = muscle,
                            undertrainedMuscle = antagonist,
                            ratio = ratio,
                            recommendation = "Consider adding more ${antagonist.displayName} work. Your ${muscle.displayName} to ${antagonist.displayName} ratio is ${String.format("%.1f", ratio)}:1."
                        )
                    )
                }
            }
        }
        return advisories
    }

    // ═══════════════════ RECOMMENDATIONS ENGINE ═══════════════════
    suspend fun getProgressiveOverloadSuggestions(): List<OverloadSuggestion> {
        val suggestions = mutableListOf<OverloadSuggestion>()
        val exercises = db.exerciseDao().getAllExercises().first()

        exercises.forEach { exercise ->
            val history = db.workoutSetDao().getSetHistoryForExercise(exercise.id).first()
            if (history.size < 6) return@forEach

            val recentSets = history.filter { it.isCompleted }.take(15)
            if (recentSets.isEmpty()) return@forEach

            // Group by workout session through workoutExerciseId
            val sessionGroups = recentSets.groupBy { it.workoutExerciseId }
            if (sessionGroups.size < 2) return@forEach

            val latestGroup = sessionGroups.entries.first().value
            val avgWeight = latestGroup.map { it.weight }.average()
            val avgReps = latestGroup.map { it.reps }.average().toInt()

            // Check if user has been at same weight/reps for 2+ sessions
            var sameCount = 0
            sessionGroups.entries.take(3).forEach { (_, sets) ->
                val w = sets.map { it.weight }.average()
                val r = sets.map { it.reps }.average().toInt()
                if (kotlin.math.abs(w - avgWeight) < 2.5 && kotlin.math.abs(r - avgReps) <= 1) {
                    sameCount++
                }
            }

            if (sameCount >= 2) {
                suggestions.add(
                    OverloadSuggestion(
                        exerciseId = exercise.id,
                        exerciseName = exercise.name,
                        currentWeight = avgWeight,
                        currentReps = avgReps,
                        suggestedWeight = avgWeight + 2.5,
                        suggestedReps = avgReps,
                        sessionsAtSameLevel = sameCount,
                        message = "You've hit ${avgWeight}kg × $avgReps for $sameCount sessions. Try ${avgWeight + 2.5}kg or add 1-2 reps."
                    )
                )
            }
        }
        return suggestions
    }

    suspend fun shouldRecommendDeload(): Boolean {
        val fourWeeksAgo = System.currentTimeMillis() - 28L * 24 * 60 * 60 * 1000
        val sessions = db.workoutSessionDao().getSessionsBetween(fourWeeksAgo, System.currentTimeMillis()).first()
        // If user has trained 4+ times per week for 4+ weeks, recommend deload
        val weeklyCounts = sessions.groupBy { weekOfYear(it.startTime) }
        return weeklyCounts.size >= 4 && weeklyCounts.all { it.value.size >= 4 }
    }

    suspend fun getNextMuscleGroupRecommendation(): List<MuscleGroup> {
        val statuses = getMuscleGroupStatuses()
        return statuses
            .filter { it.status == MuscleStatus.UNDERTRAINED }
            .sortedBy { it.daysSinceLastTrained ?: Int.MAX_VALUE }
            .map { it.muscleGroup }
            .reversed()
            .take(3)
    }

    // ═══════════════════ CALCULATORS ═══════════════════
    fun calculateOneRepMax(weight: Double, reps: Int): Double {
        if (reps <= 0) return weight
        if (reps == 1) return weight
        // Epley formula
        return weight * (1 + reps / 30.0)
    }

    fun calculatePlates(targetWeight: Double, barWeight: Double = 20.0, isMetric: Boolean = true): PlateResult {
        val plates = if (isMetric) listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
        else listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5)

        val perSide = (targetWeight - barWeight) / 2.0
        if (perSide <= 0) return PlateResult(targetWeight, barWeight, emptyList(), barWeight, targetWeight == barWeight)

        var remaining = perSide
        val result = mutableListOf<Pair<Double, Int>>()

        plates.forEach { plate ->
            val count = (remaining / plate).toInt()
            if (count > 0) {
                result.add(plate to count)
                remaining -= plate * count
            }
        }

        val achievable = barWeight + (perSide - remaining) * 2
        return PlateResult(targetWeight, barWeight, result, achievable, remaining < 0.01)
    }

    // ═══════════════════ DATA EXPORT/IMPORT ═══════════════════
    suspend fun exportAllData(): Map<String, Any> {
        return mapOf(
            "exercises" to db.exerciseDao().getAllExercises().first().filter { it.isCustom },
            "sessions" to db.workoutSessionDao().getAllSessions().first(),
            "measurements" to db.bodyMeasurementDao().getAllMeasurements().first(),
            "routines" to db.routineDao().getAllActiveRoutines().first().filter { !it.isPrebuilt },
            "preferences" to (db.userPreferencesDao().getPreferencesSync() ?: UserPreferencesEntity())
        )
    }

    // ═══════════════════ SEED DATA ═══════════════════
    suspend fun seedExercisesIfEmpty() {
        val count = db.exerciseDao().getExerciseCount()
        if (count == 0) {
            db.exerciseDao().insertAll(ExerciseSeedData.getExercises())
        }
    }

    suspend fun seedRoutinesIfEmpty() {
        val existing = db.routineDao().getPrebuiltRoutines().first()
        if (existing.isEmpty()) {
            val exercises = db.exerciseDao().getAllExercises().first()
            val exerciseMap = exercises.associateBy { it.name }

            RoutineSeedData.templates.forEach { template ->
                val routineId = db.routineDao().insertRoutine(
                    RoutineEntity(
                        name = template.name,
                        description = template.description,
                        daysPerWeek = template.daysPerWeek,
                        goal = template.goal,
                        isPrebuilt = true
                    )
                )
                template.days.forEachIndexed { index, dayTemplate ->
                    val dayId = db.routineDayDao().insertDay(
                        RoutineDayEntity(
                            routineId = routineId,
                            dayName = dayTemplate.name,
                            dayOrder = index,
                            splitType = dayTemplate.splitType
                        )
                    )
                    dayTemplate.exercises.forEachIndexed { exIndex, exTemplate ->
                        val exerciseEntity = exerciseMap[exTemplate.exerciseName]
                        if (exerciseEntity != null) {
                            db.routineDayExerciseDao().insertExercise(
                                RoutineDayExerciseEntity(
                                    routineDayId = dayId,
                                    exerciseId = exerciseEntity.id,
                                    orderIndex = exIndex,
                                    targetSets = exTemplate.targetSets,
                                    targetReps = exTemplate.targetReps,
                                    restTimeSeconds = exTemplate.restSeconds
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════ TEMPLATE IMPORT/EXPORT ═══════════════════
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun importRoutineFromJson(jsonString: String): Long {
        val template = gson.fromJson(jsonString, TemplateJson::class.java)
            ?: throw IllegalArgumentException("Invalid template JSON")
        val exercises = db.exerciseDao().getAllExercises().first()
        val exerciseMap = exercises.associateBy { it.name.lowercase() }

        val routineId = db.routineDao().insertRoutine(
            RoutineEntity(
                name = template.name,
                description = template.description,
                goal = template.goal,
                daysPerWeek = template.daysPerWeek,
                isPrebuilt = false
            )
        )
        template.days.forEachIndexed { index, day ->
            val dayId = db.routineDayDao().insertDay(
                RoutineDayEntity(
                    routineId = routineId,
                    dayName = day.name,
                    dayOrder = index,
                    splitType = day.splitType
                )
            )
            day.exercises.forEachIndexed { exIndex, ex ->
                val exerciseEntity = exerciseMap[ex.exercise.lowercase()]
                if (exerciseEntity != null) {
                    db.routineDayExerciseDao().insertExercise(
                        RoutineDayExerciseEntity(
                            routineDayId = dayId,
                            exerciseId = exerciseEntity.id,
                            orderIndex = exIndex,
                            targetSets = ex.sets,
                            targetReps = ex.reps,
                            restTimeSeconds = ex.restSeconds
                        )
                    )
                }
            }
        }
        return routineId
    }

    suspend fun exportRoutineToJson(routineId: Long): String {
        val routine = db.routineDao().getRoutineById(routineId) ?: return ""
        val days = db.routineDayDao().getDaysForRoutine(routineId).first()
        val dayJsonList = days.map { day ->
            val dayExercises = db.routineDayExerciseDao().getExercisesForDay(day.id).first()
            val exerciseJsonList = dayExercises.map { de ->
                val exercise = db.exerciseDao().getExerciseById(de.exerciseId)
                TemplateExerciseJson(
                    exercise = exercise?.name ?: "Unknown",
                    sets = de.targetSets,
                    reps = de.targetReps,
                    restSeconds = de.restTimeSeconds
                )
            }
            TemplateDayJson(name = day.dayName, splitType = day.splitType, exercises = exerciseJsonList)
        }
        val template = TemplateJson(
            version = 1,
            name = routine.name,
            description = routine.description,
            goal = routine.goal,
            daysPerWeek = routine.daysPerWeek,
            days = dayJsonList
        )
        return gson.toJson(template)
    }

    // ═══════════════════ HELPERS ═══════════════════
    private fun todayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun weekOfYear(timestamp: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal.get(Calendar.WEEK_OF_YEAR) + cal.get(Calendar.YEAR) * 100
    }
}
