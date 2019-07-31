package com.navers.vlove.settings.preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;

public class VersionPreference extends Preference {
    private static final String ERROR = "Failed to load version code.";

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VersionPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }

    public VersionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public VersionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public VersionPreference(Context context) {
        super(context);
        setup();
    }

    private void setup() {
        setSummary(getVersion());
    }

    private String getVersion() {
        try {
            PackageInfo packageInfo = getContext().getApplicationContext().getPackageManager().getPackageInfo(getContext().getApplicationContext().getPackageName(), 0);
            return packageInfo.versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            return ERROR;
        }
    }


}
