package com.navers.vlove.deprecated.features;

import java.lang.ref.WeakReference;

public class WRUtils {

    public static boolean stillExists(WeakReference obj) {
        return obj != null && obj.get() != null;
    }
}
