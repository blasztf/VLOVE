package com.navers.vlove._.deprecated.features;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;

public class StorageUtils {

    public static boolean isDirectoryExists(String path, boolean createIfNecessary) {
        File directory = new File(path);
        if (!directory.exists() || !directory.isDirectory()) {
            if (createIfNecessary) {
                if (directory.mkdir()) {
                    return StorageUtils.isDirectoryExists(directory.getParent(), true);
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static long getAvailableExternalStorageSize() {
        long memorySize = 0;
        if (isExternalStorageAvailable()) {
            StatFs statusFileSystem = getExternalStatusFileSystem();
            long blockSize = statusFileSystem.getBlockSizeLong();
            long availableBlocks = statusFileSystem.getAvailableBlocksLong();
            memorySize = blockSize * availableBlocks;
        }

        return memorySize;
    }

    public static long getTotalExternalStorageSize() {
        long memorySize = 0;
        if (isExternalStorageAvailable()) {
            StatFs statusFileSystem = getExternalStatusFileSystem();
            long blockSize = statusFileSystem.getBlockSizeLong();
            long totalBlocks = statusFileSystem.getBlockCountLong();
            memorySize = blockSize * totalBlocks;
        }

        return memorySize;
    }

    private static StatFs getExternalStatusFileSystem() {
        File path = Environment.getExternalStorageDirectory();
        return new StatFs(path.getAbsolutePath());
    }
}
