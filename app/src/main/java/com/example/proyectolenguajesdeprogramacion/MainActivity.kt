package com.example.proyectolenguajesdeprogramacion

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val button = findViewById<Button>(R.id.button3)
        button.setOnClickListener {
            val intent = Intent(this, LogininicialActivity::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        Toast.makeText(this, "Se verifico si el usuario estaba logueado. El usuario es: $currentUser", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "El usuario es: $currentUser", Toast.LENGTH_SHORT).show()
        if (currentUser != null) {
            reload()
        }
    }

    fun reload() {
        Toast.makeText(this, "Usuario no inicio sesión", Toast.LENGTH_SHORT).show()
    }

}