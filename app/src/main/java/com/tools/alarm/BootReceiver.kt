package com.tools.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {

    private lateinit var context: Context

    private val MStore by lazy {
        MStore(context)
    }

    override fun onReceive(context: Context, intent: Intent) {

        Log.i("LOG","BootReceiver")

        this.context = context

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            setAlarm()
        }
    }

    private fun setAlarm() {
        var alarmID = 0
        CoroutineScope(Dispatchers.IO).launch {
            var loop = true
            while (loop) {
                val mAlarmTime = MStore.getData("AlarmTime_$alarmID")
                if (mAlarmTime != "null") {
                    alarmID++
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent = Intent(context, AlarmReceiver::class.java)
                    intent.putExtra("title", "Task Title")
                    intent.putExtra(
                        "text",
                        "Task description of Alarm with ID=$alarmID and Time=$mAlarmTime."
                    )
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        alarmID,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )

                    val calendar:Calendar = Calendar.getInstance()
                    val mSplitTime = mAlarmTime.split(":")
                    calendar[Calendar.HOUR_OF_DAY] = mSplitTime[0].toInt()
                    calendar[Calendar.MINUTE] = mSplitTime[1].toInt()
                    calendar[Calendar.SECOND] = 0
                    calendar[Calendar.MILLISECOND] = 0

                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                } else {
                    loop = false
                }
            }
            Log.i("LOG", "All [count=$alarmID] the Alarms Rebooted Successfully.")
        }
    }
}