package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.ui.calculateSunEvents
import java.util.Calendar

object GoldenHourAlarmScheduler {
    private const val PREFS_NAME = "golden_hour_alarm_prefs"
    private const val KEY_ENABLED = "alarm_enabled"
    private const val KEY_LAT = "alarm_lat"
    private const val KEY_LNG = "alarm_lng"

    fun isAlarmEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, false)
    }

    fun setAlarmEnabled(context: Context, enabled: Boolean, lat: Double = 47.4979, lng: Double = 19.0402) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putFloat(KEY_LAT, lat.toFloat())
            .putFloat(KEY_LNG, lng.toFloat())
            .apply()

        if (enabled) {
            scheduleNextAlarm(context, lat, lng)
        } else {
            cancelAlarm(context)
        }
    }

    fun scheduleNextAlarm(context: Context, lat: Double = 47.4979, lng: Double = 19.0402) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, GoldenHourAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate trigger time for today
        val cal = Calendar.getInstance()
        val sunEvents = calculateSunEvents(lat, lng, cal)

        val timeParts = sunEvents.civilTwilightEndTime.split(":")
        val twilightHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 20
        val twilightMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        val alarmCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, twilightHour)
            set(Calendar.MINUTE, twilightMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // 15 minutes before civil twilight end
            add(Calendar.MINUTE, -15)
        }

        // If today's alarm time has already passed, schedule for tomorrow
        if (alarmCal.timeInMillis <= System.currentTimeMillis()) {
            alarmCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val triggerAtMillis = alarmCal.timeInMillis

        try {
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (canExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.i("GoldenHourScheduler", "Scheduled golden hour alarm for: ${alarmCal.time}")
        } catch (e: Exception) {
            Log.e("GoldenHourScheduler", "Error scheduling alarm: ${e.message}", e)
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (_: Exception) {}
        }
    }

    fun scheduleTestAlarmIn5Seconds(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, GoldenHourAlarmReceiver::class.java).apply {
            putExtra("is_test", true)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + 5000L

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e("GoldenHourScheduler", "Test alarm error: ${e.message}")
        }
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, GoldenHourAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
