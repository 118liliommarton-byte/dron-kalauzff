package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class GoldenHourAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("GoldenHourAlarmReceiver", "Golden hour / civil twilight alarm triggered!")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val channelId = "golden_hour_drone_channel"

        val resId = context.resources.getIdentifier("drone_alarm", "raw", context.packageName)
        val soundUri = if (resId != 0) {
            Uri.parse("android.resource://" + context.packageName + "/" + resId)
        } else {
            android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
        }

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Aranyóra & Szürkület Drón Riasztások",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Értesítés az aranyóra és a polgári szürkület lejárta előtt egyedi drón hangjelzéssel"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300, 150, 500)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Play drone audio directly as well for guaranteed audibility
        if (resId != 0) {
            try {
                val mediaPlayer = MediaPlayer.create(context, resId)
                mediaPlayer?.let { player ->
                    player.setAudioAttributes(audioAttributes)
                    player.setOnCompletionListener { mp -> mp.release() }
                    player.start()
                }
            } catch (e: Exception) {
                Log.e("GoldenHourAlarmReceiver", "Error playing drone sound: ${e.message}")
            }
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isTest = intent.getBooleanExtra("is_test", false)
        val title = if (isTest) "🔔 TESZT Riasztás: Aranyóra & Szürkület" else "📸 Aranyóra & Polgári Szürkület Riasztás"
        val message = if (isTest) {
            "A lezárt képernyős és háttérbeli értesítés tökéletesen működik ezen a készüléken!"
        } else {
            "Még 15 perc van hátra a törvényes repülési időből (polgári szürkület vége)! Készülj fel a leszállásra."
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    if (isTest) {
                        "Ez egy teszt jelzés. Az aranyóra és polgári szürkület riasztás élesben a napnyugta és a szürkület lejárta előtt 15 perccel fog automatikusan megszólalni."
                    } else {
                        "Még 15 perc van hátra a törvényes repülési időből! Az aranyóra fényei után a polgári szürkület végével kötelező a leszállás. Tarts megfelelő biztonsági magasságot és landolj biztonságosan!"
                    }
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 150, 300, 150, 500))
            .setContentIntent(pendingIntent)
            .build()

        val notifId = if (isTest) 2002 else 2001
        notificationManager.notify(notifId, notification)

        // Reschedule for tomorrow if still enabled and was not a test
        if (!isTest && GoldenHourAlarmScheduler.isAlarmEnabled(context)) {
            GoldenHourAlarmScheduler.scheduleNextAlarm(context)
        }
    }
}
