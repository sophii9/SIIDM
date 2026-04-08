package com.example.siidm.data.model

import com.google.gson.annotations.SerializedName

data class ConsumoElectrico(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("lab_id")
    val labId: Int,

    @SerializedName("fecha")
    val fecha: String,          // formato: "YYYY-MM-DD"

    @SerializedName("kwh")
    val kwh: Double,            // kilovatios-hora consumidos

    @SerializedName("horas_uso")
    val horasUso: Double,       // horas que estuvo activo el sistema

    @SerializedName("costo")
    val costo: Double = 0.0     // costo estimado en moneda local
)

data class ReporteSemanаl(
    @SerializedName("lab_nombre")
    val labNombre: String,

    @SerializedName("semana_inicio")
    val semanaInicio: String,

    @SerializedName("semana_fin")
    val semanaFin: String,

    @SerializedName("total_kwh")
    val totalKwh: Double,

    @SerializedName("total_horas")
    val totalHoras: Double,

    @SerializedName("costo_total")
    val costoTotal: Double,

    @SerializedName("datos_diarios")
    val datosDiarios: List<ConsumoElectrico>
)

data class ReporteResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("reporte")
    val reporte: ReporteSemanаl?
)