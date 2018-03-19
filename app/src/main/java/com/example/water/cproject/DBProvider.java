package com.example.water.cproject;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.water.cproject.DBHelper.CodeDBHelper;
import com.example.water.cproject.DBHelper.MachineDBHelper;

/**
 * Created by watering on 18. 3. 16.
 */

@SuppressWarnings("DefaultFileTemplate")
public class DBProvider extends ContentProvider {
    private static final String AUTHORITY = "watering.cproject.provider";
    private static final String PATH_MACHINE = "machine";
    private static final String PATH_CODE = "code";
    private static final int CODE_MACHINE = 0;
    private static final int CODE_CODE = 1;

    private MachineDBHelper machineDBHelper;
    private CodeDBHelper codeDBHelper;

    private static final UriMatcher Matcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        Matcher.addURI(AUTHORITY,PATH_MACHINE,CODE_MACHINE);
        Matcher.addURI(AUTHORITY,PATH_CODE,CODE_CODE);
    }

    @Override
    public boolean onCreate() {
        machineDBHelper = new MachineDBHelper(getContext());
        codeDBHelper = new CodeDBHelper(getContext());
        return true;
    }
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (Matcher.match(uri)) {
            case CODE_MACHINE:
                return "vnd.android.cursor.dir/vnd.cproject.machine";
            case CODE_CODE:
                return "vnd.android.cursor.dir/vnd.cproject.code";
            default:
                return null;
        }
    }
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;

        switch (Matcher.match(uri)) {
            case CODE_MACHINE:
                machineDBHelper.update(values, selection, selectionArgs);
                break;
            case CODE_CODE:
                codeDBHelper.update(values, selection, selectionArgs);
                break;
            default:
        }
        return count;
    }
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        switch (Matcher.match(uri)) {
            case CODE_MACHINE:
                machineDBHelper.insert(values);
                break;
            case CODE_CODE:
                codeDBHelper.insert(values);
                break;
            default:
                return null;
        }
        return uri;
    }
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;

        switch (Matcher.match(uri)) {
            case CODE_MACHINE:
                cursor = machineDBHelper.query(projection, selection, selectionArgs, sortOrder);
                break;
            case CODE_CODE:
                cursor = codeDBHelper.query(projection, selection, selectionArgs, sortOrder);
                break;
            default:
                return null;
        }
        return cursor;

    }
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;

        switch (Matcher.match(uri)) {
            case CODE_MACHINE:
                machineDBHelper.delete(selection, selectionArgs);
                break;
            case CODE_CODE:
                codeDBHelper.delete(selection, selectionArgs);
                break;
            default:
        }
        return count;
    }
}