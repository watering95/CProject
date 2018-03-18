package com.example.water.cproject;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by watering on 18. 3. 16.
 */

public class DBProvider extends ContentProvider {
    private static final String AUTHORITY = "watering.cproject.provider";
    private static final String PATH_MACHINE = "machine";
    private static final int CODE_MACHINE = 0;

    private DBHelper dbHelper;

    private static final UriMatcher Matcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        Matcher.addURI(AUTHORITY,PATH_MACHINE,CODE_MACHINE);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (Matcher.match(uri)) {
            case CODE_MACHINE:
                return "vnd.android.cursor.dir/vnd.cproject.machine";
            default:
                return null;
        }
    }
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;

        switch (Matcher.match(uri)) {
            case CODE_MACHINE:
                dbHelper.update(values, selection, selectionArgs);
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
                dbHelper.insert(values);
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
                cursor = dbHelper.query(projection, selection, selectionArgs, sortOrder);
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
                dbHelper.delete(selection, selectionArgs);
                break;
            default:
        }
        return count;
    }
}
