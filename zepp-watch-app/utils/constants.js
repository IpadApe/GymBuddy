// ─── Screen dimensions (Amazfit T-REX 3) ────────────────────
export const W = 480
export const H = 480
export const CX = W / 2   // 227  — horizontal center

// ─── Color palette (mirrors Android GymBuddy theme) ─────────
export const C = {
  bg:          0x0D0D1A,
  surface:     0x1A1A2C,
  surfaceHigh: 0x2A2A44,
  primary:     0xFF6B00,   // OrangePrimary
  primaryDk:   0xCC5500,
  text:        0xFFFFFF,
  textDim:     0x888899,
  textFaint:   0x444455,
  success:     0x00BFA5,
  warning:     0xFFB300,
  error:       0xEF5350,
  hr:          0xFF4757
}

// ─── Default workout settings ────────────────────────────────
export const DEFAULT_REST_SEC = 90
export const WEIGHT_STEP      = 2.5   // kg increment per arrow tap
export const MAX_HISTORY      = 20    // workouts kept in storage

// ─── Storage keys ────────────────────────────────────────────
export const KEY_HISTORY = 'gb_history'
export const KEY_CURRENT = 'gb_current'
export const KEY_LAST_WEIGHT = 'gb_last_w'  // map exerciseId → last weight

// ─── Exercise library ────────────────────────────────────────
export const EXERCISES = [
  // Chest
  { id: 1,  name: 'Bench Press',     category: 'Chest' },
  { id: 2,  name: 'Incline Press',   category: 'Chest' },
  { id: 3,  name: 'Dumbbell Fly',    category: 'Chest' },
  { id: 4,  name: 'Push-Ups',        category: 'Chest' },
  // Back
  { id: 5,  name: 'Deadlift',        category: 'Back' },
  { id: 6,  name: 'Pull-Ups',        category: 'Back' },
  { id: 7,  name: 'Barbell Row',     category: 'Back' },
  { id: 8,  name: 'Lat Pulldown',    category: 'Back' },
  { id: 9,  name: 'Dumbbell Row',    category: 'Back' },
  { id: 10, name: 'Face Pull',       category: 'Back' },
  // Shoulders
  { id: 11, name: 'OHP',             category: 'Shoulders' },
  { id: 12, name: 'Lateral Raise',   category: 'Shoulders' },
  { id: 13, name: 'Front Raise',     category: 'Shoulders' },
  // Legs
  { id: 14, name: 'Squat',           category: 'Legs' },
  { id: 15, name: 'Leg Press',       category: 'Legs' },
  { id: 16, name: 'Romanian DL',     category: 'Legs' },
  { id: 17, name: 'Leg Curl',        category: 'Legs' },
  { id: 18, name: 'Leg Extension',   category: 'Legs' },
  { id: 19, name: 'Hip Thrust',      category: 'Glutes' },
  { id: 20, name: 'Calf Raise',      category: 'Calves' },
  // Arms
  { id: 21, name: 'Barbell Curl',    category: 'Biceps' },
  { id: 22, name: 'Hammer Curl',     category: 'Biceps' },
  { id: 23, name: 'Tricep Pushdown', category: 'Triceps' },
  { id: 24, name: 'Skull Crushers',  category: 'Triceps' },
  // Core
  { id: 25, name: 'Plank',           category: 'Core' },
  { id: 26, name: 'Crunches',        category: 'Core' },
  // Cardio
  { id: 27, name: 'Running',         category: 'Cardio' },
  { id: 28, name: 'Walking',         category: 'Cardio' },
  { id: 29, name: 'Cycling',         category: 'Cardio' },
  { id: 30, name: 'Jump Rope',       category: 'Cardio' },
  { id: 31, name: 'Rowing Machine',  category: 'Cardio' }
]

export const CATEGORIES = ['All', 'Chest', 'Back', 'Shoulders', 'Legs', 'Arms', 'Core', 'Cardio']
