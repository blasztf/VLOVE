package com.navers.vlove.ui.dialogs;

import android.content.Context;

import java.lang.ref.WeakReference;

public abstract class BaseDialog {
    private WeakReference<Context> mContext;

    /*package*/ BaseDialog(Context context) {
        mContext = new WeakReference<>(context);
    }

    private BaseDialog() {}

    /**
     * Get context.
     * @return context.
     */
    protected Context getContext() {
        return mContext.get();
    }

    /**
     * Clear context.
     */
    /*package*/ void clearContext() {
        mContext.clear();
        mContext = null;
    }

    /**
     * Show dialog (ps: always put {@linkplain BaseDialog#clearContext()} in the end of this implemented method to make sure context reference are cleared by GC.
     */
    public abstract void show();
}
