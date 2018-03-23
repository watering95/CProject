package com.example.water.cproject

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

import com.example.water.cproject.dbHelper.CodeDBHelper
import com.example.water.cproject.dbHelper.MachineDBHelper

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
        return when (Matcher.match(uri)) {
            CODE_MACHINE -> "vnd.android.cursor.dir/vnd.cproject.machine"
            CODE_CODE -> "vnd.android.cursor.dir/vnd.cproject.code"
            else -> null
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

        return when (Matcher.match(uri)) {
            CODE_MACHINE -> machineDBHelper!!.query(projection, selection!!, selectionArgs!!, sortOrder)
            CODE_CODE -> codeDBHelper!!.query(projection, selection!!, selectionArgs!!, sortOrder)
            else -> return null
        }

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
        private const val AUTHORITY = "watering.cproject.provider"
        private const val PATH_MACHINE = "machine"
        private const val PATH_CODE = "code"
        private const val CODE_MACHINE = 0
        private const val CODE_CODE = 1

        private val Matcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            Matcher.addURI(AUTHORITY, PATH_MACHINE, CODE_MACHINE)
            Matcher.addURI(AUTHORITY, PATH_CODE, CODE_CODE)
        }
    }
}