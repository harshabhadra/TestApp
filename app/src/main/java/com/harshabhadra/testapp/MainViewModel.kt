package com.harshabhadra.testapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harshabhadra.testapp.network.ApiService
import com.harshabhadra.testapp.network.Entry
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

enum class Loading {
    LOADING, COMPLETED
}

sealed class Result {
    data class Success(val entries: List<Entry>) : Result()
    object Failure : Result()
}

class MainViewModel(private val apiService: ApiService) : ViewModel() {

    private var _entriesMutableLiveData = MutableLiveData<Result>()
    val entriesLiveData: LiveData<Result> get() = _entriesMutableLiveData

    private var _loadingMutableLiveData = MutableLiveData<Loading>()
    val loadingLiveData: LiveData<Loading> get() = _loadingMutableLiveData

    companion object {
        private const val TAG = "MainViewModel"
    }

    fun getEntries() {
        viewModelScope.launch {
            _loadingMutableLiveData.value = Loading.LOADING
            try {
                val response = apiService.getEntries()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _entriesMutableLiveData.value = Result.Success(it.entries)
                    } ?: let {
                        _entriesMutableLiveData.value = Result.Failure
                    }
                } else {
                    Log.e(TAG, "entries response unsuccessful: ${response.errorBody()?.string()}")
                    _entriesMutableLiveData.value = Result.Failure
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get entries: ${e.message}")
                _entriesMutableLiveData.value = Result.Failure
            } finally {
                _loadingMutableLiveData.value = Loading.COMPLETED
            }
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}