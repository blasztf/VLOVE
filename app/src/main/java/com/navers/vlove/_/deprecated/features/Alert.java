package com.navers.vlove._.deprecated.features;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class Alert {
    public static final int LENGTH_SHORT = 0;
    public static final int LENGTH_LONG = 1;
    public static final int LENGTH_INDEFINITE = 2;

    private static final String BUNDLE_KEY_TYPE = "TYPE";
    private static final String BUNDLE_KEY_STRING_RES = "STRING_RES";
    private static final String BUNDLE_KEY_STRING = "STRING";

    private static final int TYPE_TOAST = 0;
    private static final int TYPE_SNACK = 1;

    private static Alert mInstance;

    private int duration = LENGTH_LONG;

    private Handler handler;

    private final Context context;

    private static class AlertHandler extends Handler {
        private WeakReference<Alert> outerRef;

        AlertHandler(Alert outer) {
            outerRef = new WeakReference<>(outer);
        }

        private Alert getReference() {
            return outerRef.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = (Bundle) msg.obj;
            int type = bundle.getInt(BUNDLE_KEY_TYPE);
            switch (type) {
                case TYPE_TOAST:
                    String message = bundle.getInt(BUNDLE_KEY_STRING_RES, 0) == 0 ? bundle.getString(BUNDLE_KEY_STRING) : getReference().getContext().getString(bundle.getInt(BUNDLE_KEY_STRING_RES));
                    Toast.makeText(getReference().getContext(), message, getReference().duration == Alert.LENGTH_SHORT ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    }

    public static Alert with(Context context) {
        return new Alert(context);
    }

    private Alert(Context context) {
        this.context = context;
    }

    private Context getContext() {
        return context;
    }

    private Thread makeThread(final int type, final String message) {
        return new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt(BUNDLE_KEY_TYPE, type);
                bundle.putString(BUNDLE_KEY_STRING, message);
                msg.obj = bundle;
                handler.sendMessage(msg);
            }
        };
    }

    private Thread makeThread(final int type, final int messageResId) {
        return new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt(BUNDLE_KEY_TYPE, type);
                bundle.putInt(BUNDLE_KEY_STRING_RES, messageResId);
                msg.obj = bundle;
                handler.sendMessage(msg);
            }
        };
    }

    private void makeHandler() {
        handler = new AlertHandler(this);
    }

    public void showToast(String message) {
        makeHandler(); makeThread(TYPE_TOAST, message).start();
    }

    public void showToast(int messageResId) {
        makeHandler(); makeThread(TYPE_TOAST, messageResId).start();
    }

    public Alert setDuration(int duration) {
        this.duration = duration;
        return this;
    }
}
