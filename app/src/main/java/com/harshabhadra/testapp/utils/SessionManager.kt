package com.harshabhadra.testapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.harshabhadra.testapp.R

class SessionManager (context: Context){
    private val pref: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    fun savePrefBool(key: String, value: Boolean) {
        pref.edit().putBoolean(key, value).apply()
    }

    fun getPrefBool(key: String) = pref.getBoolean(key, false)

    companion object{
        const val SERVICE_RUNNING = "service_running"
    }
}