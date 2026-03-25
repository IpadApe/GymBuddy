# 💪 GymTracker — Free Offline Gym Tracking App

A fully-featured Android gym tracking application built with **Jetpack Compose** and **Kotlin**, inspired by Strong. **No payments, no subscriptions, no accounts.** All data stored locally with Room database. Works completely offline.

---

## Features

### Workout Logging
- **Split support**: Push, Pull, Legs, Upper/Lower, Full Body, Custom
- **Detailed set tracking**: weight, reps, RPE (1–10), tempo, notes
- **Set types**: Working, Warm-up, Drop Set, Failure
- **Built-in rest timer** with configurable durations, audio/vibration alerts
- **Real-time session timer** showing elapsed workout time
- **Superset & circuit grouping** via superset group IDs
- **Duplicate workouts** or save as reusable templates

### Exercise Library (200+ Exercises)
- Comprehensive database covering all major muscle groups
- **11 muscle groups**: Chest, Back, Shoulders, Biceps, Triceps, Forearms, Abs/Core, Quads, Hamstrings, Glutes, Calves
- **6 movement types**: Push, Pull, Hinge, Squat, Carry, Isolation
- **10 equipment categories**: Barbell, Dumbbell, Cable, Machine, Bodyweight, Kettlebell, Bands, EZ Bar, Smith Machine, Other
- Step-by-step instructions for every exercise
- Difficulty levels: Beginner, Intermediate, Advanced
- Full search and multi-filter support
- Create custom exercises with full metadata

### Progress Tracking
- **Per-exercise history**: all previous sets, weights, reps
- **1RM Calculator** using Epley, Brzycki, and Lombardi formulas with percentage tables
- **Workout calendar** with color-coded training days
- **Volume tracking**: per session, per week, per month
- **Automatic PR detection** with celebration animation
- **Body measurements**: weight, body fat %, chest, waist, arms, legs
- **Progressive overload suggestions**: detects plateaus and recommends next steps
- **Deload detection**: recommends deload after 4+ consecutive hard training weeks
- **Weekly/monthly summaries**

### Interactive Muscle Map
- Front and back body visualization showing muscle training status
- **Color-coded intensity** based on weekly/monthly volume:
  - 🟢 Green = Adequately trained (10–25 sets/week)
  - 🔴 Red = Overtrained (>25 sets/week)
  - ⚫ Grey = Undertrained (<10 sets/week)
- **Imbalance detection**: alerts when antagonist muscle pairs are imbalanced (e.g., chest vs. back)
- **Recovery estimator**: shows estimated recovery time per muscle group

### Personalized Recommendations
- Suggests which muscle groups to train next based on history
- Progressive overload alerts when performance plateaus for 2–3 sessions
- Exercise substitution suggestions for overtrained muscles
- Rest day recommendations based on training frequency
- Smart next-workout suggestions on the home screen

### Custom Routines
- **Full routine builder**: name, days, exercises per day with target sets/reps
- **5 prebuilt templates**:
  - Push Pull Legs (PPL) — 6 days
  - Upper/Lower Split — 4 days
  - StrongLifts 5×5 — 3 days
  - Arnold Split — 6 days
  - Full Body 3×/Week — 3 days
- Clone, edit, and archive routines
- Start any routine day directly as a workout session

### UI/UX
- **Dark mode** by default with light mode toggle
- Material 3 design with custom crimson/navy color palette
- Smooth Compose animations throughout
- **No login required** — zero friction
- **Plate calculator**: shows which plates to load per side
- **Unit toggle**: metric (kg/cm) ↔ imperial (lbs/in)
- Haptic feedback and sound cues for rest timer and PRs
- **Onboarding flow** for first-time setup (goal, frequency, units, split)
- Data export via JSON

---

## Architecture

```
com.gymtracker/
├── GymTrackerApp.kt          # Application class, DB init, seeding
├── MainActivity.kt            # Single Activity, Compose entry point
│
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt     # Room database (11 tables)
│   │   ├── entities/
│   │   │   └── Entities.kt    # All Room entities + relationship classes
│   │   ├── dao/
│   │   │   └── Daos.kt        # All DAO interfaces
│   │   ├── ExerciseSeedData.kt # 200+ built-in exercises
│   │   └── RoutineSeedData.kt  # 5 prebuilt routine templates
│   ├── model/
│   │   └── Models.kt          # Enums, domain models, DTOs
│   └── repository/
│       └── GymRepository.kt   # Single repository facade
│
├── ui/
│   ├── theme/
│   │   └── Theme.kt           # Colors, typography, dark/light schemes
│   ├── navigation/
│   │   └── Navigation.kt      # NavGraph, bottom nav, route definitions
│   ├── components/
│   │   └── Components.kt      # Reusable composables (cards, buttons, etc.)
│   └── screens/
│       ├── home/               # Dashboard with stats, recommendations
│       ├── workout/            # Setup + active workout logging
│       ├── exercises/          # Library + detail with 1RM calculator
│       ├── progress/           # Calendar, charts, measurements, PRs
│       ├── bodymap/            # Muscle map visualization
│       ├── routines/           # Browse, create, manage routines
│       ├── settings/           # Preferences, plate calculator, export
│       └── onboarding/         # First-time setup flow
│
└── util/
    └── Utils.kt               # Formatters, 1RM calculator, rest timer service
```

### Tech Stack
| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Navigation | Compose Navigation |
| Database | Room (SQLite) with KSP |
| State | ViewModel + StateFlow + Coroutines |
| Preferences | Room singleton entity |
| Charts | Vico Compose Charts |
| DI | Manual (Application-scoped singletons) |
| Background | WorkManager + Foreground Service (rest timer) |

### Database Schema (11 Tables)
1. `exercises` — 200+ exercises with metadata
2. `workout_sessions` — completed/in-progress workouts
3. `workout_exercises` — exercises within a session
4. `workout_sets` — individual sets with weight/reps/RPE
5. `personal_records` — auto-detected PRs
6. `body_measurements` — weight, body fat, circumferences
7. `routines` — custom and prebuilt routines
8. `routine_days` — days within a routine
9. `routine_day_exercises` — exercises per routine day
10. `muscle_volume_log` — aggregated daily volume per muscle
11. `user_preferences` — singleton preferences

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK 34

### Setup
```bash
# Clone or extract the project
cd gym-tracker

# Open in Android Studio
# File → Open → select the gym-tracker directory

# Sync Gradle (automatic on open)
# Run on device or emulator (API 26+)
```

### Build
```bash
./gradlew assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```

---

## Key Implementation Details

### Workout Flow
1. User taps "Start Workout" → selects split type or template
2. Session timer begins, exercises added from library
3. For each set: enter weight, reps, RPE → tap check to complete
4. Rest timer auto-starts after completing a set
5. PR detection runs on each completed set
6. "Finish" calculates total volume, updates muscle logs, saves session

### PR Detection
When a set is completed, the app checks if:
- Weight exceeds previous max weight for that exercise
- Weight matches max but reps are higher
If either is true, a `PersonalRecordEntity` is created and a celebration animation plays.

### Muscle Volume Tracking
After each session finishes, volume is aggregated by muscle group (primary + secondary) into `muscle_volume_log`. The body map reads these logs to compute 7-day and 30-day statuses.

### Imbalance Detection
The app checks antagonist muscle pairs (chest/back, biceps/triceps, quads/hamstrings) and flags when the volume ratio exceeds 1.5:1.

### Progressive Overload Engine
Analyzes the last 2–3 sessions per exercise. If weight and reps haven't changed, suggests adding 2.5kg or 1–2 reps.

---

## Customization

### Adding Exercises
Edit `ExerciseSeedData.kt` to add more exercises to the built-in database. Each exercise needs: name, primary muscle, secondary muscles, equipment, movement type, difficulty, and instructions.

### Adding Routine Templates
Edit `RoutineSeedData.kt` to add new prebuilt templates. Each template defines days and exercises with target sets/reps.

### Theming
Modify `Theme.kt` to change the color palette, typography, or add new theme variants.

---

## License

This project is provided as-is for personal use. No warranties or guarantees.

**Free forever. No accounts. No subscriptions. Your data is yours.**
