package com.example.proyectolenguajesdeprogramacion

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class CambiarContraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cambiarcontra)

        val btnRegresar = findViewById<ImageButton>(R.id.btnRegresar)

        btnRegresar.setOnClickListener {
            finish()
        }

        val btnEnviarCorreo = findViewById<Button>(R.id.btnEnviarCorreo)

        btnEnviarCorreo.setOnClickListener {
            val email = FirebaseAuth.getInstance().currentUser?.email

            if (email != null) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Correo de restablecimiento enviado a $email",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Error al enviar correo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}