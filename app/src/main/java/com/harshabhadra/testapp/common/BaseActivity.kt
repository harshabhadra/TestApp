package com.harshabhadra.testapp.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.harshabhadra.testapp.network.ApiClient
import com.harshabhadra.testapp.network.ApiService

open class BaseActivity : AppCompatActivity() {

    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        apiService = ApiClient.instance().create(ApiService::class.java)
    }
}