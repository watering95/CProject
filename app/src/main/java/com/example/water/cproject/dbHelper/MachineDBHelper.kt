package com.example.water.cproject.dbHelper

import android.content.Context

/**
 * Created by watering on 18. 3. 19.
 */

class MachineDBHelper(context: Context) : DBHelper(context) {
    init {
        tableName = "tbl_machine"
        columns = arrayOf("id_code INTEGER", "time TEXT", "state INTEGER", "angleX REAL", "angleY REAL", "angleZ REAL")
    }
}
