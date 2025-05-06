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
import android.widget.EditText
import android.widget.Toast


class LogininicialActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logininicial)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var textAreaEmail = findViewById<EditText>(R.id.textEmail)
        var textAreaPassword = findViewById<EditText>(R.id.pswdRegister)

        val buttonReg = findViewById<MaterialButton>(R.id.buttonRegistro)

        buttonReg.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val buttonRec = findViewById<Button>(R.id.buttonRecoverPass)

        buttonRec.setOnClickListener {
            val intent = Intent(this, RecuperarPswdActivity::class.java)
            startActivity(intent)
        }

        val buttonLogin = findViewById<Button>(R.id.buttonLogIn)
        buttonLogin.setOnClickListener {

            login(textAreaEmail.text.toString(), textAreaPassword.text.toString())
            Log.i("LoginScreen", "El usuario actual es: "+auth.currentUser.toString())
            Log.i("LoginScreen", "El id es ${auth.currentUser?.uid.toString()}")
            verificarUsuario()
        }


    }

    override fun onStart() {
        super.onStart()

        verificarUsuario()
    }

    private fun verificarUsuario(){
        if(auth.currentUser != null){
            Log.i("LoginScreen", "La sesión esta iniciada. Se accede a la app")
            Log.i("LoginScreen", "El usuario actual es: "+ auth.currentUser?.email.toString())

            val intent = Intent(this, InicioActivity::class.java)
            startActivity(intent)
        }else{
            Log.i("LoginScreen", "No hay sesión iniciada")
            Log.i("LoginScreen", "El usuario actual es: "+ auth.currentUser?.email.toString())
        }
    }

    private fun login(email: String, password: String) {

        Log.d("LoginScreen","email: $email")

        if(!validateData(email,password)){
            Toast.makeText(this, "No se puede iniciar sesión debido a errores en los campos", Toast.LENGTH_SHORT).show()
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LoginScreen", "signInWithEmail:success")
                    val user = auth.currentUser
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LoginScreen", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    //updateUI(null)
                }
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