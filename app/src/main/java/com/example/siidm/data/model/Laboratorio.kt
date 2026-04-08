package com.example.siidm.data.model

import com.google.gson.annotations.SerializedName

data class Laboratorio(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("activo")
    val activo: Boolean = false
)

data class ConfigSensor(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("lab_id")
    val labId: Int,

    // Tiempo en minutos que el sensor espera sin movimiento antes de apagar
    @SerializedName("tiempo_espera")
    val tiempoEspera: Int = 10,

    // Sensibilidad del sensor PIR: 1=Baja, 2=Media, 3=Alta
    @SerializedName("sensibilidad")
    val sensibilidad: Int = 2,

    // Horario de operación
    @SerializedName("hora_inicio")
    val horaInicio: String = "07:00",

    @SerializedName("hora_fin")
    val horaFin: String = "21:00",

    // Días activos: lista de días (0=Dom, 1=Lun, ... 6=Sáb)
    @SerializedName("dias_activos")
    val diasActivos: List<Int> = listOf(1, 2, 3, 4, 5)
)

data class ConfigResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("config")
    val config: ConfigSensor?
)