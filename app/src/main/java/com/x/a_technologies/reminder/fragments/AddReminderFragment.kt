package com.x.a_technologies.reminder.fragments

import android.annotation.SuppressLint
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.x.a_technologies.reminder.R
import com.x.a_technologies.reminder.utils.ReminderSetManager
import com.x.a_technologies.reminder.databinding.FragmentAddReminderBinding
import com.x.a_technologies.reminder.datas.PublicDatas
import com.x.a_technologies.reminder.datas.ReminderData
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.media.MediaMetadataRetriever

class AddReminderFragment : Fragment() {

    lateinit var binding: FragmentAddReminderBinding
    var mediaRecorder = MediaRecorder()
    var mediaPlayer = MediaPlayer()
    lateinit var timer:CountDownTimer
    lateinit var appearanceAnimation: Animation
    lateinit var disappearingAnimation: Animation
    var remindersDataList = ArrayList<ReminderData>()
    var voiceRecorded = false
    var everyDayMode = false
    var vibrateMode = false
    var recordPath = ""
    var temporarilyRecordPath = ""
    lateinit var currentThemeColor:String

    override fun onStop() {
        super.onStop()
        if (mediaPlayer.isPlaying){
            mediaPlayer.stop()
        }

        actionUpVoiceButton()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddReminderBinding.inflate(layoutInflater)
        read()
        getSystemColor()
        timerObject()
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (PublicDatas.position != -1){
            editExistingReminder(PublicDatas.position)
        }else {
            binding.reminderDate.text = SimpleDateFormat("dd.MM.yyyy").format(Date())
        }

        binding.btnPlay.setOnClickListener {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(recordPath)
            mediaPlayer.prepare()
            mediaPlayer.start()
        }

        binding.btnOk.setOnClickListener {
            if (voiceRecorded){
                mediaRecorder.stop()
                mediaRecorder.release()
                voiceRecorded=false
                timer.cancel()
            }

            if (binding.reminderTitle.text.toString() == "") {
                Toast.makeText(requireActivity(), getString(R.string.please_write_a_name), Toast.LENGTH_SHORT).show()
            } else if (recordPath == "") {
                Toast.makeText(requireActivity(), getString(R.string.please_record_your_voice), Toast.LENGTH_SHORT).show()
            } else if (Date().after(Date(getMillisTimeFormat()))) {
                Toast.makeText(requireActivity(), getString(R.string.select_upcoming_time), Toast.LENGTH_SHORT).show()
            } else if(searchReminder()){
                Toast.makeText(requireActivity(), getString(R.string.reminder_on_list), Toast.LENGTH_SHORT).show()
            } else {
                if (PublicDatas.position != -1) {
                    clickOkExistingReminder(PublicDatas.position)
                } else {
                    clickOkNewReminder()
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            if (PublicDatas.position != -1){
                if (remindersDataList[PublicDatas.position].reminderAudioPath != recordPath){
                    delateRecord(recordPath)
                    recordPath=""
                }

                if (PublicDatas.reminderSwitchOnClick){
                    PublicDatas.reminderSwitchOnClick = false
                }

                PublicDatas.position = -1
            } else if (recordPath != ""){
                delateRecord(recordPath)
                recordPath=""
            }

            replaceFragment(MainFragment())
        }

        binding.reminderTime.setOnClickListener {
            val timeArray = binding.reminderTime.text.split(":")

            val materialTimePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(timeArray[0].toInt())
                .setMinute(timeArray[1].toInt())
                .setTitleText(getString(R.string.choose_time))
                .build()

            materialTimePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MILLISECOND,0)
                calendar.set(Calendar.SECOND,0)
                calendar.set(Calendar.MINUTE,materialTimePicker.minute)
                calendar.set(Calendar.HOUR_OF_DAY,materialTimePicker.hour)

                binding.reminderTime.text = SimpleDateFormat("HH:mm").format(calendar.time)
            }

            materialTimePicker.show(requireActivity().supportFragmentManager, "tag_picker")
        }

        binding.reminderDate.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
            val picker = builder.build()

            picker.addOnPositiveButtonClickListener {
                binding.reminderDate.text = SimpleDateFormat("dd.MM.yyyy").format(Date(it))
            }

            picker.show(requireActivity().supportFragmentManager, "tag_picker")
        }

        binding.everyDayMode.setOnClickListener {
            if (binding.everyDayMode.currentTextColor == Color.parseColor("#009688")){
                binding.everyDayMode.setTextColor(Color.WHITE)
                binding.everyDayMode.setBackgroundColor(Color.parseColor("#009688"))
                everyDayMode = true
            }else{
                binding.everyDayMode.setTextColor(Color.parseColor("#009688"))
                binding.everyDayMode.setBackgroundColor(Color.parseColor(currentThemeColor))
                everyDayMode = false
            }
        }

        binding.vibrateMode.setOnClickListener {
            if (binding.vibrateMode.currentTextColor == Color.parseColor("#009688")){
                binding.vibrateMode.setTextColor(Color.WHITE)
                binding.vibrateMode.setBackgroundColor(Color.parseColor("#009688"))
                vibrateMode = true
            }else{
                binding.vibrateMode.setTextColor(Color.parseColor("#009688"))
                binding.vibrateMode.setBackgroundColor(Color.parseColor(currentThemeColor))
                vibrateMode = false
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (PublicDatas.position != -1){
                    if (remindersDataList[PublicDatas.position].reminderAudioPath != recordPath){
                        delateRecord(recordPath)
                        recordPath=""
                    }

                    if (PublicDatas.reminderSwitchOnClick){
                        PublicDatas.reminderSwitchOnClick = false
                    }

                    PublicDatas.position = -1
                } else if (recordPath != ""){
                    delateRecord(recordPath)
                    recordPath=""
                }

                replaceFragment(MainFragment())
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

        binding.btnVoiceRecord.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    actionDownVoiceButton()
                }
                MotionEvent.ACTION_UP -> {
                    actionUpVoiceButton()
                }
            }
            return@setOnTouchListener true
        }

    }

    fun actionDownVoiceButton(){
        animateTimer()
        voiceRecord()
        timer.start()

        binding.animationView.visibility = View.VISIBLE
        binding.btnPlay.isEnabled = false
        binding.btnCancel.isClickable = false
        binding.btnOk.isClickable = false
        binding.btnPlay.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#007065"))
        binding.btnPlay.supportImageTintList = ColorStateList.valueOf(Color.parseColor("#B6B6B6"))
        voiceRecorded = true
    }

    fun actionUpVoiceButton(){
        if (voiceRecorded) {
            binding.recordTime.postDelayed({
                mediaRecorder.stop()
                mediaRecorder.release()

                val audioDuration = getCurrentAudioDuration(temporarilyRecordPath)
                when {
                    audioDuration < 200 -> {
                        delateRecord(temporarilyRecordPath)
                        Toast.makeText(requireActivity(),requireActivity().getString(R.string.hold_to_record_voice)
                            ,Toast.LENGTH_SHORT).show()
                    }
                    audioDuration < 1000 -> {
                        delateRecord(temporarilyRecordPath)
                        Toast.makeText(requireActivity(), requireActivity().getString(R.string.press_hold_longer)
                            ,Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        if (PublicDatas.position == -1) {
                            if (recordPath != "") {
                                delateRecord(recordPath)
                            }
                        }else{
                            if (remindersDataList[PublicDatas.position].reminderAudioPath != recordPath) {
                                delateRecord(recordPath)
                            }
                        }
                        recordPath = temporarilyRecordPath
                    }
                }

                if (recordPath != ""){
                    binding.btnPlay.isEnabled = true
                    binding.btnPlay.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                    binding.btnPlay.supportImageTintList = ColorStateList.valueOf(Color.WHITE)
                    binding.recordTime.text = SimpleDateFormat("ss")
                        .format(Date(getCurrentAudioDuration(recordPath)))
                }else{
                    binding.recordTime.text = "00"
                }

                temporarilyRecordPath = ""
                binding.btnCancel.isClickable = true
                binding.btnOk.isClickable = true
            },200)

            timer.cancel()
            binding.animationView.visibility = View.GONE
            voiceRecorded = false
        }
    }

    fun animateTimer(){
        disappearingAnimation = AnimationUtils.loadAnimation(requireActivity(),R.anim.disappearing_animation)
        appearanceAnimation = AnimationUtils.loadAnimation(requireActivity(),R.anim.appearance_animation)

        binding.recordTime.startAnimation(disappearingAnimation)
        disappearingAnimation.setAnimationListener(object :Animation.AnimationListener{
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                binding.recordTime.startAnimation(appearanceAnimation)
            }
        })
    }

    fun timerObject(){
        timer = object :CountDownTimer(15200, 1000){

            override fun onTick(millisUntilFinished: Long) {
                binding.recordTime.text = SimpleDateFormat("ss").format(Date(15200-millisUntilFinished))
            }

            override fun onFinish() {
                mediaRecorder.stop()
                mediaRecorder.release()
                if (recordPath != ""){
                    delateRecord(recordPath)
                }
                recordPath = temporarilyRecordPath
                temporarilyRecordPath = ""

                Toast.makeText(requireActivity()
                    ,requireActivity().getString(R.string.limitation), Toast.LENGTH_SHORT).show()

                binding.animationView.visibility = View.GONE
                binding.btnPlay.isEnabled = true
                binding.btnCancel.isClickable = true
                binding.btnOk.isClickable = true
                binding.btnPlay.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                binding.btnPlay.supportImageTintList = ColorStateList.valueOf(Color.WHITE)
                voiceRecorded=false
            }

        }
    }

    fun searchReminder(): Boolean {
        if (PublicDatas.position != -1){
            return false
        }

        val currentTime = getMillisTimeFormat()

        for (item in remindersDataList){
            if (item.reminderTimeMillis == currentTime){
                return true
            }
        }

        return false
    }

    fun clickOkExistingReminder(position: Int){
        remindersDataList[position].reminderTitle = binding.reminderTitle.text.toString()
        remindersDataList[position].reminderTimeMillis = getMillisTimeFormat()
        remindersDataList[position].everyDayMode = everyDayMode
        remindersDataList[position].vibrateMode = vibrateMode

        if (remindersDataList[position].reminderAudioPath != recordPath){
            delateRecord(remindersDataList[position].reminderAudioPath)
            remindersDataList[position].reminderAudioPath = recordPath
        }

        if (remindersDataList[position].reminderActive){
            ReminderSetManager().setAlarm(requireActivity(),remindersDataList[position])
        }

        if (PublicDatas.reminderSwitchOnClick){
            ReminderSetManager().setAlarm(requireActivity(),remindersDataList[position])
            remindersDataList[PublicDatas.position].reminderActive = true
            PublicDatas.reminderSwitchOnClick = false
        }

        recordPath=""
        write()
        Toast.makeText(requireActivity(), getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
        PublicDatas.position = -1

        replaceFragment(MainFragment())
    }

    fun clickOkNewReminder(){
        remindersDataList.add(
            ReminderData(
                binding.reminderTitle.text.toString(),
                getMillisTimeFormat(),
                recordPath,
                true,
                generateRequestCode(),
                everyDayMode,
                vibrateMode)
        )
        recordPath=""
        write()
        ReminderSetManager().setAlarm(requireActivity(), remindersDataList[remindersDataList.size-1])
        Toast.makeText(requireActivity(), getString(R.string.reminder_set), Toast.LENGTH_SHORT).show()

        replaceFragment(MainFragment())
    }

    fun editExistingReminder(position:Int){
        binding.reminderTitle.text = Editable.Factory.getInstance().newEditable(
            remindersDataList[position].reminderTitle)
        binding.reminderDate.text = getStringDateFormat("dd.MM.yyyy",remindersDataList[position].reminderTimeMillis)
        binding.reminderTime.text = getStringDateFormat("HH:mm",remindersDataList[position].reminderTimeMillis)
        binding.recordTime.text = SimpleDateFormat("ss").format(Date(getAudioDuration(position)))

        if (remindersDataList[position].everyDayMode){
            binding.everyDayMode.setTextColor(Color.WHITE)
            binding.everyDayMode.setBackgroundColor(Color.parseColor("#009688"))
            everyDayMode = true
        }

        if (remindersDataList[position].vibrateMode){
            binding.vibrateMode.setTextColor(Color.WHITE)
            binding.vibrateMode.setBackgroundColor(Color.parseColor("#009688"))
            vibrateMode = true
        }

        binding.btnPlay.isEnabled = true
        binding.btnPlay.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
        binding.btnPlay.supportImageTintList = ColorStateList.valueOf(Color.WHITE)

        recordPath = remindersDataList[position].reminderAudioPath
        binding.btnPlay.visibility = View.VISIBLE
    }

    fun getAudioDuration(position: Int):Long{
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(remindersDataList[position].reminderAudioPath)
        val durationStr =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr!!.toLong()
    }

    fun getCurrentAudioDuration(path: String):Long{
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(path)
        val durationStr =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr!!.toLong()
    }

    fun getMillisTimeFormat():Long{
        val dateArray = binding.reminderDate.text.split(".")
        val timeArray = binding.reminderTime.text.split(":")

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MINUTE, timeArray[1].toInt())
        calendar.set(Calendar.HOUR_OF_DAY, timeArray[0].toInt())
        calendar.set(Calendar.DAY_OF_MONTH,dateArray[0].toInt())
        calendar.set(Calendar.MONTH,dateArray[1].toInt()-1)
        calendar.set(Calendar.YEAR,dateArray[2].toInt())

        return calendar.timeInMillis
    }

    fun getStringDateFormat(format:String, timeMillis:Long):String{
        return SimpleDateFormat(format).format(Date(timeMillis))
    }

    fun replaceFragment(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentConteiner,fragment).commit()
    }

    fun generateRequestCode():Int{
        val requestCodesList = ArrayList<Int>()
        var maxRequestCode = -1
        for (reminder in remindersDataList){
            requestCodesList.add(reminder.requestCode)

            if (reminder.requestCode > maxRequestCode){
                maxRequestCode=reminder.requestCode
            }
        }

        for (i in 0 until maxRequestCode){
            if (!requestCodesList.contains(i)){
                return i
            }
        }

        return maxRequestCode+1
    }

    fun getSystemColor(){
        val currentNightMode: Int = requireActivity().resources
            .configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        currentThemeColor = when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> "#FFFFFFFF"
            Configuration.UI_MODE_NIGHT_YES -> "#202020"
            else -> "#FFFFFFFF"
        }
    }

    //-------------------------------Working with a record audio--------------------------------\\

    fun voiceRecord(){
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncodingBitRate(16*44100)
        mediaRecorder.setAudioSamplingRate(44100)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setOutputFile(getRecordingPathName())
        mediaRecorder.prepare()
        mediaRecorder.start()
    }

    fun getRecordingPathName(): String {
        val contextWrapper = ContextWrapper(requireActivity().applicationContext)
        val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC) as File
        val file = File(musicDirectory,getGenerateAudioName())
        temporarilyRecordPath = file.path
        return file.path
    }

    fun getGenerateAudioName():String{
        return "${SimpleDateFormat("dd_MM_yyyy_hh_mm_ss").format(Date())}.mp3"
    }

    fun delateRecord(path:String){
        val contextWrapper = ContextWrapper(requireActivity().applicationContext)
        val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC) as File
        val file = File(musicDirectory,getAudioName(path))
        file.delete()
    }

    fun getAudioName(path: String): String {
        val size = path.length - 23
        return path.substring(size)
    }

    //-----------------------------------Working with a file------------------------------------\\

    fun write(){
        val gson = Gson()
        val jsonFormatStr = gson.toJson(remindersDataList)

        val pref = requireActivity().getSharedPreferences(PublicDatas.PREF_FILE_NAME, AppCompatActivity.MODE_PRIVATE)
        val edit = pref.edit()
        edit.putString("reminderDataList",jsonFormatStr)
        edit.apply()
    }

    fun read(){
        val pref = requireActivity().getSharedPreferences(PublicDatas.PREF_FILE_NAME, AppCompatActivity.MODE_PRIVATE)
        val jsonFormatStr = pref.getString("reminderDataList","empty")

        if (jsonFormatStr!="empty"){
            val gson = Gson()
            remindersDataList = gson.fromJson(jsonFormatStr,object : TypeToken<ArrayList<ReminderData>>() {}.type)
        }
    }

}