package com.doodlyz.vlove.ui.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.doodlyz.vlove.deprecated.features.WRUtils;
import com.doodlyz.vlove.logger.CrashCocoExceptionHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.util.HashMap;

abstract class BaseDialogAct extends Activity {

    public BaseDialogAct() {}

    protected abstract int getContentViewId();

    protected abstract void onPrepareContentViewElement();

    protected abstract void onReady(Intent intent);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(CrashCocoExceptionHandler.asDefault("vl_bd"));

        setContentView(getContentViewId());
        onPrepareContentViewElement();
        onReady(getIntent());
    }

}
