package com.example.trail2weather

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class UserRepository(context: Context) {

    private val dbHelper = DbHelper(context)

    fun insertUser(username: String, password: String): Long {
        if (username.isEmpty() || password.isEmpty()) {
            // Empty username or password, return -1 to indicate a failure
            return -1
        }

        if (isUsernameExists(username)) {
            // Username already exists, return -1 to indicate a failure
            return -1
        }

        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DbHelper.COLUMN_USERNAME, username)
            put(DbHelper.COLUMN_PASSWORD, password)
        }

        val userId = db.insert(DbHelper.TABLE_USERS, null, values)
        db.close()

        return userId
    }

    private fun isUsernameExists(username: String): Boolean {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(DbHelper.COLUMN_USERNAME)
        val selection = "${DbHelper.COLUMN_USERNAME} = ?"
        val selectionArgs = arrayOf(username)

        val cursor: Cursor = db.query(
            DbHelper.TABLE_USERS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val usernameExists = cursor.moveToFirst()
        cursor.close()
        db.close()

        return usernameExists
    }

    fun getUser(username: String, password: String): User? {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            DbHelper.COLUMN_ID,
            DbHelper.COLUMN_USERNAME,
            DbHelper.COLUMN_PASSWORD
        )

        val selection = "${DbHelper.COLUMN_USERNAME} = ? AND ${DbHelper.COLUMN_PASSWORD} = ?"
        val selectionArgs = arrayOf(username, password)

        val cursor: Cursor = db.query(
            DbHelper.TABLE_USERS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var user: User? = null

        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(DbHelper.COLUMN_ID)
            val usernameIndex = cursor.getColumnIndex(DbHelper.COLUMN_USERNAME)
            val passwordIndex = cursor.getColumnIndex(DbHelper.COLUMN_PASSWORD)

            // Check if the columns exist in the cursor
            if (idIndex != -1 && usernameIndex != -1 && passwordIndex != -1) {
                val userId = cursor.getLong(idIndex)
                val dbUsername = cursor.getString(usernameIndex)
                val dbPassword = cursor.getString(passwordIndex)

                user = User(userId, dbUsername, dbPassword)
            } else {
                // Handle the case where one or more columns are not found
                // This could be due to a mismatch in column names
            }
        }


        cursor.close()
        db.close()

        return user
    }
}
