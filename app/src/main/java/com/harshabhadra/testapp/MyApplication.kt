package com.harshabhadra.testapp

import android.app.Application
import android.content.Context
import com.harshabhadra.testapp.network.ApiClient
import com.harshabhadra.testapp.network.ApiService
import com.harshabhadra.testapp.utils.SessionManager

class MyApplication : Application() {

    private lateinit var apiService: ApiService

    override fun onCreate() {
        super.onCreate()

        apiService = ApiClient.instance().create(ApiService::class.java)
    }

    companion object {
        private var sessionManager: SessionManager? = null
        fun session(context: Context): SessionManager {
            if (sessionManager == null)
                sessionManager = SessionManager(context)
            return sessionManager!!
        }
    }
}