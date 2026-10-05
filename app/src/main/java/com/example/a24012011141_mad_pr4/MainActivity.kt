package com.example.a24012011141_mad_pr4

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextClock
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var textAlarm: TextView
    lateinit var cardSetAlarm: MaterialCardView
    lateinit var alarmTimeValue: TextView
    var pendingIntent: PendingIntent? = null
    lateinit var alarmManager: AlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textAlarm = findViewById<TextClock>(R.id.textView4)
        cardSetAlarm = findViewById(R.id.card_list)
        alarmTimeValue = findViewById(R.id.alarmTimeValue)
        cardSetAlarm.visibility = View.GONE
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        findViewById<MaterialButton>(R.id.btnSetAlarm).setOnClickListener {
            checkExactAlarmPermission()
            showTimedialog()
        }
        findViewById<MaterialButton>(R.id.btnCancelAlarm).setOnClickListener {
            cancelAlarm()
        }
    }

    private fun showTimedialog() {
        val cldr: Calendar = Calendar.getInstance()
        val h: Int = cldr.get(Calendar.HOUR_OF_DAY)
        val m: Int = cldr.get(Calendar.MINUTE)
        val picker = TimePickerDialog(
            this,
            { _, sHour, sMinute -> sendDialogDataToActivity(sHour, sMinute) },
            h,
            m,
            false,
        )
        picker.show()
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun sendDialogDataToActivity(hour: Int, minute: Int) {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        if (calendar.before(java.util.Calendar.getInstance())) {
            calendar.add(java.util.Calendar.DATE, 1)
        }

        cardSetAlarm.visibility = View.VISIBLE
        val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        alarmTimeValue.text = sdf.format(calendar.time)

        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        intent.putExtra(AlarmBroadcastReceiver.SERVICE_KEY, AlarmBroadcastReceiver.START_VAL)

        pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        pendingIntent?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, it)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, it)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, it)
            }
        }
    }

    private fun cancelAlarm() {
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        intent.putExtra(AlarmBroadcastReceiver.SERVICE_KEY, AlarmBroadcastReceiver.STOP_VAL)
        sendBroadcast(intent)

        pendingIntent?.let { alarmManager.cancel(it) }
        cardSetAlarm.visibility = View.GONE
        alarmTimeValue.text = "--:-- --"
    }
}