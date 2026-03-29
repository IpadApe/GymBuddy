import { localStorage } from '@zos/storage'
import { KEY_HISTORY, KEY_CURRENT, KEY_LAST_WEIGHT, MAX_HISTORY } from './constants'

// ─── Current / active workout ────────────────────────────────

export function saveCurrentWorkout(workout) {
  try {
    localStorage.setItem(KEY_CURRENT, JSON.stringify(workout))
  } catch (_) {}
}

export function loadCurrentWorkout() {
  try {
    const raw = localStorage.getItem(KEY_CURRENT)
    return raw ? JSON.parse(raw) : null
  } catch (_) {
    return null
  }
}

export function clearCurrentWorkout() {
  try {
    localStorage.removeItem(KEY_CURRENT)
  } catch (_) {}
}

// ─── Completed workout history ───────────────────────────────

export function saveCompletedWorkout(workout) {
  try {
    const history = loadHistory()
    history.unshift(workout)
    localStorage.setItem(KEY_HISTORY, JSON.stringify(history.slice(0, MAX_HISTORY)))
  } catch (_) {}
}

export function loadHistory() {
  try {
    const raw = localStorage.getItem(KEY_HISTORY)
    return raw ? JSON.parse(raw) : []
  } catch (_) {
    return []
  }
}

// ─── Per-exercise last-used weight ───────────────────────────

export function saveLastWeight(exerciseId, weight) {
  try {
    const map = loadLastWeightMap()
    map[exerciseId] = weight
    localStorage.setItem(KEY_LAST_WEIGHT, JSON.stringify(map))
  } catch (_) {}
}

export function loadLastWeight(exerciseId) {
  try {
    const map = loadLastWeightMap()
    return map[exerciseId] || 20  // Default to 20 kg
  } catch (_) {
    return 20
  }
}

function loadLastWeightMap() {
  try {
    const raw = localStorage.getItem(KEY_LAST_WEIGHT)
    return raw ? JSON.parse(raw) : {}
  } catch (_) {
    return {}
  }
}

// ─── Formatting helpers ──────────────────────────────────────

export function formatDuration(totalSeconds) {
  const h = Math.floor(totalSeconds / 3600)
  const m = Math.floor((totalSeconds % 3600) / 60)
  const s = totalSeconds % 60
  if (h > 0) return `${h}h ${m}m`
  if (m > 0) return `${m}m ${s}s`
  return `${s}s`
}

export function formatDate(timestamp) {
  const d = new Date(timestamp)
  const day  = d.getDate().toString().padStart(2, '0')
  const mon  = (d.getMonth() + 1).toString().padStart(2, '0')
  const hour = d.getHours().toString().padStart(2, '0')
  const min  = d.getMinutes().toString().padStart(2, '0')
  return `${day}/${mon} ${hour}:${min}`
}

export function formatWeight(kg) {
  return kg % 1 === 0 ? `${kg}kg` : `${kg.toFixed(1)}kg`
}

export function calcVolume(workout) {
  let vol = 0
  for (const ex of workout.exercises || []) {
    for (const set of ex.sets || []) {
      vol += (set.weight || 0) * (set.reps || 0)
    }
  }
  return vol
}

export function countTotalSets(workout) {
  return (workout.exercises || []).reduce((sum, ex) => sum + (ex.sets || []).length, 0)
}
