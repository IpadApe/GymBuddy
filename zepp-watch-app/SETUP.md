# GymBuddy — Amazfit T-REX 3 Watch App

## Requirements

- **Amazfit T-REX 3** (Zepp OS 4.0)
- Node.js ≥ 16
- Zepp developer account — https://dev.zepp.com

## Setup

```bash
cd zepp-watch-app
npm install

# Login to Zepp developer portal
npx zeus login

# Launch in simulator
npm run sim

# Build for device
npm run build
```

## Register your App ID

1. Go to https://dev.zepp.com
2. Create a new app → copy the App ID
3. Replace `1000099` in `app.json` → `"appId"` with your real ID

## Install on device

1. Build: `npm run build` → produces a `.zpk` file
2. Open **Zepp app** on your phone
3. Go to Profile → Amazfit T-REX 2 → App Store → scan QR or sideload `.zpk`

## Screens

| Screen | File | Purpose |
|---|---|---|
| Home | `page/home/index.js` | Dashboard, start/resume workout |
| Exercise Select | `page/workout/exercise-select.js` | Browse & filter 31 exercises |
| Log Set | `page/workout/log-set.js` | Weight (+/−) and reps (+/−) entry |
| Rest Timer | `page/workout/rest-timer.js` | Countdown ring, vibrates at end |
| Summary | `page/workout/summary.js` | Per-exercise breakdown, save |
| History | `page/history/index.js` | Last 20 workouts with detail view |

## Workout flow

```
Home → Exercise Select → Log Set → Rest Timer → Log Set → ... → Summary → Home
```

## Notes

- All data stored on the watch (no phone sync required)
- Last used weight per exercise is remembered
- Rest timer colours: orange → amber → red as time runs out
- Haptic feedback on set logged and timer end
