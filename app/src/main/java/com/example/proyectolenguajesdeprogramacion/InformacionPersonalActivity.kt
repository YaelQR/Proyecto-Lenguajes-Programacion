package com.example.proyectolenguajesdeprogramacion

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class InformacionPersonalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infopersonal)


        val btnRegresar = findViewById<ImageButton>(R.id.btnRegresar)
        val tvNombre = findViewById<TextView>(R.id.tvNombre)
        val tvCorreo = findViewById<TextView>(R.id.tvCorreo)
        val tvTel = findViewById<TextView>(R.id.tvTel)

        btnRegresar.setOnClickListener {
            finish()
        }
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val userRef = db.collection("usuarios").document(uid)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: "No disponible"
                    val apellido = document.getString("apellido") ?: ""
                    val telefono = document.getString("telefono") ?: "No disponible"
                    val email = FirebaseAuth.getInstance().currentUser?.email ?: "No disponible"

                    tvNombre.text = "Nombre: $nombre $apellido"
                    tvCorreo.text = "Correo: $email"
                    tvTel.text = "Número: $telefono"
                }
            }.addOnFailureListener {
                tvNombre.text = "Error al cargar datos"
                tvCorreo.text = ""
                tvTel.text = ""
            }
        }
    }
}