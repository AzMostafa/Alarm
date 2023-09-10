package com.tools.alarm

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import io.reactivex.disposables.Disposables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var context: Context

    private var calendar: Calendar? = null
    private lateinit var PTime: String

    private val MStore by lazy {
        MStore(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.context = this

        setPermission()
        setNotificationChannel()

        val txtTime = findViewById<TextView>(R.id.txt_time)
        val btnSetTime = findViewById<Button>(R.id.btn_setTime)
        val btnSetAlarm = findViewById<Button>(R.id.btn_setAlarm)
        val btnRemoveAlarm = findViewById<Button>(R.id.btn_removeAlarm)

        btnSetTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Alarm time")
                .build()

            picker.show(supportFragmentManager, "AndAlarm")
            picker.addOnPositiveButtonClickListener {
                val time = String.format(
                    "%02d", picker.hour
                ) + ":" + String.format(
                    "%02d",
                    picker.minute
                )
                PTime = time
                txtTime.text = time

                calendar = Calendar.getInstance()
                calendar!![Calendar.HOUR_OF_DAY] = picker.hour
                calendar!![Calendar.MINUTE] = picker.minute
                calendar!![Calendar.SECOND] = 0
                calendar!![Calendar.MILLISECOND] = 0
            }
        }

        btnSetAlarm.setOnClickListener {
            setAlarm()
        }

        btnRemoveAlarm.setOnClickListener {
            removeAlarm()
        }
    }

    private fun setAlarm() {
        if (calendar != null) {
            var alarmID = 0
            CoroutineScope(Dispatchers.IO).launch {
                var loop = true
                while (loop) {
                    val mAlarmTime = MStore.getData("AlarmTime_$alarmID")
                    if (mAlarmTime != "null") {
                        alarmID++
                    } else {
                        loop = false
                    }
                }
                if (alarmID == 0) {
                    setBootPermission(true)
                }
                MStore.setData("AlarmTime_$alarmID", PTime)
            }

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.action = Constants.NOTIFICATION_ACTION_NAME
            intent.putExtra("title", "Task Title")
            intent.putExtra("text", "Task description of Alarm with ID=$alarmID and Time=$PTime.")
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmID,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar!!.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Toast.makeText(
                context,
                "Alarm with ID=$alarmID and Time=$PTime has set Successfully.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(context, "Alarm Time is Not Exist!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeAlarm() {
        var alarmID = 0
        CoroutineScope(Dispatchers.IO).launch {
            var loop = true
            while (loop) {
                val mAlarmTime = MStore.getData("AlarmTime_$alarmID")
                if (mAlarmTime != "null") {
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent = Intent(context, AlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.cancel(pendingIntent)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Alarm with ID=$alarmID is Removed Successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    alarmID++
                } else {
                    loop = false
                    MStore.clearData()
                    setBootPermission(false)
                }
            }
        }
    }

    private fun setPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                Constants.PERMISSION_NOTIFICATION_REQUEST_CODE
            )
        } else {
            Log.i("LOG", "Permission [POST_NOTIFICATIONS] has been granted by user")
        }
    }

    private fun setBootPermission(mode: Boolean) {
        val receiver = ComponentName(context, BootReceiver::class.java)
        val mPManager = if (mode){
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        context.packageManager.setComponentEnabledSetting(
            receiver,
            mPManager,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.PERMISSION_NOTIFICATION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("LOG", "Permission [POST_NOTIFICATIONS] has been denied by user")
                } else {
                    Log.i("LOG", "Permission [POST_NOTIFICATIONS] has been granted by user")
                }
            }
        }
    }

    private fun setNotificationChannel() {
        if (Build.VERSION.SDK_INT > 26) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
            Log.i("LOG", "createNotificationChannel is done [SDK_INT > 26]")
        } else {
            Log.i("LOG", "createNotificationChannel is done [SDK_INT < 26]")
        }
    }
}