package com.navers.vlove.ui.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;

public abstract class BaseDialog extends Activity {
    private WeakReference<Context> mContext;
    private WeakReference<Intent>  mIntent;

    /*package*/ BaseDialog(Context context) {
        mContext = new WeakReference<>(context);
        mIntent = new WeakReference<>(new Intent(context, this.getClass()));
        buildContextIntent();
//        onPrepare(getIntent());
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.METHOD)
    @interface BuilderMethod {}

    private BaseDialog() {}

//    protected abstract void onPrepare(Intent intent);

    protected abstract int getContentViewId();

    protected abstract void onPrepareContentViewElement();

    protected abstract void onReady(Intent intent);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        onPrepareContentViewElement();
        onReady(getIntent());
    }

    private void buildContextIntent() {
        getIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getIntent().addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
    }

//    /*package*/ void putContextIntent(String name, String value) {
//        getContextIntent().putExtra(name, value);
//    }

    /**
     * Get context.
     * @return context.
     */
    protected Context getContext() {
        return mContext != null ? mContext.get() : null;
    }

    /**
     * Clear context.
     */
    /*package*/ void clearContext() {
        if (mContext != null) {
            mContext.clear();
            mContext = null;
        }
    }

    @Override
    public Intent getIntent() {
        return mIntent != null ? mIntent.get() : super.getIntent();
    }

//    protected Intent getContextIntent() {
//        return mIntent.get();
//    }

    /*package*/ void clearIntent() {
        if (mIntent != null) {
            mIntent.clear();
            mIntent = null;
        }
    }

    /**
     * Show dialog (ps: always put {@linkplain BaseDialog#clearContext()} in the end of this implemented method to make sure context reference are cleared by GC.
     */
    public void show() {
        getContext().startActivity(getIntent());
        clearIntent();
        clearContext();
    }
}
