package com.doodlyz.vlove.ui.dialogs;

import android.content.Context;
import android.content.Intent;

import com.doodlyz.vlove.deprecated.features.WRUtils;

import java.lang.ref.WeakReference;

/*package*/ abstract class BaseDialog {

    private static final String ACTIVITY_CLASS = "Act";

    private WeakReference<Context> mContext;
    private WeakReference<Intent>  mIntent;

    /*package*/ BaseDialog(Context context) {
        mContext = new WeakReference<>(context);
        mIntent = new WeakReference<>(new Intent(context, getActClass()));
    }

    void includeExtras(Intent intent) {

    }

    private Class<BaseDialogAct> getActClass() {
        try {
            return (Class<BaseDialogAct>) Class.forName(getClass().getAnnotation(DialogId.class).value() + ACTIVITY_CLASS);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private void setFlag(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
    }

    private Intent getIntent() {
        return WRUtils.get(mIntent);
    }

    /**
     * Get context.
     * @return context.
     */
    protected Context getContext() {
        return WRUtils.get(mContext);
    }

    /**
     * Clear context.
     */
    private void clearContext() {
        if (mContext != null) {
            mContext.clear();
            mContext = null;
        }
    }

    private void clearIntent() {
        if (mIntent != null) {
            mIntent.clear();
            mIntent = null;
        }
    }

    /**
     * Show dialog (ps: always put {@linkplain BaseDialog#clearContext()} in the end of this implemented method to make sure context reference are cleared by GC.
     */
    public void show() {
        Intent intent = getIntent();
        includeExtras(intent);
        setFlag(intent);
        getContext().startActivity(intent);
        clearIntent();
        clearContext();
    }

    @interface DialogId {
        String value();
    }

}
