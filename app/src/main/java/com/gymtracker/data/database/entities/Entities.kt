package com.gymtracker.data.database.entities

import androidx.room.*

// ═══════════════════════════════════════════════════════════════
// EXERCISE ENTITY
// ═══════════════════════════════════════════════════════════════
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val primaryMuscleGroup: String,      // Chest, Back, Shoulders, etc.
    val secondaryMuscleGroups: String,   // Comma-separated
    val equipmentType: String,           // Barbell, Dumbbell, Cable, Machine, Bodyweight
    val movementType: String,            // Push, Pull, Hinge, Squat, Carry, Isolation
    val difficulty: String,              // Beginner, Intermediate, Advanced
    val instructions: String,
    val imageResName: String = "",       // Drawable resource name
    val isCustom: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// ═══════════════════════════════════════════════════════════════
// WORKOUT SESSION ENTITY
// ═══════════════════════════════════════════════════════════════
@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                    // "Push Day", "Pull Day", etc.
    val splitType: String,               // Push, Pull, Legs, Full Body, Custom
    val startTime: Long,
    val endTime: Long? = null,
    val durationSeconds: Int = 0,
    val totalVolumeKg: Double = 0.0,
    val notes: String = "",
    val isTemplate: Boolean = false,
    val routineId: Long? = null
)

// ═══════════════════════════════════════════════════════════════
// WORKOUT EXERCISE (links exercises to workout sessions)
// ═══════════════════════════════════════════════════════════════
@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("exerciseId")]
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
    val restTimeSeconds: Int = 90,
    val supersetGroupId: Int? = null,    // null = no superset, same int = grouped
    val notes: String = ""
)

// ═══════════════════════════════════════════════════════════════
// WORKOUT SET ENTITY
// ═══════════════════════════════════════════════════════════════
@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutExerciseId")]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val setType: String,                 // Working, WarmUp, DropSet, Failure
    val weight: Double = 0.0,
    val reps: Int = 0,
    val rpe: Float? = null,              // 1-10
    val tempo: String? = null,           // e.g. "3-1-2-0"
    val isCompleted: Boolean = false,
    val isPersonalRecord: Boolean = false,
    val notes: String = "",
    val completedAt: Long? = null
)

// ═══════════════════════════════════════════════════════════════
// PERSONAL RECORD ENTITY
// ═══════════════════════════════════════════════════════════════
@Entity(
    tableName = "personal_records",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId")]
)
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val recordType: String,              // MaxWeight, Max1RM, MaxVolume, MaxReps
    val value: Double,
    val achievedAt: Long = System.currentTimeMillis(),
    val sessionId: Long? = null
)

// ═══════════════════════════════════════════════════════════════
// BODY MEASUREMENT ENTITY
// ═══════════════════════════════════════════════════════════════
@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val bodyWeight: Double? = null,      // kg
    val bodyFatPercentage: Double? = null,
    val chest: Double? = null,           // cm
    val waist: Double? = null,
    val leftArm: Double? = null,
    val rightArm: Double? = null,
    val leftThigh: Double? = null,
    val rightThigh: Double? = null,
    val leftCalf: Double? = null,
    val rightCalf: Double? = null,
    val shoulders: Double? = null,
    val hips: Double? = null,
    val neck: Double? = null,
    val notes: String = ""
)

// ═══════════════════════════════════════════════════════════════
// ROUTINE ENTITY
// ═══════════════════════════════════════════════════════════════
@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val daysPerWeek: Int = 3,
    val goal: String = "Hypertrophy",    // Strength, Hypertrophy, Endurance, FatLoss
    val isArchived: Boolean = false,
    val isPrebuilt: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ═══════════════════════════════════════════════════════════════
// ROUTINE DAY ENTITY
// ═══════════════════════════════════════════════════════════════
@Entity(
    tableName = "routine_days",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineId")]
)
data class RoutineDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val dayName: String,                 // "Push Day", "Day 1", etc.
    val dayOrder: Int,
    val splitType: String = "Custom"
)

// ═══════════════════════════════════════════════════════════════
// ROUTINE DAY EXERCISE
// ═══════════════════════════════════════════════════════════════
@Entity(
    tableName = "routine_day_exercises",
    foreignKeys = [
        ForeignKey(
            entity = RoutineDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineDayId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineDayId"), Index("exerciseId")]
)
data class RoutineDayExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineDayId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
    val targetSets: Int = 3,
    val targetReps: String = "8-12",     // Can be range
    val targetWeight: Double? = null,
    val restTimeSeconds: Int = 90,
    val supersetGroupId: Int? = null,
    val intensityTarget: Float? = null   // RPE target
)

// ═══════════════════════════════════════════════════════════════
// MUSCLE VOLUME TRACKING (aggregated daily)
// ═══════════════════════════════════════════════════════════════
@Entity(tableName = "muscle_volume_log")
data class MuscleVolumeLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val muscleGroup: String,
    val date: Long,
    val totalSets: Int = 0,
    val totalVolume: Double = 0.0,       // kg
    val totalReps: Int = 0
)

// ═══════════════════════════════════════════════════════════════
// USER PREFERENCES ENTITY
// ═══════════════════════════════════════════════════════════════
@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,         // Singleton
    val useMetric: Boolean = true,
    val darkMode: Boolean = true,
    val defaultRestTimeSeconds: Int = 90,
    val trainingGoal: String = "Hypertrophy",
    val daysPerWeek: Int = 4,
    val onboardingCompleted: Boolean = false,
    val preferredSplit: String = "PPL",
    val availableEquipment: String = "All", // Comma-separated
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val bodyWeightKg: Double? = null
)

// ═══════════════════════════════════════════════════════════════
// RELATIONSHIP DATA CLASSES
// ═══════════════════════════════════════════════════════════════
data class WorkoutWithExercises(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val exercises: List<WorkoutExerciseEntity>
)

data class WorkoutExerciseWithSets(
    @Embedded val workoutExercise: WorkoutExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutExerciseId"
    )
    val sets: List<WorkoutSetEntity>
)

data class WorkoutExerciseDetail(
    @Embedded val workoutExercise: WorkoutExerciseEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity
)

data class RoutineWithDays(
    @Embedded val routine: RoutineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val days: List<RoutineDayEntity>
)

data class RoutineDayWithExercises(
    @Embedded val day: RoutineDayEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineDayId"
    )
    val exercises: List<RoutineDayExerciseEntity>
)
