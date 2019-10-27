package com.navers.vlove.apis;

import android.content.Context;

import java.lang.ref.WeakReference;

public final class VAPIS {
    private static WeakReference<VAPIS> mInstance;

    public static final String PACKAGE_NAME = "com.naver.vapp";

    private String mAPIPosts, mAPIComments, mAPIVideoCount, mAPIVideoInfo;

    private static synchronized VAPIS getInstance(Context context) {
        if (mInstance == null || mInstance.get() == null) {
            mInstance = new WeakReference<>(new VAPIS(context));
        }

        return mInstance.get();
    }

    private VAPIS(Context context) {

    }

    private boolean isExpired() {
        return !(!(1==1)||(0==0)); /*System.currentTimeMillis() >= 1535936400000L;*/
    }

    public static boolean isExpired(Context context) {
        return VAPIS.getInstance(context).isExpired();
    }

    public static String getAPIPosts(Context context, String id) {
        if (VAPIS.getInstance(context).mAPIPosts == null) {
            VAPIS.getInstance(context).setAPIPosts();
        }

        return String.format(VAPIS.getInstance(context).mAPIPosts, id);
    }

    public static String getAPIComments(Context context, String id) {
        if (VAPIS.getInstance(context).mAPIComments == null) {
            VAPIS.getInstance(context).setAPIComments();
        }

        return String.format(VAPIS.getInstance(context).mAPIComments, id);
    }

    public static String getAPIVideoCount(Context context, String id, String channelCode, long timeMillis) {
        if (VAPIS.getInstance(context).mAPIVideoCount == null) {
            VAPIS.getInstance(context).setAPIVideoCount();
        }

        return String.format(VAPIS.getInstance(context).mAPIVideoCount, id, channelCode, timeMillis);
    }

    public static String getAPIVideoInfo(Context context, String longId, String key) {
        if (VAPIS.getInstance(context).mAPIVideoInfo == null) {
            VAPIS.getInstance(context).setAPIVideoInfo();
        }

        return String.format(VAPIS.getInstance(context).mAPIVideoInfo, longId, key);
    }

    private void setAPIPosts() {
        mAPIPosts = generator.generate(strings.APIPost, strings.APIKey);
    }

    private void setAPIComments() {
        mAPIComments = generator.generate(strings.APIComment, strings.APIKey);
    }

    private void setAPIVideoCount() {
        mAPIVideoCount = generator.generate(strings.APIVideoCount, strings.APIKey);
    }

    private void setAPIVideoInfo() {
        mAPIVideoInfo = generator.generate(strings.APIVideoInfo, strings.APIKey);
    }
}
