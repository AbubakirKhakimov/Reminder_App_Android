package com.x.a_technologies.reminder.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.x.a_technologies.reminder.R
import com.x.a_technologies.reminder.databinding.ActivityIntroBinding
import com.x.a_technologies.reminder.utils.LocaleManager

class IntroActivity : AppCompatActivity() {

    lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.animationView.postDelayed({
            startActivity(Intent(this,MainActivity()::class.java))
            finish()
        },3000)

    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager().setLocale(newBase!!))
    }
}