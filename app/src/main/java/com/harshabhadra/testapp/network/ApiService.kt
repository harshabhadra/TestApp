package com.harshabhadra.testapp.network

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("entries")
    suspend fun getEntries(): Response<EntryResponse>
}