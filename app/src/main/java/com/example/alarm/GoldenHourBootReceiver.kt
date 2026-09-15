package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class GoldenHourBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (GoldenHourAlarmScheduler.isAlarmEnabled(context)) {
                GoldenHourAlarmScheduler.scheduleNextAlarm(context)
            }
        }
    }
}
