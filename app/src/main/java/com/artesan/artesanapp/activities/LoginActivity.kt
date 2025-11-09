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
import com.artesan.artesanapp.services.AuthService
import com.google.android.material.appbar.MaterialToolbar

class LoginActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        authService = AuthService(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        editTextUsername = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        setSupportActionBar(toolbar)

        supportActionBar?.let {
            it.title = "Iniciar sesión"
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        buttonLogin.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val username = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString()

        authService.login(username, password)
            .onSuccess { user ->
                Toast.makeText(this, "Bienvenido ${user.username}", Toast.LENGTH_SHORT).show()

                // Navigate to HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
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