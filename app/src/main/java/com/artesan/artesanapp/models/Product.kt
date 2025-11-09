package com.artesan.artesanapp.models

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    var stock: Int,
    val imageBase64: String? = null
)
