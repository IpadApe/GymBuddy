/**
 * Exercise selection screen.
 * Shows a scrollable list of exercises filtered by category.
 * Tapping an exercise adds it to the current workout and navigates to log-set.
 */
import { createWidget, widget, align, prop } from '@zos/ui'
import { push, pop } from '@zos/router'
import { vibrate } from '@zos/interaction'
import { W, H, C, EXERCISES } from '../../utils/constants'
import { loadCurrentWorkout, saveCurrentWorkout } from '../../utils/storage'

// Active category filter — survives page rebuilds via module-level var
let _activeCategory = 'All'

Page({
  state: {
    filtered: EXERCISES
  },

  onInit() {
    // Ensure a workout session exists in globalData
    const app = getApp()
    if (!app.globalData.currentWorkout) {
      const saved = loadCurrentWorkout()
      if (saved) {
        app.globalData.currentWorkout = saved
      } else {
        app.globalData.currentWorkout = {
          startTime: Date.now(),
          exercises: []
        }
        saveCurrentWorkout(app.globalData.currentWorkout)
      }
    }
    this._applyFilter(_activeCategory)
  },

  _applyFilter(category) {
    _activeCategory = category
    this.state.filtered = category === 'All'
      ? EXERCISES
      : EXERCISES.filter(e =>
          e.category === category ||
          (category === 'Arms' && (e.category === 'Biceps' || e.category === 'Triceps'))
        )
  },

  build() {
    // ── Background ──────────────────────────────────────────
    createWidget(widget.FILL_RECT, { x: 0, y: 0, w: W, h: H, color: C.bg })

    // ── Top bar ─────────────────────────────────────────────
    createWidget(widget.FILL_RECT, { x: 0, y: 0, w: W, h: 58, color: C.surface })
    createWidget(widget.TEXT, {
      x: 0, y: 8, w: W, h: 42,
      text: 'Choose Exercise',
      text_size: 24,
      color: C.text,
      align_h: align.CENTER_H
    })

    // ── Category filter row ──────────────────────────────────
    const CATS = ['All', 'Chest', 'Back', 'Legs', 'Arms', 'Shoulders', 'Core', 'Cardio']
    const CAT_W = 80
    const catRow = createWidget(widget.SCROLL_LIST, {
      x: 0, y: 58, w: W, h: 46,
      // Horizontal scroll via item width
      item_space: 6,
      item_config: [{ type_name: 'cat', w: CAT_W, h: 38 }],
      item_config_count: 1,
      data_count: CATS.length,
      data_array: CATS.map(c => ({ type_name: 'cat', label: c })),
      item_build_func: (item, i) => {
        const cat = CATS[i]
        const sel = cat === _activeCategory
        item.createWidget(widget.FILL_RECT, {
          x: 0, y: 0, w: CAT_W, h: 36,
          radius: 18,
          color: sel ? C.primary : C.surface
        })
        item.createWidget(widget.TEXT, {
          x: 0, y: 4, w: CAT_W, h: 28,
          text: cat,
          text_size: 14,
          color: sel ? C.text : C.textDim,
          align_h: align.CENTER_H
        })
      },
      item_click_func: (_, i) => {
        this._applyFilter(CATS[i])
        // Force page rebuild to reflect new filter
        catRow.setProperty(prop.MORE, { data_count: CATS.length })
        exerciseList.setProperty(prop.MORE, {
          data_count: this.state.filtered.length,
          data_array: this._buildListData()
        })
      }
    })

    // ── Exercise list ────────────────────────────────────────
    const ITEM_H = 72
    const exerciseList = createWidget(widget.SCROLL_LIST, {
      x: 0, y: 106, w: W, h: H - 106,
      item_space: 2,
      item_config: [{ type_name: 'ex', w: W, h: ITEM_H }],
      item_config_count: 1,
      data_count: this.state.filtered.length,
      data_array: this._buildListData(),
      item_build_func: (item, i) => {
        const ex = this.state.filtered[i]
        if (!ex) return

        // Row background
        item.createWidget(widget.FILL_RECT, {
          x: 8, y: 2, w: W - 16, h: ITEM_H - 4,
          radius: 12,
          color: C.surface
        })

        // Exercise name
        item.createWidget(widget.TEXT, {
          x: 24, y: 10, w: W - 120, h: 30,
          text: ex.name,
          text_size: 21,
          color: C.text
        })

        // Category badge
        item.createWidget(widget.TEXT, {
          x: W - 110, y: 12, w: 94, h: 26,
          text: ex.category,
          text_size: 14,
          color: C.textDim,
          align_h: align.RIGHT
        })

        // Dot accent
        item.createWidget(widget.FILL_RECT, {
          x: 10, y: ITEM_H / 2 - 4, w: 8, h: 8,
          radius: 4,
          color: this._catColor(ex.category)
        })
      },
      item_click_func: (_, i) => {
        const ex = this.state.filtered[i]
        if (!ex) return
        vibrate({ type: 1 })
        getApp().globalData.activeExercise = ex
        push({ url: 'page/workout/log-set' })
      }
    })
  },

  _buildListData() {
    return this.state.filtered.map(e => ({ type_name: 'ex', ...e }))
  },

  _catColor(category) {
    switch (category) {
      case 'Chest':     return 0xFF6B35
      case 'Back':      return 0x4FC3F7
      case 'Shoulders': return 0xFFB300
      case 'Legs':      return 0x81D4FA
      case 'Biceps':    return 0x26C6A6
      case 'Triceps':   return 0x9575CD
      case 'Core':      return 0xFF8E5E
      case 'Glutes':    return 0xFFCC02
      case 'Calves':    return 0x80DEEA
      case 'Cardio':    return 0xEF5350
      default:          return C.textDim
    }
  }
})
