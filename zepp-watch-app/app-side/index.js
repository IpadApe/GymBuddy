/**
 * GymBuddy — Side Service (runs inside the Zepp app on the phone)
 *
 * Receives a SAVE_WORKOUT message from the watch and forwards
 * the workout data to the Android app's local HTTP server.
 */
import { MessageBuilder } from '../shared/message-side'

const messageBuilder = new MessageBuilder()

const ANDROID_ENDPOINT = 'http://localhost:8765/api/sync-workout'

const syncWorkout = async (workoutData, ctx) => {
  try {
    const { body } = await fetch({
      url: ANDROID_ENDPOINT,
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(workoutData)
    })
    ctx.response({ data: { result: 'ok' } })
  } catch (err) {
    ctx.response({ data: { result: 'error', message: String(err) } })
  }
}

AppSideService({
  onInit() {
    messageBuilder.listen(() => {})

    messageBuilder.on('request', (ctx) => {
      const payload = messageBuilder.buf2Json(ctx.request.payload)
      if (payload.method === 'SAVE_WORKOUT') {
        return syncWorkout(payload.params, ctx)
      }
      ctx.response({ data: { result: 'unknown_method' } })
    })
  },

  onRun() {},

  onDestroy() {}
})
