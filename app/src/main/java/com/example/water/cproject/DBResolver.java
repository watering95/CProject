package com.example.water.cproject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.icu.text.IDNA;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by watering on 18. 3. 16.
 */

public class DBResolver{
    private ContentResolver cr;
    private static final int CODE_MACHINE = 0;
    private static final String URI_MACHINE = "content://watering.cproject.provider/machine";
    private static final String TAG = "CProject";
    private List<Info_Machine> lists = new ArrayList<>();

    private MainActivity mainActivity;


    public DBResolver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    void getContentResolver(ContentResolver cr) {
        this.cr = cr;
    }

    void insert(String code, int state, float[] imu) {
        ContentValues cv = new ContentValues();

        cv.put("code",code);
        cv.put("date",mainActivity.getToday());
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
            Log.e(TAG,"DB 추가 error");
        }
    }

    public String getLatestCode(String date) {
        String selection = "date=?";
        String[] selectionArgs = new String[] {date};

        getData(CODE_MACHINE, URI_MACHINE, selection, selectionArgs, "code DESC");

        if(lists.size() > 0) return lists.get(0).getCode();
        else return "0000000000";
    }
    public ArrayList<String> getCodes(String date) {
        ArrayList<String> codes = new ArrayList<>();

        String selection = "date=?";
        String[] selectionArgs = new String[] {date};

        getData(CODE_MACHINE, URI_MACHINE, selection, selectionArgs, "code DESC");
        if(lists.size() > 0) {
            for(int i = 0, size = lists.size(); i < size; i++) {
                codes.add(lists.get(i).getCode());
            }
            return codes;
        }
        else return null;
    }
    public List<Info_Machine> getInfoMachine(String code) {
        String selection = "code=?";
        String[] selectionArgs = new String[] {code};

        getData(CODE_MACHINE, URI_MACHINE, selection, selectionArgs, null);
        if(lists.size() > 0) {
            return lists;
        }
        else return null;
    }


    private void getData(int code, String uri, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        Info_Machine info = new Info_Machine();
        float[] imu = new float[6];

        lists.clear();

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
                    info.setCode(cursor.getString(0));
                    info.setDate(cursor.getString(1));
                    info.setTime(cursor.getString(2));

                    for(int i = 0; i < 6; i++) {
                        imu[i] = cursor.getFloat(i + 3);
                    }
                    info.setImu(imu);
                    lists.add(info);
                    break;
                default:
                    break;
            }
        }

        cursor.close();
    }
}
