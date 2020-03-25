package com.doodlyz.vlove.models;

import android.support.annotation.NonNull;

public class SimpleModel extends ItemModelAbs {
    public static final int TYPE = 0;

    @NonNull
    private final String mTitle;

    public SimpleModel(@NonNull final String title) {
        mTitle = title;
    }

    @Override
    public int getType() {
        return TYPE;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }
}
