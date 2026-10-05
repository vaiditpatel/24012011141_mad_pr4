package com.example.a24012011141_mad_pr4

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val SERVICE_KEY = "Service1"
        const val START_VAL = "start"
        const val STOP_VAL = "stop"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val str1 = intent.getStringExtra(SERVICE_KEY)
        if (str1 == START_VAL || str1 == STOP_VAL) {
            val intentService = Intent(context, AlarmService::class.java)
            if (str1 == START_VAL) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intentService)
                } else {
                    context.startService(intentService)
                }
            } else {
                context.stopService(intentService)
            }
        }
    }
}