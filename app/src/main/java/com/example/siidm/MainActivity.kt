package com.example.siidm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.siidm.data.network.SessionManager
import android.app.Application

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }
}

class SIIDM : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar el gestor de sesión con el contexto de la aplicación
        SessionManager.init(this)
    }
}