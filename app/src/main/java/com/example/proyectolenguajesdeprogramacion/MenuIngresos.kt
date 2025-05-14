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

class MenuIngresos : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        cargarIngresos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_ingresos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonAgregar = findViewById<Button>(R.id.AgregarIngresoBtn)
        buttonAgregar.setOnClickListener {
            val intent = Intent(this, AgregarIngresoActivity::class.java)
            startActivity(intent)
        }

        val botonRegresar = findViewById<ImageButton>(R.id.buttonReturn)
        botonRegresar.setOnClickListener {
            finish()
        }

    }

    private fun cargarIngresos() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val tabla = findViewById<TableLayout>(R.id.tableLayout)

        while (tabla.childCount > 1) {
            tabla.removeViewAt(1)
        }

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        db.collection("usuarios").document(uid).collection("ingresos")
            .get()
            .addOnSuccessListener { snapshot ->
                val listaOrdenada = snapshot.documents.sortedByDescending {
                    val fechaStr = it.getString("fecha")
                    try {
                        formatoFecha.parse(fechaStr)
                    } catch (e: Exception) {
                        Date(0)
                    }
                }

                for (document in listaOrdenada) {
                    val monto = document.getDouble("monto") ?: 0.0
                    val categoria = document.getString("categoria") ?: "Sin categoría"
                    val fecha = document.getString("fecha") ?: "Fecha desconocida"

                    // Crear nueva fila
                    val fila = TableRow(this).apply {
                        val params = TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(0, 0, 0, dpToPx(12)) // margen inferior entre filas
                        layoutParams = params
                    }

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
                Toast.makeText(this, "Error al cargar ingresos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

}
