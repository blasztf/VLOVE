package com.doodlyz.vlove.databases.helper;

import android.database.Cursor;

public final class CursorHelper {
    public static String getStringValue(Cursor cursor, String key) {
        return cursor.getString(cursor.getColumnIndex(key));
    }

    public static int getIntValue(Cursor cursor, String key) {
        return cursor.getInt(cursor.getColumnIndex(key));
    }

    public static long getLongValue(Cursor cursor, String key) {
        return cursor.getLong(cursor.getColumnIndex(key));
    }

    public static double getDoubleValue(Cursor cursor, String key) {
        return cursor.getDouble(cursor.getColumnIndex(key));
    }
}
