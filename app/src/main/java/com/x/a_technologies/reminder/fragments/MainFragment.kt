package com.x.a_technologies.reminder.fragments

import android.Manifest
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.x.a_technologies.reminder.R
import com.x.a_technologies.reminder.utils.ReminderSetManager
import com.x.a_technologies.reminder.adapters.RemindersAdapter
import com.x.a_technologies.reminder.adapters.RemindersAdapterCallBack
import com.x.a_technologies.reminder.databinding.FragmentMainBinding
import com.x.a_technologies.reminder.datas.PublicDatas
import com.x.a_technologies.reminder.datas.ReminderData
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainFragment : Fragment(),RemindersAdapterCallBack {

    lateinit var binding: FragmentMainBinding
    var remindersDataList = ArrayList<ReminderData>()
    lateinit var adapter:RemindersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.add.setOnClickListener {
            if (getMicrophonePermission()) {
                replaceFragment(AddReminderFragment())
            }else{
                if (adapter.mediaPlayer.isPlaying){
                    adapter.mediaPlayer.stop()
                }
                Toast.makeText(requireActivity(), getString(R.string.allowMicrophone), Toast.LENGTH_SHORT).show()
            }
        }

        binding.setLanguage.setOnClickListener {
            if (adapter.mediaPlayer.isPlaying){
                adapter.mediaPlayer.stop()
            }
            val fragment = LanguageSetFragment()
            fragment.show(requireActivity().supportFragmentManager, fragment.tag)
        }

    }

    //-------------------------------Working with a life circle--------------------------------\\

    override fun onStart() {
        super.onStart()
        read()
        adapter = RemindersAdapter(remindersDataList,this)
        binding.rvReminders.adapter = adapter
    }

    override fun onStop() {
        super.onStop()
        if (adapter.mediaPlayer.isPlaying){
            adapter.mediaPlayer.stop()
        }
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

    //----------------------------------------Permissions----------------------------------------\\

    fun getMicrophonePermission(): Boolean {
        if(ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO),1)
            return false
        }else{
            return true
        }
    }

    //---------------------------------Implementation functions---------------------------------\\

    override fun delateReminder(position: Int) {
        if (remindersDataList[position].reminderActive){
            ReminderSetManager().cancelAlarm(requireActivity(),remindersDataList[position])
        }
        delateRecord(remindersDataList[position].reminderAudioPath)
        remindersDataList.removeAt(position)
        write()
    }

    override fun editReminder(position: Int) {
        PublicDatas.position = position
        replaceFragment(AddReminderFragment())
    }

    override fun setReminder(position:Int) {
        if (Date().after(Date(remindersDataList[position].reminderTimeMillis))){
            if (adapter.mediaPlayer.isPlaying){
                adapter.mediaPlayer.stop()
            }
            PublicDatas.position = position
            PublicDatas.reminderSwitchOnClick = true
            replaceFragment(AddReminderFragment())
        }else{
            ReminderSetManager().setAlarm(requireActivity(),remindersDataList[position])
            remindersDataList[position].reminderActive = true
            write()
        }
    }

    override fun cancelReminder(position: Int) {
        remindersDataList[position].reminderActive = false
        ReminderSetManager().cancelAlarm(requireActivity(),remindersDataList[position])
        write()
    }

    //-------------------------------Working with a delete audio--------------------------------\\

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

    fun replaceFragment(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentConteiner,fragment).commit()
    }

}