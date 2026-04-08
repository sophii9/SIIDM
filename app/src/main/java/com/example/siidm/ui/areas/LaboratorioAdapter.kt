package com.example.siidm.ui.areas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.siidm.data.model.Laboratorio
import com.example.siidm.databinding.ItemLaboratorioBinding

class LaboratorioAdapter(
    private val laboratorios: List<Laboratorio>,
    private val onItemClick: (Laboratorio) -> Unit
) : RecyclerView.Adapter<LaboratorioAdapter.LabViewHolder>() {

    inner class LabViewHolder(private val binding: ItemLaboratorioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(lab: Laboratorio) {
            binding.tvLabNombre.text = lab.nombre
            binding.tvLabDescripcion.text = lab.descripcion

            // Indicador visual de estado activo/inactivo
            binding.viewEstado.setBackgroundResource(
                if (lab.activo) android.R.color.holo_green_light
                else android.R.color.holo_red_light
            )
            binding.tvEstado.text = if (lab.activo) "Activo" else "Inactivo"

            binding.root.setOnClickListener { onItemClick(lab) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabViewHolder {
        val binding = ItemLaboratorioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LabViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LabViewHolder, position: Int) {
        holder.bind(laboratorios[position])
    }

    override fun getItemCount() = laboratorios.size
}