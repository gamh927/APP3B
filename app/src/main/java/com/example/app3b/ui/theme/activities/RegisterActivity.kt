package com.example.app3b.ui.theme.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app3b.MainActivity
import com.example.app3b.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvBackToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Vincular Vistas
        etName = findViewById(R.id.etRegisterName)
        etEmail = findViewById(R.id.etRegisterEmail)
        etPassword = findViewById(R.id.etRegisterPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressBar = findViewById(R.id.progressBarRegister)
        tvBackToLogin = findViewById(R.id.tvBackToLogin)

        btnRegister.setOnClickListener { registerUser() }
        tvBackToLogin.setOnClickListener { finish() }
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val role = "usuario" // Rol por defecto (puedes cambiarlo a "estudiante", "maestro", etc.)

        if (name.isEmpty()) {
            etName.error = "Ingresa tu nombre completo"
            etName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "El correo es obligatorio"
            etEmail.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            etPassword.requestFocus()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        // 1. Crear el usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        // 2. Guardar datos adicionales en Firestore
                        saveUserDataToFirestore(uid, name, email, role)
                    } else {
                        progressBar.visibility = View.GONE
                        btnRegister.isEnabled = true
                    }
                } else {
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true
                    val errorMsg = task.exception?.localizedMessage ?: "Error al registrar cuenta"
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserDataToFirestore(uid: String, name: String, email: String, role: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "nombre" to name,
            "correo" to email,
            "rol" to role,
            "fechaCreacion" to com.google.firebase.Timestamp.now()
        )

        // Crear un documento en la colección "users" usando el UID como ID del documento
        db.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Registro guardado correctamente", Toast.LENGTH_SHORT).show()

                // Redirigir a MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnRegister.isEnabled = true
                Toast.makeText(this, "Error al guardar perfil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}