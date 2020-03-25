package com.doodlyz.vlove.databases.helper;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.RequiresApi;

public abstract class SQLiteHelper extends SQLiteOpenHelper {
    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @RequiresApi(api = 28)
    public SQLiteHelper(Context context, String name, int version, SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//            if (!db.isReadOnly()) {
//                // enable foreign key
//                db.execSQL("PRAGMA foreign_keys = ON;");
//            }
//        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // enable foreign key
            db.setForeignKeyConstraintsEnabled(true);
//        }
    }
}
