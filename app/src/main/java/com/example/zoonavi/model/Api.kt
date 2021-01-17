package com.example.zoonavi.model

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class Api {
    enum class Source {Area, Plant}
    companion object {
        private val AreasUrl = "https://data.taipei/api/v1/dataset/5a0e5fbb-72f8-41c6-908e-2fb25eff9b8a?scope=resourceAquire"
        private val PlantsUrl = "https://data.taipei/api/v1/dataset/f18de02f-b6c9-47c0-8cda-50efad621c14?scope=resourceAquire"
        private val client = OkHttpClient()
    }

    suspend fun sendRequest(source: Source): String? {
        var result: String? = null
        val request = Request.Builder()
            .url(when(source) {
                Source.Area -> AreasUrl
                else -> PlantsUrl
            })
            .build()
        withContext(Dispatchers.IO) {
            client.newCall(request).execute().also {
                if (it.code == 200) {
                    result = it.body?.string()
                    Log.d("test",result)
                }
            }
        }
        return result
    }
}