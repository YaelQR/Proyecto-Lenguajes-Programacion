package com.example.proyectolenguajesdeprogramacion

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.text.TextUtils
import android.widget.Toast


class LogininicialActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logininicial)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val button = findViewById<MaterialButton>(R.id.buttonSignUp)

        button.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val button1 = findViewById<Button>(R.id.buttonRecoverPass)
        button1.setOnClickListener {
            val intent = Intent(this, RecuperarPswdActivity::class.java)
            startActivity(intent)
        }

        val buttonLogin = findViewById<Button>(R.id.buttonLogIn)
        buttonLogin.setOnClickListener {




            val intent = Intent(this, InicioActivity::class.java)
            startActivity(intent)
        }

        auth = Firebase.auth

    }

    private fun login(email: String, password: String) {

        Log.d("LoginScreen","email: $email")

        if(!validateData(email,password)){
            Toast.makeText(this, "No se puede iniciar sesión debido a errores en los campos", Toast.LENGTH_SHORT).show()
        }



    }

    private fun validateData(email: String, password: String): Boolean {

        var isValid = true

        if (TextUtils.isEmpty(email)){
            isValid = false
            Toast.makeText(this, "El correo $email no es valido", Toast.LENGTH_SHORT).show()
        }

        if (TextUtils.isEmpty(password)){
            isValid = false
            Toast.makeText(this, "La contraseña $password no es valida", Toast.LENGTH_SHORT).show()
        }

        return isValid

    }


}