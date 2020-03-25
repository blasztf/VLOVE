package com.doodlyz.vlove.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.doodlyz.vlove.R;
import com.doodlyz.vlove.ui.dialogs.Saver;
import com.doodlyz.vlove.ui.helper.NotificationHelper;
import com.doodlyz.vlove.views.MenuScreenActivity;

public class SaverService extends Service implements ClipboardManager.OnPrimaryClipChangedListener {
    public static final String INTENT_EXTRA_BUBBLE = SaverService.class.getSimpleName() + ".INTENT_EXTRA_BUBBLE:Z";

    private static final int SAVER_SERVICE_NOTIFICATION_ID = 0x92834212;

    private WindowManager mWindowManager;
    private ImageView mBubble;
    private ClipboardManager mClipboardManager;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();
        mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (mClipboardManager != null)
            mClipboardManager.addPrimaryClipChangedListener(this);

        startForeground(SAVER_SERVICE_NOTIFICATION_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean useBubbleIcon = intent.getBooleanExtra(INTENT_EXTRA_BUBBLE, false);

        if (useBubbleIcon) {
            enableBubbleIcon();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mBubble != null) {
            mWindowManager.removeView(mBubble);
            mBubble = null;
        }
        if (mClipboardManager != null) {
            mClipboardManager.removePrimaryClipChangedListener(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onPrimaryClipChanged() {
        // TODO Auto-generated method stub
        if (mClipboardManager.hasPrimaryClip()) {
            ClipData cData = mClipboardManager.getPrimaryClip();
            String clip = cData != null ? cData.getItemAt(0).getText().toString() : "empty";
            if (clip.contains("vlive")) {
                newDownloadVideo(clip);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableBubbleIcon() {
        mBubble = new ImageView(this);

        /* a face floating bubble as imageView */
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Point size = new Point();
        mWindowManager.getDefaultDisplay().getSize(size);

        mBubble.setImageResource(R.mipmap.ic_launcher_round);
        /* here is all the science of params */
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = size.x;
        params.y = 0;
        /* add a floatingfacebubble icon in window */
        mWindowManager.addView(mBubble, params);
        try {
            mBubble.setOnClickListener(view -> {
                // TODO Auto-generated method stub
                Toast.makeText(SaverService.this, getString(R.string.saver_has_been_stopped), Toast.LENGTH_SHORT).show();
                stopSelf();
            });

            //for moving the picture on touch and slide
            mBubble.setOnTouchListener(new View.OnTouchListener() {
                //WindowManager.LayoutParams paramsT = params;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                private long touchStartTime = 0;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //remove face bubble on long press
					/*if(System.currentTimeMillis()-touchStartTime>ViewConfiguration.getLongPressTimeout() && initialTouchX== event.getX()){
						mWindowManager.removeView(mBubble);
						stopSelf();
						return false;
					} */
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            touchStartTime = System.currentTimeMillis();
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            if ((System.currentTimeMillis() - touchStartTime > ViewConfiguration.getLongPressTimeout()) && (initialTouchX == event.getX() && initialTouchY == event.getY())) {
                                v.performClick();
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            mWindowManager.updateViewLayout(v, params);
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notification buildNotification() {
        Intent intent = new Intent(SaverService.this, MenuScreenActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, intent, 0);
        return NotificationHelper.getBuilder(this, getString(R.string.pref_group_vsaver), getString(R.string.saver_copy_to_download))
                .setContentIntent(pending).build();
    }

    private void newDownloadVideo(String url) {
        Saver.with(this, url).show();
    }
}
