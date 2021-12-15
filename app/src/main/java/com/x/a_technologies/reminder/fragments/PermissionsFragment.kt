package com.x.a_technologies.reminder.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.x.a_technologies.reminder.R
import com.x.a_technologies.reminder.databinding.FragmentPermissionsBinding

class PermissionsFragment : Fragment() {

    lateinit var binding: FragmentPermissionsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPermissionsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissions()

        binding.voiceAllow.setOnClickListener {
            getMicrophonePermission()
            binding.voiceAllow.setBackgroundColor(Color.GRAY)
        }

        binding.overlayAllow.setOnClickListener {
            getTopWindowsPermission()
            binding.overlayAllow.setBackgroundColor(Color.GRAY)
        }

        binding.appSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + requireActivity().packageName)
            startActivity(intent)
        }

        binding.next.setOnClickListener {
            replaceFragment(MainFragment())
        }

    }

    fun replaceFragment(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentConteiner,fragment).commit()
    }

    fun checkPermissions(){
        if(ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_DENIED) {
            binding.voiceAllow.setBackgroundColor(Color.GRAY)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(requireActivity())) {
                binding.overlayAllow.setBackgroundColor(Color.GRAY)
            }
        }else{
            binding.overlayAllow.setBackgroundColor(Color.GRAY)
        }
    }

    fun getMicrophonePermission() {
        if(ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO),1)
        }
    }

    fun getTopWindowsPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(requireActivity())) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${requireActivity().packageName}")
                )
                startActivity(intent)
            }
        }
    }
}