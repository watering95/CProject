package com.example.water.cproject.DBHelper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

/**
 * Created by watering on 18. 3. 16.
 */

open class DBHelper internal constructor(context: Context) : SQLiteOpenHelper(context, DB_FILE_NAME, null, db_version) {
    internal var table_name: String? = null
    internal var columns: Array<String>? = null

    override fun onCreate(db: SQLiteDatabase) {
        val sql: StringBuilder

        sql = StringBuilder("CREATE TABLE IF NOT EXISTS " + table_name + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ")

        for (column in columns!!) {
            sql.append(", ").append(column)
        }
        sql.append(");")
        db.execSQL(sql.toString())
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)

        val sql: StringBuilder

        sql = StringBuilder("CREATE TABLE IF NOT EXISTS ").append(table_name).append(" (")
                .append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT")

        for (COLUMN in columns!!) {
            sql.append(", ").append(COLUMN)
        }
        sql.append(");")
        db.execSQL(sql.toString())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + table_name!!)
        onCreate(db)
    }

    @Throws(SQLiteException::class)
    fun insert(values: ContentValues) {
        writableDatabase.insert(table_name, null, values)
    }

    @Throws(SQLiteException::class)
    fun delete(where: String?, whereArgs: Array<String>) {
        val selection: String?

        if (where == null) {
            selection = null
        } else {
            selection = "$where=?"
        }
        writableDatabase.delete(table_name, selection, whereArgs)
    }

    @Throws(SQLiteException::class)
    fun update(values: ContentValues, where: String, selectionArgs: Array<String>) {
        writableDatabase.update(table_name, values, "$where=?", selectionArgs)
    }

    @Throws(SQLiteException::class)
    fun query(columns: Array<String>?, selection: String, selectionArgs: Array<String>, orderBy: String?): Cursor {
        return readableDatabase.query(table_name, columns, selection, selectionArgs, null, null, orderBy)
    }

    companion object {

        private val db_version = 1
        private val DB_FILE_NAME = "Machine.db"
    }
}
