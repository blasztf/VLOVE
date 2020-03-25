package com.doodlyz.vlove.ui.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.doodlyz.vlove.R;

public class NotificationHelper {
    private static final String NOTIFICATION_CHANNEL_ID = "com.doodlyz.vlove.notification.channelid";

    private static NotificationManager getManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotificationCompat.Builder getBuilder(Context context) {
        return new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.symbol_white_s)
                .setColor(ContextCompat.getColor(context, R.color.color_light_black));
    }

    public static NotificationCompat.Builder getBuilder(Context context, String title, String text) {
        return getBuilder(context)
                .setContentTitle(title)
                .setContentText(text);
    }

    public static void notify(Context context, int id, Notification notification) {
        NotificationHelper.getManager(context).notify(id, notification);
    }

    public static void cancel(Context context, int id) {
        NotificationHelper.getManager(context).cancel(id);
    }

    public static void cancelAll(Context context) {
        NotificationHelper.getManager(context).cancelAll();
    }
}
