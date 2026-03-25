package com.gymtracker.data.database

import android.content.Context
import androidx.room.*
import com.gymtracker.data.database.dao.*
import com.gymtracker.data.database.entities.*

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        PersonalRecordEntity::class,
        BodyMeasurementEntity::class,
        RoutineEntity::class,
        RoutineDayEntity::class,
        RoutineDayExerciseEntity::class,
        MuscleVolumeLogEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun workoutSetDao(): WorkoutSetDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun routineDao(): RoutineDao
    abstract fun routineDayDao(): RoutineDayDao
    abstract fun routineDayExerciseDao(): RoutineDayExerciseDao
    abstract fun muscleVolumeDao(): MuscleVolumeDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_tracker.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
