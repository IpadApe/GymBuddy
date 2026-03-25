package com.gymtracker.data.model

// ═══════════════════════════════════════════════════════════════
// ENUMS
// ═══════════════════════════════════════════════════════════════
enum class MuscleGroup(val displayName: String) {
    CHEST("Chest"),
    BACK("Back"),
    SHOULDERS("Shoulders"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    FOREARMS("Forearms"),
    ABS("Abs/Core"),
    QUADS("Quads"),
    HAMSTRINGS("Hamstrings"),
    GLUTES("Glutes"),
    CALVES("Calves");

    companion object {
        fun fromString(s: String): MuscleGroup? = entries.find {
            it.displayName.equals(s, ignoreCase = true) || it.name.equals(s, ignoreCase = true)
        }

        val antagonistPairs = mapOf(
            CHEST to BACK,
            BACK to CHEST,
            BICEPS to TRICEPS,
            TRICEPS to BICEPS,
            QUADS to HAMSTRINGS,
            HAMSTRINGS to QUADS,
            ABS to BACK
        )
    }
}

enum class EquipmentType(val displayName: String) {
    BARBELL("Barbell"),
    DUMBBELL("Dumbbell"),
    CABLE("Cable"),
    MACHINE("Machine"),
    BODYWEIGHT("Bodyweight"),
    KETTLEBELL("Kettlebell"),
    BANDS("Resistance Bands"),
    EZ_BAR("EZ Bar"),
    SMITH_MACHINE("Smith Machine"),
    OTHER("Other")
}

enum class MovementType(val displayName: String) {
    PUSH("Push"),
    PULL("Pull"),
    HINGE("Hinge"),
    SQUAT("Squat"),
    CARRY("Carry"),
    ISOLATION("Isolation")
}

enum class Difficulty(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

enum class SetType(val displayName: String) {
    WORKING("Working"),
    WARM_UP("Warm-up"),
    DROP_SET("Drop Set"),
    FAILURE("Failure")
}

enum class SplitType(val displayName: String) {
    PUSH("Push Day"),
    PULL("Pull Day"),
    LEGS("Leg Day"),
    UPPER("Upper Body"),
    LOWER("Lower Body"),
    FULL_BODY("Full Body"),
    CUSTOM("Custom")
}

enum class TrainingGoal(val displayName: String) {
    STRENGTH("Strength"),
    HYPERTROPHY("Hypertrophy"),
    ENDURANCE("Endurance"),
    FAT_LOSS("Fat Loss")
}

enum class RecordType(val displayName: String) {
    MAX_WEIGHT("Max Weight"),
    MAX_1RM("Estimated 1RM"),
    MAX_VOLUME("Max Volume"),
    MAX_REPS("Max Reps")
}

// ═══════════════════════════════════════════════════════════════
// MUSCLE STATUS FOR BODY MAP
// ═══════════════════════════════════════════════════════════════
enum class MuscleStatus {
    UNDERTRAINED,   // Grey
    ADEQUATE,       // Green
    OVERTRAINED     // Red
}

data class MuscleGroupStatus(
    val muscleGroup: MuscleGroup,
    val status: MuscleStatus,
    val totalSets7Days: Int,
    val totalSets30Days: Int,
    val totalVolume7Days: Double,
    val daysSinceLastTrained: Int?,
    val estimatedRecoveryHours: Int
)

// ═══════════════════════════════════════════════════════════════
// IMBALANCE ADVISORY
// ═══════════════════════════════════════════════════════════════
data class ImbalanceAdvisory(
    val overtrainedMuscle: MuscleGroup,
    val undertrainedMuscle: MuscleGroup,
    val ratio: Double,
    val recommendation: String
)

// ═══════════════════════════════════════════════════════════════
// PROGRESSIVE OVERLOAD SUGGESTION
// ═══════════════════════════════════════════════════════════════
data class OverloadSuggestion(
    val exerciseId: Long,
    val exerciseName: String,
    val currentWeight: Double,
    val currentReps: Int,
    val suggestedWeight: Double?,
    val suggestedReps: Int?,
    val sessionsAtSameLevel: Int,
    val message: String
)

// ═══════════════════════════════════════════════════════════════
// WEEKLY SUMMARY
// ═══════════════════════════════════════════════════════════════
data class WeeklySummary(
    val weekStartDate: Long,
    val totalSessions: Int,
    val totalVolume: Double,
    val totalDuration: Int,
    val avgSessionDuration: Int,
    val muscleGroupBreakdown: Map<String, Int>,
    val personalRecords: Int,
    val isDeloadRecommended: Boolean = false
)

// ═══════════════════════════════════════════════════════════════
// PLATE CALCULATOR
// ═══════════════════════════════════════════════════════════════
data class PlateResult(
    val targetWeight: Double,
    val barWeight: Double,
    val platesPerSide: List<Pair<Double, Int>>,   // plate weight to count
    val achievableWeight: Double,
    val isExact: Boolean
)
