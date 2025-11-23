package com.artesan.artesanapp.storage

import android.content.Context
import com.artesan.artesanapp.database.*
import com.artesan.artesanapp.models.CartItem
import com.artesan.artesanapp.models.Product
import com.artesan.artesanapp.models.User

class StorageManager(context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private val db = dbHelper.writableDatabase

    private val userDao = UserDao(db)
    private val productDao = ProductDao(db)
    private val cartDao = CartDao(db)
    private val sessionDao = SessionDao(db)

    // User Management
    fun saveUser(user: User): Boolean {
        return userDao.insert(user) != -1L
    }

    fun getUsers(): List<User> {
        return userDao.getAll()
    }

    fun findUserByUsername(username: String): User? {
        return userDao.findByUsername(username)
    }

    fun usernameExists(username: String): Boolean {
        return userDao.usernameExists(username)
    }

    fun emailExists(email: String): Boolean {
        return userDao.emailExists(email)
    }

    fun updateUser(user: User): Boolean {
        return userDao.update(user) > 0
    }

    fun deleteUser(id: String): Boolean {
        return userDao.delete(id) > 0
    }

    fun searchUsersByNameOrEmail(query: String): List<User> {
        return if (query.isBlank()) {
            userDao.getAll()
        } else {
            userDao.searchByNameOrEmail(query)
        }
    }

    fun saveCurrentUser(user: User?) {
        sessionDao.setCurrentUser(user?.id)
    }

    fun getCurrentUser(): User? {
        val userId = sessionDao.getCurrentUserId()
        return if (userId != null) {
            userDao.findById(userId)
        } else {
            null
        }
    }

    // Product Management
    fun saveProduct(product: Product): Boolean {
        return productDao.insert(product) != -1L
    }

    fun updateProduct(product: Product): Boolean {
        return productDao.update(product) > 0
    }

    fun deleteProduct(id: String): Boolean {
        return productDao.delete(id) > 0
    }

    fun getProducts(): List<Product> {
        return productDao.getAll()
    }

    fun getProductById(id: String): Product? {
        return productDao.findById(id)
    }

    fun getProductCount(): Int {
        return productDao.getCount()
    }

    // Cart Management
    fun addToCart(productId: String, quantity: Int): Boolean {
        val existing = cartDao.findByProductId(productId)
        return if (existing != null) {
            cartDao.update(productId, existing.second + quantity) > 0
        } else {
            cartDao.insert(productId, quantity) != -1L
        }
    }

    fun updateCartQuantity(productId: String, quantity: Int): Boolean {
        return cartDao.update(productId, quantity) > 0
    }

    fun removeFromCart(productId: String): Boolean {
        return cartDao.delete(productId) > 0
    }

    fun getCart(): List<CartItem> {
        return cartDao.getAllWithProducts()
    }

    fun clearCart() {
        cartDao.clear()
    }

    fun getCartItemCount(): Int {
        return cartDao.getItemCount()
    }

    fun getCartItemByProductId(productId: String): Pair<Int, Int>? {
        return cartDao.findByProductId(productId)
    }

    // Clear all data
    fun clearAll() {
        cartDao.clear()
        sessionDao.clearSession()
        // Note: We don't delete users and products as they are persistent data
    }

    fun close() {
        db.close()
        dbHelper.close()
    }
}
