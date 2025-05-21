package com.example.proyectolenguajesdeprogramacion.db

import com.example.proyectolenguajesdeprogramacion.entidades.RegistroGasto

class DbGasto {

    constructor() {
    }

    fun getListaGastos(): ArrayList<RegistroGasto> {
        val listaGastos = ArrayList<RegistroGasto>()

        var registro = RegistroGasto(1,501.3f,"4-03-2025", "Entretenimiento")
        listaGastos.add(registro)
        registro = RegistroGasto(2,200f,"10-03-2025", "Salud")
        listaGastos.add(registro)
        registro = RegistroGasto(3,500f,"11-03-2025", "Deporte")
        listaGastos.add(registro)
        registro = RegistroGasto(4,80f,"10-03-2025", "Comida")
        listaGastos.add(registro)

        return listaGastos
    }

}