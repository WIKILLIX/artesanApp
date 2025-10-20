package com.artesan.artesanapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.artesan.artesanapp.R

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        var btn_login = findViewById<Button>(R.id.buttonLogin)

        btn_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        var btn_register = findViewById<Button>(R.id.buttonRegister)

        btn_register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}