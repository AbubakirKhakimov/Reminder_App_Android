package com.x.a_technologies.reminder.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.os.Build
import com.x.a_technologies.reminder.datas.PublicDatas
import java.util.*

class LocaleManager {

    fun setLocale(mContext: Context): Context? {
        if (getLanguagePref(mContext) == "empty") {
            return mContext
        } else {
            return updateResources(mContext, getLanguagePref(mContext))
        }
    }

    fun getLanguagePref(mContext: Context?): String {
        val pref = mContext!!.getSharedPreferences(PublicDatas.PREF_FILE_NAME, MODE_PRIVATE)
        return pref.getString("pref_lang", "empty")!!
    }

    private fun updateResources(context: Context, language: String): Context? {
        var context = context
        val locale = Locale(language)
        Locale.setDefault(locale)
        val res = context.resources

        val config = Configuration(res.configuration)
        config.setLocale(locale)
        context = context.createConfigurationContext(config)
        res.updateConfiguration(config, res.displayMetrics)
        return context
    }

}