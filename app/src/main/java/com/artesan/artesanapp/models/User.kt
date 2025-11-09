package com.artesan.artesanapp.models

data class User(
    val id: String,
    val username: String,
    val password: String,
    val role: UserRole
)

enum class UserRole {
    ADMIN,
    USER
}
