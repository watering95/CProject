package com.example.water.cproject.DBHelper;

import android.content.Context;

/**
 * Created by watering on 18. 3. 19.
 */

@SuppressWarnings("DefaultFileTemplate")
public class CodeDBHelper extends DBHelper {
    public CodeDBHelper(Context context) {
        super(context);

        TABLE_NAME = "tbl_code";
        COLUMNS = new String[] {"code TEXT","date TEXT"};
    }
}
