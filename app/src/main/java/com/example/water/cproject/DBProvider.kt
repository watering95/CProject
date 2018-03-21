package com.example.water.cproject

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

import com.example.water.cproject.DBHelper.CodeDBHelper
import com.example.water.cproject.DBHelper.MachineDBHelper

/**
 * Created by watering on 18. 3. 16.
 */

class DBProvider : ContentProvider() {

    private var machineDBHelper: MachineDBHelper? = null
    private var codeDBHelper: CodeDBHelper? = null

    override fun onCreate(): Boolean {
        machineDBHelper = MachineDBHelper(context!!)
        codeDBHelper = CodeDBHelper(context!!)
        return true
    }

    override fun getType(uri: Uri): String? {
        when (Matcher.match(uri)) {
            CODE_MACHINE -> return "vnd.android.cursor.dir/vnd.cproject.machine"
            CODE_CODE -> return "vnd.android.cursor.dir/vnd.cproject.code"
            else -> return null
        }
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val count = 0

        when (Matcher.match(uri)) {
            CODE_MACHINE -> machineDBHelper!!.update(values!!, selection!!, selectionArgs!!)
            CODE_CODE -> codeDBHelper!!.update(values!!, selection!!, selectionArgs!!)
        }
        return count
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        when (Matcher.match(uri)) {
            CODE_MACHINE -> machineDBHelper!!.insert(values!!)
            CODE_CODE -> codeDBHelper!!.insert(values!!)
            else -> return null
        }
        return uri
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val cursor: Cursor

        when (Matcher.match(uri)) {
            CODE_MACHINE -> cursor = machineDBHelper!!.query(projection, selection!!, selectionArgs!!, sortOrder)
            CODE_CODE -> cursor = codeDBHelper!!.query(projection, selection!!, selectionArgs!!, sortOrder)
            else -> return null
        }
        return cursor

    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val count = 0

        when (Matcher.match(uri)) {
            CODE_MACHINE -> machineDBHelper!!.delete(selection, selectionArgs!!)
            CODE_CODE -> codeDBHelper!!.delete(selection, selectionArgs!!)
        }
        return count
    }

    companion object {
        private val AUTHORITY = "watering.cproject.provider"
        private val PATH_MACHINE = "machine"
        private val PATH_CODE = "code"
        private val CODE_MACHINE = 0
        private val CODE_CODE = 1

        private val Matcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            Matcher.addURI(AUTHORITY, PATH_MACHINE, CODE_MACHINE)
            Matcher.addURI(AUTHORITY, PATH_CODE, CODE_CODE)
        }
    }
}