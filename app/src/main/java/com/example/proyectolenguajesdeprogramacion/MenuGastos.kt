package com.example.proyectolenguajesdeprogramacion

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Color
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import androidx.core.content.edit


class MenuGastos : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        val ordenGuardado = getSharedPreferences("config", MODE_PRIVATE)
            .getString("orden_gastos", "fecha_desc") ?: "fecha_desc"
        cargarGastos(ordenGuardado)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_gastos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonAgregar = findViewById<Button>(R.id.AgregarGastoBtn)
        buttonAgregar.setOnClickListener {
            val intent = Intent(this, AgregarGastoActivity::class.java)
            startActivity(intent)
        }

        val botonRegresar = findViewById<ImageButton>(R.id.buttonReturn)
        botonRegresar.setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.button_sort).setOnClickListener {
            val opciones = arrayOf(
                "Monto (menor a mayor)",
                "Monto (mayor a menor)",
                "Fecha (más reciente)",
                "Fecha (más antigua)",
                "Categoría (A-Z)"
            )
            val valoresOrden = arrayOf(
                "monto_asc",
                "monto_desc",
                "fecha_desc",
                "fecha_asc",
                "categoria_asc"
            )

            AlertDialog.Builder(this)
                .setTitle("Ordenar por")
                .setItems(opciones) { _, which ->
                    val orden = valoresOrden[which]

                    val prefs = getSharedPreferences("config", MODE_PRIVATE)
                    prefs.edit() { putString("orden_gastos", orden) }

                    cargarGastos(orden)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun cargarGastos(orden: String = "fecha_desc") {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val tabla = findViewById<TableLayout>(R.id.tableLayout)

        for (i in tabla.childCount - 1 downTo 0) {
            tabla.removeViewAt(i)
        }

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        db.collection("usuarios").document(uid).collection("gastos")
            .get()
            .addOnSuccessListener { snapshot ->
                val listaOrdenada = when (orden) {
                    "monto_asc" -> snapshot.documents.sortedBy { it.getDouble("monto") ?: 0.0 }
                    "monto_desc" -> snapshot.documents.sortedByDescending { it.getDouble("monto") ?: 0.0 }
                    "fecha_asc" -> snapshot.documents.sortedBy {
                        try {
                            formatoFecha.parse(it.getString("fecha") ?: "") ?: Date(0)
                        } catch (e: Exception) {
                            Date(0)
                        }
                    }

                    "fecha_desc" -> snapshot.documents.sortedByDescending {
                        try {
                            formatoFecha.parse(it.getString("fecha") ?: "") ?: Date(0)
                        } catch (e: Exception) {
                            Date(0)
                        }
                    }

                    "categoria_asc" -> snapshot.documents.sortedBy { it.getString("categoria") ?: "" }
                    else -> snapshot.documents
                }

                for (document in listaOrdenada) {
                    val monto = document.getDouble("monto") ?: continue
                    val categoria = document.getString("categoria") ?: continue
                    val fecha = document.getString("fecha") ?: continue
                    val gastoId = document.id.toString()

                    Log.d("GastoDebug", "Monto: $monto - Fecha: $fecha - Categoria: $categoria")

                    // Crear nueva fila
                    val fila = TableRow(this).apply {
                        val params = TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(0, 0, 0, dpToPx(12)) // margen inferior entre filas
                        layoutParams = params
                    }

                    fila.isClickable = true

                    // Crear cada celda
                    val tvMonto = TextView(this).apply {
                        text = "$${"%.2f".format(monto)}"
                        gravity = Gravity.CENTER
                        textSize = 14f
                        setTextColor(Color.BLACK)
                    }

                    val tvFecha = TextView(this).apply {
                        text = fecha
                        gravity = Gravity.CENTER
                        textSize = 14f
                        setTextColor(Color.BLACK)
                    }

                    val tvCategoria = TextView(this).apply {
                        text = categoria
                        gravity = Gravity.CENTER
                        textSize = 14f
                        setTextColor(Color.BLACK)
                    }

                    // Agregar celdas a la fila
                    fila.addView(tvMonto)
                    fila.addView(tvFecha)
                    fila.addView(tvCategoria)

                    // Agregar fila a la tabla
                    tabla.addView(fila)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar gastos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}