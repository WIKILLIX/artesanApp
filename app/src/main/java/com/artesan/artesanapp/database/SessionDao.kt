package com.artesan.artesanapp.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class SessionDao(private val db: SQLiteDatabase) {

    fun setCurrentUser(userId: String?) {
        if (userId == null) {
            // Clear session
            db.delete(DatabaseHelper.TABLE_SESSION, null, null)
        } else {
            // Check if session row exists
            val cursor = db.query(
                DatabaseHelper.TABLE_SESSION,
                arrayOf(DatabaseHelper.COLUMN_SESSION_ID),
                null,
                null,
                null,
                null,
                null
            )

            val exists = cursor.use { it.count > 0 }

            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_SESSION_ID, 1)
                put(DatabaseHelper.COLUMN_SESSION_USER_ID, userId)
            }

            if (exists) {
                db.update(
                    DatabaseHelper.TABLE_SESSION,
                    values,
                    "${DatabaseHelper.COLUMN_SESSION_ID} = ?",
                    arrayOf("1")
                )
            } else {
                db.insert(DatabaseHelper.TABLE_SESSION, null, values)
            }
        }
    }

    fun getCurrentUserId(): String? {
        val cursor = db.query(
            DatabaseHelper.TABLE_SESSION,
            arrayOf(DatabaseHelper.COLUMN_SESSION_USER_ID),
            "${DatabaseHelper.COLUMN_SESSION_ID} = ?",
            arrayOf("1"),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SESSION_USER_ID))
            } else {
                null
            }
        }
    }

    fun clearSession() {
        db.delete(DatabaseHelper.TABLE_SESSION, null, null)
    }
}
