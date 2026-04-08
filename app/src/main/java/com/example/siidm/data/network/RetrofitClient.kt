package com.example.siidm.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.siidm.data.network.SessionManager

object RetrofitClient {

    /**
     * IMPORTANTE: Cambia esta IP por la IP local de tu servidor PHP.
     * Ejemplo: Si tu servidor XAMPP está en la PC con IP 192.168.1.100,
     * la URL base sería "http://192.168.1.100/docencia1/api/"
     *
     * Para encontrar la IP de tu servidor en Windows: ejecuta "ipconfig" en CMD.
     * El celular y el servidor DEBEN estar en la misma red WiFi.
     */
    private const val BASE_URL = "http://192.168.1.100/docencia1/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(provideAuthInterceptor())
        .addInterceptor(loggingInterceptor)
        // Interceptor para agregar token de sesión en cada petición

        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

private fun provideAuthInterceptor() = Interceptor { chain ->
    val token = SessionManager.getToken()
    val request = chain.request().newBuilder().apply {
        token?.let {
            addHeader("Authorization", "Bearer $it")
        }
    }.build()
    chain.proceed(request)
}