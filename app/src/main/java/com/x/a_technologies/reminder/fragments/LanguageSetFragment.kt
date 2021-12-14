package com.x.a_technologies.reminder.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.x.a_technologies.reminder.activities.MainActivity
import com.x.a_technologies.reminder.databinding.FragmentLanguageSetBinding
import com.x.a_technologies.reminder.datas.PublicDatas

class LanguageSetFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentLanguageSetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLanguageSetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.english.setOnClickListener {
            setLanguage("en")
        }

        binding.russian.setOnClickListener {
            setLanguage("ru")
        }

        binding.uzbek.setOnClickListener {
            setLanguage("uz")
        }

    }

    fun setLanguage(language:String){
        val pref = requireActivity().getSharedPreferences(PublicDatas.PREF_FILE_NAME, MODE_PRIVATE)
        val edit = pref.edit()
        edit.putString("pref_lang",language)
        edit.apply()

        startActivity(Intent(requireActivity(), MainActivity::class.java))
        requireActivity().finish()
    }
}