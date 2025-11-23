package com.artesan.artesanapp.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "artesanapp.db"
        private const val DATABASE_VERSION = 2

        // Users table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_NAME = "name"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_USERNAME = "username"
        const val COLUMN_USER_PASSWORD = "password"
        const val COLUMN_USER_ROLE = "role"

        // Products table
        const val TABLE_PRODUCTS = "products"
        const val COLUMN_PRODUCT_ID = "id"
        const val COLUMN_PRODUCT_NAME = "name"
        const val COLUMN_PRODUCT_DESCRIPTION = "description"
        const val COLUMN_PRODUCT_PRICE = "price"
        const val COLUMN_PRODUCT_STOCK = "stock"
        const val COLUMN_PRODUCT_IMAGE = "image_base64"

        // Cart table
        const val TABLE_CART = "cart"
        const val COLUMN_CART_ID = "id"
        const val COLUMN_CART_PRODUCT_ID = "product_id"
        const val COLUMN_CART_QUANTITY = "quantity"

        // Session table (for current user)
        const val TABLE_SESSION = "session"
        const val COLUMN_SESSION_ID = "id"
        const val COLUMN_SESSION_USER_ID = "user_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create users table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID TEXT PRIMARY KEY,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_USER_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_USER_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_USER_PASSWORD TEXT NOT NULL,
                $COLUMN_USER_ROLE TEXT NOT NULL
            )
        """.trimIndent()

        // Create products table
        val createProductsTable = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_PRODUCT_ID TEXT PRIMARY KEY,
                $COLUMN_PRODUCT_NAME TEXT NOT NULL,
                $COLUMN_PRODUCT_DESCRIPTION TEXT NOT NULL,
                $COLUMN_PRODUCT_PRICE REAL NOT NULL,
                $COLUMN_PRODUCT_STOCK INTEGER NOT NULL,
                $COLUMN_PRODUCT_IMAGE TEXT
            )
        """.trimIndent()

        // Create cart table
        val createCartTable = """
            CREATE TABLE $TABLE_CART (
                $COLUMN_CART_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CART_PRODUCT_ID TEXT NOT NULL,
                $COLUMN_CART_QUANTITY INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_CART_PRODUCT_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_PRODUCT_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        // Create session table (single row to track current user)
        val createSessionTable = """
            CREATE TABLE $TABLE_SESSION (
                $COLUMN_SESSION_ID INTEGER PRIMARY KEY CHECK ($COLUMN_SESSION_ID = 1),
                $COLUMN_SESSION_USER_ID TEXT,
                FOREIGN KEY($COLUMN_SESSION_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createProductsTable)
        db.execSQL(createCartTable)
        db.execSQL(createSessionTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SESSION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")

        // Create tables again
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Enable foreign key constraints
        db.setForeignKeyConstraintsEnabled(true)
    }
}
