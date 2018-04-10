package com.example.water.cproject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.water.cproject.machine.InfoCode;
import com.example.water.cproject.machine.InfoMachine;

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
    private final List<InfoMachine> listsInfoMachine = new ArrayList<>();
    private final List<InfoCode> listsCode = new ArrayList<>();

    private final MainActivity mainActivity;

    public DBResolver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    void getContentResolver(ContentResolver cr) {
        this.cr = cr;
    }

    void insertMachine(int code, int state, float[] angle) {
        ContentValues cv = new ContentValues();

        cv.put("id_code",code);
        cv.put("time", mainActivity.getNow());
        cv.put("state",state);
        cv.put("angleX",angle[0]);
        cv.put("angleY",angle[1]);
        cv.put("angleZ",angle[2]);

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

        if(listsCode.size() > 0) return listsCode.get(0).getCode();
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
    public List<InfoMachine> getInfoMachine(String code) {
        String selection = "id_code=?";
        String[] selectionArgs = new String[] {String.valueOf(getCodeId(code))};

        getData(CODE_MACHINE, URI_MACHINE, selection, selectionArgs, "time DESC");
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
    public InfoCode getCode(int id) {
        String selection = "_id=?";
        String[] selectionArgs = new String[] {String.valueOf(id)};

        getData(CODE_CODE, URI_CODE, selection, selectionArgs, null);
        if(listsCode.size() > 0) {
            return listsCode.get(0);
        }
        else return null;
    }

    public void deleteCode(String code) {
        String selection = "code";
        String selectionArg[] = new String[] {code};

        cr.delete(Uri.parse(URI_CODE), selection, selectionArg);
    }
    public void deleteInfoMachine(String code) {
        int id = getCodeId(code);
        String selection = "id_code";
        String selectionArg[] = new String[] {String.valueOf(id)};

        cr.delete(Uri.parse(URI_MACHINE), selection, selectionArg);
    }

    private void getData(int code, String uri, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        InfoMachine infoMachine;
        InfoCode infoCode;
        float[] angle;

        switch (code) {
            case CODE_MACHINE:
                listsInfoMachine.clear();
                break;
            case CODE_CODE:
                listsCode.clear();
                break;
        }

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
                    infoMachine = new InfoMachine();
                    angle = new float[3];
                    infoMachine.setCode(cursor.getInt(1));
                    infoMachine.setTime(cursor.getString(2));
                    infoMachine.setState(cursor.getInt(3));

                    for(int i = 0; i < 3; i++) {
                        angle[i] = cursor.getFloat(i + 4);
                    }
                    infoMachine.setAngle(angle);
                    listsInfoMachine.add(infoMachine);
                    break;
                case CODE_CODE:
                    infoCode = new InfoCode();
                    infoCode.setId(cursor.getInt(0));
                    infoCode.setCode(cursor.getString(1));
                    infoCode.setDate(cursor.getString(2));

                    listsCode.add(infoCode);
                    break;
                default:
                    break;
            }
        }

        cursor.close();
    }
}
