package com.doodlyz.vlove.ui.dialogs;

import android.content.Context;
import android.content.Intent;

public class Translator extends BaseDialog {
    private String mUrlOrContent;

    public static synchronized Translator with(Context context) {
        return new Translator(context);
    }

    private Translator(Context context) {
        super(context);
    }

    public void translate(String urlOrContent) {
        mUrlOrContent = urlOrContent;
        show();
    }

    @Override
    void includeExtras(Intent intent) {
        intent.putExtra(TranslatorAct.EXTRA_POST, mUrlOrContent);
    }
}
