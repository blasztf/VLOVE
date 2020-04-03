package com.doodlyz.vlove.ui.dialogs;

import android.content.Context;
import android.content.Intent;

@BaseDialog.DialogId("Saver")
public class Saver extends BaseDialog {
    private String mUri;

    public static Saver with(Context context, String videoURI) {
        return new Saver(context, videoURI);
    }

    private Saver(Context context, String videoURI) {
        super(context);
        setVideo(videoURI);
    }

    private void setVideo(String uri) {
        mUri = uri;
    }

    @Override
    void includeExtras(Intent intent) {
        intent.putExtra(SaverAct.EXTRA_VIDEO_URL, mUri);
    }
}
