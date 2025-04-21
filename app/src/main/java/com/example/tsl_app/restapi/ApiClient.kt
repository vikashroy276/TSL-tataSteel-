package com.example.tsl_app.restapi

import android.content.Context
import com.example.tsl_app.utils.CacheUtils
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private var BASE_URL = ""
    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null

    fun refreshRetrofit() {
        apiService = null
        retrofit = null
    }

    fun getClient(context: Context): ApiService? {
        val token = CacheUtils.getString(context, "AUTH_TOKEN")
        val httpClient = UnsafeOkHttpClient.createUnsafeOkHttpClient(token)
        if (retrofit == null) {
            BASE_URL = getBaseURL(context)

            val gson = GsonBuilder()
                .setLenient()
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl("$BASE_URL/api/User/")
//                .baseUrl("https://tsl-api.maxworth.in/api/User/")
//                .baseUrl("http://192.168.2.8:8192/api/User/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .build()

            apiService = retrofit!!.create(ApiService::class.java)
        }
        return apiService
    }

    private fun getBaseURL(context: Context): String {
        val storedUrl = CacheUtils.getBASEURL(context)
        return if (storedUrl.isNullOrEmpty()) {
            "http://" // Provide a default URL if needed
        } else {
            storedUrl
        }
    }
}
