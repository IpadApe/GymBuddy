package com.gymtracker.data.database.dao

import androidx.room.*
import com.gymtracker.data.database.entities.*
import kotlinx.coroutines.flow.Flow

// ═══════════════════════════════════════════════════════════════
// EXERCISE DAO
// ═══════════════════════════════════════════════════════════════
@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE primaryMuscleGroup = :muscleGroup ORDER BY name")
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE equipmentType = :equipment ORDER BY name")
    fun getExercisesByEquipment(equipment: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE movementType = :movement ORDER BY name")
    fun getExercisesByMovement(movement: String): Flow<List<ExerciseEntity>>

    @Query("""
        SELECT * FROM exercises 
        WHERE name LIKE '%' || :query || '%' 
        OR primaryMuscleGroup LIKE '%' || :query || '%'
        OR equipmentType LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE isCustom = 1 ORDER BY name")
    fun getCustomExercises(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    @Query("SELECT * FROM exercises WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getExerciseByName(name: String): ExerciseEntity?
}

// ═══════════════════════════════════════════════════════════════
// WORKOUT SESSION DAO
// ═══════════════════════════════════════════════════════════════
@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions WHERE isTemplate = 0 ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE isTemplate = 0 ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<WorkoutSessionEntity>>

    @Query("""
        SELECT * FROM workout_sessions 
        WHERE isTemplate = 0 
        AND startTime >= :startDate AND startTime <= :endDate 
        ORDER BY startTime DESC
    """)
    fun getSessionsBetween(startDate: Long, endDate: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE isTemplate = 1 ORDER BY name")
    fun getTemplates(): Flow<List<WorkoutSessionEntity>>

    @Query("""
        SELECT * FROM workout_sessions 
        WHERE isTemplate = 0 AND splitType = :splitType 
        ORDER BY startTime DESC LIMIT 1
    """)
    suspend fun getLastSessionBySplit(splitType: String): WorkoutSessionEntity?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionWithExercises(id: Long): WorkoutWithExercises?

    @Query("""
        SELECT COUNT(*) FROM workout_sessions 
        WHERE isTemplate = 0 
        AND startTime >= :startDate AND startTime <= :endDate
    """)
    fun getSessionCountBetween(startDate: Long, endDate: Long): Flow<Int>

    @Query("""
        SELECT COALESCE(SUM(totalVolumeKg), 0.0) FROM workout_sessions 
        WHERE isTemplate = 0 
        AND startTime >= :startDate AND startTime <= :endDate
    """)
    fun getTotalVolumeBetween(startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(AVG(durationSeconds), 0) FROM workout_sessions 
        WHERE isTemplate = 0 
        AND endTime IS NOT NULL
        AND startTime >= :startDate AND startTime <= :endDate
    """)
    fun getAverageDurationBetween(startDate: Long, endDate: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE isTemplate = 0 AND endTime IS NOT NULL")
    suspend fun getCompletedWorkoutCount(): Int

    @Query("SELECT startTime FROM workout_sessions WHERE isTemplate = 0 AND endTime IS NOT NULL ORDER BY startTime DESC")
    suspend fun getAllCompletedSessionDates(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Delete
    suspend fun deleteSession(session: WorkoutSessionEntity)
}

// ═══════════════════════════════════════════════════════════════
// WORKOUT EXERCISE DAO
// ═══════════════════════════════════════════════════════════════
@Dao
interface WorkoutExerciseDao {
    @Query("SELECT * FROM workout_exercises WHERE sessionId = :sessionId ORDER BY orderIndex")
    fun getExercisesForSession(sessionId: Long): Flow<List<WorkoutExerciseEntity>>

    @Query("SELECT * FROM workout_exercises WHERE id = :id")
    suspend fun getById(id: Long): WorkoutExerciseEntity?

    @Transaction
    @Query("SELECT * FROM workout_exercises WHERE sessionId = :sessionId ORDER BY orderIndex")
    fun getExercisesWithSets(sessionId: Long): Flow<List<WorkoutExerciseWithSets>>

    @Transaction
    @Query("SELECT * FROM workout_exercises WHERE sessionId = :sessionId ORDER BY orderIndex")
    fun getExerciseDetails(sessionId: Long): Flow<List<WorkoutExerciseDetail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(exercise: WorkoutExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<WorkoutExerciseEntity>): List<Long>

    @Update
    suspend fun updateWorkoutExercise(exercise: WorkoutExerciseEntity)

    @Delete
    suspend fun deleteWorkoutExercise(exercise: WorkoutExerciseEntity)

    @Query("""
        SELECT we.* FROM workout_exercises we
        INNER JOIN workout_sessions wse ON we.sessionId = wse.id
        WHERE we.exerciseId = :exerciseId
        AND we.sessionId != :currentSessionId
        AND wse.isTemplate = 0
        AND wse.endTime IS NOT NULL
        ORDER BY wse.startTime DESC
        LIMIT 1
    """)
    suspend fun getLastWorkoutExerciseForExercise(exerciseId: Long, currentSessionId: Long): WorkoutExerciseEntity?

    @Query("DELETE FROM workout_exercises WHERE sessionId = :sessionId")
    suspend fun deleteAllForSession(sessionId: Long)
}

// ═══════════════════════════════════════════════════════════════
// WORKOUT SET DAO
// ═══════════════════════════════════════════════════════════════
@Dao
interface WorkoutSetDao {
    @Query("SELECT * FROM workout_sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY setNumber")
    fun getSetsForExercise(workoutExerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE id = :id")
    suspend fun getById(id: Long): WorkoutSetEntity?

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        WHERE we.exerciseId = :exerciseId
        ORDER BY ws.completedAt DESC
    """)
    fun getSetHistoryForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        WHERE we.exerciseId = :exerciseId AND ws.isCompleted = 1
        ORDER BY ws.weight DESC LIMIT 1
    """)
    suspend fun getMaxWeightForExercise(exerciseId: Long): WorkoutSetEntity?

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        WHERE we.exerciseId = :exerciseId AND ws.isCompleted = 1
        ORDER BY (ws.weight * (1 + ws.reps / 30.0)) DESC LIMIT 1
    """)
    suspend fun getMax1RMSetForExercise(exerciseId: Long): WorkoutSetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sets: List<WorkoutSetEntity>): List<Long>

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Delete
    suspend fun deleteSet(set: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun deleteAllForWorkoutExercise(workoutExerciseId: Long)
}

// ═══════════════════════════════════════════════════════════════
// PERSONAL RECORD DAO
// ═══════════════════════════════════════════════════════════════
data class PRWithExerciseName(
    val id: Long,
    val exerciseId: Long,
    val recordType: String,
    val value: Double,
    val achievedAt: Long,
    val sessionId: Long?,
    val exerciseName: String
)

@Dao
interface PersonalRecordDao {
    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId ORDER BY achievedAt DESC")
    fun getRecordsForExercise(exerciseId: Long): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records ORDER BY achievedAt DESC LIMIT :limit")
    fun getRecentRecords(limit: Int = 20): Flow<List<PersonalRecordEntity>>

    @Query("""
        SELECT pr.id, pr.exerciseId, pr.recordType, pr.value, pr.achievedAt, pr.sessionId,
               e.name as exerciseName
        FROM personal_records pr
        INNER JOIN exercises e ON pr.exerciseId = e.id
        WHERE pr.achievedAt = (
            SELECT MAX(pr2.achievedAt) FROM personal_records pr2
            WHERE pr2.exerciseId = pr.exerciseId
        )
        GROUP BY pr.exerciseId
        ORDER BY pr.achievedAt DESC
        LIMIT :limit
    """)
    fun getRecentRecordsWithExercises(limit: Int = 20): Flow<List<PRWithExerciseName>>

    @Query("""
        SELECT * FROM personal_records 
        WHERE exerciseId = :exerciseId AND recordType = :recordType 
        ORDER BY value DESC LIMIT 1
    """)
    suspend fun getBestRecord(exerciseId: Long, recordType: String): PersonalRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PersonalRecordEntity): Long

    @Delete
    suspend fun deleteRecord(record: PersonalRecordEntity)
}

// ═══════════════════════════════════════════════════════════════
// BODY MEASUREMENT DAO
// ═══════════════════════════════════════════════════════════════
@Dao
interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY date DESC")
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements ORDER BY date DESC LIMIT 1")
    suspend fun getLatestMeasurement(): BodyMeasurementEntity?

    @Query("SELECT * FROM body_measurements WHERE date >= :startDate ORDER BY date ASC")
    fun getMeasurementsSince(startDate: Long): Flow<List<BodyMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: BodyMeasurementEntity): Long

    @Update
    suspend fun updateMeasurement(measurement: BodyMeasurementEntity)

    @Delete
    suspend fun deleteMeasurement(measurement: BodyMeasurementEntity)
}

// ═══════════════════════════════════════════════════════════════
// ROUTINE DAO
// ═══════════════════════════════════════════════════════════════
@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE isArchived = 0 ORDER BY name")
    fun getAllActiveRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE isPrebuilt = 1 ORDER BY name")
    fun getPrebuiltRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): RoutineEntity?

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineWithDays(id: Long): RoutineWithDays?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)
}

// ═══════════════════════════════════════════════════════════════
// ROUTINE DAY DAO
// ═══════════════════════════════════════════════════════════════
@Dao
interface RoutineDayDao {
    @Query("SELECT * FROM routine_days WHERE routineId = :routineId ORDER BY dayOrder")
    fun getDaysForRoutine(routineId: Long): Flow<List<RoutineDayEntity>>

    @Transaction
    @Query("SELECT * FROM routine_days WHERE id = :id")
    suspend fun getDayWithExercises(id: Long): RoutineDayWithExercises?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: RoutineDayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(days: List<RoutineDayEntity>): List<Long>

    @Update
    suspend fun updateDay(day: RoutineDayEntity)

    @Delete
    suspend fun deleteDay(day: RoutineDayEntity)
}

// ═══════════════════════════════════════════════════════════════
// ROUTINE DAY EXERCISE DAO
// ═══════════════════════════════════════════════════════════════
@Dao
interface RoutineDayExerciseDao {
    @Query("SELECT * FROM routine_day_exercises WHERE routineDayId = :dayId ORDER BY orderIndex")
    fun getExercisesForDay(dayId: Long): Flow<List<RoutineDayExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: RoutineDayExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<RoutineDayExerciseEntity>): List<Long>

    @Update
    suspend fun updateExercise(exercise: RoutineDayExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: RoutineDayExerciseEntity)

    @Query("DELETE FROM routine_day_exercises WHERE routineDayId = :dayId")
    suspend fun deleteAllForDay(dayId: Long)
}

// ═══════════════════════════════════════════════════════════════
// MUSCLE VOLUME DAO
// ═══════════════════════════════════════════════════════════════
@Dao
interface MuscleVolumeDao {
    @Query("""
        SELECT * FROM muscle_volume_log 
        WHERE date >= :startDate AND date <= :endDate 
        ORDER BY date DESC
    """)
    fun getVolumeBetween(startDate: Long, endDate: Long): Flow<List<MuscleVolumeLogEntity>>

    @Query("""
        SELECT muscleGroup, SUM(totalSets) as totalSets, SUM(totalVolume) as totalVolume, SUM(totalReps) as totalReps
        FROM muscle_volume_log 
        WHERE date >= :startDate AND date <= :endDate 
        GROUP BY muscleGroup
    """)
    suspend fun getAggregatedVolume(startDate: Long, endDate: Long): List<MuscleVolumeAggregation>

    @Query("""
        SELECT muscleGroup, MAX(date) as lastTrainedDate
        FROM muscle_volume_log
        GROUP BY muscleGroup
    """)
    suspend fun getLastTrainedDates(): List<MuscleLastTrained>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVolumeLog(log: MuscleVolumeLogEntity): Long

    @Query("DELETE FROM muscle_volume_log WHERE date < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: Long)
}

data class MuscleVolumeAggregation(
    val muscleGroup: String,
    val totalSets: Int,
    val totalVolume: Double,
    val totalReps: Int
)

data class MuscleLastTrained(
    val muscleGroup: String,
    val lastTrainedDate: Long
)

// ═══════════════════════════════════════════════════════════════
// USER PREFERENCES DAO
// ═══════════════════════════════════════════════════════════════
@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getPreferences(): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getPreferencesSync(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(prefs: UserPreferencesEntity)
}
