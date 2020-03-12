package com.navers.vlove.versions;

import android.content.Context;

import com.navers.vlove.BuildConfig;
import com.navers.vlove.VolleyRequest;

import static com.navers.vlove.versions.VersionControlImpl.VERSION_STATUS_ERROR;
import static com.navers.vlove.versions.VersionControlImpl.VERSION_STATUS_NEWER;
import static com.navers.vlove.versions.VersionControlImpl.VERSION_STATUS_OLDER;

public final class VersionControl {
    private static final String VERSION_CONTROL_SOURCE = "";

    public static void validate(Context context) {
        final VersionControlImpl versionControl = validateContext(context);
        if (versionControl != null) {
            VolleyRequest.StringRequest request;
            request = new VolleyRequest.StringRequest(VERSION_CONTROL_SOURCE,
                    response -> versionControl.onVersionReady(getValidationStatus(response)),
                    error -> versionControl.onVersionReady(VERSION_STATUS_ERROR));
            VolleyRequest.with(context).addToQueue(request);
        }
    }

    public static int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    private static VersionControlImpl validateContext(Context context) {
        if (context instanceof VersionControlImpl) {
            return (VersionControlImpl) context;
        }
        else {
            return null;
        }
    }

    private static int getValidationStatus(String response) {
        String[] versions = response.split("\\|");
        int versionCode = Integer.parseInt(versions[0]);
//        String versionName = versions[1];

        if (BuildConfig.VERSION_CODE < versionCode) {
            return VERSION_STATUS_OLDER;
        }
        else if (BuildConfig.VERSION_CODE == versionCode) {
            return VERSION_STATUS_NEWER;
        }
        else {
            return VERSION_STATUS_ERROR;
        }
    }
}
