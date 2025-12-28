package com.example.doancuoikymobile.data.remote.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Deezer Retrofit Client Builder
 *
 * Deezer PUBLIC API configuration:
 * - Base URL: https://api.deezer.com
 * - No authentication required
 * - Response timeout: 15 seconds (for reliability)
 * - Connection timeout: 10 seconds
 */
object DeezerRetrofitClient {
    
    private const val DEEZER_BASE_URL = "https://api.deezer.com/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(DEEZER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val deezerApiService: DeezerApiService = retrofit.create(DeezerApiService::class.java)
}
