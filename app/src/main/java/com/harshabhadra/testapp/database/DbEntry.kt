package com.harshabhadra.testapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entry_table")
data class DbEntry(
    @PrimaryKey(autoGenerate = true)
    val id:Long? = System.currentTimeMillis(),
    var api: String,
    var description: String,
    var auth: String,
    var https: Boolean,
    var cors: String,
    var link: String,
    var category: String
)