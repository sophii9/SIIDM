package com.example.siidm.ui.reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siidm.data.model.ReporteSemanаl
import com.example.siidm.data.network.RetrofitClient
import kotlinx.coroutines.launch

sealed class ReporteState {
    object Loading : ReporteState()
    data class Success(val reporte: ReporteSemanаl) : ReporteState()
    data class Error(val message: String) : ReporteState()
}

class ReportViewModel(private val labId: Int) : ViewModel() {

    private val _reporteState = MutableLiveData<ReporteState>()
    val reporteState: LiveData<ReporteState> = _reporteState

    fun loadReporte(fechaInicio: String) {
        _reporteState.value = ReporteState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getReporteSemanal(labId, fechaInicio)
                if (response.isSuccessful && response.body()?.success == true) {
                    val reporte = response.body()?.reporte
                    if (reporte != null) {
                        _reporteState.value = ReporteState.Success(reporte)
                    } else {
                        _reporteState.value = ReporteState.Error("Sin datos para esta semana")
                    }
                } else {
                    _reporteState.value = ReporteState.Error("Error al cargar el informe")
                }
            } catch (e: Exception) {
                _reporteState.value = ReporteState.Error(
                    "No se pudo obtener el informe. Verifica la conexión."
                )
            }
        }
    }
}

class ReportViewModelFactory(private val labId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ReportViewModel(labId) as T
    }
}