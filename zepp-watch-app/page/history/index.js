/**
 * Workout History screen.
 *
 * Lists the last 20 saved workouts. Tapping one opens a detail view.
 */
import { createWidget, widget, align } from '@zos/ui'
import { push, pop } from '@zos/router'
import { W, H, C } from '../../utils/constants'
import {
  loadHistory, formatDate, formatDuration,
  calcVolume, countTotalSets
} from '../../utils/storage'

// Module-level selected index for detail view
let _selectedIndex = -1

Page({
  state: {
    history: [],
    showDetail: false
  },

  onInit() {
    this.state.history = loadHistory()
    this.state.showDetail = _selectedIndex >= 0
  },

  build() {
    if (this.state.showDetail && _selectedIndex >= 0) {
      this._buildDetail(this.state.history[_selectedIndex])
    } else {
      this._buildList()
    }
  },

  _buildList() {
    const history = this.state.history

    // ── Background ──────────────────────────────────────────
    createWidget(widget.FILL_RECT, { x: 0, y: 0, w: W, h: H, color: C.bg })

    // ── Top bar ─────────────────────────────────────────────
    createWidget(widget.FILL_RECT, { x: 0, y: 0, w: W, h: 56, color: C.surface })
    createWidget(widget.TEXT, {
      x: 0, y: 10, w: W, h: 36,
      text: 'History',
      text_size: 26, color: C.text, align_h: align.CENTER_H
    })

    if (history.length === 0) {
      createWidget(widget.TEXT, {
        x: 0, y: 200, w: W, h: 54,
        text: 'No workouts yet.\nStart training!',
        text_size: 20, color: C.textDim, align_h: align.CENTER_H
      })
      return
    }

    // ── Scrollable list ───────────────────────────────────────
    const ITEM_H = 80

    createWidget(widget.SCROLL_LIST, {
      x: 0, y: 56, w: W, h: H - 56,
      item_space: 2,
      item_config: [{ type_name: 'session', w: W, h: ITEM_H }],
      item_config_count: 1,
      data_count: history.length,
      data_array: history.map((_, i) => ({ type_name: 'session', idx: i })),
      item_build_func: (item, i) => {
        const w = history[i]
        if (!w) return

        const dur    = formatDuration(Math.round(((w.endTime || w.startTime) - w.startTime) / 1000))
        const sets   = countTotalSets(w)
        const vol    = Math.round(calcVolume(w))
        const exList = (w.exercises || []).map(e => e.name).slice(0, 3).join(', ')
        const moreEx = (w.exercises || []).length > 3 ? ` +${(w.exercises || []).length - 3}` : ''

        // Row BG
        item.createWidget(widget.FILL_RECT, {
          x: 8, y: 2, w: W - 16, h: ITEM_H - 4,
          radius: 14, color: C.surface
        })

        // Date / duration
        item.createWidget(widget.TEXT, {
          x: 20, y: 8, w: W - 100, h: 24,
          text: formatDate(w.startTime),
          text_size: 18, color: C.text
        })
        item.createWidget(widget.TEXT, {
          x: W - 110, y: 8, w: 94, h: 24,
          text: dur, text_size: 16, color: C.primary, align_h: align.RIGHT
        })

        // Sets & volume
        item.createWidget(widget.TEXT, {
          x: 20, y: 34, w: W - 40, h: 20,
          text: `${sets} sets • ${vol} kg vol`,
          text_size: 14, color: C.textDim
        })

        // Exercises
        item.createWidget(widget.TEXT, {
          x: 20, y: 54, w: W - 40, h: 20,
          text: exList + moreEx,
          text_size: 13, color: C.textFaint
        })
      },
      item_click_func: (_, i) => {
        _selectedIndex = i
        // Rebuild to show detail view
        push({ url: 'page/history/index' })
      }
    })
  },

  _buildDetail(workout) {
    if (!workout) { _selectedIndex = -1; pop(); return }

    const dur  = formatDuration(Math.round(((workout.endTime || workout.startTime) - workout.startTime) / 1000))
    const sets = countTotalSets(workout)
    const vol  = Math.round(calcVolume(workout))

    // ── Background ──────────────────────────────────────────
    createWidget(widget.FILL_RECT, { x: 0, y: 0, w: W, h: H, color: C.bg })

    // ── Top bar with back ────────────────────────────────────
    createWidget(widget.FILL_RECT, { x: 0, y: 0, w: W, h: 56, color: C.surface })
    createWidget(widget.TEXT, {
      x: 0, y: 8, w: W, h: 40,
      text: formatDate(workout.startTime),
      text_size: 20, color: C.text, align_h: align.CENTER_H
    })

    // Stats strip
    createWidget(widget.TEXT, {
      x: 0, y: 62, w: W, h: 26,
      text: `${dur}  •  ${sets} sets  •  ${vol} kg`,
      text_size: 16, color: C.textDim, align_h: align.CENTER_H
    })

    // ── Exercise detail list ──────────────────────────────────
    const exercises = workout.exercises || []
    const ITEM_H    = 64

    createWidget(widget.SCROLL_LIST, {
      x: 0, y: 94, w: W, h: H - 94,
      item_space: 2,
      item_config: [{ type_name: 'exd', w: W, h: ITEM_H }],
      item_config_count: 1,
      data_count: exercises.length,
      data_array: exercises.map(e => ({ type_name: 'exd', ...e })),
      item_build_func: (item, i) => {
        const ex = exercises[i]
        if (!ex) return

        item.createWidget(widget.FILL_RECT, {
          x: 8, y: 2, w: W - 16, h: ITEM_H - 4,
          radius: 10, color: C.surface
        })

        item.createWidget(widget.TEXT, {
          x: 20, y: 8, w: 230, h: 24,
          text: ex.name, text_size: 18, color: C.text
        })

        const setsStr = (ex.sets || []).map(s => `${s.weight}×${s.reps}`).join('  ')
        item.createWidget(widget.TEXT, {
          x: 20, y: 34, w: W - 40, h: 20,
          text: setsStr, text_size: 14, color: C.textDim
        })
      }
    })
  }
})
