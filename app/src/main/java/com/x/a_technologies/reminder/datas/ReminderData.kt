package com.x.a_technologies.reminder.datas

data class ReminderData(
    var reminderTitle:String,
    var reminderTimeMillis:Long,
    var reminderAudioPath:String,
    var reminderActive:Boolean,
    var requestCode:Int,
    var everyDayMode:Boolean,
    var vibrateMode:Boolean
)
