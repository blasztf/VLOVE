package com.doodlyz.vlove.ui.dialogs;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;

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

    @Override
    void includeExtras(Intent intent) {
        intent.putExtra(PopupAct.EXTRA_ID, mId);
        intent.putExtra(PopupAct.EXTRA_TITLE, mTitle);
        intent.putExtra(PopupAct.EXTRA_MESSAGE, mMessage);
        if (mAction != null) {
            intent.putExtra(PopupAct.EXTRA_ACTION, mAction);
        }
    }
}
