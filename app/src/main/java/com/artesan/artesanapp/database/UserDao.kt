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
            put(DatabaseHelper.COLUMN_USER_USERNAME, user.username)
            put(DatabaseHelper.COLUMN_USER_PASSWORD, user.password)
            put(DatabaseHelper.COLUMN_USER_ROLE, user.role.name)
        }
        return db.insert(DatabaseHelper.TABLE_USERS, null, values)
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

    private fun cursorToUser(cursor: Cursor): User {
        return User(
            id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)),
            username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_USERNAME)),
            password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD)),
            role = UserRole.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ROLE)))
        )
    }
}
