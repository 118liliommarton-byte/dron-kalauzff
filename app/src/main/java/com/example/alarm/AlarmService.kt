package com.example.alarm

import android.content.Context

interface AlarmService {
    fun isAlarmEnabled(): Boolean
    fun setAlarmEnabled(enabled: Boolean, lat: Double, lng: Double)
    fun scheduleNextAlarm(lat: Double, lng: Double)
    fun scheduleTestAlarmIn5Seconds()
    fun cancelAlarm()
}

/**
 * Android implementation wrapping GoldenHourAlarmScheduler.
 */
class AndroidAlarmService(private val context: Context) : AlarmService {
    override fun isAlarmEnabled(): Boolean {
        return GoldenHourAlarmScheduler.isAlarmEnabled(context)
    }

    override fun setAlarmEnabled(enabled: Boolean, lat: Double, lng: Double) {
        GoldenHourAlarmScheduler.setAlarmEnabled(context, enabled, lat, lng)
    }

    override fun scheduleNextAlarm(lat: Double, lng: Double) {
        GoldenHourAlarmScheduler.scheduleNextAlarm(context, lat, lng)
    }

    override fun scheduleTestAlarmIn5Seconds() {
        GoldenHourAlarmScheduler.scheduleTestAlarmIn5Seconds(context)
    }

    override fun cancelAlarm() {
        GoldenHourAlarmScheduler.cancelAlarm(context)
    }
}
