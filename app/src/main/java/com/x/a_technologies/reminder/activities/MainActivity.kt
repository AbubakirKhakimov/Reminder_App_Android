package com.x.a_technologies.reminder.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.x.a_technologies.reminder.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment
import com.x.a_technologies.reminder.R
import com.x.a_technologies.reminder.fragments.MainFragment
import com.x.a_technologies.reminder.fragments.PermissionsFragment
import com.x.a_technologies.reminder.utils.LocaleManager

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkPermissions()){
            replaceFragment(MainFragment())
        }else{
            replaceFragment(PermissionsFragment())
        }

    }

    fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.fragmentConteiner,fragment).commit()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager().setLocale(newBase!!))
    }

    fun checkPermissions(): Boolean {
        var audio = false
        var overlay = false

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_DENIED) {
            audio = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                overlay = true
            }
        }else{
            overlay = true
        }

        return audio && overlay
    }

}