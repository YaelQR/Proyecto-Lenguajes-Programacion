package com.example.proyectolenguajesdeprogramacion

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.Color
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.button.MaterialButton
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import java.util.Date
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.TimeZone


class MenuEstado : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart

    private lateinit var ingresosBtn: MaterialButton
    private lateinit var gastosBtn: MaterialButton
    private lateinit var gananciasBtn: MaterialButton
    private lateinit var fecha1EditText: TextView
    private lateinit var fecha2EditText: TextView
    private lateinit var btnActualizar: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var totalIngresos = 0.0
    private var totalGastos = 0.0
    private val ingresosPorCategoria = mutableMapOf<String, Double>()
    private val gastosPorCategoria = mutableMapOf<String, Double>()

    private var fechaMin: Date? = null
    private var fechaMax: Date? = null
    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formatoUTC = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private var tipoGraficoSeleccionado: String = "ganancia"


    override fun onResume() {
        super.onResume()

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_estado)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val botonRegresar = findViewById<ImageButton>(R.id.buttonReturn)
        botonRegresar.setOnClickListener {
            finish()
        }

        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)
        ingresosBtn = findViewById(R.id.ingresosBtn)
        gastosBtn = findViewById(R.id.gastosBtn)
        gananciasBtn = findViewById(R.id.gananciaBtn)

        ingresosBtn.setOnClickListener { mostrarGraficoPorCategoria("ingreso") }
        gastosBtn.setOnClickListener { mostrarGraficoPorCategoria("gasto") }
        gananciasBtn.setOnClickListener { mostrarGraficoGanancias() }

        fecha1EditText = findViewById(R.id.fecha1)
        fecha2EditText = findViewById(R.id.fecha2)
        btnActualizar = findViewById(R.id.btnActualizar)

        val btnReiniciar = findViewById<Button>(R.id.btnReiniciar)
        btnReiniciar.setOnClickListener {
            tipoGraficoSeleccionado = "ganancia"
            actualizarBotones()
            cargarDatosDeFirestore()
        }

        ingresosBtn.setOnClickListener {
            tipoGraficoSeleccionado = "ingreso"
            actualizarBotones()
            mostrarGraficoPorCategoria("ingreso")
        }

        gastosBtn.setOnClickListener {
            tipoGraficoSeleccionado = "gasto"
            actualizarBotones()
            mostrarGraficoPorCategoria("gasto")
        }

        gananciasBtn.setOnClickListener {
            tipoGraficoSeleccionado = "ganancia"
            actualizarBotones()
            mostrarGraficoGanancias()
        }

        btnActualizar.setOnClickListener {
            cargarDatosDeFirestoreConFiltro()
        }

        actualizarBotones()
        cargarDatosDeFirestore()

        fecha1EditText.setOnClickListener {
            if (fechaMin == null || fechaMax == null) {
                Toast.makeText(this, "Carga primero los datos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val constraints = CalendarConstraints.Builder()
                .setValidator(
                    CompositeDateValidator.allOf(
                        listOf(
                            DateValidatorPointForward.from(fechaMin!!.time),
                            DateValidatorPointBackward.before(fechaMax!!.time)
                        )
                    )
                )
                .build()

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona fecha de inicio")
                .setSelection(fechaMin!!.time)
                .setCalendarConstraints(constraints)
                .build()

            picker.show(supportFragmentManager, "fecha_inicio")

            picker.addOnPositiveButtonClickListener { millis ->
                val fecha = formatoUTC.format(Date(millis))
                fecha1EditText.text = fecha
            }
        }

        fecha2EditText.setOnClickListener {
            if (fechaMin == null || fechaMax == null) {
                Toast.makeText(this, "Carga primero los datos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val constraints = CalendarConstraints.Builder()
                .setValidator(
                    CompositeDateValidator.allOf(
                        listOf(
                            DateValidatorPointForward.from(fechaMin!!.time),
                            DateValidatorPointBackward.before(fechaMax!!.time)
                        )
                    )
                )
                .build()


            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona fecha de fin")
                .setSelection(fechaMax!!.time)
                .setCalendarConstraints(constraints)
                .build()

            picker.show(supportFragmentManager, "fecha_fin")

            picker.addOnPositiveButtonClickListener { millis ->
                val fecha = formatoUTC.format(Date(millis))
                fecha2EditText.text = fecha
            }
        }

    }

    private fun actualizarFechasYGraficos() {
        fecha1EditText.setText(fechaMin?.let { formatoFecha.format(it) } ?: "")
        fecha2EditText.setText(fechaMax?.let { formatoFecha.format(it) } ?: "")
        mostrarGraficoGanancias()
    }

    private fun cargarDatosDeFirestore() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        val ingresosRef = db.collection("usuarios").document(uid).collection("ingresos")
        val gastosRef = db.collection("usuarios").document(uid).collection("gastos")

        // Limpiar datos anteriores
        totalIngresos = 0.0
        totalGastos = 0.0
        ingresosPorCategoria.clear()
        gastosPorCategoria.clear()
        fechaMin = null
        fechaMax = null

        var tareasTerminadas = 0
        val totalTareas = 2

        ingresosRef.get().addOnSuccessListener { ingresosSnapshot ->
            for (document in ingresosSnapshot) {
                val monto = document.getDouble("monto") ?: 0.0
                val categoria = document.getString("categoria") ?: "Sin categoría"
                totalIngresos += monto
                ingresosPorCategoria[categoria] =
                    ingresosPorCategoria.getOrDefault(categoria, 0.0) + monto

                val fechaStr = document.getString("fecha") ?: continue
                val fecha = try {
                    formatoFecha.parse(fechaStr)
                } catch (e: Exception) {
                    null
                } ?: continue

                if (fechaMin == null || fecha.before(fechaMin)) fechaMin = fecha
                if (fechaMax == null || fecha.after(fechaMax)) fechaMax = fecha
            }

            tareasTerminadas++
            if (tareasTerminadas == totalTareas) {
                actualizarFechasYGraficos()
            }
        }

        gastosRef.get().addOnSuccessListener { gastosSnapshot ->
            for (document in gastosSnapshot) {
                val monto = document.getDouble("monto") ?: 0.0
                val categoria = document.getString("categoria") ?: "Sin categoría"
                totalGastos += monto
                gastosPorCategoria[categoria] =
                    gastosPorCategoria.getOrDefault(categoria, 0.0) + monto

                val fechaStr = document.getString("fecha") ?: continue
                val fecha = try {
                    formatoFecha.parse(fechaStr)
                } catch (e: Exception) {
                    null
                } ?: continue

                if (fechaMin == null || fecha.before(fechaMin)) fechaMin = fecha
                if (fechaMax == null || fecha.after(fechaMax)) fechaMax = fecha
            }

            tareasTerminadas++
            if (tareasTerminadas == totalTareas) {
                actualizarFechasYGraficos()
            }
        }
    }

    private fun actualizarBotones() {
        val colorActivo = Color.parseColor("#2567E8") // Fondo azul
        val textoBlanco = Color.WHITE
        val colorNormal = Color.WHITE
        val textoNegro = Color.BLACK
        val colorBorde = Color.BLACK

        fun MaterialButton.aplicarSeleccionado(seleccionado: Boolean) {
            setBackgroundColor(if (seleccionado) colorActivo else colorNormal)
            setTextColor(if (seleccionado) textoBlanco else textoNegro)
            strokeColor = android.content.res.ColorStateList.valueOf(colorBorde)
            strokeWidth = 2
        }


        gananciasBtn.aplicarSeleccionado(tipoGraficoSeleccionado == "ganancia")
        ingresosBtn.aplicarSeleccionado(tipoGraficoSeleccionado == "ingreso")
        gastosBtn.aplicarSeleccionado(tipoGraficoSeleccionado == "gasto")
    }


    private fun cargarDatosDeFirestoreConFiltro() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        val fechaInicioStr = fecha1EditText.text.toString().trim()
        val fechaFinStr = fecha2EditText.text.toString().trim()

        // Validar que ambos campos tengan texto
        if (fechaInicioStr.isEmpty() || fechaFinStr.isEmpty()) {
            Toast.makeText(this, "Ambas fechas deben estar llenas", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar el formato
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formato.isLenient = false // Validación estricta

        val fechaInicio = try {
            formato.parse(fechaInicioStr)
        } catch (e: Exception) {
            null
        }

        val fechaFin = try {
            formato.parse(fechaFinStr)
        } catch (e: Exception) {
            null
        }

        if (fechaInicio == null || fechaFin == null) {
            Toast.makeText(this, "Formato de fecha inválido. Usa dd/MM/yyyy", Toast.LENGTH_SHORT).show()
            return
        }

        if (fechaFin.before(fechaInicio)) {
            Toast.makeText(this, "La fecha de fin no puede ser anterior a la de inicio", Toast.LENGTH_SHORT).show()
            return
        }

        // Limpieza de datos anteriores
        totalIngresos = 0.0
        totalGastos = 0.0
        ingresosPorCategoria.clear()
        gastosPorCategoria.clear()

        val ingresosRef = db.collection("usuarios").document(uid).collection("ingresos")
        val gastosRef = db.collection("usuarios").document(uid).collection("gastos")

        var datosEncontrados = false
        var tareasCompletadas = 0

        ingresosRef.get().addOnSuccessListener { ingresosSnapshot ->
            for (document in ingresosSnapshot) {
                val fechaDoc = document.getString("fecha") ?: continue
                val fecha = try { formato.parse(fechaDoc) } catch (e: Exception) { null } ?: continue

                if (fecha in fechaInicio..fechaFin) {
                    val monto = document.getDouble("monto") ?: 0.0
                    val categoria = document.getString("categoria") ?: "Sin categoría"

                    totalIngresos += monto
                    ingresosPorCategoria[categoria] = ingresosPorCategoria.getOrDefault(categoria, 0.0) + monto
                    datosEncontrados = true
                }
            }

            tareasCompletadas++
            if (tareasCompletadas == 2) mostrarSiHayDatos(datosEncontrados)
        }

        gastosRef.get().addOnSuccessListener { gastosSnapshot ->
            for (document in gastosSnapshot) {
                val fechaDoc = document.getString("fecha") ?: continue
                val fecha = try { formato.parse(fechaDoc) } catch (e: Exception) { null } ?: continue

                if (fecha in fechaInicio..fechaFin) {
                    val monto = document.getDouble("monto") ?: 0.0
                    val categoria = document.getString("categoria") ?: "Sin categoría"

                    totalGastos += monto
                    gastosPorCategoria[categoria] = gastosPorCategoria.getOrDefault(categoria, 0.0) + monto
                    datosEncontrados = true
                }
            }

            tareasCompletadas++
            if (tareasCompletadas == 2) mostrarSiHayDatos(datosEncontrados)
        }
    }

    private fun mostrarSiHayDatos(hayDatos: Boolean) {
        if (!hayDatos) {
            Toast.makeText(this, "No se encontraron registros en ese periodo", Toast.LENGTH_LONG).show()
            return
        }

        when (tipoGraficoSeleccionado) {
            "ganancia" -> mostrarGraficoGanancias()
            "ingreso" -> mostrarGraficoPorCategoria("ingreso")
            "gasto" -> mostrarGraficoPorCategoria("gasto")
        }
    }




    private fun mostrarGraficoGanancias() {
        barChart.visibility = View.GONE
        pieChart.visibility = View.VISIBLE
        val entries = listOf(
            PieEntry(totalGastos.toFloat(), "Gastos"),
            PieEntry(totalIngresos.toFloat(), "Ingresos")
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(Color.RED, Color.GREEN)
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK

        val data = PieData(dataSet)

        pieChart.data = data
        pieChart.setDrawHoleEnabled(true)
        pieChart.setHoleRadius(60f)
        pieChart.setHoleColor(Color.WHITE)
        val gananciaNeta = totalIngresos - totalGastos
        val textoCentro = "$${"%.0f".format(kotlin.math.abs(gananciaNeta))}"

        pieChart.centerText = textoCentro
        pieChart.setCenterTextSize(20f)
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD)

        if (gananciaNeta >= 0) {
            pieChart.setCenterTextColor(Color.parseColor("#006400")) // Verde oscuro
        } else {
            pieChart.setCenterTextColor(Color.RED)
        }
        pieChart.description.isEnabled = false
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun mostrarGraficoPorCategoria(tipo: String) {
        val mapa = if (tipo == "ingreso") ingresosPorCategoria else gastosPorCategoria

        pieChart.visibility = View.GONE
        barChart.visibility = View.VISIBLE
        barChart.clear()

        val entries = mapa.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }
        val labels = mapa.keys.toList()

        val dataSet = BarDataSet(entries, "Categorías de $tipo")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f
        barChart.data = barData

        val legend = barChart.legend
        legend.isWordWrapEnabled = true
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.textSize = 12f

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -45f
        xAxis.labelCount = labels.size

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.invalidate()
        barChart.animateY(1000)

        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e is BarEntry) {
                    val index = e.x.toInt()
                    val categoria = labels.getOrNull(index) ?: "Desconocido"
                    val monto = e.y

                    Toast.makeText(
                        this@MenuEstado,
                        "Categoría: $categoria\nMonto: $${"%.2f".format(monto)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onNothingSelected() {}
        })
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