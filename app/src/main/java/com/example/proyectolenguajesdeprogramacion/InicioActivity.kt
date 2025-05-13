package com.example.proyectolenguajesdeprogramacion

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InicioActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var linearLayout: LinearLayout
    private var completedTasks = 0
    private val totalTasks = 2
    private var totalIngresos = 0.0
    private var totalGastos = 0.0
    private lateinit var saldoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)

        auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val user = auth.currentUser

        val titulo2 = findViewById<TextView>(R.id.titulo2)
        linearLayout = findViewById(R.id.linearSeguimiento)
        saldoTextView = findViewById(R.id.text_saldo)

        // Obtener datos del usuario
        user?.uid?.let { uid ->
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    val nombre = document.getString("nombre") ?: ""
                    val apellido = document.getString("apellido") ?: ""
                    titulo2.text = if (document.exists()) "Hola, $nombre $apellido" else "Hola, Usuario"
                }
                .addOnFailureListener {
                    titulo2.text = "Hola, Usuario"
                }

            // Limpiar layout
            linearLayout.removeAllViews()

            // Referencias a subcolecciones
            val ingresosRef = db.collection("usuarios").document(uid).collection("ingresos")
            val gastosRef = db.collection("usuarios").document(uid).collection("gastos")

            // Flags
            var hasIngresos = false
            var hasGastos = false
            // Dentro de onCreate(), modifica los listeners:
            ingresosRef.get().addOnSuccessListener { ingresosSnapshot ->
                totalIngresos = 0.0 // Reiniciar acumulador
                if (!ingresosSnapshot.isEmpty) {
                    for (document in ingresosSnapshot) {
                        totalIngresos += document.getDouble("monto") ?: 0.0
                    }
                }
                calcularSaldo()

                hasIngresos = !ingresosSnapshot.isEmpty
                if (hasIngresos) {
                    for (document in ingresosSnapshot) {
                        val textView = crearItemTexto("$${document.getDouble("monto")} - ${document.getString("descripcion")}", Color.GREEN)
                        linearLayout.addView(textView)
                    }
                }
                completedTasks++
                if (completedTasks == totalTasks) checkAndShowEmptyMessage(hasIngresos, hasGastos)
            }

            gastosRef.get().addOnSuccessListener { gastosSnapshot ->
                totalGastos = 0.0 // Reiniciar acumulador
                if (!gastosSnapshot.isEmpty) {
                    for (document in gastosSnapshot) {
                        totalGastos += document.getDouble("monto") ?: 0.0
                    }
                }
                calcularSaldo()

                hasGastos = !gastosSnapshot.isEmpty
                if (hasGastos) {
                    for (document in gastosSnapshot) {
                        val textView = crearItemTexto("$${document.getDouble("monto")} - ${document.getString("descripcion")}", Color.RED)
                        linearLayout.addView(textView)
                    }
                }
                completedTasks++
                if (completedTasks == totalTasks) checkAndShowEmptyMessage(hasIngresos, hasGastos)
            }
        }

        // Botón cerrar sesión
        val btnCerrarSesion = findViewById<MaterialButton>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LogininicialActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val btnGasto = findViewById<MaterialButton>(R.id.GastosBtn)
        btnGasto.setOnClickListener {
            val intent = Intent(this, MenuGastos::class.java)
            startActivity(intent)
        }

        val btnIngreso = findViewById<MaterialButton>(R.id.IngresosBtn)
        btnIngreso.setOnClickListener {
            val intent = Intent(this, MenuIngresos::class.java)
            startActivity(intent)
        }

        // Ajustar padding para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun crearItemTexto(texto: String, color: Int): TextView {
        val textView = TextView(this)
        textView.text = texto
        textView.setTextColor(color)
        textView.textSize = 20f
        textView.setBackgroundResource(R.drawable.circulo)
        textView.gravity = Gravity.END
        textView.setPadding(dpToPx(10), dpToPx(10), dpToPx(48), dpToPx(10))

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, dpToPx(10), 0, dpToPx(10))
        textView.layoutParams = params

        return textView
    }

    private fun checkAndShowEmptyMessage(hasIngresos: Boolean, hasGastos: Boolean) {
        if (!hasIngresos && !hasGastos) {
            val noDataText = TextView(this)
            noDataText.text = "No tienes ingresos ni gastos registrados."
            noDataText.setTextColor(Color.GRAY)
            noDataText.textSize = 20f
            noDataText.gravity = Gravity.CENTER
            noDataText.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.gravity = Gravity.CENTER
            noDataText.layoutParams = params

            linearLayout.addView(noDataText)
        }
    }

    private fun calcularSaldo() {
        val saldo = totalIngresos - totalGastos
        saldoTextView.text = if (saldo == 0.0 && totalIngresos == 0.0 && totalGastos == 0.0) {
            "\$0.00" // Si no hay datos
        } else {
            "\$${"%.2f".format(saldo)}" // Formato con 2 decimales
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            val intent = Intent(this, LogininicialActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
