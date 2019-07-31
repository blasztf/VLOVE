package com.navers.vlove.databases.helper;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TableCreator {
    private static final TableCreator ourInstance = new TableCreator();

    private StringBuilder creator;
    private SQLiteDatabase database;

    private String primaryKey;
    private StringBuilder foreignKey;

    public static TableCreator with(SQLiteDatabase database, String name) {
        return ourInstance.set(database, name);
    }

    public static void drop(SQLiteDatabase database, String name) {
        database.execSQL("DROP IF EXISTS " + name);
    }

    // Prevent to instantiate directly.
    private TableCreator() {}

    private TableCreator set(SQLiteDatabase database, String name) {
        this.database = database;
        this.creator = new StringBuilder("CREATE TABLE IF NOT EXISTS " + name + " (");
        this.foreignKey = new StringBuilder();
        return this;
    }

    public void create() throws SQLException {
        creator
                .insert(creator.indexOf("(") + 1, primaryKey)
                .append(foreignKey);
        int lastNextIndex = creator.lastIndexOf(DataTypes.next());
        creator
                .delete(lastNextIndex++, ++lastNextIndex)
                .append(");");
        database.execSQL(creator.toString());
        clear();
    }

    public TableCreator setPrimary(String key, String dataType) {
        return append(key, dataType, true, false, null, null, false);
    }

    public TableCreator append(String key, String dataType) {
        return append(key, dataType, false, false, null, null, false);
    }

    public TableCreator appendForeign(String key, String dataType, String foreignTable, String foreignKey, boolean onDeleteCascade) {
        return append(key, dataType, false, true, foreignTable, foreignKey, onDeleteCascade);
    }

    private TableCreator append(String key, String dataType, boolean isPrimary, boolean isForeign, String foreignTable, String foreignKey, boolean onDeleteCascade) {
        if (isPrimary) {
            if (key == null || key.trim().isEmpty() || dataType == null || dataType.trim().isEmpty()) {
                primaryKey = "";
            }
            else {
                primaryKey = DataTypes.asPrimary(key, dataType) + DataTypes.next();
            }
        }
        else {
            if (!(key == null || key.trim().isEmpty() || dataType == null || dataType.trim().isEmpty())) {
                creator.append(key).append(dataType).append(DataTypes.next());
                if (isForeign & !(foreignTable == null || foreignTable.trim().isEmpty() || foreignKey == null || foreignKey.trim().isEmpty())) {
                    this.foreignKey.append(DataTypes.asForeignKey(key, foreignTable, foreignKey, true)).append(DataTypes.next());
                }
//                if (isForeign & !(foreignTable == null || foreignTable.trim().isEmpty() || foreignKey == null || foreignKey.trim().isEmpty())) {
//                    creator.append(DataTypes.asForeign(key, dataType, foreignTable, onDeleteCascade)).append(DataTypes.next());
//                }
//                else {
//                    creator.append(key).append(dataType).append(DataTypes.next());
//                }
            }
        }
        return this;
    }

    private void clear() {
        database = null;
        creator = null;
        primaryKey = null;
        foreignKey = null;
    }

}
