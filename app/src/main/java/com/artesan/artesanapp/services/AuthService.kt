package com.artesan.artesanapp.services

import android.content.Context
import com.artesan.artesanapp.models.User
import com.artesan.artesanapp.models.UserRole
import com.artesan.artesanapp.storage.StorageManager
import java.util.UUID

class AuthService(context: Context) {

    private val storageManager = StorageManager(context)

    init {
        // Create default admin user if no users exist
        if (storageManager.getUsers().isEmpty()) {
            createDefaultAdmin()
        }
    }

    private fun createDefaultAdmin() {
        val adminUser = User(
            id = UUID.randomUUID().toString(),
            name = "Administrador",
            email = "admin@artesanapp.com",
            username = "admin",
            password = "admin123",
            role = UserRole.ADMIN
        )
        storageManager.saveUser(adminUser)
    }

    fun register(name: String, email: String, username: String, password: String, role: UserRole = UserRole.USER): Result<User> {
        if (name.isBlank()) {
            return Result.failure(Exception("El nombre no puede estar vacío"))
        }

        if (email.isBlank()) {
            return Result.failure(Exception("El email no puede estar vacío"))
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("El email no es válido"))
        }

        if (username.isBlank()) {
            return Result.failure(Exception("El nombre de usuario no puede estar vacío"))
        }

        if (password.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }

        // Check if username already exists
        if (storageManager.usernameExists(username)) {
            return Result.failure(Exception("El nombre de usuario ya existe"))
        }

        // Check if email already exists
        if (storageManager.emailExists(email)) {
            return Result.failure(Exception("El email ya está registrado"))
        }

        val newUser = User(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            username = username,
            password = password,
            role = role
        )

        return if (storageManager.saveUser(newUser)) {
            Result.success(newUser)
        } else {
            Result.failure(Exception("Error al crear el usuario"))
        }
    }

    fun login(username: String, password: String): Result<User> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Usuario y contraseña son requeridos"))
        }

        val user = storageManager.findUserByUsername(username)

        return if (user != null && user.password == password) {
            storageManager.saveCurrentUser(user)
            Result.success(user)
        } else {
            Result.failure(Exception("Usuario o contraseña incorrectos"))
        }
    }

    fun logout() {
        storageManager.saveCurrentUser(null)
    }

    fun getCurrentUser(): User? {
        return storageManager.getCurrentUser()
    }

    fun isLoggedIn(): Boolean {
        return getCurrentUser() != null
    }

    fun isAdmin(): Boolean {
        return getCurrentUser()?.role == UserRole.ADMIN
    }

    fun getAllUsers(): List<User> {
        return storageManager.getUsers()
    }
}
