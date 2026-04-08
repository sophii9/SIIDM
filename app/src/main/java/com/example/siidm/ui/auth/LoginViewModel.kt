package com.example.siidm.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.siidm.data.model.LoginRequest
import com.example.siidm.data.network.RetrofitClient
import com.example.siidm.data.network.SessionManager
import kotlinx.coroutines.launch

// Estados posibles del login
sealed class LoginState {
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(username: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.login(
                    LoginRequest(username = username, password = password)
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.user != null) {
                        // Guardar sesión localmente
                        SessionManager.saveSession(body.user)
                        _loginState.value = LoginState.Success
                    } else {
                        _loginState.value = LoginState.Error(
                            body?.message ?: "Usuario o contraseña incorrectos"
                        )
                    }
                } else {
                    _loginState.value = LoginState.Error(
                        "Error del servidor: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(
                    "No se pudo conectar al servidor. Verifica que estés en la red WiFi del edificio."
                )
            }
        }
    }
}