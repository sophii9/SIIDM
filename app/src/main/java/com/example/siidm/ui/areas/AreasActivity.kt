package com.example.siidm.ui.areas

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.siidm.R
import com.example.siidm.data.model.Laboratorio
import com.example.siidm.data.network.SessionManager
import com.example.siidm.databinding.ActivityAreasBinding
import com.example.siidm.ui.auth.LoginActivity
import com.example.siidm.ui.config.LabConfigActivity
import com.example.siidm.ui.areas.LaboratorioAdapter

class AreasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAreasBinding

    // Lista estática de laboratorios de Docencia 1
    // En el futuro puedes cargarlos desde la API con getLaboratorios()
    private val laboratorios = listOf(
        Laboratorio(id = 1, nombre = "Laboratorio 1",
            descripcion = "Control de iluminación y sensores de movimiento", activo = true),
        Laboratorio(id = 5, nombre = "Laboratorio 5",
            descripcion = "Control de iluminación y sensores de movimiento", activo = true),
        Laboratorio(id = 3, nombre = "Laboratorio de Redes",
            descripcion = "Control de iluminación y sensores de movimiento", activo = true)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAreasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Docencia 1"
        supportActionBar?.subtitle = "Bienvenido, ${SessionManager.getUsername()}"

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = LaboratorioAdapter(laboratorios) { lab ->
            // Al tocar un laboratorio, ir a su pantalla de configuración
            val intent = Intent(this, LabConfigActivity::class.java).apply {
                putExtra("LAB_ID", lab.id)
                putExtra("LAB_NOMBRE", lab.nombre)
            }
            startActivity(intent)
        }

        binding.rvLaboratorios.apply {
            layoutManager = LinearLayoutManager(this)
            this.adapter = adapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_areas, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                SessionManager.clearSession()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}