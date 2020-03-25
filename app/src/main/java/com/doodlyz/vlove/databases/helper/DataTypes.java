package com.doodlyz.vlove.databases.helper;

public class DataTypes {
    public static final String VARCHAR = " VARCHAR";
    public static final String TEXT = " TEXT";
    public static final String INTEGER = " INTEGER";
    public static final String LONG = " INTEGER";
    public static final String DOUBLE = " DOUBLE";

    public static String asPrimary(String key, String dataType) {
        return key + dataType + " PRIMARY KEY";
    }

    @Deprecated
    public static String asForeignKey(String key, String foreignTable, String foreignKey, boolean onDeleteCascade) {
        return "FOREIGN KEY (" + key + ") REFERENCES " + foreignTable + "(" + foreignKey + ")" + (onDeleteCascade ? " ON DELETE CASCADE" : "");
    }

    public static String asForeign(String key, String dataType, String foreignTable, boolean onDeleteCascade) {
        return key + dataType + " REFERENCES " + foreignTable + (onDeleteCascade ? " ON DELETE CASCADE" : "");
    }

    public static String next() {
        return ", ";
    }
}
