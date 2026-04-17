package com.example.siidm.ui.config

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.siidm.databinding.ActivityLabConfigBinding
import com.example.siidm.ui.reports.ReportActivity

class LabConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLabConfigBinding
    private lateinit var viewModel: LabConfigViewModel

    private var labId: Int = 0
    private var labNombre: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        labId     = intent.getIntExtra("LAB_ID", 0)
        labNombre = intent.getStringExtra("LAB_NOMBRE") ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = labNombre
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(
            this, LabConfigViewModelFactory(labId)
        )[LabConfigViewModel::class.java]

        setupUI()
        setupObservers()
        setupListeners()
        viewModel.loadConfig()
    }

    private fun setupUI() {
        // SeekBar sensibilidad: 0=Baja, 1=Media, 2=Alta
        binding.seekBarSensibilidad.max      = 2
        binding.seekBarSensibilidad.progress = 1
        updateSensibilidadLabel(1)

        // SeekBar tiempo de espera: 1-60 minutos
        binding.seekBarTiempo.max      = 59
        binding.seekBarTiempo.progress = 9
        updateTiempoLabel(10)

        binding.seekBarSensibilidad.setOnSeekBarChangeListener(
            object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: android.widget.SeekBar?, p: Int, u: Boolean) {
                    updateSensibilidadLabel(p)
                }
                override fun onStartTrackingTouch(sb: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(sb: android.widget.SeekBar?) {}
            }
        )

        binding.seekBarTiempo.setOnSeekBarChangeListener(
            object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: android.widget.SeekBar?, p: Int, u: Boolean) {
                    updateTiempoLabel(p + 1)
                }
                override fun onStartTrackingTouch(sb: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(sb: android.widget.SeekBar?) {}
            }
        )
    }

    private fun setupListeners() {
        binding.btnGuardar.setOnClickListener {
            viewModel.saveConfig(buildConfig())
        }

        binding.btnVerInforme.setOnClickListener {
            startActivity(
                Intent(this, ReportActivity::class.java).apply {
                    putExtra("LAB_ID",     labId)
                    putExtra("LAB_NOMBRE", labNombre)
                }
            )
        }
    }

    private fun setupObservers() {
        viewModel.configState.observe(this) { state ->
            when (state) {
                is ConfigState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is ConfigState.ConfigLoaded -> {
                    binding.progressBar.visibility = View.GONE
                    val c = state.config
                    binding.seekBarSensibilidad.progress = c.sensibilidad - 1
                    binding.seekBarTiempo.progress       = c.tiempoEspera - 1
                    binding.tpHoraInicio.apply {
                        val p = c.horaInicio.split(":")
                        hour   = p[0].toInt()
                        minute = p[1].toInt()
                    }
                    binding.tpHoraFin.apply {
                        val p = c.horaFin.split(":")
                        hour   = p[0].toInt()
                        minute = p[1].toInt()
                    }
                }
                is ConfigState.Saved -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "✓ Configuración guardada", Toast.LENGTH_SHORT).show()
                }
                is ConfigState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun buildConfig() = com.example.siidm.data.model.ConfigSensor(
        labId        = labId,
        sensibilidad = binding.seekBarSensibilidad.progress + 1,
        tiempoEspera = binding.seekBarTiempo.progress + 1,
        horaInicio   = String.format("%02d:%02d",
            binding.tpHoraInicio.hour, binding.tpHoraInicio.minute),
        horaFin      = String.format("%02d:%02d",
            binding.tpHoraFin.hour, binding.tpHoraFin.minute)
    )

    private fun updateSensibilidadLabel(progress: Int) {
        binding.tvSensibilidadValor.text = when (progress) {
            0    -> "Baja"
            1    -> "Media"
            else -> "Alta"
        }
    }

    private fun updateTiempoLabel(minutos: Int) {
        binding.tvTiempoValor.text = "$minutos min"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}