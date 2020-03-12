package com.navers.vlove.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.navers.vlove.R;
import com.navers.vlove.ui.dialogs.Popup;
import com.navers.vlove.versions.VersionControl;
import com.navers.vlove.versions.VersionControlImpl;

abstract class ScreenAbs extends AppCompatActivity implements Screen, VersionControlImpl {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
