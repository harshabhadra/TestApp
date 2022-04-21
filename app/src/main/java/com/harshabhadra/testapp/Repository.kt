package com.harshabhadra.testapp

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.harshabhadra.testapp.database.DbEntry
import com.harshabhadra.testapp.database.EntryDatabase
import com.harshabhadra.testapp.network.ApiService
import com.harshabhadra.testapp.network.EntryResponse
import com.harshabhadra.testapp.network.asDbEntries
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

class Repository(
    private val apiService: ApiService,
    private val database: EntryDatabase
) {

    companion object {
        private const val TAG = "Repository"
    }

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    //Get entries from network
    fun refreshEntries() {
        scope.launch {
            try {
                val response = apiService.getEntries()
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.e(TAG,"no. of services: ${it.entries.size}")
                        addDataToDb(it)
                    }
                } else {
                    Log.e(
                        TAG,
                        "entries response unsuccessful"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get entries: ${e.message}")
            }
        }
    }

    //Add entries to db
   private suspend fun addDataToDb(it: EntryResponse) {
        withContext(Dispatchers.IO) {
            database.entryDao.addService(it.entries.asDbEntries())
        }
    }

    //Get entries from Db
    suspend fun getEntries(): Flow<PagingData<DbEntry>> =

        withContext(Dispatchers.Main) {
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    enablePlaceholders = true,
                    maxSize = 1000
                )
            ) {
                database.entryDao.getAllEntries()
            }.flow
        }

}