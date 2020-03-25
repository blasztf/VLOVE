package com.doodlyz.vlove.deprecated.features;

import java.lang.ref.WeakReference;

public class WRUtils {

    public static <T extends Object> T get(WeakReference obj) {
        T res = (T) obj.get();
        return res;
    }
}
