package com.artesan.artesanapp.services

import android.content.Context
import com.artesan.artesanapp.models.CartItem
import com.artesan.artesanapp.models.Product
import com.artesan.artesanapp.storage.StorageManager

class CartService(context: Context) {

    private val storageManager = StorageManager(context)

    fun addToCart(product: Product, quantity: Int = 1): Result<CartItem> {
        if (quantity <= 0) {
            return Result.failure(Exception("La cantidad debe ser mayor a 0"))
        }

        if (product.stock < quantity) {
            return Result.failure(Exception("Stock insuficiente. Disponible: ${product.stock}"))
        }

        val existing = storageManager.getCartItemByProductId(product.id)

        return if (existing != null) {
            val newQuantity = existing.second + quantity
            if (product.stock < newQuantity) {
                Result.failure(Exception("Stock insuficiente. Disponible: ${product.stock}"))
            } else {
                if (storageManager.updateCartQuantity(product.id, newQuantity)) {
                    Result.success(CartItem(product, newQuantity))
                } else {
                    Result.failure(Exception("Error al actualizar el carrito"))
                }
            }
        } else {
            if (storageManager.addToCart(product.id, quantity)) {
                Result.success(CartItem(product, quantity))
            } else {
                Result.failure(Exception("Error al agregar al carrito"))
            }
        }
    }

    fun removeFromCart(productId: String): Result<Boolean> {
        return if (storageManager.removeFromCart(productId)) {
            Result.success(true)
        } else {
            Result.failure(Exception("Producto no encontrado en el carrito"))
        }
    }

    fun updateQuantity(productId: String, quantity: Int): Result<CartItem> {
        if (quantity <= 0) {
            return removeFromCart(productId).map { CartItem(Product("", "", "", 0.0, 0), 0) }
        }

        // Get product to check stock
        val product = storageManager.getProductById(productId)
            ?: return Result.failure(Exception("Producto no encontrado"))

        if (product.stock < quantity) {
            return Result.failure(Exception("Stock insuficiente. Disponible: ${product.stock}"))
        }

        return if (storageManager.updateCartQuantity(productId, quantity)) {
            Result.success(CartItem(product, quantity))
        } else {
            Result.failure(Exception("Producto no encontrado en el carrito"))
        }
    }

    fun getCart(): List<CartItem> {
        return storageManager.getCart()
    }

    fun getCartTotal(): Double {
        return storageManager.getCart().sumOf { it.getTotalPrice() }
    }

    fun getCartItemCount(): Int {
        return storageManager.getCartItemCount()
    }

    fun clearCart() {
        storageManager.clearCart()
    }

    fun checkout(): Result<Boolean> {
        val cart = storageManager.getCart()

        if (cart.isEmpty()) {
            return Result.failure(Exception("El carrito está vacío"))
        }

        // Verify stock availability for all items
        for (item in cart) {
            if (item.product.stock < item.quantity) {
                return Result.failure(Exception("Stock insuficiente para ${item.product.name}"))
            }
        }

        // In a real app, here you would:
        // 1. Process payment
        // 2. Update product stock
        // 3. Create order record
        // 4. Send confirmation

        clearCart()
        return Result.success(true)
    }
}
