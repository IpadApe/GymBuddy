/**
 * Rest Timer screen.
 *
 * Counts down from DEFAULT_REST_SEC.
 * Vibrates at 10 s remaining and at 0.
 * User can skip the timer or finish the entire workout from here.
 */
import { createWidget, widget, align, prop } from '@zos/ui'
import { push, pop } from '@zos/router'
import { vibrate } from '@zos/interaction'
import { setInterval, clearInterval } from '@zos/timer'
import { W, H, C } from '../../utils/constants'
import {
  saveCurrentWorkout, saveCompletedWorkout,
  clearCurrentWorkout, formatDuration, calcVolume, countTotalSets
} from '../../utils/storage'

Page({
  state: {
    remaining:    90,
    total:        90,
    exerciseId:   null,
    paused:       false
  },

  _timer:      null,
  _arcWidget:  null,
  _timeText:   null,
  _labelText:  null,

  onInit(paramStr) {
    try {
      const p = JSON.parse(paramStr || '{}')
      this.state.remaining  = p.restSeconds || 90
      this.state.total      = this.state.remaining
      this.state.exerciseId = p.exerciseId || null
    } catch (_) {}
  },

  build() {
    // ── Background ──────────────────────────────────────────
    createWidget(widget.FILL_RECT, { x: 0, y: 0, w: W, h: H, color: C.bg })

    // ── Title ────────────────────────────────────────────────
    createWidget(widget.TEXT, {
      x: 0, y: 46, w: W, h: 32,
      text: 'REST',
      text_size: 22,
      color: C.textDim,
      align_h: align.CENTER_H
    })

    // ── Arc progress ring ────────────────────────────────────
    // Background ring
    createWidget(widget.ARC, {
      x: 40, y: 80, w: W - 80, h: W - 80,
      start_angle: -90,
      end_angle:   270,
      color:       C.surfaceHigh,
      line_width:  14
    })
    // Progress ring (foreground)
    this._arcWidget = createWidget(widget.ARC, {
      x: 40, y: 80, w: W - 80, h: W - 80,
      start_angle: -90,
      end_angle:   this._calcArcEnd(),
      color:       C.primary,
      line_width:  14
    })

    // ── Countdown time ────────────────────────────────────────
    this._timeText = createWidget(widget.TEXT, {
      x: 0, y: 190, w: W, h: 80,
      text: this._formatCountdown(),
      text_size: 64,
      color: C.text,
      align_h: align.CENTER_H,
      align_v: align.CENTER_V
    })

    // ── Status label ──────────────────────────────────────────
    this._labelText = createWidget(widget.TEXT, {
      x: 0, y: 274, w: W, h: 26,
      text: 'seconds remaining',
      text_size: 16,
      color: C.textDim,
      align_h: align.CENTER_H
    })

    // ── HR display ────────────────────────────────────────────
    const hr = getApp().globalData.heartRate
    createWidget(widget.TEXT, {
      x: 0, y: 308, w: W, h: 24,
      text: hr > 0 ? `♥ ${hr} bpm` : '',
      text_size: 18, color: C.hr, align_h: align.CENTER_H
    })

    // ── Skip rest button ──────────────────────────────────────
    createWidget(widget.BUTTON, {
      x: 77, y: 340, w: 300, h: 58,
      radius: 29,
      normal_color: C.surface,
      press_color:  C.surfaceHigh,
      text: 'Skip Rest  →',
      text_size: 20,
      color: C.text,
      click_func: () => this._skipRest()
    })

    // ── Finish workout button ──────────────────────────────────
    createWidget(widget.BUTTON, {
      x: 127, y: 408, w: 200, h: 40,
      radius: 20,
      normal_color: 0x0A2A0A,
      press_color:  0x051505,
      text: 'Finish Workout',
      text_size: 15,
      color: C.success,
      click_func: () => this._finishWorkout()
    })

    // ── Start countdown ───────────────────────────────────────
    this._startTimer()
  },

  onDestroy() {
    this._stopTimer()
  },

  _startTimer() {
    this._stopTimer()
    this._timer = setInterval(() => {
      if (this.state.paused) return
      this.state.remaining -= 1

      // Update UI
      this._timeText.setProperty(prop.MORE, { text: this._formatCountdown() })
      this._arcWidget.setProperty(prop.MORE, {
        start_angle: -90,
        end_angle: this._calcArcEnd()
      })

      // Color transitions: green → orange → red
      const frac = this.state.remaining / this.state.total
      const color = frac > 0.5 ? C.primary
                  : frac > 0.2 ? C.warning
                  : C.error
      this._arcWidget.setProperty(prop.MORE, { color })

      if (this.state.remaining === 10) {
        vibrate({ type: 1 })
        this._labelText.setProperty(prop.MORE, { text: '10 seconds left!', color: C.warning })
      }

      if (this.state.remaining <= 0) {
        this._stopTimer()
        vibrate({ type: 3 })   // strong finish buzz
        this._labelText.setProperty(prop.MORE, { text: 'Go! Next set', color: C.success })
        // Auto-navigate after 1.5 s
        const { setTimeout } = require('@zos/timer')
        setTimeout(() => this._skipRest(), 1500)
      }
    }, 1000)
  },

  _stopTimer() {
    if (this._timer !== null) {
      clearInterval(this._timer)
      this._timer = null
    }
  },

  _skipRest() {
    this._stopTimer()
    // Return to log-set for the same exercise
    pop()
  },

  _finishWorkout() {
    this._stopTimer()
    push({ url: 'page/workout/summary' })
  },

  _formatCountdown() {
    const s = Math.max(0, this.state.remaining)
    const m = Math.floor(s / 60)
    const sec = s % 60
    return m > 0
      ? `${m}:${sec.toString().padStart(2, '0')}`
      : String(s)
  },

  _calcArcEnd() {
    const frac = Math.max(0, this.state.remaining / this.state.total)
    // Arc goes from -90° (top) clockwise. Full circle = 360°.
    return -90 + frac * 360
  }
})
