package com.x.a_technologies.reminder.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.x.a_technologies.reminder.activities.AlarmAutoRunActivity
import com.x.a_technologies.reminder.activities.MainActivity
import com.x.a_technologies.reminder.datas.ReminderData

class ReminderSetManager {

    fun setAlarm(context: Context, reminderData: ReminderData){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmClockInfo = AlarmManager.AlarmClockInfo(reminderData.reminderTimeMillis,
            getAlarmInfoPendingIntent(context,reminderData.requestCode))

        alarmManager.setAlarmClock(alarmClockInfo,
            getAlarmActionPendingIntent(context,reminderData.requestCode))
    }

    fun cancelAlarm(context: Context, reminderData: ReminderData){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getAlarmActionPendingIntent(context,reminderData.requestCode))
    }

    private fun getAlarmActionPendingIntent(context: Context,requestCode:Int): PendingIntent? {
        val intent = Intent(context, AlarmAutoRunActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getAlarmInfoPendingIntent(context: Context,requestCode:Int): PendingIntent? {
        val alarmInfoIntent = Intent(context, MainActivity::class.java)
        alarmInfoIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(context, requestCode, alarmInfoIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}