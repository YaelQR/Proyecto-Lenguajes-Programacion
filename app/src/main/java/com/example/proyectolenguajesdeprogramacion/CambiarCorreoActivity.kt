package com.example.proyectolenguajesdeprogramacion

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class CambiarCorreoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cambiarcorreo)

        val btnRegresar = findViewById<ImageButton>(R.id.btnRegresar)

        btnRegresar.setOnClickListener {
            finish()
        }
        val etNuevoCorreo = findViewById<EditText>(R.id.etNuevoCorreo)
        val btnEnviarConfirmacion = findViewById<Button>(R.id.btnEnviarConfirmacion)

        btnEnviarConfirmacion.setOnClickListener {
            val nuevoCorreo = etNuevoCorreo.text.toString().trim()
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null && nuevoCorreo.isNotEmpty()) {
                // Enviar enlace de verificación al nuevo correo
                user.verifyBeforeUpdateEmail(nuevoCorreo)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Correo de confirmación enviado a $nuevoCorreo",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Error al enviar correo de confirmación",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
            }
        }
    }
}