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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InicioActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var linearLayout: LinearLayout
    private var completedTasks = 0
    private val totalTasks = 2
    private var totalIngresos = 0.0
    private var totalGastos = 0.0
    private lateinit var saldoTextView: TextView

    data class Movimiento(
        val tipo: String,
        val monto: Double,
        val fecha: Date,
        val categoria: String
    )

    val movimientos = mutableListOf<Movimiento>()

    override fun onResume() {
        super.onResume()
        movimientos.clear()
        linearLayout.removeAllViews()
        completedTasks = 0
        cargarMovimientos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)
        auth = FirebaseAuth.getInstance()
        linearLayout = findViewById(R.id.linearSeguimiento)
        saldoTextView = findViewById(R.id.text_saldo)


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

        val btnEstado = findViewById<MaterialButton>(R.id.EstadoBtn)
        btnEstado.setOnClickListener {
            val intent = Intent(this, MenuEstado::class.java)
            startActivity(intent)
        }

        // Ajustar padding para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun cargarMovimientos() {
        val db = FirebaseFirestore.getInstance()
        val user = auth.currentUser

        val titulo2 = findViewById<TextView>(R.id.titulo2)
        linearLayout = findViewById(R.id.linearSeguimiento)
        saldoTextView = findViewById(R.id.text_saldo)
        var hasIngresos = false
        var hasGastos = false




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

            val refIngresos = db.collection("usuarios").document(uid).collection("categorias_ingresos")
            val refGastos = db.collection("usuarios").document(uid).collection("categorias_gastos")

            refIngresos.get().addOnCompleteListener { ingresosTask ->
                refGastos.get().addOnCompleteListener { gastosTask ->
                    val ingresosVacios = ingresosTask.isSuccessful && (ingresosTask.result?.isEmpty ?: true)
                    val gastosVacios = gastosTask.isSuccessful && (gastosTask.result?.isEmpty ?: true)

                    if (ingresosVacios || gastosVacios) {
                        guardarCategoriasPorDefecto(uid)
                    }
                }
            }
            // Limpiar layout
            linearLayout.removeAllViews()

            // Referencias a subcolecciones
            val ingresosRef = db.collection("usuarios").document(uid).collection("ingresos")
            val gastosRef = db.collection("usuarios").document(uid).collection("gastos")

            ingresosRef.get().addOnSuccessListener { ingresosSnapshot ->
                totalIngresos = 0.0
                if (!ingresosSnapshot.isEmpty) {
                    hasIngresos = true
                    // agregar movimientos...
                }
                val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                for (document in ingresosSnapshot) {
                    val fechaStr = document.getString("fecha") ?: continue
                    val fecha = try { formatoFecha.parse(fechaStr) } catch (e: Exception) { Date(0) }
                    val monto = document.getDouble("monto") ?: 0.0
                    val categoria = document.getString("categoria") ?: "Sin categoría"

                    totalIngresos += monto
                    movimientos.add(Movimiento("ingreso", monto, fecha, categoria))
                }

                calcularSaldo()
                completedTasks++
                if (completedTasks == totalTasks) mostrarMovimientos()
            }

            gastosRef.get().addOnSuccessListener { gastosSnapshot ->
                totalGastos = 0.0
                if (!gastosSnapshot.isEmpty) {
                    hasGastos = true
                    // agregar movimientos...
                }
                val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                for (document in gastosSnapshot) {
                    val fechaStr = document.getString("fecha") ?: continue
                    val fecha = try { formatoFecha.parse(fechaStr) } catch (e: Exception) { Date(0) }
                    val monto = document.getDouble("monto") ?: 0.0
                    val categoria = document.getString("categoria") ?: "Sin categoría"

                    totalGastos += monto
                    movimientos.add(Movimiento("gasto", monto, fecha, categoria))
                }

                calcularSaldo()
                completedTasks++
                if (completedTasks == totalTasks) mostrarMovimientos()
            }
        }
    }

    private fun mostrarMovimientos() {
        linearLayout.removeAllViews()

        if (movimientos.isEmpty()) {
            checkAndShowEmptyMessage(false, false)
            return
        }

        val listaOrdenada = movimientos.sortedByDescending { it.fecha }

        var fechaAnterior: String? = null
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        for (mov in listaOrdenada) {
            val fechaActual = formatoFecha.format(mov.fecha)

            if (fechaActual != fechaAnterior) {
                // Crear TextView de encabezado de fecha
                val fechaTextView = TextView(this).apply {
                    text = "📅 $fechaActual"
                    setTextColor(Color.DKGRAY)
                    textSize = 16f
                    setPadding(dpToPx(10), dpToPx(20), dpToPx(10), dpToPx(10))
                }
                linearLayout.addView(fechaTextView)
                fechaAnterior = fechaActual
            }

            // Agregar el movimiento como lo haces ahora
            val color = if (mov.tipo == "ingreso") Color.GREEN else Color.RED
            val texto = "${mov.categoria} - \$${"%.2f".format(mov.monto)}"
            val textView = crearItemTexto(texto, color)
            linearLayout.addView(textView)
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

    private fun guardarCategoriasPorDefecto(uid: String) {
        val db = FirebaseFirestore.getInstance()
        val categoriasGastos = listOf("Transporte", "Comida", "Servicios", "Entretenimiento")
        val categoriasIngresos = listOf("Salario", "Ventas", "Rentas", "Regalos", "Reembolsos", "Premios")

        val gastosRef = db.collection("usuarios").document(uid).collection("categorias_gastos")
        val ingresosRef = db.collection("usuarios").document(uid).collection("categorias_ingresos")

        categoriasGastos.forEach { nombre ->
            gastosRef.add(mapOf("nombre" to nombre))
        }
        categoriasIngresos.forEach { nombre ->
            ingresosRef.add(mapOf("nombre" to nombre))
        }
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
