package com.example.a24012011141_mad_pr4

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var layoutCreateAlarm: LinearLayout
    private lateinit var layoutCancelAlarm: LinearLayout
    private lateinit var tvAlarmTime: TextView
    private lateinit var btnAction: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        layoutCreateAlarm = findViewById(R.id.layout_create_alarm)
        layoutCancelAlarm = findViewById(R.id.layout_cancel_alarm)
        tvAlarmTime = findViewById(R.id.tv_alarm_time)
        btnAction = findViewById(R.id.btn_action)

        btnAction.setOnClickListener {
            if (layoutCreateAlarm.isVisible) {
                showTimerDialog()
            } else {
                cancelAlarm()
            }
        }
    }

    private fun showTimerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val alarmCalendar = Calendar.getInstance()
                alarmCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                alarmCalendar.set(Calendar.MINUTE, selectedMinute)
                alarmCalendar.set(Calendar.SECOND, 0)

                if (alarmCalendar.before(Calendar.getInstance())) {
                    alarmCalendar.add(Calendar.DATE, 1)
                }

                setAlarm(alarmCalendar.timeInMillis)

                val sdf = SimpleDateFormat("hh:mm:ss a MMM, dd yyyy", Locale.getDefault())
                tvAlarmTime.text = sdf.format(alarmCalendar.time)

                layoutCreateAlarm.visibility = View.GONE
                layoutCancelAlarm.visibility = View.VISIBLE
                btnAction.text = "Cancel Alarm"
            },
            hour,
            minute,
            false,
        )
        timePickerDialog.show()
    }

    private fun setAlarm(millis: Long) {
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        intent.putExtra("ALARM_KEY", "START")
        
        val pendingIntent = PendingIntent.getBroadcast(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
        }
    }

    private fun cancelAlarm() {
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        intent.putExtra("ALARM_KEY", "STOP")
        sendBroadcast(intent)

        val alarmIntent = Intent(this, AlarmBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 
            0, 
            alarmIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        layoutCreateAlarm.visibility = View.VISIBLE
        layoutCancelAlarm.visibility = View.GONE
        btnAction.text = "Create Alarm"
    }
}