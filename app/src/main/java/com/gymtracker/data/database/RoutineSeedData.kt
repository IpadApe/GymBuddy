package com.gymtracker.data.database

object RoutineSeedData {

    data class RoutineTemplate(
        val name: String,
        val description: String,
        val daysPerWeek: Int,
        val goal: String,
        val days: List<DayTemplate>
    )

    data class DayTemplate(
        val name: String,
        val splitType: String,
        val exercises: List<ExerciseTemplate>
    )

    data class ExerciseTemplate(
        val exerciseName: String,
        val targetSets: Int,
        val targetReps: String,
        val restSeconds: Int = 90
    )

    val templates = listOf(
        RoutineTemplate(
            name = "Push Pull Legs (PPL)",
            description = "Classic 6-day split hitting each muscle group twice per week. Great for intermediate to advanced lifters.",
            daysPerWeek = 6,
            goal = "Hypertrophy",
            days = listOf(
                DayTemplate("Push A", "Push", listOf(
                    ExerciseTemplate("Barbell Bench Press", 4, "6-8", 120),
                    ExerciseTemplate("Incline Dumbbell Press", 3, "8-10", 90),
                    ExerciseTemplate("Dumbbell Shoulder Press", 3, "8-10", 90),
                    ExerciseTemplate("Lateral Raise", 3, "12-15", 60),
                    ExerciseTemplate("Tricep Pushdown", 3, "10-12", 60),
                    ExerciseTemplate("Overhead Tricep Extension", 3, "10-12", 60)
                )),
                DayTemplate("Pull A", "Pull", listOf(
                    ExerciseTemplate("Barbell Deadlift", 4, "5-6", 180),
                    ExerciseTemplate("Pull-Ups", 3, "6-10", 120),
                    ExerciseTemplate("Barbell Row", 3, "8-10", 90),
                    ExerciseTemplate("Face Pull", 3, "15-20", 60),
                    ExerciseTemplate("Barbell Curl", 3, "8-10", 60),
                    ExerciseTemplate("Hammer Curl", 3, "10-12", 60)
                )),
                DayTemplate("Legs A", "Legs", listOf(
                    ExerciseTemplate("Barbell Back Squat", 4, "6-8", 180),
                    ExerciseTemplate("Romanian Deadlift", 3, "8-10", 120),
                    ExerciseTemplate("Leg Press", 3, "10-12", 90),
                    ExerciseTemplate("Leg Extension", 3, "12-15", 60),
                    ExerciseTemplate("Lying Leg Curl", 3, "10-12", 60),
                    ExerciseTemplate("Standing Calf Raise", 4, "12-15", 60)
                )),
                DayTemplate("Push B", "Push", listOf(
                    ExerciseTemplate("Overhead Press", 4, "6-8", 120),
                    ExerciseTemplate("Incline Barbell Bench Press", 3, "8-10", 90),
                    ExerciseTemplate("Cable Crossover", 3, "12-15", 60),
                    ExerciseTemplate("Lateral Raise", 4, "12-15", 60),
                    ExerciseTemplate("Skull Crushers", 3, "8-10", 60),
                    ExerciseTemplate("Tricep Kickback", 3, "12-15", 60)
                )),
                DayTemplate("Pull B", "Pull", listOf(
                    ExerciseTemplate("Barbell Row", 4, "6-8", 120),
                    ExerciseTemplate("Lat Pulldown", 3, "8-10", 90),
                    ExerciseTemplate("Seated Cable Row", 3, "10-12", 90),
                    ExerciseTemplate("Reverse Flyes", 3, "12-15", 60),
                    ExerciseTemplate("EZ Bar Curl", 3, "8-10", 60),
                    ExerciseTemplate("Incline Dumbbell Curl", 3, "10-12", 60)
                )),
                DayTemplate("Legs B", "Legs", listOf(
                    ExerciseTemplate("Front Squat", 4, "6-8", 180),
                    ExerciseTemplate("Bulgarian Split Squat", 3, "8-10", 90),
                    ExerciseTemplate("Hip Thrust", 3, "10-12", 90),
                    ExerciseTemplate("Lying Leg Curl", 3, "10-12", 60),
                    ExerciseTemplate("Leg Extension", 3, "12-15", 60),
                    ExerciseTemplate("Seated Calf Raise", 4, "15-20", 60)
                ))
            )
        ),
        RoutineTemplate(
            name = "Upper/Lower Split",
            description = "4-day split alternating between upper and lower body. Excellent balance of frequency and recovery.",
            daysPerWeek = 4,
            goal = "Hypertrophy",
            days = listOf(
                DayTemplate("Upper A (Strength)", "Upper", listOf(
                    ExerciseTemplate("Barbell Bench Press", 4, "5-6", 180),
                    ExerciseTemplate("Barbell Row", 4, "5-6", 180),
                    ExerciseTemplate("Overhead Press", 3, "6-8", 120),
                    ExerciseTemplate("Pull-Ups", 3, "6-8", 120),
                    ExerciseTemplate("Barbell Curl", 2, "8-10", 60),
                    ExerciseTemplate("Tricep Pushdown", 2, "8-10", 60)
                )),
                DayTemplate("Lower A (Strength)", "Lower", listOf(
                    ExerciseTemplate("Barbell Back Squat", 4, "5-6", 180),
                    ExerciseTemplate("Romanian Deadlift", 3, "6-8", 120),
                    ExerciseTemplate("Leg Press", 3, "8-10", 90),
                    ExerciseTemplate("Lying Leg Curl", 3, "8-10", 90),
                    ExerciseTemplate("Standing Calf Raise", 4, "10-12", 60),
                    ExerciseTemplate("Plank", 3, "60s", 60)
                )),
                DayTemplate("Upper B (Volume)", "Upper", listOf(
                    ExerciseTemplate("Incline Dumbbell Press", 3, "10-12", 90),
                    ExerciseTemplate("Seated Cable Row", 3, "10-12", 90),
                    ExerciseTemplate("Dumbbell Shoulder Press", 3, "10-12", 90),
                    ExerciseTemplate("Lat Pulldown", 3, "10-12", 90),
                    ExerciseTemplate("Lateral Raise", 3, "15-20", 60),
                    ExerciseTemplate("Dumbbell Curl", 3, "12-15", 60),
                    ExerciseTemplate("Overhead Tricep Extension", 3, "12-15", 60)
                )),
                DayTemplate("Lower B (Volume)", "Lower", listOf(
                    ExerciseTemplate("Front Squat", 3, "8-10", 120),
                    ExerciseTemplate("Hip Thrust", 3, "10-12", 90),
                    ExerciseTemplate("Bulgarian Split Squat", 3, "10-12", 90),
                    ExerciseTemplate("Leg Extension", 3, "12-15", 60),
                    ExerciseTemplate("Seated Leg Curl", 3, "12-15", 60),
                    ExerciseTemplate("Seated Calf Raise", 4, "15-20", 60),
                    ExerciseTemplate("Hanging Leg Raise", 3, "12-15", 60)
                ))
            )
        ),
        RoutineTemplate(
            name = "StrongLifts 5x5",
            description = "Simple and effective beginner program focused on linear progression with compound lifts. Train 3 days per week.",
            daysPerWeek = 3,
            goal = "Strength",
            days = listOf(
                DayTemplate("Workout A", "Full Body", listOf(
                    ExerciseTemplate("Barbell Back Squat", 5, "5", 180),
                    ExerciseTemplate("Barbell Bench Press", 5, "5", 180),
                    ExerciseTemplate("Barbell Row", 5, "5", 180)
                )),
                DayTemplate("Workout B", "Full Body", listOf(
                    ExerciseTemplate("Barbell Back Squat", 5, "5", 180),
                    ExerciseTemplate("Overhead Press", 5, "5", 180),
                    ExerciseTemplate("Barbell Deadlift", 1, "5", 180)
                ))
            )
        ),
        RoutineTemplate(
            name = "Arnold Split",
            description = "Arnold Schwarzenegger's classic 6-day bodybuilding split focusing on high volume training.",
            daysPerWeek = 6,
            goal = "Hypertrophy",
            days = listOf(
                DayTemplate("Chest & Back", "Custom", listOf(
                    ExerciseTemplate("Barbell Bench Press", 4, "8-10", 120),
                    ExerciseTemplate("Incline Dumbbell Press", 3, "10-12", 90),
                    ExerciseTemplate("Dumbbell Flyes", 3, "12-15", 60),
                    ExerciseTemplate("Pull-Ups", 4, "8-10", 120),
                    ExerciseTemplate("Barbell Row", 4, "8-10", 90),
                    ExerciseTemplate("T-Bar Row", 3, "10-12", 90)
                )),
                DayTemplate("Shoulders & Arms", "Custom", listOf(
                    ExerciseTemplate("Arnold Press", 4, "8-10", 90),
                    ExerciseTemplate("Lateral Raise", 4, "12-15", 60),
                    ExerciseTemplate("Reverse Flyes", 3, "12-15", 60),
                    ExerciseTemplate("Barbell Curl", 3, "8-10", 60),
                    ExerciseTemplate("Skull Crushers", 3, "8-10", 60),
                    ExerciseTemplate("Hammer Curl", 3, "10-12", 60),
                    ExerciseTemplate("Tricep Pushdown", 3, "10-12", 60)
                )),
                DayTemplate("Legs", "Legs", listOf(
                    ExerciseTemplate("Barbell Back Squat", 5, "8-10", 180),
                    ExerciseTemplate("Leg Press", 4, "10-12", 120),
                    ExerciseTemplate("Walking Lunges", 3, "12-15", 90),
                    ExerciseTemplate("Leg Extension", 3, "12-15", 60),
                    ExerciseTemplate("Lying Leg Curl", 4, "10-12", 60),
                    ExerciseTemplate("Standing Calf Raise", 5, "15-20", 60)
                )),
                DayTemplate("Chest & Back (B)", "Custom", listOf(
                    ExerciseTemplate("Incline Barbell Bench Press", 4, "8-10", 120),
                    ExerciseTemplate("Dumbbell Bench Press", 3, "10-12", 90),
                    ExerciseTemplate("Cable Crossover", 3, "12-15", 60),
                    ExerciseTemplate("Lat Pulldown", 4, "8-10", 90),
                    ExerciseTemplate("Dumbbell Row", 3, "10-12", 90),
                    ExerciseTemplate("Seated Cable Row", 3, "10-12", 90)
                )),
                DayTemplate("Shoulders & Arms (B)", "Custom", listOf(
                    ExerciseTemplate("Overhead Press", 4, "8-10", 90),
                    ExerciseTemplate("Cable Lateral Raise", 4, "12-15", 60),
                    ExerciseTemplate("Face Pull", 3, "15-20", 60),
                    ExerciseTemplate("EZ Bar Curl", 3, "8-10", 60),
                    ExerciseTemplate("Close Grip Bench Press", 3, "8-10", 90),
                    ExerciseTemplate("Concentration Curl", 3, "10-12", 60),
                    ExerciseTemplate("Cable Overhead Extension", 3, "10-12", 60)
                )),
                DayTemplate("Legs (B)", "Legs", listOf(
                    ExerciseTemplate("Front Squat", 4, "8-10", 180),
                    ExerciseTemplate("Hack Squat", 3, "10-12", 120),
                    ExerciseTemplate("Bulgarian Split Squat", 3, "10-12", 90),
                    ExerciseTemplate("Stiff Leg Deadlift", 3, "10-12", 90),
                    ExerciseTemplate("Seated Leg Curl", 3, "12-15", 60),
                    ExerciseTemplate("Seated Calf Raise", 5, "15-20", 60)
                ))
            )
        ),
        RoutineTemplate(
            name = "Full Body 3x/Week",
            description = "Efficient full body routine for beginners or those with limited time. Train 3 days per week with rest days between.",
            daysPerWeek = 3,
            goal = "Strength",
            days = listOf(
                DayTemplate("Full Body A", "Full Body", listOf(
                    ExerciseTemplate("Barbell Back Squat", 3, "8-10", 120),
                    ExerciseTemplate("Barbell Bench Press", 3, "8-10", 120),
                    ExerciseTemplate("Barbell Row", 3, "8-10", 120),
                    ExerciseTemplate("Overhead Press", 2, "8-10", 90),
                    ExerciseTemplate("Barbell Curl", 2, "10-12", 60),
                    ExerciseTemplate("Plank", 3, "45s", 60)
                )),
                DayTemplate("Full Body B", "Full Body", listOf(
                    ExerciseTemplate("Barbell Deadlift", 3, "6-8", 180),
                    ExerciseTemplate("Incline Dumbbell Press", 3, "8-10", 90),
                    ExerciseTemplate("Lat Pulldown", 3, "8-10", 90),
                    ExerciseTemplate("Leg Press", 3, "10-12", 90),
                    ExerciseTemplate("Lateral Raise", 3, "12-15", 60),
                    ExerciseTemplate("Tricep Pushdown", 2, "10-12", 60)
                )),
                DayTemplate("Full Body C", "Full Body", listOf(
                    ExerciseTemplate("Front Squat", 3, "8-10", 120),
                    ExerciseTemplate("Dumbbell Bench Press", 3, "10-12", 90),
                    ExerciseTemplate("Dumbbell Row", 3, "10-12", 90),
                    ExerciseTemplate("Dumbbell Shoulder Press", 3, "10-12", 90),
                    ExerciseTemplate("Hammer Curl", 2, "10-12", 60),
                    ExerciseTemplate("Hanging Leg Raise", 3, "10-15", 60)
                ))
            )
        )
    )
}
