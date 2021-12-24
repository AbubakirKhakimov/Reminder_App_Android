package com.x.a_technologies.reminder.activities

import android.content.Context
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.view.Window
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.x.a_technologies.reminder.databinding.ActivityAlarmAutoRunBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.view.WindowManager
import com.x.a_technologies.reminder.datas.PublicDatas
import com.x.a_technologies.reminder.utils.ReminderSetManager
import com.x.a_technologies.reminder.datas.ReminderData
import com.x.a_technologies.reminder.utils.LocaleManager


class AlarmAutoRunActivity : AppCompatActivity() {

    lateinit var binding: ActivityAlarmAutoRunBinding
    lateinit var mediaPlayer: MediaPlayer
    lateinit var vibrator: Vibrator
    lateinit var timer:CountDownTimer
    var remindersDataList = ArrayList<ReminderData>()
    var currentReminderIndex = -1
    var workVibrator = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        binding = ActivityAlarmAutoRunBinding.inflate(layoutInflater)
        setContentView(binding.root)
        read()
        searchAlarm()
        instalizationValuesToViews()

        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)

        timer = object: CountDownTimer(30000,30000){
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                workVibrator = false
                vibrator.cancel()
                mediaPlayer.stop()
                mediaPlayer.release()
                modesCheck()
                finish()
            }
        }.start()

        binding.animationView.setOnClickListener {
            timer.cancel()
            workVibrator = false
            vibrator.cancel()
            mediaPlayer.stop()
            mediaPlayer.release()
            modesCheck()
            finish()
        }
    }

    fun modesCheck(){
        if (remindersDataList[currentReminderIndex].everyDayMode){
            remindersDataList[currentReminderIndex].reminderTimeMillis += 86400000
            ReminderSetManager().setAlarm(this,remindersDataList[currentReminderIndex])
        }else{
            remindersDataList[currentReminderIndex].reminderActive = false
        }
        write()
    }

    fun instalizationValuesToViews(){
        binding.time.text = getCurrentDateStringFormat("HH:mm")
        binding.date.text = getCurrentDateStringFormat("E, dd MMMM")
        binding.title.text = remindersDataList[currentReminderIndex].reminderTitle
        runToAudio()

        if (remindersDataList[currentReminderIndex].vibrateMode){
            Thread{
                vibrate()
            }.start()
        }
    }

    fun vibrate(){
        while (workVibrator) {
            vibrator.vibrate(1000)
            Thread.sleep(2000)
        }
    }

    fun runToAudio(){
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(remindersDataList[currentReminderIndex].reminderAudioPath)
        mediaPlayer.isLooping = true
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    fun searchAlarm(){
        val dateAndTime = getCurrentDateStringFormat("dd.MM.yyyy HH:mm")

        for (i in remindersDataList.indices){
            if (dateAndTime == getMillisDateStringFormat(remindersDataList[i].reminderTimeMillis)){
                currentReminderIndex = i
                break
            }
        }
    }

    fun getMillisDateStringFormat(timeMillis:Long):String{
        return SimpleDateFormat("dd.MM.yyyy HH:mm").format(Date(timeMillis))
    }

    fun getCurrentDateStringFormat(format:String):String{
        return SimpleDateFormat(format).format(Date())
    }

    fun write(){
        val gson = Gson()
        val jsonFormatStr = gson.toJson(remindersDataList)

        val pref = getSharedPreferences(PublicDatas.PREF_FILE_NAME, MODE_PRIVATE)
        val edit = pref.edit()
        edit.putString("reminderDataList",jsonFormatStr)
        edit.apply()
    }

    fun read(){
        val pref = getSharedPreferences(PublicDatas.PREF_FILE_NAME, MODE_PRIVATE)
        val jsonFormatStr = pref.getString("reminderDataList","empty")

        if (jsonFormatStr!="empty"){
            val gson = Gson()
            remindersDataList = gson.fromJson(jsonFormatStr,object : TypeToken<ArrayList<ReminderData>>() {}.type)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager().setLocale(newBase!!))
    }

}