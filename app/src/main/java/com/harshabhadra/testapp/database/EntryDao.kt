package com.harshabhadra.testapp.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EntryDao {

    @Query("SELECT * FROM entry_table")
    fun getAllEntries():PagingSource<Int,DbEntry>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addService(service: List<DbEntry>)

    @Query("DELETE from entry_table")
    fun clearServiceDb()
}