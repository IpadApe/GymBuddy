/**
 * Log Set screen.
 *
 * Lets the user adjust weight and reps, then confirm a set.
 * Shows all sets logged for this exercise in the current workout.
 * After confirming: saves the set and navigates to rest-timer.
 */
import { createWidget, widget, align, deleteWidget, prop } from '@zos/ui'
import { push, pop } from '@zos/router'
import { vibrate } from '@zos/interaction'
import { W, H, C, DEFAULT_REST_SEC, WEIGHT_STEP } from '../../utils/constants'
import { saveCurrentWorkout, saveLastWeight, loadLastWeight, formatWeight } from '../../utils/storage'

Page({
  state: {
    exercise:  null,
    weight:    20,
    reps:      8,
    setNumber: 1,
    prevSets:  []   // Sets already logged for this exercise this workout
  },

  // ── Widget refs for live update ──────────────────────────
  _weightText: null,
  _repsText:   null,
  _setLabel:   null,
  _hrText:     null,
  _prevList:   null,

  onInit() {
    const app     = getApp()
    const ex      = app.globalData.activeExercise
    this.state.exercise = ex

    // Find previously logged sets for this exercise in current workout
    const workout = app.globalData.currentWorkout
    if (workout && ex) {
      const found = (workout.exercises || []).find(e => e.id === ex.id)
      this.state.prevSets  = found ? found.sets : []
      this.state.setNumber = this.state.prevSets.length + 1
    }

    // Recall last used weight for this exercise
    if (ex) {
      this.state.weight = loadLastWeight(ex.id)
    }
  },

  build() {
    const ex = this.state.exercise
    if (!ex) { pop(); return }

    // ── Background ──────────────────────────────────────────
    createWidget(widget.FILL_RECT, { x: 0, y: 0, w: W, h: H, color: C.bg })

    // ── Top bar: exercise name + set number ─────────────────
    createWidget(widget.FILL_RECT, { x: 0, y: 0, w: W, h: 60, color: C.surface })
    createWidget(widget.TEXT, {
      x: 10, y: 6, w: W - 20, h: 28,
      text: ex.name,
      text_size: 22,
      color: C.text,
      align_h: align.CENTER_H
    })
    this._setLabel = createWidget(widget.TEXT, {
      x: 10, y: 34, w: W - 20, h: 22,
      text: `Set ${this.state.setNumber}`,
      text_size: 16,
      color: C.primary,
      align_h: align.CENTER_H
    })

    // ── Weight control ───────────────────────────────────────
    // "-" button
    createWidget(widget.BUTTON, {
      x: 20, y: 76, w: 70, h: 70,
      radius: 35,
      normal_color: C.surfaceHigh,
      press_color:  C.textFaint,
      text: '−', text_size: 36, color: C.text,
      click_func: () => {
        this.state.weight = Math.max(0, Math.round((this.state.weight - WEIGHT_STEP) * 10) / 10)
        this._weightText.setProperty(prop.MORE, { text: formatWeight(this.state.weight) })
      }
    })
    // Weight display
    this._weightText = createWidget(widget.TEXT, {
      x: 96, y: 76, w: 168, h: 70,
      text: formatWeight(this.state.weight),
      text_size: 38,
      color: C.text,
      align_h: align.CENTER_H,
      align_v: align.CENTER_V
    })
    // "+" button
    createWidget(widget.BUTTON, {
      x: 270, y: 76, w: 70, h: 70,
      radius: 35,
      normal_color: C.surfaceHigh,
      press_color:  C.textFaint,
      text: '+', text_size: 36, color: C.text,
      click_func: () => {
        this.state.weight = Math.round((this.state.weight + WEIGHT_STEP) * 10) / 10
        this._weightText.setProperty(prop.MORE, { text: formatWeight(this.state.weight) })
      }
    })
    // Label
    createWidget(widget.TEXT, {
      x: 0, y: 150, w: W, h: 22,
      text: 'WEIGHT',
      text_size: 14,
      color: C.textDim,
      align_h: align.CENTER_H
    })

    // ── Reps control ─────────────────────────────────────────
    // "−" button
    createWidget(widget.BUTTON, {
      x: 60, y: 180, w: 56, h: 56,
      radius: 28,
      normal_color: C.surfaceHigh,
      press_color:  C.textFaint,
      text: '−', text_size: 28, color: C.text,
      click_func: () => {
        this.state.reps = Math.max(1, this.state.reps - 1)
        this._repsText.setProperty(prop.MORE, { text: String(this.state.reps) })
      }
    })
    // Reps display
    this._repsText = createWidget(widget.TEXT, {
      x: 120, y: 180, w: 116, h: 56,
      text: String(this.state.reps),
      text_size: 42,
      color: C.text,
      align_h: align.CENTER_H,
      align_v: align.CENTER_V
    })
    // "+" button
    createWidget(widget.BUTTON, {
      x: 240, y: 180, w: 56, h: 56,
      radius: 28,
      normal_color: C.surfaceHigh,
      press_color:  C.textFaint,
      text: '+', text_size: 28, color: C.text,
      click_func: () => {
        this.state.reps = Math.min(99, this.state.reps + 1)
        this._repsText.setProperty(prop.MORE, { text: String(this.state.reps) })
      }
    })
    // Label
    createWidget(widget.TEXT, {
      x: 0, y: 240, w: W, h: 22,
      text: 'REPS',
      text_size: 14,
      color: C.textDim,
      align_h: align.CENTER_H
    })

    // ── Previous sets (compact) ───────────────────────────────
    this._renderPrevSets()

    // ── Heart rate ────────────────────────────────────────────
    const hr = getApp().globalData.heartRate
    this._hrText = createWidget(widget.TEXT, {
      x: 0, y: 268, w: W, h: 24,
      text: hr > 0 ? `♥ ${hr}` : '',
      text_size: 16, color: C.hr, align_h: align.CENTER_H
    })

    // ── LOG SET button ────────────────────────────────────────
    createWidget(widget.BUTTON, {
      x: 57, y: 300, w: 340, h: 72,
      radius: 36,
      normal_color: C.primary,
      press_color:  C.primaryDk,
      text: 'LOG SET',
      text_size: 26,
      color: C.text,
      click_func: () => this._logSet()
    })

    // ── Change exercise ───────────────────────────────────────
    createWidget(widget.BUTTON, {
      x: 107, y: 386, w: 240, h: 46,
      radius: 23,
      normal_color: C.surface,
      press_color:  C.surfaceHigh,
      text: 'Change Exercise',
      text_size: 17,
      color: C.textDim,
      click_func: () => pop()
    })
  },

  _renderPrevSets() {
    const sets = this.state.prevSets
    if (sets.length === 0) return
    // Show last 3 sets compactly above the LOG SET button
    const recent = sets.slice(-3)
    recent.forEach((set, i) => {
      createWidget(widget.TEXT, {
        x: 0,
        y: 276 + i * 0,   // same row, spaced inline
        w: W,
        h: 0             // hidden — shown as a single summary line below
      })
    })
    // Summary of all sets: "3 sets: 80kg×8  80kg×8  82.5kg×6"
    const summary = recent.map(s => `${s.weight}×${s.reps}`).join('  ')
    createWidget(widget.TEXT, {
      x: 10, y: 272, w: W - 20, h: 24,
      text: `Sets: ${summary}`,
      text_size: 15,
      color: C.textDim,
      align_h: align.CENTER_H
    })
  },

  _logSet() {
    vibrate({ type: 1 })
    const { weight, reps, exercise } = this.state
    const newSet = { weight, reps, completedAt: Date.now() }

    // Update globalData
    const app     = getApp()
    const workout = app.globalData.currentWorkout
    let exEntry   = (workout.exercises || []).find(e => e.id === exercise.id)
    if (!exEntry) {
      exEntry = { id: exercise.id, name: exercise.name, category: exercise.category, sets: [] }
      workout.exercises.push(exEntry)
    }
    exEntry.sets.push(newSet)
    this.state.prevSets  = exEntry.sets
    this.state.setNumber = exEntry.sets.length + 1

    // Persist
    saveCurrentWorkout(workout)
    saveLastWeight(exercise.id, weight)

    // Navigate to rest timer
    push({
      url: 'page/workout/rest-timer',
      param: JSON.stringify({ restSeconds: DEFAULT_REST_SEC, exerciseId: exercise.id })
    })
  }
})
