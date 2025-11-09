package com.artesan.artesanapp.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.artesan.artesanapp.models.Product

class ProductDao(private val db: SQLiteDatabase) {

    fun insert(product: Product): Long {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_PRODUCT_ID, product.id)
            put(DatabaseHelper.COLUMN_PRODUCT_NAME, product.name)
            put(DatabaseHelper.COLUMN_PRODUCT_DESCRIPTION, product.description)
            put(DatabaseHelper.COLUMN_PRODUCT_PRICE, product.price)
            put(DatabaseHelper.COLUMN_PRODUCT_STOCK, product.stock)
            put(DatabaseHelper.COLUMN_PRODUCT_IMAGE, product.imageBase64)
        }
        return db.insert(DatabaseHelper.TABLE_PRODUCTS, null, values)
    }

    fun update(product: Product): Int {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_PRODUCT_NAME, product.name)
            put(DatabaseHelper.COLUMN_PRODUCT_DESCRIPTION, product.description)
            put(DatabaseHelper.COLUMN_PRODUCT_PRICE, product.price)
            put(DatabaseHelper.COLUMN_PRODUCT_STOCK, product.stock)
            put(DatabaseHelper.COLUMN_PRODUCT_IMAGE, product.imageBase64)
        }
        return db.update(
            DatabaseHelper.TABLE_PRODUCTS,
            values,
            "${DatabaseHelper.COLUMN_PRODUCT_ID} = ?",
            arrayOf(product.id)
        )
    }

    fun delete(id: String): Int {
        return db.delete(
            DatabaseHelper.TABLE_PRODUCTS,
            "${DatabaseHelper.COLUMN_PRODUCT_ID} = ?",
            arrayOf(id)
        )
    }

    fun findById(id: String): Product? {
        val cursor = db.query(
            DatabaseHelper.TABLE_PRODUCTS,
            null,
            "${DatabaseHelper.COLUMN_PRODUCT_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                cursorToProduct(it)
            } else {
                null
            }
        }
    }

    fun getAll(): List<Product> {
        val products = mutableListOf<Product>()
        val cursor = db.query(
            DatabaseHelper.TABLE_PRODUCTS,
            null,
            null,
            null,
            null,
            null,
            "${DatabaseHelper.COLUMN_PRODUCT_NAME} ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                products.add(cursorToProduct(it))
            }
        }

        return products
    }

    fun getCount(): Int {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_PRODUCTS}", null)
        return cursor.use {
            if (it.moveToFirst()) {
                it.getInt(0)
            } else {
                0
            }
        }
    }

    private fun cursorToProduct(cursor: Cursor): Product {
        return Product(
            id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_NAME)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_DESCRIPTION)),
            price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_PRICE)),
            stock = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_STOCK)),
            imageBase64 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_IMAGE))
        )
    }
}
