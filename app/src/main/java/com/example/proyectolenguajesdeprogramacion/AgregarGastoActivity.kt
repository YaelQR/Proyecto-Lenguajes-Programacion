package com.example.proyectolenguajesdeprogramacion

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate

class AgregarGastoActivity : AppCompatActivity() {
    private var ignorarSeleccion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregargasto)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val categoriasRef = db.collection("usuarios").document(uid).collection("categorias_gastos")

        val opcionesBase = mutableListOf<String>()
        val spinner = findViewById<Spinner>(R.id.categoria)

        categoriasRef.get().addOnSuccessListener { snapshot ->
            for (doc in snapshot) {
                val nombre = doc.getString("nombre")
                if (!nombre.isNullOrBlank()) {
                    opcionesBase.add(nombre)
                }
            }
            opcionesBase.add("Agregar nueva categoría...") // ✅ Solo aquí
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcionesBase)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(0)
        }

        val adapter = ArrayAdapter(this, R.layout.spinner_item, opcionesBase)
        adapter.setDropDownViewResource(R.layout.spinner_item)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (ignorarSeleccion) {
                    ignorarSeleccion = false
                    return
                }
                val opcion = parent.getItemAtPosition(position).toString()
                if (opcion == "Agregar nueva categoría...") {
                    val input = EditText(this@AgregarGastoActivity)
                    AlertDialog.Builder(this@AgregarGastoActivity)
                        .setTitle("Nueva categoría")
                        .setView(input)
                        .setPositiveButton("Agregar") { _, _ ->
                            val nuevaCategoria = input.text.toString().trim()

                            if (nuevaCategoria.isNotEmpty()) {
                                opcionesBase.add(opcionesBase.size - 1, nuevaCategoria)

                                val nuevoAdapter = ArrayAdapter(this@AgregarGastoActivity, android.R.layout.simple_spinner_item, opcionesBase)
                                nuevoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinner.adapter = nuevoAdapter

                                ignorarSeleccion = true
                                spinner.setSelection(opcionesBase.indexOf(nuevaCategoria))

                                guardarCategoriaPersonalizada(nuevaCategoria)
                            }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                } else {
                    Toast.makeText(applicationContext, "Seleccionaste: $opcion", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
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


        val btnAgregarGasto = findViewById<Button>(R.id.AgregarGastoBtn)

        btnAgregarGasto.setOnClickListener {
            val editTextIngreso = findViewById<EditText>(R.id.edittext_ingreso)
            val editTextFecha = findViewById<EditText>(R.id.edittext_date)
            val spinner = findViewById<Spinner>(R.id.categoria)

            val monto = editTextIngreso.text.toString().toDoubleOrNull()
            val fecha = editTextFecha.text.toString()
            val categoriaSeleccionada = spinner.selectedItem.toString()

            // Validación
            if (monto == null || monto <= 0.0 || valMonto(monto)) {
                editTextIngreso.error = "Ingresa un monto válido. Debe ser menor a 100,000"
                return@setOnClickListener
            }

            if (categoriaSeleccionada == "Agregar nueva categoría...") {
                Toast.makeText(this, "Selecciona una categoría válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtener UID
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gasto = hashMapOf(
                "monto" to monto,
                "fecha" to fecha,
                "categoria" to categoriaSeleccionada
            )

            // Guardar en Firestore
            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios")
                .document(uid)
                .collection("gastos")
                .add(gasto)
                .addOnSuccessListener {
                    Toast.makeText(this, "Gasto guardado con éxito", Toast.LENGTH_SHORT).show()
                    finish() // Opcional: cerrar pantalla
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            val botonRegresar = findViewById<ImageButton>(R.id.buttonReturn)
            botonRegresar.setOnClickListener {
                finish() // Esto cierra la actividad actual y regresa a la anterior
            }
        }

        val botonRegresar = findViewById<ImageButton>(R.id.buttonReturn)
        botonRegresar.setOnClickListener {
            finish()
        }
    }
    private fun guardarCategoriaPersonalizada(categoria: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val nuevaCategoria = hashMapOf("nombre" to categoria)
        db.collection("usuarios").document(uid)
            .collection("categorias_gastos") // 🔄 corregido aquí
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

    private fun valMonto(monto: Double): Boolean {
        return (monto > 100000)
    }
}