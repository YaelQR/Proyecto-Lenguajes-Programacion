package com.example.proyectolenguajesdeprogramacion

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class PerfilActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val btnRegresar = findViewById<ImageButton>(R.id.bntRegresar)
        val btnCambiarCorreo = findViewById<MaterialButton>(R.id.CambiarCorreoBtn)
        val btnCambiarContra = findViewById<MaterialButton>(R.id.CambiarContraBtn)
        val btnInfoPersonal = findViewById<MaterialButton>(R.id.InfoPersonalBtn)

        btnCambiarCorreo.setOnClickListener {
            val intent = Intent(this, CambiarCorreoActivity::class.java)
            startActivity(intent)
        }

        btnCambiarContra.setOnClickListener {
            val intent = Intent(this, CambiarContraActivity::class.java)
            startActivity(intent)
        }

        btnInfoPersonal.setOnClickListener {
            val intent = Intent(this, InformacionPersonalActivity::class.java)
            startActivity(intent)
        }

        btnRegresar.setOnClickListener {
            finish()
        }

        val btnCerrarSesion = findViewById<MaterialButton>(R.id.cerrarsesionBtn)
        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cerrarSesion(){
        auth = FirebaseAuth.getInstance()
        auth.signOut()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LogininicialActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}