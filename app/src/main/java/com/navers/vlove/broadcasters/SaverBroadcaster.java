package com.navers.vlove.broadcasters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SaverBroadcaster extends BroadcastReceiver {
    public static final String BROADCAST_FILTER_SAVER = SaverBroadcaster.class.getSimpleName() + ".BROADCAST_FILTER_DOWNLOAD:LString";

    public static final String BRIDGE_FILTER_DATA_PROGRESS = SaverBroadcaster.class.getSimpleName() + ".BRIDGE_FILTER_DATA_PROGRESS:LString";
    public static final String BRIDGE_FILTER_DATA_STATUS = SaverBroadcaster.class.getSimpleName() + ".BRIDGE_FILTER_DATA_STATUS:LString";

    public static final int BRIDGE_STATUS_SUCCESS = 1000;
    public static final int BRIDGE_STATUS_FAILED = -1;

    private OnSaverListener mListener;

    public interface OnSaverListener {
        void onDownload(int progress, String status);
        void onError();
        void onSuccess();
    }

    public void setListener(OnSaverListener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SaverBroadcaster.BROADCAST_FILTER_SAVER.equals(intent.getAction())) {
            int progress = intent.getIntExtra(BRIDGE_FILTER_DATA_PROGRESS, BRIDGE_STATUS_FAILED);
            String status = intent.getStringExtra(BRIDGE_FILTER_DATA_STATUS);

            if (mListener != null) {
                switch (progress) {
                    case BRIDGE_STATUS_FAILED:
                        mListener.onError();
                        break;
                    case BRIDGE_STATUS_SUCCESS:
                        mListener.onSuccess();
                        break;
                    default:
                        mListener.onDownload(progress, status);
                        break;
                }
            }
        }
    }
}
