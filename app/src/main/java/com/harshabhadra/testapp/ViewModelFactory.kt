package com.harshabhadra.testapp

import android.app.Application
import android.content.Context
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.harshabhadra.testapp.network.ApiService

class ViewModelFactory(
    context: SavedStateRegistryOwner,
    private val apiService: ApiService? = null,
) :
    AbstractSavedStateViewModelFactory(context, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(apiService!!) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}