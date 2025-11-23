package com.artesan.artesanapp.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.artesan.artesanapp.models.User
import com.artesan.artesanapp.models.UserRole

class UserDao(private val db: SQLiteDatabase) {

    fun insert(user: User): Long {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_ID, user.id)
            put(DatabaseHelper.COLUMN_USER_NAME, user.name)
            put(DatabaseHelper.COLUMN_USER_EMAIL, user.email)
            put(DatabaseHelper.COLUMN_USER_USERNAME, user.username)
            put(DatabaseHelper.COLUMN_USER_PASSWORD, user.password)
            put(DatabaseHelper.COLUMN_USER_ROLE, user.role.name)
        }
        return db.insert(DatabaseHelper.TABLE_USERS, null, values)
    }

    fun update(user: User): Int {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_NAME, user.name)
            put(DatabaseHelper.COLUMN_USER_EMAIL, user.email)
            put(DatabaseHelper.COLUMN_USER_USERNAME, user.username)
            put(DatabaseHelper.COLUMN_USER_PASSWORD, user.password)
            put(DatabaseHelper.COLUMN_USER_ROLE, user.role.name)
        }
        return db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(user.id)
        )
    }

    fun findByUsername(username: String): User? {
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_USER_USERNAME} = ?",
            arrayOf(username),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                cursorToUser(it)
            } else {
                null
            }
        }
    }

    fun findById(id: String): User? {
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                cursorToUser(it)
            } else {
                null
            }
        }
    }

    fun getAll(): List<User> {
        val users = mutableListOf<User>()
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            null,
            null,
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                users.add(cursorToUser(it))
            }
        }

        return users
    }

    fun usernameExists(username: String): Boolean {
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COLUMN_USER_ID),
            "${DatabaseHelper.COLUMN_USER_USERNAME} = ?",
            arrayOf(username),
            null,
            null,
            null
        )

        return cursor.use { it.count > 0 }
    }

    fun delete(id: String): Int {
        return db.delete(
            DatabaseHelper.TABLE_USERS,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(id)
        )
    }

    fun searchByNameOrEmail(query: String): List<User> {
        val users = mutableListOf<User>()
        val searchQuery = "%$query%"
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_USER_NAME} LIKE ? OR ${DatabaseHelper.COLUMN_USER_EMAIL} LIKE ?",
            arrayOf(searchQuery, searchQuery),
            null,
            null,
            "${DatabaseHelper.COLUMN_USER_NAME} ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                users.add(cursorToUser(it))
            }
        }

        return users
    }

    fun emailExists(email: String): Boolean {
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COLUMN_USER_ID),
            "${DatabaseHelper.COLUMN_USER_EMAIL} = ?",
            arrayOf(email),
            null,
            null,
            null
        )

        return cursor.use { it.count > 0 }
    }

    private fun cursorToUser(cursor: Cursor): User {
        return User(
            id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)),
            username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_USERNAME)),
            password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD)),
            role = UserRole.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ROLE)))
        )
    }
}
