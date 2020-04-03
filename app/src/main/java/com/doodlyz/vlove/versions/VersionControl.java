package com.doodlyz.vlove.versions;

import android.content.Context;

import com.doodlyz.vlove.BuildConfig;
import com.doodlyz.vlove.R;
import com.doodlyz.vlove.VloveRequest;

import static com.doodlyz.vlove.versions.VersionControlImpl.VERSION_STATUS_ERROR;
import static com.doodlyz.vlove.versions.VersionControlImpl.VERSION_STATUS_NEWER;
import static com.doodlyz.vlove.versions.VersionControlImpl.VERSION_STATUS_OLDER;

public final class VersionControl {

    public static void validate(Context context) {
        final VersionControlImpl versionControl = validateContext(context);
        if (versionControl != null) {
            VloveRequest.ApiRequest request;
            request = new VloveRequest.ApiRequest(
                    context.getString(R.string.url_version_control_source),
                    response -> versionControl.onVersionReady(getValidationStatus(response)),
                    error -> versionControl.onVersionReady(VERSION_STATUS_ERROR));
            VloveRequest.with(context).addToQueue(request);
        }
    }

//    public static int getVersionCode() {
//        return BuildConfig.VERSION_CODE;
//    }
//
//    public static String getVersionName() {
//        return BuildConfig.VERSION_NAME;
//    }

    private static boolean isPackageValid(Context context) {
        return true;
//        return "com.doodlyz.vlove".equals(context.getApplicationContext().getPackageName().substring(0, 17));
    }

    private static VersionControlImpl validateContext(Context context) {
        if (isPackageValid(context)) {
            if (context instanceof VersionControlImpl) {
                return (VersionControlImpl) context;
            } else {
                return null;
            }
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
