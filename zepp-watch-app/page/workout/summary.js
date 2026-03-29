/**
 * Workout Summary screen.
 *
 * Shows total sets, volume, duration, and a per-exercise breakdown.
 * Tapping "Save Workout" finalises the session and returns to home.
 */
import { createWidget, widget, align } from '@zos/ui'
import { push } from '@zos/router'
import { vibrate } from '@zos/interaction'
import { W, H, C } from '../../utils/constants'
import {
  saveCompletedWorkout, clearCurrentWorkout,
  formatDuration, formatDate, formatWeight, calcVolume, countTotalSets
} from '../../utils/storage'

Page({
  state: {
    workout: null
  },

  onInit() {
    const app = getApp()
    this.state.workout = app.globalData.currentWorkout
  },

  build() {
    const workout = this.state.workout
    if (!workout) { push({ url: 'page/home/index' }); return }

    // Compute stats
    workout.endTime = Date.now()
    const durationSec = Math.round((workout.endTime - workout.startTime) / 1000)
    const totalSets   = countTotalSets(workout)
    const totalVol    = calcVolume(workout)
    const exercises   = workout.exercises || []

    // ── Background ──────────────────────────────────────────
    createWidget(widget.FILL_RECT, { x: 0, y: 0, w: W, h: H, color: C.bg })

    // ── Header ───────────────────────────────────────────────
    createWidget(widget.FILL_RECT, { x: 0, y: 0, w: W, h: 56, color: C.surface })
    createWidget(widget.TEXT, {
      x: 0, y: 8, w: W, h: 40,
      text: 'Workout Done!',
      text_size: 26,
      color: C.success,
      align_h: align.CENTER_H
    })

    // ── Stats row ─────────────────────────────────────────────
    createWidget(widget.TEXT, {
      x: 0, y: 62, w: W, h: 28,
      text: `${formatDuration(durationSec)}  •  ${totalSets} sets  •  ${Math.round(totalVol)} kg vol`,
      text_size: 17,
      color: C.text,
      align_h: align.CENTER_H
    })
    createWidget(widget.TEXT, {
      x: 0, y: 90, w: W, h: 22,
      text: formatDate(workout.startTime),
      text_size: 14,
      color: C.textDim,
      align_h: align.CENTER_H
    })

    // ── Divider ───────────────────────────────────────────────
    createWidget(widget.FILL_RECT, { x: 40, y: 116, w: W - 80, h: 1, color: C.surfaceHigh })

    // ── Per-exercise breakdown (scrollable) ───────────────────
    const LIST_Y  = 120
    const ITEM_H  = 56
    const LIST_H  = exercises.length <= 3 ? exercises.length * ITEM_H : 160

    createWidget(widget.SCROLL_LIST, {
      x: 0, y: LIST_Y, w: W, h: LIST_H,
      item_space: 2,
      item_config: [{ type_name: 'ex', w: W, h: ITEM_H }],
      item_config_count: 1,
      data_count: exercises.length,
      data_array: exercises.map(e => ({ type_name: 'ex', ...e })),
      item_build_func: (item, i) => {
        const ex = exercises[i]
        if (!ex) return

        // BG
        item.createWidget(widget.FILL_RECT, {
          x: 8, y: 2, w: W - 16, h: ITEM_H - 4,
          radius: 10, color: C.surface
        })

        // Exercise name
        item.createWidget(widget.TEXT, {
          x: 20, y: 8, w: 200, h: 24,
          text: ex.name, text_size: 18, color: C.text
        })

        // Sets summary: e.g. "4 sets • 82.5kg max"
        const maxW  = Math.max(...ex.sets.map(s => s.weight || 0))
        const nSets = ex.sets.length
        item.createWidget(widget.TEXT, {
          x: 20, y: 32, w: 280, h: 20,
          text: `${nSets} set${nSets !== 1 ? 's' : ''} • max ${formatWeight(maxW)}`,
          text_size: 14, color: C.textDim
        })

        // Set details (compact)
        const setsStr = ex.sets.map(s => `${s.weight}×${s.reps}`).join('  ')
        item.createWidget(widget.TEXT, {
          x: W - 200, y: 14, w: 186, h: 28,
          text: setsStr, text_size: 13, color: C.textFaint,
          align_h: align.RIGHT
        })
      }
    })

    const btnY = LIST_Y + LIST_H + 12

    // ── Save Workout button ───────────────────────────────────
    createWidget(widget.BUTTON, {
      x: 57, y: btnY, w: 340, h: 68,
      radius: 34,
      normal_color: C.success,
      press_color:  0x009980,
      text: 'Save Workout',
      text_size: 24,
      color: C.text,
      click_func: () => {
        vibrate({ type: 3 })
        saveCompletedWorkout(workout)

        // Sync to phone app via BLE messaging
        const mb = getApp().globalData.messageBuilder
        if (mb) {
          mb.request(
            { method: 'SAVE_WORKOUT', params: workout },
            { timeout: 15000 }
          ).catch(() => { /* sync failed silently — data is already saved locally */ })
        }

        clearCurrentWorkout()
        getApp().globalData.currentWorkout = null
        getApp().globalData.activeExercise = null
        push({ url: 'page/home/index' })
      }
    })

    // ── Discard button ────────────────────────────────────────
    createWidget(widget.BUTTON, {
      x: 127, y: btnY + 76, w: 200, h: 40,
      radius: 20,
      normal_color: 0x1A0808,
      press_color:  0x0D0404,
      text: 'Discard',
      text_size: 16,
      color: C.error,
      click_func: () => {
        clearCurrentWorkout()
        getApp().globalData.currentWorkout = null
        getApp().globalData.activeExercise = null
        push({ url: 'page/home/index' })
      }
    })
  }
})
