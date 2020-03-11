package com.navers.vlove.ui.dialogs;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    static final String EXTRA_ID = "com.navers.vlove.Popup$Activity$EXTRA_ID:d";
    static final String EXTRA_TITLE = "com.navers.vlove.Popup$Activity$EXTRA_TITLE:LString";
    static final String EXTRA_MESSAGE = "com.navers.vlove.Popup$Activity$EXTRA_MESSAGE:LString";
    static final String EXTRA_ACTION = "com.navers.vlove.Popup$Activity$EXTRA_ACTION:LPendingIntent";

    private Vibrator mVibrator;
    private TextView mTitle, mMessage;
    private Button mPositive, mNegative;

    public static synchronized Popup with(Context context, int id) {
        return new Popup(context, id);
    }

    private Popup(Context context, int id) {
        super(context);
        setId(id);
    }

    public Popup() {
        super();
    }

    @BuilderMethod
    public Popup make(@StringRes int messageId) {
        return make(-1, messageId);
    }

    @BuilderMethod
    public Popup make(@StringRes int titleId, @StringRes int messageId) {
        make(
                (titleId != -1 ? getContext().getString(titleId) : null),
                (messageId != -1 ? getContext().getString(messageId) : null)
        );
        return this;
    }

    @BuilderMethod
    public Popup make(String message) {
        return make(null, message);
    }

    @BuilderMethod
    public Popup make(String title, String message) {
        getIntent().putExtra(Popup.EXTRA_TITLE, title);
        getIntent().putExtra(Popup.EXTRA_MESSAGE, message);
        return this;
    }

    @BuilderMethod
    public Popup setAction(PendingIntent action) {
        if (action != null) getIntent().putExtra(Popup.EXTRA_ACTION, action);
        return this;
    }

    @BuilderMethod
    private void setId(int id) {
        getIntent().putExtra(Popup.EXTRA_ID, id);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_dialog_popup;
    }

    @Override
    protected void onPrepareContentViewElement() {
        mTitle    = findViewById(R.id.contentTitle);
        mMessage  = findViewById(R.id.contentMessage);
        mPositive = findViewById(R.id.positive);
        mNegative = findViewById(R.id.negative);
    }

    @Override
    protected void onReady(Intent intent) {
        int id = intent.getIntExtra(EXTRA_ID, -1);
        String title = intent.getStringExtra(EXTRA_TITLE);
        String message = intent.getStringExtra(EXTRA_MESSAGE);

        // Show popup notification (force user to see the notification [mwahahahahahaha]).
        if (isScreenLocked()) {
            PendingIntent action = intent.getParcelableExtra(EXTRA_ACTION);
            if (action == null) action = determineAction(title);

            setContentText(id, title, message);
            setContentAction(action);

            setFinishOnTouchOutside(false);
            wakeLock();
            enableVibrate();
        }
        // Toast that information to user.
        else {
            toastContent(title, message);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableVibrate();
    }

    private void toastContent(String title, String message) {
        Toast toast = Toast.makeText(this,  title + "\n" + message, Toast.LENGTH_SHORT);
        View toastView = toast.getView();
        toastView.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.color_light_blue), PorterDuff.Mode.SRC_IN);
        toast.show();
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
        View.OnClickListener onClickListener = view -> {
            switch (view.getId()) {
                case R.id.positive:
                    Popup.this.start(action);
                    break;
                case R.id.negative:
                    break;
            }
            Popup.this.finish();
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

    private boolean isScreenLocked() {
        KeyguardManager kgManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return kgManager != null && kgManager.isKeyguardLocked();
    }

    private void wakeLock() {
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
//                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
    }
}
