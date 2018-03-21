package com.example.water.cproject.DBHelper

import android.content.Context

/**
 * Created by watering on 18. 3. 19.
 */

class CodeDBHelper(context: Context) : DBHelper(context) {
    init {
        table_name = "tbl_code"
        columns = arrayOf("code TEXT UNIQUE", "date TEXT")
    }
}
