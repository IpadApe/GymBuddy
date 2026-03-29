import { createWidget, widget, align, deleteWidget } from '@zos/ui'
import { push } from '@zos/router'
import { vibrate } from '@zos/interaction'
import { W, H, C } from '../../utils/constants'
import { loadCurrentWorkout, loadHistory, formatDate, countTotalSets, formatDuration } from '../../utils/storage'

Page({
  state: {
    hasActive: false,
    lastWorkout: null
  },

  onInit() {
    this.state.hasActive  = !!loadCurrentWorkout()
    const hist            = loadHistory()
    this.state.lastWorkout = hist.length > 0 ? hist[0] : null
  },

  build() {
    const { hasActive, lastWorkout } = this.state

    // ── Background ──────────────────────────────────────────
    createWidget(widget.FILL_RECT, { x: 0, y: 0, w: W, h: H, color: C.bg })

    // ── App name ────────────────────────────────────────────
    createWidget(widget.TEXT, {
      x: 0, y: 68, w: W, h: 46,
      text: 'GymBuddy',
      text_size: 38,
      color: C.text,
      align_h: align.CENTER_H
    })

    // ── Last workout info ────────────────────────────────────
    if (lastWorkout) {
      const sets = countTotalSets(lastWorkout)
      const dur  = formatDuration(Math.round((lastWorkout.endTime - lastWorkout.startTime) / 1000))
      createWidget(widget.TEXT, {
        x: 0, y: 118, w: W, h: 28,
        text: `Last: ${formatDate(lastWorkout.startTime)}  •  ${sets} sets  •  ${dur}`,
        text_size: 17,
        color: C.textDim,
        align_h: align.CENTER_H
      })
    } else {
      createWidget(widget.TEXT, {
        x: 0, y: 118, w: W, h: 28,
        text: 'No workouts yet — let\'s go!',
        text_size: 17,
        color: C.textDim,
        align_h: align.CENTER_H
      })
    }

    // ── Heart rate (live from sensor) ────────────────────────
    const hr = getApp().globalData.heartRate
    createWidget(widget.TEXT, {
      x: 0, y: 152, w: W, h: 26,
      text: hr > 0 ? `♥  ${hr} bpm` : '',
      text_size: 18,
      color: C.hr,
      align_h: align.CENTER_H
    })

    // ── Primary button: Start or Resume ─────────────────────
    createWidget(widget.BUTTON, {
      x: 77, y: 188, w: 300, h: 72,
      radius: 36,
      normal_color: hasActive ? C.success : C.primary,
      press_color:  hasActive ? 0x009980  : C.primaryDk,
      text: hasActive ? 'Resume Workout' : 'Start Workout',
      text_size: 22,
      color: C.text,
      click_func: () => {
        vibrate({ type: 1 })
        push({ url: 'page/workout/exercise-select' })
      }
    })

    // ── History button ───────────────────────────────────────
    createWidget(widget.BUTTON, {
      x: 77, y: 276, w: 300, h: 60,
      radius: 30,
      normal_color: C.surface,
      press_color:  C.surfaceHigh,
      text: 'History',
      text_size: 20,
      color: C.textDim,
      click_func: () => {
        push({ url: 'page/history/index' })
      }
    })

    // ── Discard active workout (if any) ──────────────────────
    if (hasActive) {
      createWidget(widget.BUTTON, {
        x: 127, y: 350, w: 200, h: 46,
        radius: 23,
        normal_color: 0x3A1010,
        press_color:  0x250A0A,
        text: 'Discard Session',
        text_size: 16,
        color: C.error,
        click_func: () => {
          vibrate({ type: 2 })
          const { clearCurrentWorkout } = require('../../utils/storage')
          clearCurrentWorkout()
          getApp().globalData.currentWorkout = null
          // Rebuild page
          push({ url: 'page/home/index' })
        }
      })
    }
  }
})
