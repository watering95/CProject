package com.example.water.cproject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.water.cproject.Machine.Info_Code;
import com.example.water.cproject.Machine.Info_Machine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by watering on 18. 3. 16.
 */

@SuppressWarnings("DefaultFileTemplate")
public class DBResolver{
    private ContentResolver cr;
    private static final int CODE_MACHINE = 0;
    private static final int CODE_CODE = 1;
    private static final String URI_MACHINE = "content://watering.cproject.provider/machine";
    private static final String URI_CODE = "content://watering.cproject.provider/code";
    private static final String TAG = "CProject";
    private final List<Info_Machine> listsInfoMachine = new ArrayList<>();
    private final List<Info_Code> listsCode = new ArrayList<>();

    private final MainActivity mainActivity;

    public DBResolver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    void getContentResolver(ContentResolver cr) {
        this.cr = cr;
    }

    void insertMachine(int code, int state, float[] imu) {
        ContentValues cv = new ContentValues();

        cv.put("code",code);
        cv.put("time", mainActivity.getNow());
        cv.put("state",state);
        cv.put("gx",imu[0]);
        cv.put("gy",imu[1]);
        cv.put("gz",imu[2]);
        cv.put("ax",imu[3]);
        cv.put("ay",imu[4]);
        cv.put("az",imu[5]);

        try {
            cr.insert(Uri.parse(URI_MACHINE), cv);
        } catch (Exception e) {
            Log.e(TAG,"Machine DB insert error");
        }
    }
    void insertCode(String code) {
        ContentValues cv = new ContentValues();

        cv.put("code",code);
        cv.put("date", mainActivity.getToday());

        try {
            cr.insert(Uri.parse(URI_CODE), cv);
        } catch (Exception e) {
            Log.e(TAG,"Code DB insert error");
        }
    }

    public String getLatestCode(String date) {
        String selection = "date=?";
        String[] selectionArgs = new String[] {date};

        getData(CODE_CODE, URI_CODE, selection, selectionArgs, "code DESC");

        if(listsInfoMachine.size() > 0) return listsCode.get(0).getCode();
        else return "0000000000";
    }
    public ArrayList<String> getCodes(String date) {
        ArrayList<String> codes = new ArrayList<>();

        String selection = "date=?";
        String[] selectionArgs = new String[] {date};

        getData(CODE_CODE, URI_CODE, selection, selectionArgs, "code DESC");
        if(listsCode.size() > 0) {
            for(int i = 0, size = listsCode.size(); i < size; i++) {
                codes.add(listsCode.get(i).getCode());
            }
            return codes;
        }
        else return null;
    }
    public List<Info_Machine> getInfoMachine(String code) {
        String selection = "id_code=?";
        String[] selectionArgs = new String[] {String.valueOf(getCodeId(code))};

        getData(CODE_MACHINE, URI_MACHINE, selection, selectionArgs, null);
        if(listsInfoMachine.size() > 0) {
            return listsInfoMachine;
        }
        else return null;
    }
    public int getCodeId(String code) {
        String selection = "code=?";
        String[] selectionArgs = new String[] {code};

        getData(CODE_CODE, URI_CODE, selection, selectionArgs, null);
        if(listsCode.size() > 0) {
            return listsCode.get(0).getId();
        }
        else return -1;
    }

    private void getData(int code, String uri, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        Info_Machine infoMachine = new Info_Machine();
        Info_Code infoCode = new Info_Code();
        float[] imu = new float[6];

        listsInfoMachine.clear();
        listsCode.clear();

        cursor = cr.query(Uri.parse(uri),null, selection, selectionArgs, sortOrder);

        if((cursor != null ? cursor.getCount() : 0) < 1) {
            assert cursor != null;
            cursor.close();
            return;
        }

        assert cursor != null;
        while(cursor.moveToNext()) {
            switch (code) {
                case CODE_MACHINE:
                    infoMachine.setCode(cursor.getInt(1));
                    infoMachine.setTime(cursor.getString(2));

                    for(int i = 0; i < 6; i++) {
                        imu[i] = cursor.getFloat(i + 3);
                    }
                    infoMachine.setImu(imu);
                    listsInfoMachine.add(infoMachine);
                    break;
                case CODE_CODE:
                    infoCode.setId(cursor.getInt(0));
                    infoCode.setCode(cursor.getString(1));
                    infoCode.setDate(cursor.getString(2));

                    listsCode .add(infoCode);
                    break;
                default:
                    break;
            }
        }

        cursor.close();
    }
}
