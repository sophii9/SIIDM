package com.example.siidm.ui.reports

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.siidm.databinding.ActivityReportBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private lateinit var viewModel: ReportViewModel

    private var labId: Int = 0
    private var labNombre: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        labId = intent.getIntExtra("LAB_ID", 0)
        labNombre = intent.getStringExtra("LAB_NOMBRE") ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Informe — $labNombre"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(
            this, ReportViewModelFactory(labId)
        )[ReportViewModel::class.java]

        setupObservers()

        // Cargar la semana actual
        val fechaInicio = getInicioSemanaActual()
        viewModel.loadReporte(fechaInicio)
    }

    private fun setupObservers() {
        viewModel.reporteState.observe(this) { state ->
            when (state) {
                is ReporteState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutContenido.visibility = View.GONE
                }
                is ReporteState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutContenido.visibility = View.VISIBLE
                    val r = state.reporte

                    binding.tvPeriodo.text = "${r.semanaInicio} al ${r.semanaFin}"
                    binding.tvTotalKwh.text = String.format("%.2f kWh", r.totalKwh)
                    binding.tvTotalHoras.text = String.format("%.1f horas", r.totalHoras)
                    binding.tvCostoTotal.text = String.format("$%.2f MXN", r.costoTotal)

                    setupChart(r.datosDiarios.map { it.kwh.toFloat() })
                }
                is ReporteState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupChart(kwhPorDia: List<Float>) {
        val dias = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

        val entries = kwhPorDia.mapIndexed { index, valor ->
            BarEntry(index.toFloat(), valor)
        }

        val dataSet = BarDataSet(entries, "kWh por día").apply {
            color = getColor(com.example.siidm.R.color.colorPrimary)
            valueTextSize = 12f
        }

        binding.barChart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            setFitBars(true)

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(dias)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }

            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            animateY(800)
            invalidate()
        }
    }

    private fun getInicioSemanaActual(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}