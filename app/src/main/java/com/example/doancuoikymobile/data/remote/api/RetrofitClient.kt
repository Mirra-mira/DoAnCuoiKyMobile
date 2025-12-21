package com.example.doancuoikymobile.data.remote.api

import com.example.doancuoikymobile.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.SAAVN_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val songApiService: SongApiService = retrofit.create(SongApiService::class.java)
}