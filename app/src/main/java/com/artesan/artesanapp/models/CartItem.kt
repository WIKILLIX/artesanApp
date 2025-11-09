package com.artesan.artesanapp.models

data class CartItem(
    val product: Product,
    var quantity: Int
) {
    fun getTotalPrice(): Double = product.price * quantity
}
