package com.hades.mission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import java.util.Set;

public class PermissionCompat {
    private static final String PERMISSION_PRIVATE = "permission_compat";

    /**
     * Request permission listener for action that need dangerous permission in API >= 23 (Android Marshmallow & above).
     *
     * @author Hades
     */
    public interface OnPermissionRequestedListener {
        /**
         * Callback when permission need to be asked.
         */
        public void onPermissionNeeded(String permission);

        /**
         * Callback when permission is denied before.
         */
        public void onPermissionShouldExplain(String permission);

        /**
         * Callback when permission disabled (when user check "Never show again" & denied it).
         */
        public void onPermissionDisabled(String permission);

        /**
         * Callback when permission is granted.
         */
        public void onPermissionGranted(String permission);
    }

    /**
     * Use this instead of {@linkplain PermissionCompat.OnPermissionRequestedListener} to override needed block.
     *
     * @author Hades
     */
    public static class SimpleOnPermissionRequestedListener implements OnPermissionRequestedListener {

        /**
         * Callback when permission need to be asked.
         *
         * @param permission
         */
        @Override
        public void onPermissionNeeded(String permission) {

        }

        /**
         * Callback when permission is denied before.
         *
         * @param permission
         */
        @Override
        public void onPermissionShouldExplain(String permission) {

        }

        /**
         * Callback when permission disabled (when user check "Never show again" & denied it).
         *
         * @param permission
         */
        @Override
        public void onPermissionDisabled(String permission) {

        }

        /**
         * Callback when permission is granted.
         *
         * @param permission
         */
        @Override
        public void onPermissionGranted(String permission) {

        }
    }

    /**
     * Check if API >= 26 (Android Oreo & above) or not.
     *
     * @return <b>true</b> if API >= 26 (Android Oreo & above), <b>false</b> otherwise.
     */
    public static boolean APIGET26() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * Check if API >= 23 (Android Marshmallow & above) or not.
     *
     * @return <b>true</b> if API >= 23 (Android Marshmallow & above), <b>false</b> otherwise.
     */
    public static boolean APIGET23() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Check if API >= 20 (Android Kitkat Watch & above) or not.
     *
     * @return <b>true</b> if API >= 20 (Android Kitkat Watch & above), <b>false</b> otherwise.
     */
    public static boolean APIGET20() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH;
    }

    /**
     * Check if API >= 16 (Android Jelly Bean & above) or not.
     *
     * @return <b>true</b> if API >= 16 (Android Jelly Bean & above), <b>false</b> otherwise.
     */
    public static boolean APIGET16() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * Check if permission should be requested or not.
     *
     * @param context
     * @param permission
     */
    private static boolean shouldRequestPermission(Context context, String permission) {
        return !permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE) || APIGET16() ?
                ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED :
                true;
    }

    /**
     * Check if permission should be display permission detail.
     *
     * @param activity
     * @param permission
     */
    private static boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * Check if permission already requested. If not, add permission to "remember me".
     *
     * @param context
     * @param permission
     * @return
     */
    private static boolean isPermissionAlreadyRequested(Context context, String permission) {
        boolean already = false;
        already = context.getApplicationContext().getSharedPreferences(PERMISSION_PRIVATE, Context.MODE_PRIVATE).getBoolean(permission, false);
        if (!already) {
            context.getApplicationContext().getSharedPreferences(PERMISSION_PRIVATE, Context.MODE_PRIVATE).edit().putBoolean(permission, true).apply();
        }
        return already;
    }

    /**
     * Handling action for API >= 23 (Android Marshmallow & above) that need dangerous permission (for API < 23, it will be executed right away).
     *
     * @param activity
     * @param permission
     * @param listener
     */
    public static void shouldRequestPermission(Activity activity, String permission, OnPermissionRequestedListener listener) {
        if (!isPermissionSetting(activity, permission, listener)) {
            if (shouldRequestPermission(activity, permission)) {
                if (isPermissionAlreadyRequested(activity, permission)) {
                    if (!shouldShowRequestPermissionRationale(activity, permission)) {
                        listener.onPermissionDisabled(permission);
                    } else {
                        listener.onPermissionShouldExplain(permission);
                    }
                } else {
                    listener.onPermissionNeeded(permission);
                }
            } else {
                listener.onPermissionGranted(permission);
            }
        }
    }

    public static void shouldRequestPermissions(Activity activity, String[] permissions, OnPermissionRequestedListener listener) {
        for (String permission : permissions) {
            shouldRequestPermission(activity, permission, listener);
        }
    }
//    public static void shouldRequestPermission(Activity activity, String permission, OnPermissionRequestedListener listener) {
//        if (!isPermissionSetting(activity, permission, listener)) {
//            if (shouldRequestPermission(activity, permission)) {
//                if (isPermissionAlreadyRequested(activity, permission)) {
//                    if (!shouldShowRequestPermissionRationale(activity, permission)) {
//                        listener.onPermissionDisabled();
//                    } else {
//                        listener.onPermissionShouldExplain();
//                    }
//                } else {
//                    listener.onPermissionNeeded();
//                }
//            } else {
//                listener.onPermissionGranted();
//            }
//        }
//    }

    /**
     * Handle permission that categorized as permission_setting. Requested permission will be process right away.
     *
     * @param activity
     * @param permission
     * @param listener
     * @return <b>true</b> if and only if <b>permission</b> is permission_setting, <b>false</b> otherwise.
     */
    public static boolean isPermissionSetting(Activity activity, String permission, OnPermissionRequestedListener listener) {
        if (!APIGET23()) return false;
        if (permission.equals(ManifestCompat.permission_setting.NOTIFICATION_LISTENER_SETTINGS)) {
            handleNotificationListenerSettings(activity, permission, listener);
            return true;
        } else if (permission.equals(ManifestCompat.permission_setting.MANAGE_OVERLAY_PERMISSION)) {
            handleManageOverlayPermission(activity, permission, listener);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if notification listener already enabled or not.
     *
     * @param context
     * @return <b>true</b> if and only if notification listener enabled, <b>false</b> otherwise.
     */
    public static boolean isNotificationListenerEnabled(Context context) {
        Set<String> listPackageApp = NotificationManagerCompat.getEnabledListenerPackages(context);
        for (String packageApp : listPackageApp) {
            if (packageApp.equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if overlay permission already enabled or not.
     *
     * @param context
     * @return <b>true</b> if and only if overlay permission enabled, <b>false</b> otherwise.
     */
    public static boolean isOverlayPermissionEnabled(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }

    /**
     * Handle notification listener request when requested with {@linkplain PermissionCompat#shouldRequestPermission(Activity activity, String permission, OnPermissionRequestedListener listener)}
     *
     * @param context
     * @param listener
     */
    private static void handleNotificationListenerSettings(Context context, String permission, OnPermissionRequestedListener listener) {
        if (!isNotificationListenerEnabled(context)) {
            if (isPermissionAlreadyRequested(context, ManifestCompat.permission_setting.NOTIFICATION_LISTENER_SETTINGS)) {
                listener.onPermissionShouldExplain(permission);
            } else {
                listener.onPermissionNeeded(permission);
            }
        } else {
            listener.onPermissionGranted(permission);
        }
    }

    /**
     * Handle overlay permission request when requested with {@linkplain PermissionCompat#shouldRequestPermission(Activity activity, String permission, OnPermissionRequestedListener listener)}
     *
     * @param context
     * @param listener
     */
    private static void handleManageOverlayPermission(Context context, String permission, OnPermissionRequestedListener listener) {
        if (!isOverlayPermissionEnabled(context)) {
            if (isPermissionAlreadyRequested(context, ManifestCompat.permission_setting.MANAGE_OVERLAY_PERMISSION)) {
                listener.onPermissionShouldExplain(permission);
            } else {
                listener.onPermissionNeeded(permission);
            }
        } else {
            listener.onPermissionGranted(permission);
        }
    }

    /**
     * Request permission notification listener.
     *
     * @param activity
     * @param requestCode
     */
    private static void requestPermissionNotificationListener(Activity activity, int requestCode) {
        Intent intent = new Intent(ManifestCompat.permission_setting.NOTIFICATION_LISTENER_SETTINGS);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Request permission overlay.
     *
     * @param activity
     * @param requestCode
     */
    private static void requestPermissionOverlay(Activity activity, int requestCode) {
        Intent intent = new Intent(ManifestCompat.permission_setting.MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Request permissions that categorized as permission_setting.
     *
     * @param activity
     * @param requestCode
     */
    public static void requestPermissionSettings(Activity activity, String permission, int requestCode) {
        if (permission.equals(ManifestCompat.permission_setting.NOTIFICATION_LISTENER_SETTINGS)) {
            requestPermissionNotificationListener(activity, requestCode);
        } else if (permission.equals(ManifestCompat.permission_setting.MANAGE_OVERLAY_PERMISSION)) {
            requestPermissionOverlay(activity, requestCode);
        } else throw new RuntimeException("Permission setting not supported!");
    }

    /**
     * Request one or more permission.
     *
     * @param activity
     * @param requestCode
     * @param permissions
     */
    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }
}
