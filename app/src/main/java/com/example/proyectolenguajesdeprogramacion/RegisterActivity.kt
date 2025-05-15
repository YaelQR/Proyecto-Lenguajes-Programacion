package com.example.proyectolenguajesdeprogramacion

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtNombre: EditText
    private lateinit var txtApellido: EditText
    private lateinit var txtTelefono: EditText



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa los EditText
        txtEmail = findViewById(R.id.emailRegister)
        txtPassword = findViewById(R.id.pswdRegister)
        txtNombre = findViewById(R.id.txtNombre)
        txtApellido = findViewById(R.id.txtApellido)
        txtTelefono = findViewById(R.id.txtTelefono)

        val buttonRegistro = findViewById<MaterialButton>(R.id.buttonRegistro)
        buttonRegistro.setOnClickListener {
            val email = txtEmail.text.toString()
            val password = txtPassword.text.toString()
            val nombre = txtNombre.text.toString()
            val apellido = txtApellido.text.toString()
            val telefono = txtTelefono.text.toString()

            if (validateRegisterData(email, password, nombre, apellido, telefono)) {
                performRegister(email, password, nombre, apellido, telefono)
            }
        }
    }

    private fun performRegister(email: String, password: String, nombre: String, apellido: String, telefono: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val uid = user?.uid ?: return@addOnCompleteListener

                val db = FirebaseFirestore.getInstance()
                val userMap = hashMapOf(
                    "email" to email,
                    "nombre" to nombre,
                    "apellido" to apellido,
                    "telefono" to telefono,
                    "uid" to uid
                )

                db.collection("usuarios").document(uid)
                    .set(userMap)
                    .addOnSuccessListener {
                        guardarCategoriasPorDefecto(uid) // ✅ aquí

                        Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, InicioActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al guardar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

            } else {
                Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun validateRegisterData(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        telefono: String
    ): Boolean {

        // Validar email
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar contraseña
        if (TextUtils.isEmpty(password) || password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar nombre (no vacío y sin espacios)
        if (TextUtils.isEmpty(nombre) || nombre.contains(" ")) {
            Toast.makeText(this, "El nombre no debe estar vacío ni contener espacios", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar apellido (no vacío y sin espacios)
        if (TextUtils.isEmpty(apellido) || apellido.contains(" ")) {
            Toast.makeText(this, "El apellido no debe estar vacío ni contener espacios", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar teléfono (10 dígitos)
        if (!telefono.matches(Regex("^\\d{10}$"))) {
            Toast.makeText(this, "El teléfono debe tener exactamente 10 dígitos numéricos", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}


