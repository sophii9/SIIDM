package com.example.siidm.ui.areas

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.siidm.databinding.ActivityLabConfigBinding
import com.example.siidm.ui.reports.ReportActivity

class LabConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLabConfigBinding
    private lateinit var viewModel: LabConfigViewModel

    private var labId: Int = 0
    private var labNombre: String = ""

    // Launcher para solicitar permisos de Bluetooth en Android 12+
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            showBluetoothDevices()
        } else {
            Toast.makeText(this, "Se necesitan permisos de Bluetooth", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        labId = intent.getIntExtra("LAB_ID", 0)
        labNombre = intent.getStringExtra("LAB_NOMBRE") ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = labNombre
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(
            this, LabConfigViewModelFactory(this, labId)
        )[LabConfigViewModel::class.java]

        setupUI()
        setupObservers()
        setupListeners()

        viewModel.loadConfig()
    }

    private fun setupUI() {
        // SeekBar de sensibilidad (1-3)
        binding.seekBarSensibilidad.max = 2       // 0→Baja, 1→Media, 2→Alta
        binding.seekBarSensibilidad.progress = 1  // Default: Media

        // SeekBar de tiempo de espera (1-60 minutos)
        binding.seekBarTiempo.max = 59
        binding.seekBarTiempo.progress = 9        // Default: 10 min

        updateSensibilidadLabel(1)
        updateTiempoLabel(10)

        binding.seekBarSensibilidad.setOnSeekBarChangeListener(
            object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    updateSensibilidadLabel(progress)
                }
                override fun onStartTrackingTouch(sb: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(sb: android.widget.SeekBar?) {}
            }
        )

        binding.seekBarTiempo.setOnSeekBarChangeListener(
            object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    updateTiempoLabel(progress + 1)
                }
                override fun onStartTrackingTouch(sb: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(sb: android.widget.SeekBar?) {}
            }
        )
    }

    private fun setupListeners() {
        // Guardar configuración en servidor y enviar al Arduino
        binding.btnGuardar.setOnClickListener {
            val config = buildConfig()
            viewModel.saveConfig(config)
        }

        // Conectar al Arduino por Bluetooth
        binding.btnConectarBluetooth.setOnClickListener {
            requestBluetoothPermissions()
        }

        // Encender luces manualmente
        binding.btnEncender.setOnClickListener {
            viewModel.encenderLuces()
        }

        // Apagar luces manualmente
        binding.btnApagar.setOnClickListener {
            viewModel.apagarLuces()
        }

        // Ver informe semanal
        binding.btnVerInforme.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java).apply {
                putExtra("LAB_ID", labId)
                putExtra("LAB_NOMBRE", labNombre)
            }
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        viewModel.configState.observe(this) { state ->
            when (state) {
                is ConfigState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is ConfigState.ConfigLoaded -> {
                    binding.progressBar.visibility = View.GONE
                    val config = state.config
                    binding.seekBarSensibilidad.progress = config.sensibilidad - 1
                    binding.seekBarTiempo.progress = config.tiempoEspera - 1
                    binding.tpHoraInicio.apply {
                        val parts = config.horaInicio.split(":")
                        hour = parts[0].toInt()
                        minute = parts[1].toInt()
                    }
                    binding.tpHoraFin.apply {
                        val parts = config.horaFin.split(":")
                        hour = parts[0].toInt()
                        minute = parts[1].toInt()
                    }
                }
                is ConfigState.Saved -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Configuración guardada ✓", Toast.LENGTH_SHORT).show()
                }
                is ConfigState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.bluetoothState.observe(this) { state ->
            when (state) {
                is BluetoothState.Connected -> {
                    binding.tvBluetoothStatus.text = "BT: Conectado a ${state.deviceName}"
                    binding.btnEncender.isEnabled = true
                    binding.btnApagar.isEnabled = true
                }
                is BluetoothState.Disconnected -> {
                    binding.tvBluetoothStatus.text = "BT: No conectado"
                    binding.btnEncender.isEnabled = false
                    binding.btnApagar.isEnabled = false
                }
                is BluetoothState.Error -> {
                    binding.tvBluetoothStatus.text = "BT: Error de conexión"
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun buildConfig() = com.example.siidm.data.model.ConfigSensor(
        labId = labId,
        sensibilidad = binding.seekBarSensibilidad.progress + 1,
        tiempoEspera = binding.seekBarTiempo.progress + 1,
        horaInicio = String.format("%02d:%02d",
            binding.tpHoraInicio.hour, binding.tpHoraInicio.minute),
        horaFin = String.format("%02d:%02d",
            binding.tpHoraFin.hour, binding.tpHoraFin.minute)
    )

    private fun updateSensibilidadLabel(progress: Int) {
        binding.tvSensibilidadValor.text = when (progress) {
            0 -> "Baja"
            1 -> "Media"
            2 -> "Alta"
            else -> "Media"
        }
    }

    private fun updateTiempoLabel(minutos: Int) {
        binding.tvTiempoValor.text = "$minutos min"
    }

    private fun requestBluetoothPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            showBluetoothDevices()
        }
    }

    private fun showBluetoothDevices() {
        val devices = viewModel.getPairedDevices()
        if (devices.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Sin dispositivos")
                .setMessage("No hay dispositivos Bluetooth emparejados. Ve a Configuración → Bluetooth de tu teléfono y empareja el módulo HC-05 primero.")
                .setPositiveButton("Entendido", null)
                .show()
            return
        }

        val nombres = devices.map {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED) it.name ?: it.address
            else it.address
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Selecciona el módulo HC-05")
            .setItems(nombres) { _, index ->
                viewModel.connectBluetooth(devices[index])
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disconnectBluetooth()
    }
}