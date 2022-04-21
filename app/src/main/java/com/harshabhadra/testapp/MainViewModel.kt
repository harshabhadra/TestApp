package com.harshabhadra.testapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.harshabhadra.testapp.database.DbEntry
import kotlinx.coroutines.flow.Flow

class MainViewModel(
    private val repository: Repository,
) : ViewModel() {

    init {
        repository.refreshEntries()
    }

    suspend fun getEntries(): Flow<PagingData<DbEntry>> {
        return repository.getEntries().cachedIn(viewModelScope)
    }
}
