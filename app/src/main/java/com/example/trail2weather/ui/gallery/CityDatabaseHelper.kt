package com.example.trail2weather.ui.gallery


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CityDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "CityDatabase.db"
        private const val TABLE_NAME = "cities"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
    }


    fun deleteCity(cityName: String) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_NAME = ?", arrayOf(cityName))
        db.close()
    }



    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_NAME TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertCity(cityName: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, cityName)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllCities(): List<String> {
        val cityList = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val cityName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
            cityList.add(cityName)
        }
        cursor.close()
        db.close()
        return cityList
    }
}
