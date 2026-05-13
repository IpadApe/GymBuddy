---
name: GymBuddy
colors:
  # ── Dark scheme (default) ──────────────────────────────────────
  background: "#0C0C16"
  surface: "#131320"
  surface-container: "#1F1F32"
  surface-container-low: "#131320"
  surface-container-high: "#252538"
  outline: "#2A2A40"
  outline-variant: "#1E1E30"
  on-surface: "#F0F0F8"
  on-surface-variant: "#AAABC0"

  # ── Brand primary (default: Orange — 7 themes available) ───────
  primary: "#FF6B35"
  primary-light: "#FF8E5E"
  primary-dark: "#D95A28"
  primary-container: "#3D1A0A"
  on-primary: "#0C0C16"
  on-primary-container: "#FF8E5E"

  # ── Secondary — Sky Blue ───────────────────────────────────────
  secondary: "#4FC3F7"
  secondary-container: "#00334D"
  on-secondary: "#002A3F"
  on-secondary-container: "#4FC3F7"

  # ── Tertiary — Teal ───────────────────────────────────────────
  tertiary: "#26C6A6"
  tertiary-container: "#00352C"
  on-tertiary: "#00201A"
  on-tertiary-container: "#26C6A6"

  # ── Semantic ──────────────────────────────────────────────────
  success: "#26C6A6"
  warning: "#FFB300"
  error: "#FF4757"
  on-error: "#FFFFFF"
  error-container: "#3D0A0A"
  on-error-container: "#FF4757"

  # ── Light scheme (optional) ────────────────────────────────────
  light-background: "#F5F5FA"
  light-surface: "#FFFFFF"
  light-surface-container: "#EEEEEF5"
  light-outline: "#DDDDE8"
  light-outline-variant: "#CCCCDD"
  light-on-surface: "#0E0E1A"
  light-on-surface-variant: "#50506A"

  # ── Muscle group palette (body map & exercise chips) ───────────
  muscle-chest: "#FF6B35"
  muscle-back: "#4FC3F7"
  muscle-shoulders: "#FFB300"
  muscle-biceps: "#26C6A6"
  muscle-triceps: "#7C83FD"
  muscle-forearms: "#80CBC4"
  muscle-abs-core: "#FF8E5E"
  muscle-quads: "#81D4FA"
  muscle-hamstrings: "#4DB6AC"
  muscle-glutes: "#FFCC02"
  muscle-calves: "#80DEEA"
  muscle-cardio: "#EF5350"
  muscle-unknown: "#888899"

  # ── Training-state overlay (body map SVG fills) ───────────────
  muscle-undertrained: "#3A3A50"
  muscle-adequate: "#26C6A6"
  muscle-overtrained: "#FF4757"
  muscle-recovering: "#FFB300"

typography:
  # System font stack — Android Roboto / iOS SF Pro fallback
  display-lg:
    fontFamily: System
    fontSize: 36px
    fontWeight: "900"
    lineHeight: 44px
    letterSpacing: -0.5px
  display-md:
    fontFamily: System
    fontSize: 28px
    fontWeight: "700"
    lineHeight: 34px
    letterSpacing: 0px
  display-sm:
    fontFamily: System
    fontSize: 24px
    fontWeight: "700"
    lineHeight: 30px
    letterSpacing: 0px
  headline-lg:
    fontFamily: System
    fontSize: 22px
    fontWeight: "700"
    lineHeight: 28px
    letterSpacing: 0px
  headline-md:
    fontFamily: System
    fontSize: 20px
    fontWeight: "600"
    lineHeight: 26px
    letterSpacing: 0px
  headline-sm:
    fontFamily: System
    fontSize: 18px
    fontWeight: "600"
    lineHeight: 24px
    letterSpacing: 0px
  title-lg:
    fontFamily: System
    fontSize: 16px
    fontWeight: "700"
    lineHeight: 22px
    letterSpacing: 0.15px
  title-md:
    fontFamily: System
    fontSize: 14px
    fontWeight: "600"
    lineHeight: 20px
    letterSpacing: 0.1px
  title-sm:
    fontFamily: System
    fontSize: 13px
    fontWeight: "500"
    lineHeight: 18px
    letterSpacing: 0.1px
  body-lg:
    fontFamily: System
    fontSize: 16px
    fontWeight: "400"
    lineHeight: 24px
    letterSpacing: 0.25px
  body-md:
    fontFamily: System
    fontSize: 14px
    fontWeight: "400"
    lineHeight: 20px
    letterSpacing: 0.25px
  body-sm:
    fontFamily: System
    fontSize: 12px
    fontWeight: "400"
    lineHeight: 16px
    letterSpacing: 0.4px
  label-lg:
    fontFamily: System
    fontSize: 14px
    fontWeight: "600"
    lineHeight: 20px
    letterSpacing: 0.1px
  label-md:
    fontFamily: System
    fontSize: 12px
    fontWeight: "500"
    lineHeight: 16px
    letterSpacing: 0.5px
  label-sm:
    fontFamily: System
    fontSize: 10px
    fontWeight: "500"
    lineHeight: 14px
    letterSpacing: 0.5px
  # Special: section eyebrow labels use 2px extra tracking + UPPERCASE
  label-eyebrow:
    fontFamily: System
    fontSize: 10px
    fontWeight: "700"
    lineHeight: 14px
    letterSpacing: 2px

rounded:
  xs: 0.25rem      # 4dp  — tiny chips, PR badge
  sm: 0.5rem       # 8dp  — small chips, tag labels
  DEFAULT: 0.625rem # 10dp — bottom-nav pill, icon boxes
  md: 0.75rem      # 12dp — small cards, chips rows
  lg: 0.875rem     # 14dp — CTA button
  xl: 1rem         # 16dp — standard cards, search field
  2xl: 1.25rem     # 20dp — muscle filter pills (full-pill)
  full: 9999px     # circle avatars, icon dots

spacing:
  unit: 8px
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 20px
  2xl: 28px
  screen-horizontal: 20px
  card-padding: 16px
  card-gap: 10px
  section-gap: 28px
  bottom-nav-top: 6px
  bottom-nav-bottom: 10px

motion:
  screen-transition: 200ms
  screen-easing: linear
  button-feedback: 150ms
  button-easing: ease-in-out

elevation:
  card:
    borderWidth: 1px
    borderColor: "{colors.outline}"
    shadow: none
  card-active:
    borderWidth: 1px
    borderColor: "{colors.primary}"
    borderAlpha: 0.4
    shadow: none
  bottom-nav:
    borderTopWidth: 1px
    borderTopColor: "{colors.outline}"

components:
  # ── CTA Button (gradient) ──────────────────────────────────────
  button-primary:
    backgroundGradient: "horizontal: {colors.primary} → {colors.primary}@75%"
    textColor: "#FFFFFF"
    typography: "{typography.title-md}"
    rounded: "{rounded.lg}"
    height: 52px
    paddingHorizontal: 24px

  # ── Outlined / ghost button ────────────────────────────────────
  button-outlined:
    backgroundColor: transparent
    borderWidth: 1px
    borderColor: "{colors.outline}"
    textColor: "{colors.on-surface}"
    typography: "{typography.label-lg}"
    rounded: "{rounded.xl}"
    height: 44px

  # ── Search field ──────────────────────────────────────────────
  input-search:
    backgroundColor: "{colors.surface-container}"
    borderWidth: 1px
    borderColor: "{colors.outline}"
    borderFocusColor: "{colors.primary}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-md}"
    rounded: "{rounded.xl}"
    height: 48px
    paddingHorizontal: 16px
    leadingEmoji: "🔍"

  # ── Number input (sets/reps/weight) ───────────────────────────
  input-number:
    backgroundColor: transparent
    borderWidth: 1px
    borderColor: "{colors.outline}"
    borderFocusColor: "{colors.primary}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-md}"
    fontWeight: "600"
    rounded: "{rounded.DEFAULT}"

  # ── Standard card ─────────────────────────────────────────────
  card-standard:
    backgroundColor: "{colors.surface-container}"
    borderWidth: 1px
    borderColor: "{colors.outline}"
    rounded: "{rounded.xl}"
    padding: "{spacing.card-padding}"

  # ── Accent card (workout in progress, update banner) ──────────
  card-accent:
    backgroundGradient: "horizontal: {colors.primary}@25% → {colors.primary}@12%"
    borderWidth: 1px
    borderColor: "{colors.primary}@40%"
    rounded: "{rounded.xl}"
    padding: "{spacing.card-padding}"

  # ── Stat box (THIS WEEK section) ──────────────────────────────
  card-stat-box:
    backgroundColor: "{colors.surface-container}"
    borderWidth: 1px
    borderColor: "accentColor@25%"
    rounded: "{rounded.xl}"
    padding: 14px

  # ── Explore card (gradient tile) ──────────────────────────────
  card-explore:
    backgroundGradient: "linear: primaryContainer → primary"
    rounded: "{rounded.xl}"
    padding: 14px
    emojiSize: 22px
    labelTypography: "{typography.title-sm}"
    labelColor: "#FFFFFF"
    sublabelTypography: "{typography.body-sm}"
    sublabelColor: "#FFFFFF@70%"

  # ── Exercise row ──────────────────────────────────────────────
  exercise-row:
    backgroundColor: "{colors.surface-container}"
    borderWidth: 1px
    borderColor: "{colors.outline}"
    rounded: "{rounded.md}"
    padding: 14px
    iconBoxSize: 44px
    iconBoxRadius: "{rounded.DEFAULT}"
    iconBoxAlpha: 0.2
    chevron: "›"
    chevronSize: 18px

  # ── Muscle filter chip ────────────────────────────────────────
  chip-muscle:
    selectedBackground: "muscleColor@20%"
    selectedTextColor: "muscleColor"
    unselectedBackground: "{colors.surface-container}"
    unselectedTextColor: "{colors.on-surface-variant}"
    typography: "{typography.label-sm}"
    rounded: "{rounded.full}"
    paddingHorizontal: 12px
    paddingVertical: 5px

  # ── Muscle tag inline chip ────────────────────────────────────
  chip-tag:
    backgroundColor: "accentColor@15%"
    textColor: "accentColor"
    typography: "{typography.label-sm}"
    fontWeight: "700"
    rounded: "{rounded.xs}"
    paddingHorizontal: 6px
    paddingVertical: 2px

  # ── Bottom navigation ─────────────────────────────────────────
  bottom-nav:
    backgroundColor: "{colors.surface}"
    borderTopWidth: 1px
    borderTopColor: "{colors.outline}"
    paddingTop: "{spacing.bottom-nav-top}"
    paddingBottom: "{spacing.bottom-nav-bottom}"
    itemCount: 6
    iconEmoji: true
    emojiSize: 16px
    labelTypography: "{typography.label-sm}"
    labelFontSize: 9px
    labelLetterSpacing: 0.3px
    activeLabelWeight: "700"
    activeLabelColor: "{colors.primary}"
    inactiveLabelColor: "{colors.on-surface-variant}"
    pillWidth: 36px
    pillHeight: 20px
    pillRadius: "{rounded.DEFAULT}"
    pillActiveBackground: "{colors.primary}@12%"

  # ── Segmented tab bar (Progress screen) ───────────────────────
  tab-bar:
    backgroundColor: "{colors.surface-container}"
    rounded: "{rounded.xl}"
    padding: 4px
    activeTabBackground: "{colors.primary}"
    activeTabTextColor: "#FFFFFF"
    inactiveTabBackground: transparent
    inactiveTabTextColor: "{colors.on-surface-variant}"
    tabTypography: "{typography.label-sm}"
    tabFontWeight: "700"
    tabRadius: 9px
    tabPaddingVertical: 7px

  # ── Section eyebrow label ─────────────────────────────────────
  section-eyebrow:
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.label-eyebrow}"
    textTransform: uppercase
    marginBottom: 10px
    paddingHorizontal: "{spacing.screen-horizontal}"

  # ── Gradient primary button large ─────────────────────────────
  gradient-button:
    backgroundGradient: "horizontal: {colors.primary} → {colors.primary}@75%"
    disabledBackground: "#808080@30%"
    textColor: "#FFFFFF"
    typography: "{typography.title-md}"
    fontWeight: "700"
    rounded: "{rounded.lg}"
    height: 52px

  # ── PR celebration card ───────────────────────────────────────
  pr-celebration:
    backgroundColor: "{colors.success}@15%"
    borderGradient: "horizontal: {colors.success} → {colors.warning}"
    rounded: 20px
    padding: 24px
    trophyEmoji: "🏆"
    trophySize: 48px

  # ── Rest timer bar ────────────────────────────────────────────
  rest-timer:
    backgroundColor: "timerColor@8%"
    progressColor: "timerColor"
    trackColor: "timerColor@15%"
    height: 3px
    urgentColor: "{colors.error}"
    normalColor: "{colors.primary}"
    urgentThreshold: 5

  # ── Onboarding option card ────────────────────────────────────
  onboarding-option:
    backgroundColor: "{colors.surface-container}"
    borderWidth: 1px
    borderColor: "{colors.outline}"
    selectedBorderColor: "{colors.primary}"
    selectedBackground: "{colors.primary}@15%"
    rounded: "{rounded.xl}"
    padding: 14px
    emojiSize: 20px
    labelTypography: "{typography.title-sm}"
    sublabelTypography: "{typography.body-sm}"
    sublabelColor: "{colors.on-surface-variant}"

  # ── App icon ──────────────────────────────────────────────────
  app-icon:
    background: "{colors.primary}"
    foreground: "#FFFFFF"
    motif: barbell
    shape: adaptive-rounded-square
---

## Brand & Style

GymBuddy is a dark-first fitness tracker built on the principle that serious training deserves a serious UI. The aesthetic is **deep-space dark with energetic accent pops** — the kind of screen that looks at home in a dimly-lit gym at 6 AM.

The brand personality is focused, data-driven, and motivating without being aggressive. Every screen communicates progress and accountability. Emoji icons replace Material Icons throughout the interface, keeping the tone human and expressive without adding visual weight.

The default accent color is **"Burn Orange" (#FF6B35)**, chosen to evoke effort and heat. Seven user-selectable themes (Orange, Blue, Purple, Green, Red, Teal, Gold) let users personalise their environment. All themes share the same dark background stack — only the primary/container pair changes.

---

## Colors

### Background Stack

The screen is built from three progressively lighter dark layers:

| Role | Hex | Usage |
|---|---|---|
| `background` | `#0C0C16` | App background, deepest layer |
| `surface` | `#131320` | Bottom nav, modal sheets |
| `surface-container` | `#1F1F32` | Cards, input fields, stat boxes |

The stack has a slight blue-indigo tint (not pure black/gray), giving screens a "midnight" atmosphere rather than a flat dark mode. The `outline` (`#2A2A40`) sits at exactly the right contrast to separate card edges from the background without becoming visible borders at a glance.

### Primary (Theming System)

The app ships with a runtime theme switcher. Each theme provides a `primary`, `primaryLight`, `primaryDark`, and `primaryContainer` quad. Dark themes use a vibrant primary for tinting; light themes use a darker, higher-contrast primary. The default orange theme:

- **Active / CTA:** `#FF6B35` — buttons, selected nav items, active borders
- **Tint gradient:** Used at 18% opacity in the hero header to create a branded atmospheric wash at top of screen
- **Containers:** `#3D1A0A` — dark tinted backgrounds inside cards (e.g., pill icon boxes)

### Semantic Colors

Fixed regardless of theme:

- **Success / Tertiary:** `#26C6A6` teal — overload suggestions, PR highlights, adequate muscle state
- **Warning:** `#FFB300` amber — deload warnings, recovering muscle state
- **Error:** `#FF4757` red — overtrained muscle state, form validation

### Muscle Group Palette

Each of the 12 muscle groups has a unique identity color. These appear as chip tints, icon box backgrounds, and body-map SVG fills. The palette spans the full spectrum at approximately equal lightness, ensuring no two adjacent muscles share a hue. The same colors serve double duty as filter chip tints in the exercise library.

---

## Typography

No custom typeface is loaded — the system font (Roboto on Android) carries all text. The type system compensates with aggressive weight differentiation:

- **Black (900)** for display headlines — screen titles, greeting text
- **Bold (700)** for section headers, card titles, CTA button labels
- **SemiBold (600)** for secondary headings, tab labels, chip labels
- **Medium (500)** for body text that needs slight emphasis, labels
- **Regular (400)** for supporting body copy

Section labels use a distinctive **ALL-CAPS eyebrow** style (10sp, FontWeight.Bold, letterSpacing=2sp). These recur throughout the app as visual rhythm anchors — "THIS WEEK", "EXPLORE", "RECENT WORKOUTS", "WORKOUT CALENDAR" — keeping the scrollable content scannable.

Large numeric values (PR weights, volume totals, stat box numbers) use 18–20sp at Black/Bold weight in `on-surface` color, creating strong visual anchors within their cards.

---

## Layout & Spacing

All screens are `LazyColumn` (scrollable) with no fixed app bar. The header floats at the top of the scroll content. This gives maximum vertical space to workout content.

- **Screen horizontal padding:** 20dp (content), 16dp (some cards)
- **Section gap:** 28dp between major sections
- **Card-to-card gap:** 10–12dp
- **Card internal padding:** 14–16dp
- **Bottom safe area:** 24dp content padding below last item

The home screen hero header uses a vertical gradient overlay (`primary@18% → transparent`) to brand the top edge without a traditional toolbar.

---

## Elevation & Depth

This design system uses **borders over shadows**. Every card sits on a 1dp solid `outline` (`#2A2A40`) border, not a drop shadow. On the deep dark background this creates clean geometric separation while keeping the UI flat and fast.

Active or highlighted states switch the border color to `primary@40%` and add a subtle horizontal gradient background (`primary@25% → primary@12%`), effectively making selected elements "glow" from within rather than lift upward.

The bottom navigation sits behind a 1dp `outline` top divider only — no elevation shadow.

---

## Shapes

| Context | Radius | Examples |
|---|---|---|
| Standard cards | 16dp | Workout cards, stat boxes, search field |
| Small cards | 12dp | Exercise rows, PR cards, measurement cards |
| CTA button | 14dp | "Start Workout" gradient button |
| Icon container boxes | 9–10dp | Stat box icons, explore card icons |
| Muscle chips (filter) | full pill | 20dp+ radius muscle filter chips |
| Inline tags | 4–8dp | Muscle/equipment labels inside rows |
| Bottom nav pill | 10dp | Active indicator behind emoji |
| Segmented tabs | 12dp outer / 9dp inner | Progress screen tab bar |
| Circle avatars | CircleShape | Workout-in-progress icon dot |

The overall shape language is **rounded but not bubbly** — 16dp is large enough to feel modern, small enough to keep density high in a data-rich fitness app.

---

## Motion

Navigation transitions use a 200ms `fadeIn + slideInHorizontally(offset=screenWidth/4)` pair. Exit is `fadeOut` only (no slide out). Pop transitions mirror with a negative slide. The shallow offset (25% of screen width) gives enough motion cue to orient without feeling slow.

No spring animations or elaborate enter/exit sequences — the app prioritises responsiveness over theatrics.

---

## Icons

All icons in the navigation, exercise rows, stat boxes, and workout cards are **emoji**, not vector icon fonts. This was a deliberate design decision:

- Emoji render at native platform quality with zero dependency overhead
- They provide colour and character without requiring tinting logic
- They feel more personal and expressive in a wellness context

Navigation: 🏠🏋📅📈📍⚙️  
Stats: 🏋 (workouts) · 📈 (volume) · ⏱ (time)  
Equipment: 🏋 (barbell/default) · 💪 (dumbbell) · 🔗 (cable) · ⚙ (machine) · 🧍 (bodyweight) · ⚫ (kettlebell)  
Semantic: 🔥 (streak) · ⚡ (recommendations) · 🏆 (PR) · ▶ (start workout)

Material Icons are still used in a few secondary contexts (timer displays, measurement icons, warning icons) where emoji sizing is problematic.

---

## Component Patterns

### The Card Grammar

Every card in the app follows a three-property pattern:
1. **Container fill** — `surface-container` (`#1F1F32`)
2. **Border** — 1dp `outline` (`#2A2A40`)
3. **Accent** — border tint or background tint using the contextual accent color

Accent tints are always applied at low alpha (15–25%) to maintain the dark aesthetic. Never use a fully saturated fill on a card background.

### Gradient Button

The primary CTA button uses a horizontal gradient from `primary` to `primary@75%`. Combined with the 14dp radius and 52dp height, it reads as a solid orange pill at a glance but has a subtle depth that distinguishes it from flat Material buttons. The disabled state uses a grey gradient at 30% opacity rather than a grayed-out primary, preventing the disabled color from reading as a muted orange.

### Section Eyebrow Labels

Short ALL-CAPS labels with 2sp letter spacing precede every content section. They're rendered in `on-surface-variant` (`#AAABC0`) at 10sp Bold. These are not section headers — they're orientation markers. The visual language says "here is what follows" without competing with card content.

### Bottom Navigation Pill

The active nav item shows a 36×20dp rounded pill (10dp radius) behind the emoji icon at `primary@12%` opacity. The label switches to `primary` color and Bold weight. There is no underline, no icon state change, no scale transform — just the background pill and color shift. Simple and legible at 16dp font size.

### Segmented Tab Bar

The Progress screen uses a single-component segmented control: a `surface-container` container (12dp radius) containing three equal-width pill segments (9dp radius). The active segment fills with `primary`. This pattern reuses the same color vocabulary as the bottom nav pill but at full container scale.

### Muscle Color Chips

Muscle group tags appear in two places:
1. **Filter row** — larger FilterChip with full-pill radius, selected state uses `muscleColor@20%` background with `muscleColor` text
2. **Inline tag** — tiny label `9sp Bold` with 4dp radius, `muscleColor@15%` background, `muscleColor` text

Both follow the same alpha formula: 15–20% for fill, 100% for text.

### Hero Header

The home screen top area skips a toolbar entirely. A vertical gradient overlay (`primary@18% → transparent`) bleeds from the top edge of the scroll content, tinting the greeting area with the brand color. This creates a "branded atmosphere" effect that disappears as the user scrolls down into neutral card territory.

---

## Theming Notes

User-selectable accent colors share a fixed structural palette:

| Theme | Dark Primary | Light Primary |
|---|---|---|
| Orange (default) | `#FF6B35` | `#D95A28` |
| Blue | `#42A5F5` | `#1565C0` |
| Purple | `#CE93D8` | `#7B1FA2` |
| Green | `#81C784` | `#2E7D32` |
| Red | `#EF9A9A` | `#C62828` |
| Teal | `#4DB6AC` | `#00695C` |
| Gold | `#FFD54F` | `#F57F17` |

Only `primary`, `primaryLight`, `primaryDark`, and `primaryContainer` change between themes. All backgrounds, neutrals, semantic colors, and muscle group colors remain fixed. This means any new theme is guaranteed to work correctly with every existing component.
