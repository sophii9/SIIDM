package com.example.siidm.data.network

import com.example.siidm.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── AUTENTICACIÓN ──────────────────────────────────────────────
    @POST("auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ── LABORATORIOS ───────────────────────────────────────────────
    @GET("labs/list.php")
    suspend fun getLaboratorios(): Response<List<Laboratorio>>

    // ── CONFIGURACIÓN DE SENSORES ──────────────────────────────────
    @GET("config/get.php")
    suspend fun getConfig(@Query("lab_id") labId: Int): Response<ConfigResponse>

    @POST("config/save.php")
    suspend fun saveConfig(@Body config: ConfigSensor): Response<ConfigResponse>

    // ── CONSUMO ELÉCTRICO ──────────────────────────────────────────
    @GET("reports/weekly.php")
    suspend fun getReporteSemanal(
        @Query("lab_id") labId: Int,
        @Query("fecha_inicio") fechaInicio: String
    ): Response<ReporteResponse>

    // ── REGISTRO DE EVENTOS DEL SENSOR ────────────────────────────
    @POST("sensor/event.php")
    suspend fun registrarEvento(
        @Body evento: EventoRequest
    ): Response<Map<String, Any>>
}