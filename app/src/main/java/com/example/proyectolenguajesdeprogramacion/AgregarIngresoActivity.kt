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
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AgregarIngresoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregaringreso)

        val spinner = findViewById<Spinner>(R.id.categoria)

        // Cargar las opciones desde strings.xml
        val opciones = resources.getStringArray(R.array.categorias_ingresos)

        // Crear un adaptador para el Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)

        // Especificar el layout para el menú desplegable
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Asignar el adaptador al Spinner
        spinner.adapter = adapter

        // Para detectar qué opción se selecciona
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val opcionSeleccionada = parent.getItemAtPosition(position).toString()
                Toast.makeText(applicationContext, "Seleccionaste: $opcionSeleccionada", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Acción si no se selecciona nada
            }
        }

        val editTextFecha = findViewById<EditText>(R.id.edittext_date)

        // Establece la fecha actual por defecto
        val fechaActual = Calendar.getInstance().time
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formato.timeZone = TimeZone.getTimeZone("UTC")
        editTextFecha.setText(formato.format(fechaActual))

        // Abre el selector al hacer clic
        editTextFecha.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona una fecha")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(supportFragmentManager, "selector_fecha")

            datePicker.addOnPositiveButtonClickListener { seleccion ->
                val fechaElegida = formato.format(Date(seleccion))
                editTextFecha.setText(fechaElegida)
            }
        }


        val btnAgregarGasto = findViewById<Button>(R.id.AgregarIngresoBtn)

        btnAgregarGasto.setOnClickListener {
            val editTextIngreso = findViewById<EditText>(R.id.edittext_ingreso)
            val editTextFecha = findViewById<EditText>(R.id.edittext_date)
            val spinner = findViewById<Spinner>(R.id.categoria)

            val monto = editTextIngreso.text.toString().toDoubleOrNull()
            val fecha = editTextFecha.text.toString()
            val categoriaSeleccionada = spinner.selectedItem.toString()

            // Validación
            if (monto == null || monto <= 0.0) {
                editTextIngreso.error = "Ingresa un monto válido"
                return@setOnClickListener
            }

            // Obtener UID
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ingerso = hashMapOf(
                "monto" to monto,
                "fecha" to fecha,
                "categoria" to categoriaSeleccionada
            )

            // Guardar en Firestore
            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios")
                .document(uid)
                .collection("ingresos")
                .add(ingerso)
                .addOnSuccessListener {
                    Toast.makeText(this, "Ingreso guardado con éxito", Toast.LENGTH_SHORT).show()
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
    }
}