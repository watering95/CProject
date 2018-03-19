package com.example.water.cproject.DBHelper;

import android.content.Context;

/**
 * Created by watering on 18. 3. 19.
 */

@SuppressWarnings("DefaultFileTemplate")
public class MachineDBHelper extends DBHelper {
    public MachineDBHelper(Context context) {
        super(context);

        TABLE_NAME = "tbl_machine";
        COLUMNS = new String[] {"id_code INTEGER","time TEXT","state INTEGER","gx REAL","gy REAL","gz REAL","ax REAL","ay REAL","az REAL"};
    }
}
