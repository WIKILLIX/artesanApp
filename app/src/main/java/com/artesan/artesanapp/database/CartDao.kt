package com.artesan.artesanapp.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.artesan.artesanapp.models.CartItem
import com.artesan.artesanapp.models.Product

class CartDao(private val db: SQLiteDatabase) {

    fun insert(productId: String, quantity: Int): Long {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_CART_PRODUCT_ID, productId)
            put(DatabaseHelper.COLUMN_CART_QUANTITY, quantity)
        }
        return db.insert(DatabaseHelper.TABLE_CART, null, values)
    }

    fun update(productId: String, quantity: Int): Int {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_CART_QUANTITY, quantity)
        }
        return db.update(
            DatabaseHelper.TABLE_CART,
            values,
            "${DatabaseHelper.COLUMN_CART_PRODUCT_ID} = ?",
            arrayOf(productId)
        )
    }

    fun delete(productId: String): Int {
        return db.delete(
            DatabaseHelper.TABLE_CART,
            "${DatabaseHelper.COLUMN_CART_PRODUCT_ID} = ?",
            arrayOf(productId)
        )
    }

    fun findByProductId(productId: String): Pair<Int, Int>? {
        val cursor = db.query(
            DatabaseHelper.TABLE_CART,
            arrayOf(DatabaseHelper.COLUMN_CART_ID, DatabaseHelper.COLUMN_CART_QUANTITY),
            "${DatabaseHelper.COLUMN_CART_PRODUCT_ID} = ?",
            arrayOf(productId),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                val id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_ID))
                val quantity = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_QUANTITY))
                Pair(id, quantity)
            } else {
                null
            }
        }
    }

    fun getAllWithProducts(): List<CartItem> {
        val cartItems = mutableListOf<CartItem>()

        val query = """
            SELECT
                c.${DatabaseHelper.COLUMN_CART_QUANTITY},
                p.${DatabaseHelper.COLUMN_PRODUCT_ID},
                p.${DatabaseHelper.COLUMN_PRODUCT_NAME},
                p.${DatabaseHelper.COLUMN_PRODUCT_DESCRIPTION},
                p.${DatabaseHelper.COLUMN_PRODUCT_PRICE},
                p.${DatabaseHelper.COLUMN_PRODUCT_STOCK},
                p.${DatabaseHelper.COLUMN_PRODUCT_IMAGE}
            FROM ${DatabaseHelper.TABLE_CART} c
            INNER JOIN ${DatabaseHelper.TABLE_PRODUCTS} p
                ON c.${DatabaseHelper.COLUMN_CART_PRODUCT_ID} = p.${DatabaseHelper.COLUMN_PRODUCT_ID}
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        cursor.use {
            while (it.moveToNext()) {
                cartItems.add(cursorToCartItem(it))
            }
        }

        return cartItems
    }

    fun clear(): Int {
        return db.delete(DatabaseHelper.TABLE_CART, null, null)
    }

    fun getItemCount(): Int {
        val cursor = db.rawQuery(
            "SELECT SUM(${DatabaseHelper.COLUMN_CART_QUANTITY}) FROM ${DatabaseHelper.TABLE_CART}",
            null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                it.getInt(0)
            } else {
                0
            }
        }
    }

    private fun cursorToCartItem(cursor: Cursor): CartItem {
        val product = Product(
            id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_NAME)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_DESCRIPTION)),
            price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_PRICE)),
            stock = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_STOCK)),
            imageBase64 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_IMAGE))
        )

        val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_QUANTITY))

        return CartItem(product, quantity)
    }
}
