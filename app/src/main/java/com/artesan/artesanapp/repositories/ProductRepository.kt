package com.artesan.artesanapp.repositories

import android.content.Context
import com.artesan.artesanapp.models.Product
import com.artesan.artesanapp.storage.StorageManager
import com.artesan.artesanapp.utils.ImageHelper
import java.util.UUID

class ProductRepository(context: Context) {

    private val storageManager = StorageManager(context)
    private val context = context

    fun create(product: Product): Result<Product> {
        if (product.name.isBlank()) {
            return Result.failure(Exception("El nombre del producto es requerido"))
        }

        if (product.price <= 0) {
            return Result.failure(Exception("El precio debe ser mayor a 0"))
        }

        if (product.stock < 0) {
            return Result.failure(Exception("El stock no puede ser negativo"))
        }

        val newProduct = product.copy(id = UUID.randomUUID().toString())

        return if (storageManager.saveProduct(newProduct)) {
            Result.success(newProduct)
        } else {
            Result.failure(Exception("Error al crear el producto"))
        }
    }

    fun getAll(): List<Product> {
        return storageManager.getProducts()
    }

    fun getById(id: String): Product? {
        return storageManager.getProductById(id)
    }

    fun update(product: Product): Result<Product> {
        if (product.name.isBlank()) {
            return Result.failure(Exception("El nombre del producto es requerido"))
        }

        if (product.price <= 0) {
            return Result.failure(Exception("El precio debe ser mayor a 0"))
        }

        if (product.stock < 0) {
            return Result.failure(Exception("El stock no puede ser negativo"))
        }

        return if (storageManager.updateProduct(product)) {
            Result.success(product)
        } else {
            Result.failure(Exception("Producto no encontrado"))
        }
    }

    fun delete(id: String): Result<Boolean> {
        return if (storageManager.deleteProduct(id)) {
            Result.success(true)
        } else {
            Result.failure(Exception("Producto no encontrado"))
        }
    }

    fun updateStock(productId: String, newStock: Int): Result<Product> {
        val product = getById(productId) ?: return Result.failure(Exception("Producto no encontrado"))
        product.stock = newStock
        return update(product)
    }

    fun decreaseStock(productId: String, quantity: Int): Result<Product> {
        val product = getById(productId) ?: return Result.failure(Exception("Producto no encontrado"))

        if (product.stock < quantity) {
            return Result.failure(Exception("Stock insuficiente"))
        }

        product.stock -= quantity
        return update(product)
    }
}
