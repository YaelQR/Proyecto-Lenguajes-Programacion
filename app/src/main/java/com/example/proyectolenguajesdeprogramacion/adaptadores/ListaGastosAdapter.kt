package com.example.proyectolenguajesdeprogramacion.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView

import com.example.proyectolenguajesdeprogramacion.entidades.RegistroGasto

import com.example.proyectolenguajesdeprogramacion.R


//class ListaGastosAdapter(private val listaDatos: ArrayList<RegistroGasto>): RecyclerView.Adapter<ListaGastosAdapter.ViewHolder>() {
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
//
//        public ViewHolder( itemView: View ) {
//            super(itemView)
//        }
//
//
//    }
//
//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int
//    ): ViewHolder {
//        TODO("Not yet implemented")
//    }
//
//    override fun onBindViewHolder(
//        holder: ViewHolder,
//        position: Int
//    ) {
//        val item = listaDatos[position]
//        holder.textViewTitulo.text = item.titulo
//        holder.textViewDescripcion.text = item.descripcion
//    }
//
//    override fun getItemCount(): Int {
//        return listaDatos.size
//    }
//
//
//}