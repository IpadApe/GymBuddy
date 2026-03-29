import { HeartRate } from '@zos/sensor'
import { getPackageInfo } from '@zos/app'
import * as ble from '@zos/ble'
import { MessageBuilder } from './shared/message'

App({
  globalData: {
    // Active workout session — persists across page navigations
    currentWorkout: null,  // { startTime, exercises: [{ id, name, sets: [{ weight, reps }] }] }
    activeExercise: null,  // { id, name, category } currently being logged
    heartRate: 0,
    hrSensor: null,
    messageBuilder: null
  },

  onCreate() {
    // BLE messaging — connects to the phone side service
    try {
      const { appId } = getPackageInfo()
      const messageBuilder = new MessageBuilder({ appId, appDevicePort: 20, appSidePort: 0, ble })
      this.globalData.messageBuilder = messageBuilder
      messageBuilder.connect()
    } catch (_) {
      // BLE not available in simulator
    }

    // Start heart rate monitoring for the whole app session
    try {
      const hr = new HeartRate()
      this.globalData.hrSensor = hr
      hr.start()
      hr.onChange(() => {
        this.globalData.heartRate = hr.getCurrent()
      })
    } catch (_) {
      // Heart rate not available — continue without it
    }
  },

  onDestroy() {
    if (this.globalData.hrSensor) {
      this.globalData.hrSensor.stop()
    }
    if (this.globalData.messageBuilder) {
      this.globalData.messageBuilder.disConnect()
    }
  }
})
