package com.navers.vlove.ui.dialogs;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.navers.vlove.AppSettings;
import com.navers.vlove.R;

public class Popup extends BaseDialog {

    /**
     * Identifier for displaying information to user (this is default).
     */
    public static final int ID_INFO = 0x00000000;

    /**
     * Identifier for displaying error caused by dependency app is not installed to user.
     */
    public static final int ID_INSTALL = 0x00000001;

    /**
     * Identifier for displaying popup version of notification to user (usually when channel upload new video or on live).
     */
    public static final int ID_WATCH = 0x00000002;

    private int mId;
    private String mTitle;
    private String mMessage;
    private PendingIntent mAction;

    public static synchronized Popup with(Context context, int id) {
        return new Popup(context, id);
    }

    private Popup(Context context, int id) {
        super(context);
        mId = id;
    }

    public Popup make(@StringRes int titleId, @StringRes int messageId) {
        mTitle = titleId != -1 ? getContext().getString(titleId) : null;
        mMessage = messageId != -1 ? getContext().getString(messageId) : null;
        return this;
    }

    public Popup make(@StringRes int messageId) {
        return make(-1, messageId);
    }

    public Popup make(String title, String message) {
        mTitle = title;
        mMessage = message;
        return this;
    }

    public Popup make(String message) {
        return make(null, message);
    }

    public Popup setAction(PendingIntent action) {
        mAction = action;
        return this;
    }

    @Override
    public void show() {
        Intent intent = new Intent(getContext(), Popup.Dialog.class);
        intent.putExtra(Dialog.EXTRA_ID, mId);
        intent.putExtra(Dialog.EXTRA_TITLE, mTitle);
        intent.putExtra(Dialog.EXTRA_MESSAGE, mMessage);
        if (mAction != null) intent.putExtra(Dialog.EXTRA_ACTION, mAction);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        getContext().startActivity(intent);
        clearContext();
    }

    public static class Dialog extends android.app.Activity {
        static final String EXTRA_ID = "com.navers.vlove.Popup$Activity$EXTRA_ID:d";
        static final String EXTRA_TITLE = "com.navers.vlove.Popup$Activity$EXTRA_TITLE:LString";
        static final String EXTRA_MESSAGE = "com.navers.vlove.Popup$Activity$EXTRA_MESSAGE:LString";
        static final String EXTRA_ACTION = "com.navers.vlove.Popup$Activity$EXTRA_ACTION:LPendingIntent";

        private Vibrator mVibrator;
        private TextView mTitle, mMessage;
        private Button mPositive, mNegative;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            int id = getIntent().getIntExtra(EXTRA_ID, -1);
            String title = getIntent().getStringExtra(EXTRA_TITLE);
            String message = getIntent().getStringExtra(EXTRA_MESSAGE);
            PendingIntent action = getIntent().getParcelableExtra(EXTRA_ACTION);
            if (action == null) action = determineAction(title);

            setContentView();
            setContentText(id, title, message);
            setContentAction(action);

            setFinishOnTouchOutside(false);
            wakeLock();
            enableVibrate();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();

            disableVibrate();
        }

        private void setContentView() {
            setContentView(R.layout.activity_dialog_popup);

            mTitle = findViewById(R.id.contentTitle);
            mMessage = findViewById(R.id.contentMessage);
            mPositive = findViewById(R.id.positive);
            mNegative = findViewById(R.id.negative);
        }

        private void setContentText(int id, String title, String message) {
            if (message == null) {
                message = title;
                title = null;
            }

            if (title == null) {
                mTitle.setVisibility(View.GONE);
            }
            else {
                mTitle.setText(title);
            }

            switch (id) {
                case ID_INSTALL:
                    mPositive.setText(R.string.popup_install);
                    break;
                case ID_WATCH:
                    mPositive.setText(R.string.popup_watch);
                    break;
                default:
                    mNegative.setVisibility(View.GONE);
                    mPositive.setText(R.string.popup_ok);
                    break;
            }

            mMessage.setText(message);
        }

        private void setContentAction(final PendingIntent action) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.positive:
                            start(action);
                            break;
                    }
                    finish();
                }
            };

            mPositive.setOnClickListener(onClickListener);
            mNegative.setOnClickListener(onClickListener);
        }

        private void start(PendingIntent action) {
            if (action != null) {
                try {
                    action.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }

        private PendingIntent determineAction(String title) {
            return "NOT_INSTALLED".equals(title) ?
                    getInstallAppIntent("com.naver.vapp") :
                    null;
        }

        private PendingIntent getInstallAppIntent(String appPackageName) {
            return PendingIntent.getActivity(this, 0, new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)), 0);
        }

        private void enableVibrate() {
            if (AppSettings.getInstance(this).isPopupUseVibrate()) {
                mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (mVibrator != null && mVibrator.hasVibrator()) {
                    long[] pattern = {100, 1000, 500, 1000, 500, 1000, 500, 1000, 500, 1000, 500, 1000, 500, 1000, 500, 1000};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                    }
                    else {
                        mVibrator.vibrate(pattern, -1);
                    }
                }
            }
        }

        private void disableVibrate() {
            if (AppSettings.getInstance(this).isPopupUseVibrate() && mVibrator != null && mVibrator.hasVibrator()) mVibrator.cancel();
        }

        private void wakeLock() {
            getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }
    }
}
