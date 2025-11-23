package com.artesan.artesanapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.artesan.artesanapp.R
import com.artesan.artesanapp.models.UserRole
import com.artesan.artesanapp.services.AuthService
import com.google.android.material.appbar.MaterialToolbar

class RegisterActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var editTextFullName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        authService = AuthService(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        editTextFullName = findViewById(R.id.editTextFullName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonRegister = findViewById(R.id.buttonRegister)

        setSupportActionBar(toolbar)

        supportActionBar?.let {
            it.title = "Crear cuenta"
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        buttonRegister.setOnClickListener {
            performRegister()
        }
    }

    private fun performRegister() {
        val fullName = editTextFullName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString()

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese su nombre completo", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese su email", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate username from email (part before @)
        val username = email.substringBefore("@")

        authService.register(fullName, email, username, password, UserRole.USER)
            .onSuccess { user ->
                Toast.makeText(this, "Registro exitoso. Inicie sesión", Toast.LENGTH_SHORT).show()
                finish()
            }
            .onFailure { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}