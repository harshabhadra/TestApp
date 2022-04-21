package com.harshabhadra.testapp.network

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

data class EntryResponse(
    @SerializedName("count")
    @Expose
    var count: Int,
    @SerializedName("entries")
    @Expose
    var entries: List<Entry>
)

data class Entry(
    @SerializedName("API")
    @Expose
    var api: String,
    @SerializedName("Description")
    @Expose
    var description: String,
    @SerializedName("Auth")
    @Expose
    var auth: String,
    @SerializedName("HTTPS")
    @Expose
    var https: Boolean,
    @SerializedName("Cors")
    @Expose
    var cors: String,
    @SerializedName("Link")
    @Expose
    var link: String,
    @SerializedName("Category")
    @Expose
    var category: String
)