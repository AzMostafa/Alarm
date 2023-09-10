package com.tools.alarm

import android.Manifest
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        Log.i("LOG", "AlarmReceiver")

        val resultIntent = Intent(context, MainActivity::class.java)
        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val mID = intent?.getStringExtra("ID")
        val mTitle = intent?.getStringExtra("title")
        val mText = intent?.getStringExtra("text")

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            mID?.toInt() ?: run { 0 },
            resultIntent,
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        if (context != null) {
            val notification =
                NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(mTitle)
                    .setContentText(mText)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .build()
            val notificationManager = NotificationManagerCompat.from(context)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(Constants.NOTIFICATION_ID, notification)
            }
        }
    }
}