package com.navers.vlove.broadcasters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.navers.vlove.Action;
import com.navers.vlove.AppSettings;
import com.navers.vlove.apis.VAPIS;
import com.navers.vlove.services.BoardService;

public class BoardBroadcasters extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Action.BOARD_SYNC.equals(intent.getAction()) || Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (!VAPIS.isExpired(context)) {
                syncBoard(context);
            } else {
                cancelSyncBoard(context);
            }
        }
        else if (Action.BOARD_SYNC_CANCEL.equals(intent.getAction())) {
            cancelSyncBoard(context);
        }
    }

    private void syncBoard(Context context) {
        long interval = AppSettings.getInstance(context).getBoardSyncInterval();
        if (getSyncBoardIntent(context, true) != null) {
            cancelSyncBoard(context);
        }
        if (interval != -1L) {
            interval *= 60L * 1000L;
            getAlarmManager(context).setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, getSyncBoardIntent(context, false));
        }
    }

    private void cancelSyncBoard(Context context) {
        PendingIntent pendingIntent = getSyncBoardIntent(context, true);
        if (pendingIntent != null) {
            getAlarmManager(context).cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private PendingIntent getSyncBoardIntent(Context context, boolean checkOnly) {
        int requestCode = 1;
        int check = 0;
        Intent intent = new Intent(context, BoardService.class);
        intent.setAction(Action.BOARD_SYNC);
        if (checkOnly) {
            check = PendingIntent.FLAG_NO_CREATE;
        }
        return PendingIntent.getService(context, requestCode, intent, check);
    }

    private AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
}


