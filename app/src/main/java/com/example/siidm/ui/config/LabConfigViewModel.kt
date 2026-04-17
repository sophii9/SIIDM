package com.example.siidm.ui.config

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siidm.data.model.ConfigSensor
import com.example.siidm.data.network.RetrofitClient
import kotlinx.coroutines.launch

// ── Estados de configuración ───────────────────────────────
sealed class ConfigState {
    object Loading : ConfigState()
    data class ConfigLoaded(val config: ConfigSensor) : ConfigState()
    object Saved : ConfigState()
    data class Error(val message: String) : ConfigState()
}

class LabConfigViewModel(private val labId: Int) : ViewModel() {

    private val _configState = MutableLiveData<ConfigState>()
    val configState: LiveData<ConfigState> = _configState

    fun loadConfig() {
        _configState.value = ConfigState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getConfig(labId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val config = response.body()?.config
                    _configState.value = ConfigState.ConfigLoaded(
                        config ?: ConfigSensor(labId = labId)
                    )
                } else {
                    // Sin configuración previa → cargar valores por defecto
                    _configState.value = ConfigState.ConfigLoaded(ConfigSensor(labId = labId))
                }
            } catch (e: Exception) {
                // Sin conexión → valores por defecto
                _configState.value = ConfigState.ConfigLoaded(ConfigSensor(labId = labId))
            }
        }
    }

    fun saveConfig(config: ConfigSensor) {
        _configState.value = ConfigState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.saveConfig(config)
                if (response.isSuccessful && response.body()?.success == true) {
                    _configState.value = ConfigState.Saved
                } else {
                    _configState.value = ConfigState.Error("No se pudo guardar la configuración")
                }
            } catch (e: Exception) {
                _configState.value = ConfigState.Error("Error de red: ${e.message}")
            }
        }
    }
}

class LabConfigViewModelFactory(
    private val labId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LabConfigViewModel(labId) as T
    }
}