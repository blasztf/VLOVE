package com.doodlyz.vlove.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.doodlyz.vlove.R;
import com.doodlyz.vlove.logger.CrashCocoExceptionHandler;
import com.doodlyz.vlove.ui.dialogs.Popup;
import com.doodlyz.vlove.versions.VersionControl;
import com.doodlyz.vlove.versions.VersionControlImpl;

abstract class ScreenAbs extends AppCompatActivity implements Screen, VersionControlImpl {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(CrashCocoExceptionHandler.asDefault("vlv.scr"));
        VersionControl.validate(this);
    }

    @Override
    public void onVersionReady(int status) {
        if (status == VersionControlImpl.VERSION_STATUS_OLDER) {
            Popup.with(this, Popup.ID_INFO)
                    .make(R.string.alert_new_version_title, R.string.alert_new_version_message)
                    .show();
        }
    }
}
