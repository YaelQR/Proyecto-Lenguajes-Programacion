package com.example.proyectolenguajesdeprogramacion.entidades

class RegistroGasto {

    private var id: Int
        get() {
            return id
        }
        set(newId) {
            id = newId
        }
    private var monto: Float
        get() {
            return monto
        }
        set(newMonto) {
            monto = newMonto
        }

    private var fecha: String
        get() {
            return fecha
        }
        set(newFecha) {
            fecha = newFecha
        }

    private var categoria: String
        get() {
            return categoria
        }
        set(newCategoria) {
            categoria = newCategoria
        }

    constructor(id: Int, monto: Float, fecha: String, categoria: String) {
        this.id = id;
        this.monto = monto;
        this.fecha = fecha;
        this.categoria = categoria;
    }





}