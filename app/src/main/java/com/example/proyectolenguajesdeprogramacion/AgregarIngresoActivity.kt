package com.example.proyectolenguajesdeprogramacion

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import java.util.*

class AgregarIngresoActivity : AppCompatActivity() {
    private var ignorarSeleccion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregaringreso)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val categoriasRef = db.collection("usuarios").document(uid).collection("categorias_ingresos")

        val opcionesBase = mutableListOf<String>()
        val spinner = findViewById<Spinner>(R.id.categoria)

        categoriasRef.get().addOnSuccessListener { snapshot ->
            for (doc in snapshot) {
                val nombre = doc.getString("nombre")
                if (!nombre.isNullOrBlank()) {
                    opcionesBase.add(nombre)
                }
            }

            opcionesBase.add("Agregar nueva categoría...")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcionesBase)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(0)

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (ignorarSeleccion) {
                        ignorarSeleccion = false
                        return
                    }

                    val opcion = parent.getItemAtPosition(position).toString()
                    if (opcion == "Agregar nueva categoría...") {
                        val input = EditText(this@AgregarIngresoActivity)
                        AlertDialog.Builder(this@AgregarIngresoActivity)
                            .setTitle("Nueva categoría")
                            .setView(input)
                            .setPositiveButton("Agregar") { _, _ ->
                                val nuevaCategoria = input.text.toString().trim()
                                if (nuevaCategoria.isNotEmpty() && !opcionesBase.contains(nuevaCategoria)) {
                                    opcionesBase.add(opcionesBase.size - 1, nuevaCategoria)

                                    val nuevoAdapter = ArrayAdapter(
                                        this@AgregarIngresoActivity,
                                        android.R.layout.simple_spinner_item,
                                        opcionesBase
                                    )
                                    nuevoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    spinner.adapter = nuevoAdapter

                                    ignorarSeleccion = true
                                    spinner.setSelection(opcionesBase.indexOf(nuevaCategoria))

                                    guardarCategoriaPersonalizada(nuevaCategoria)
                                } else {
                                    Toast.makeText(applicationContext, "Categoría vacía o duplicada", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        val editTextFecha = findViewById<EditText>(R.id.edittext_date)

        // Establece la fecha actual
        val fechaActual = Calendar.getInstance().time
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        editTextFecha.setText(formato.format(fechaActual))

        editTextFecha.setOnClickListener {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona una fecha")
                .setSelection(calendar.timeInMillis) //
                .build()

            datePicker.show(supportFragmentManager, "selector_fecha")

            datePicker.addOnPositiveButtonClickListener { seleccion ->
                val fechaElegida = formato.format(Date(seleccion))
                editTextFecha.setText(fechaElegida)
            }
        }

        val botonRegresar = findViewById<ImageButton>(R.id.buttonReturn)
        botonRegresar.setOnClickListener {
            finish()
        }

        val btnAgregarIngreso = findViewById<Button>(R.id.AgregarIngresoBtn)
        btnAgregarIngreso.setOnClickListener {
            val editTextIngreso = findViewById<EditText>(R.id.edittext_ingreso)
            val editTextFecha = findViewById<EditText>(R.id.edittext_date)
            val categoriaSeleccionada = spinner.selectedItem.toString()

            val monto = editTextIngreso.text.toString().toDoubleOrNull()
            val fecha = editTextFecha.text.toString()

            if (monto == null || monto <= 0.0) {
                editTextIngreso.error = "Ingresa un monto válido"
                return@setOnClickListener
            }

            if (categoriaSeleccionada == "Agregar nueva categoría...") {
                Toast.makeText(this, "Selecciona una categoría válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uidConfirmado = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            val ingreso = hashMapOf(
                "monto" to monto,
                "fecha" to fecha,
                "categoria" to categoriaSeleccionada
            )

            db.collection("usuarios")
                .document(uidConfirmado)
                .collection("ingresos")
                .add(ingreso)
                .addOnSuccessListener {
                    Toast.makeText(this, "Ingreso guardado con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun guardarCategoriaPersonalizada(categoria: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val nuevaCategoria = hashMapOf("nombre" to categoria)

        db.collection("usuarios")
            .document(uid)
            .collection("categorias_ingresos")
            .add(nuevaCategoria)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}
