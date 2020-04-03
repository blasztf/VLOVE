package com.doodlyz.vlove.ui.dialogs;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.doodlyz.vlove.R;

@BaseDialog.DialogId("Popup")
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

    private String mTitle, mMessage;
    private int mId;
    private PendingIntent mAction;

    public static synchronized Popup with(Context context, int id) {
        return new Popup(context, id);
    }

    private Popup(Context context, int id) {
        super(context);
        setId(id);
    }

    public Popup make(@StringRes int messageId) {
        return make(-1, messageId);
    }

    public Popup make(@StringRes int titleId, @StringRes int messageId) {
        make(
                (titleId != -1 ? getContext().getString(titleId) : null),
                (messageId != -1 ? getContext().getString(messageId) : null)
        );
        return this;
    }

    public Popup make(String message) {
        return make(null, message);
    }

    public Popup make(String title, String message) {
        mTitle = title;
        mMessage = message;
        return this;
    }

    public Popup setAction(PendingIntent action) {
        mAction = action;
        return this;
    }

    private void setId(int id) {
        mId = id;
    }

    private boolean isScreenLocked() {
        KeyguardManager kgManager = (KeyguardManager) getContext().getSystemService(Context.KEYGUARD_SERVICE);
        return kgManager != null && kgManager.isKeyguardLocked();
    }

    private void toast(String title, String message) {
        Toast toast = Toast.makeText(getContext(),  title + "\n" + message, Toast.LENGTH_SHORT);
        View toastView = toast.getView();
        toastView.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.color_light_blue), PorterDuff.Mode.SRC_IN);
        toast.show();
    }

    @Override
    void includeExtras(Intent intent) {
        intent.putExtra(PopupAct.EXTRA_ID, mId);
        intent.putExtra(PopupAct.EXTRA_TITLE, mTitle);
        intent.putExtra(PopupAct.EXTRA_MESSAGE, mMessage);
        if (mAction != null) {
            intent.putExtra(PopupAct.EXTRA_ACTION, mAction);
        }
    }

    @Override
    public void show() {
        if (!isScreenLocked()) {
            toast(mTitle, mMessage);
        }
        else {
            super.show();
        }
    }
}
