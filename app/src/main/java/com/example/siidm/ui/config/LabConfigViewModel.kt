package com.example.siidm.ui.config

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siidm.bluetooth.BluetoothManager
import com.example.siidm.data.model.ConfigSensor
import com.example.siidm.data.network.RetrofitClient
import kotlinx.coroutines.launch

sealed class ConfigState {
    object Loading : ConfigState()
    data class ConfigLoaded(val config: ConfigSensor) : ConfigState()
    object Saved : ConfigState()
    data class Error(val message: String) : ConfigState()
}

sealed class BluetoothState {
    data class Connected(val deviceName: String) : BluetoothState()
    object Disconnected : BluetoothState()
    data class Error(val message: String) : BluetoothState()
}

class LabConfigViewModel(
    application: Application,
    private val labId: Int
) : AndroidViewModel(application) {

    private val _configState = MutableLiveData<ConfigState>()
    val configState: LiveData<ConfigState> = _configState

    private val _bluetoothState = MutableLiveData<BluetoothState>(BluetoothState.Disconnected)
    val bluetoothState: LiveData<BluetoothState> = _bluetoothState

    private val btManager = BluetoothManager(application.applicationContext)

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
                    _configState.value = ConfigState.ConfigLoaded(ConfigSensor(labId = labId))
                }
            } catch (e: Exception) {
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
                    if (btManager.isConnected) {
                        btManager.setSensibilidad(config.sensibilidad)
                        btManager.setTiempoEspera(config.tiempoEspera)
                    }
                    _configState.value = ConfigState.Saved
                } else {
                    _configState.value = ConfigState.Error("No se pudo guardar la configuración")
                }
            } catch (e: Exception) {
                _configState.value = ConfigState.Error("Error de red: ${e.message}")
            }
        }
    }

    fun getPairedDevices(): List<BluetoothDevice> = btManager.getPairedDevices()

    fun connectBluetooth(device: BluetoothDevice) {
        viewModelScope.launch {
            val success = btManager.connect(device)
            if (success) {
                val name = try { device.name ?: device.address }
                catch (e: SecurityException) { device.address }
                _bluetoothState.value = BluetoothState.Connected(name)
            } else {
                _bluetoothState.value = BluetoothState.Error(
                    "No se pudo conectar. Asegúrate que el HC-05 esté encendido."
                )
            }
        }
    }

    fun disconnectBluetooth() {
        btManager.closeConnection()
        _bluetoothState.value = BluetoothState.Disconnected
    }

    fun encenderLuces() { viewModelScope.launch { btManager.encenderLuces() } }
    fun apagarLuces()   { viewModelScope.launch { btManager.apagarLuces()   } }

    override fun onCleared() {
        super.onCleared()
        btManager.closeConnection()
    }
}

class LabConfigViewModelFactory(
    private val application: Application,
    private val labId: Int
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LabConfigViewModel(application, labId) as T
    }
}