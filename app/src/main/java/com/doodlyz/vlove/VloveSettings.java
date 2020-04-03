package com.doodlyz.vlove;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.view.ContextThemeWrapper;

import com.doodlyz.vlove.services.DownloaderService;
import com.doodlyz.vlove.services.PopupNotificationService;
import com.doodlyz.vlove.services.SaverService;
import com.doodlyz.vlove.ui.dialogs.Login;
import com.doodlyz.vlove.ui.dialogs.LoginAct;
import com.doodlyz.vlove.ui.dialogs.PopupAct;
import com.doodlyz.vlove.ui.dialogs.SaverAct;
import com.doodlyz.vlove.views.BoardScreenActivity;
import com.doodlyz.vlove.views.LaterScreenActivity;
import com.doodlyz.vlove.views.MenuScreenActivity;

import java.util.Objects;
import java.util.Set;

public class VloveSettings {
    private static VloveSettings mInstance;

    private static String KEY_POPUP_VIBRATE;
    private static String KEY_POPUP_WCHANNEL;
    private static String KEY_POPUP_BCHANNEL;
    private static String KEY_BOARD_INTERVAL;
    private static String KEY_BOARD_USERNAME;
    private static String KEY_SAVER_DPATH;
    private static String KEY_LINFO_TEXT;
    private static String KEY_LINFO_VPDID;

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public static final String VLOVE_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:74.0) Gecko/20100101 Firefox/74.0";
    public static final String APIS_NAVER_HOST = "global.apis.naver.com";
    public static final String VLIVE_HOST = "www.vlive.tv";

    private VloveSettings(Context context) {
        mContext = context.getApplicationContext();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        setAllKey();
    }

    private Context getContext() {
        return mContext;
    }

    private void setAllKey() {
        KEY_POPUP_VIBRATE = getContext().getString(R.string.pref_key_is_vibrate);
        KEY_POPUP_WCHANNEL = getContext().getString(R.string.pref_key_whitelist_channel);
        KEY_POPUP_BCHANNEL = getContext().getString(R.string.pref_key_blacklist_channel);
        KEY_BOARD_INTERVAL = getContext().getString(R.string.pref_key_sync_post_interval);
        KEY_BOARD_USERNAME = getContext().getString(R.string.pref_key_user);
        KEY_SAVER_DPATH = getContext().getString(R.string.pref_key_download_path);
        KEY_LINFO_TEXT = getContext().getString(R.string.pref_key_login_data);
        KEY_LINFO_VPDID = getContext().getString(R.string.pref_key_vpdid);
    }

    public static synchronized VloveSettings getInstance(Context context) {
        boolean contextValid = checkContext(context);
        if (contextValid) {
            if (mInstance == null || mInstance.getContext() == null) {
                mInstance = new VloveSettings(context);
            }
            return mInstance;
        }
        else {
            throw new RuntimeException("Context not valid!");
        }
    }

    public boolean isVLiveInstalled() {
        return isAppInstalled(getContext(), "com.naver.vapp");
    }

    public boolean isPopupUseVibrate() {
        return mSharedPreferences.getBoolean(KEY_POPUP_VIBRATE, false);
    }

    public boolean isPopupEnabled() {
        return isNotificationListenerEnabled(getContext(), getContext().getPackageName());
    }

    public String[] getWhitelistChannel() {
        return validateListChannel(mSharedPreferences.getString(KEY_POPUP_WCHANNEL, "").split(";"));
    }

    public String[] getBlacklistChannel() {
        return validateListChannel(mSharedPreferences.getString(KEY_POPUP_BCHANNEL, "").split(";"));
    }

    public long getBoardSyncInterval() {
        return Long.parseLong(mSharedPreferences.getString(KEY_BOARD_INTERVAL, "-1"));
    }

    public String getBoardUsername() {
        return mSharedPreferences.getString(KEY_BOARD_USERNAME, "");
    }

    public String getSaverDownloadPath() {
        return mSharedPreferences.getString(KEY_SAVER_DPATH, "1").equals("0") ? Objects.requireNonNull(getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES)).getAbsolutePath() : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
    }

    public String getVLivePlusDeviceId() {
        return mSharedPreferences.getString(KEY_LINFO_VPDID, "");
    }

    public void setVLivePlusDeviceId(String vpdid) {
        mSharedPreferences.edit().putString(KEY_LINFO_VPDID, vpdid).apply();
    }

    public Login.LoginInfo getLoginInfo() {
        String loginInfoText = mSharedPreferences.getString(KEY_LINFO_TEXT, "").trim();
        return !loginInfoText.isEmpty() ? new Login.LoginInfo(loginInfoText) : null;
    }

    public void setLoginInfo(Login.LoginInfo loginInfo) {
        mSharedPreferences.edit().putString(KEY_LINFO_TEXT, loginInfo.toString()).apply();
    }

    private String[] validateListChannel(String[] listChannel) {
        return listChannel.length == 1 && listChannel[0].trim().isEmpty() ? null : listChannel;
    }

    private boolean isAppInstalled(Context context, String appPackage) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(appPackage, PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isNotificationListenerEnabled(Context context, String packageOrClassName) {
        Set<String> listPackageApp = NotificationManagerCompat.getEnabledListenerPackages(context);
        for (String packageApp : listPackageApp) {
            if (packageApp.contains(packageOrClassName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkContext(Context context) {
        return context instanceof MenuScreenActivity ||
                context instanceof PopupNotificationService ||
                context instanceof BoardScreenActivity ||
                context instanceof LaterScreenActivity ||
                context instanceof SaverService ||
                context instanceof SettingsActivity ||
                context instanceof DownloaderService ||
                context instanceof PopupAct ||
                context instanceof LoginAct ||
                context instanceof SaverAct ||
                isContextFromBroadcast(context);
    }

    private static boolean isContextFromBroadcast(Context context) {
        return !(context instanceof Service) &&
                !(context instanceof Application) &&
                !(context instanceof ContextThemeWrapper) &&
                context instanceof ContextWrapper;
    }
}
