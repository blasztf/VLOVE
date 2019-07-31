package com.blaszt.exeater;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Locale;

import static com.blaszt.exeater.ExEater.ConsoleActivity.ACTION_START;
import static com.blaszt.exeater.ExEater.ConsoleActivity.CONSOLE_ID;

public final class ExEater {
    private static final String ENV = File.separator + "CrashCoco_Reports" + File.separator;
    private static final String ID = "foodtasticID";
    private static final String DEFAULT_ID = "def";

    private static final boolean ENABLE = true;

//    private static final PrintStream ERR = System.err;

    private static ConsoleActivity.RecyclerAdapter adapter;

    public interface Foodtastic {
    }

    public static void eat(Foodtastic food) {
        if (ENABLE) {
            String id = getId(food);
            Context realFood;
            Intent intent;
            if (food instanceof Activity || food instanceof Service) {
                realFood = (Context) food;
                intent = new Intent(realFood, ConsoleActivity.class);
                intent.setAction(ACTION_START);
                intent.putExtra(CONSOLE_ID, id);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                realFood.startService(intent);

                try {
                    FileOutputStream fos = new FileOutputStream(getFileLog(id), true);
                    PrintStream outStream = new ParseStream(fos);
                    System.setErr(outStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static File getFileLog(String id) {
        File file;

        if (ENABLE) {
            file = new File(Environment.getExternalStorageDirectory(), String.format("%s%s.%s.%s.%s", ENV, "cc", id, "log", "txt"));
            if (!file.exists() || !file.getParentFile().isDirectory()) {
                file.getParentFile().mkdirs();
            }
        }

        return file;
    }

    private static String getId(Foodtastic food) {
        String id = DEFAULT_ID;
        try {
            id = (String) food.getClass().getField(ID).get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return id;
    }

    private static class ParseStream extends PrintStream {

        ParseStream(@NonNull OutputStream out) {
            super(out);
        }

        /**
         * Prints a string.  If the argument is <code>null</code> then the string
         * <code>"null"</code> is printed.  Otherwise, the string's characters are
         * converted into bytes according to the platform's default character
         * encoding, and these bytes are written in exactly the manner of the
         * <code>{@link #write(int)}</code> method.
         *
         * @param s The <code>String</code> to be printed
         */
        @Override
        public void print(String s) {
            super.print(s);
            printError(s);
        }

        @Override
        public void print(boolean b) {
            super.print(b);
            printError(Boolean.toString(b));
        }

        @Override
        public void print(char c) {
            super.print(c);
            printError(Character.toString(c));
        }

        @Override
        public void print(int i) {
            super.print(i);
            printError(Integer.toString(i));
        }

        @Override
        public void print(long l) {
            super.print(l);
            printError(Long.toString(l));
        }

        @Override
        public void print(float f) {
            super.print(f);
            printError(Float.toString(f));
        }

        @Override
        public void print(double d) {
            super.print(d);
            printError(Double.toString(d));
        }

        @Override
        public void print(@NonNull char[] s) {
            super.print(s);
            printError(new String(s));
        }

        @Override
        public void print(Object obj) {
            super.print(obj);
            printError(obj.toString());
        }

        @Override
        public void println() {
            super.println();
            printError("\n");
        }

        @Override
        public void println(boolean x) {
            super.println(x);
            printError(Boolean.toString(x) + "\n");
        }

        @Override
        public void println(char x) {
            super.println(x);
            printError(Character.toString(x) + "\n");
        }

        @Override
        public void println(int x) {
            super.println(x);
            printError(Integer.toString(x) + "\n");
        }

        @Override
        public void println(long x) {
            super.println(x);
            printError(Long.toString(x) + "\n");
        }

        @Override
        public void println(float x) {
            super.println(x);
            printError(Float.toString(x) + "\n");
        }

        @Override
        public void println(double x) {
            super.println(x);
            printError(Double.toString(x) + "\n");
        }

        @Override
        public void println(@NonNull char[] x) {
            super.println(x);
            printError(new String(x) + "\n");
        }

        @Override
        public void println(String x) {
            super.println(x);
            printError(x + "\n");
        }

        @Override
        public void println(Object x) {
            super.println(x);
            printError(x.toString() + "\n");
        }

        @Override
        public PrintStream printf(@NonNull String format, Object... args) {
            printError(String.format(format, args));
            return super.printf(format, args);
        }

        @Override
        public PrintStream printf(Locale l, @NonNull String format, Object... args) {
            printError(String.format(l, format, args));
            return super.printf(l, format, args);
        }

        private void printError(String s) {
            if (adapter != null) {
                adapter.addItem(s);
            }
        }
    }

    public static class ConsoleActivity extends Service {

        private WindowManager mWindowManager;
        private RecyclerView view;
        private LinearLayout container;
        private TextView title;
        private Notification.Builder notification;
        private Notification.InboxStyle console;
        private String actionStop = "Action.Console.Stop";

        static final String ACTION_START = "Action.Console.Start";
        static final String CONSOLE_ID = "Extra.Console.ID";

        private static final int NOTIF_CONS_ID = 920291;

        @Override
        public void onCreate() {
            super.onCreate();
            startForeground(12931, buildNotification());
            setupConsole();
//            buildNotificationConsole();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mWindowManager != null) {
                mWindowManager.removeView(container);
                mWindowManager = null;
                view.setAdapter(null);
                view = null;
                adapter.clear();
                adapter = null;
            }
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if (actionStop.equals(intent.getAction())) {
                stopSelf();
            } else if (ACTION_START.equals(intent.getAction())) {
                if (title != null) {
                    title.setText("Console id: " + intent.getStringExtra(CONSOLE_ID));
                }
                updateNotifConsoleSumText("Console id: " + intent.getStringExtra(CONSOLE_ID));
            }
            return super.onStartCommand(intent, flags, startId);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        private void updateNotifConsoleSumText(String sumText) {
            if (notification != null && console != null) {
                console.setSummaryText(sumText);
                notification.setContentTitle(sumText);
                NotificationManagerCompat.from(this).notify(NOTIF_CONS_ID, notification.build());
            }
        }

        private void addNotifConsoleError(String error) {
            if (notification != null && console != null) {
                console.addLine(error);
                NotificationManagerCompat.from(this).notify(NOTIF_CONS_ID, notification.build());
            }
        }

        private Notification buildNotification() {
            Intent intent = new Intent(this, this.getClass());
            intent.setAction(actionStop);

            return new Notification.Builder(this)
                    .setContentTitle("ExEater")
                    .setContentText("Tap this to close")
                    .setContentIntent(PendingIntent.getService(this, 0, intent, 0)).build();
        }

        private void buildNotificationConsole() {
            if (console == null) {
                console = new Notification.InboxStyle();
            }
            if (notification == null) {
                notification = new Notification.Builder(this)
                        .setContentTitle("ExEater")
                        .setContentText("Tap this to close")
                        .setSmallIcon(R.drawable.ic_ex_eater)
                        .setAutoCancel(true)
                        .setStyle(console);
            }
        }

        private void setupConsole() {
            final Point size = new Point();
            final int dividerW = 2;
            final int dividerH = 3;
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mWindowManager.getDefaultDisplay().getSize(size);
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    size.x / dividerW,
                    size.y / dividerH,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            params.gravity = Gravity.TOP | Gravity.START;
            params.x = size.x - params.width;
            params.y = size.y - params.height / 3;

            container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);

            if (adapter == null) adapter = new ConsoleActivity.RecyclerAdapter(this);

            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            containerParams.weight = 0.2f;

            title = new TextView(this);
            title.setGravity(Gravity.CENTER);
            title.setTextSize(12);
            title.setTextColor(Color.BLACK);
            title.setBackgroundColor(Color.GRAY);
            title.setLayoutParams(containerParams);

            containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            containerParams.weight = 0.8f;

            view = new RecyclerView(this);
            view.setBackgroundColor(0x80000000);
            view.setLayoutParams(containerParams);
            int pad = 4;
            view.setPadding(pad, pad, pad, pad);
            view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            view.setAdapter(adapter);

            container.addView(title);
            container.addView(view);

            mWindowManager.addView(container, params);

            title.setOnClickListener(new View.OnClickListener() {
                private int defH = -1;

                @Override
                public void onClick(View v) {
                    if (defH == -1) {
                        defH = params.height;
                    }

                    if (params.height != defH) {
                        params.height = defH;
                    } else {
                        params.height /= 3;
                    }
                    container.setLayoutParams(params);
                }
            });
            final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    stopSelf();
                    return true;
                }
            });
            title.setOnTouchListener(new View.OnTouchListener() {
                private static final int MAX_CLICK_DURATION = 300;
                private static final int MAX_CLICK_DISTANCE = 10;

                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                private long pressStartTime;
                private float pressedX;
                private float pressedY;
                private boolean stayedWithinClickDistance;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            pressStartTime = System.currentTimeMillis();
                            pressedX = event.getX();
                            pressedY = event.getY();
                            stayedWithinClickDistance = true;

                            break;
                        case MotionEvent.ACTION_UP:
                            long pressDuration = System.currentTimeMillis()
                                    - pressStartTime;
                            if (pressDuration < MAX_CLICK_DURATION
                                    && stayedWithinClickDistance) {
                                v.performClick();
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (stayedWithinClickDistance
                                    && distance(pressedX, pressedY, event.getX(),
                                    event.getY()) > MAX_CLICK_DISTANCE) {
                                stayedWithinClickDistance = false;
                            }

                            final int x = initialX
                                    + (int) (event.getRawX() - initialTouchX),
                                    y = initialY + (int) (event.getRawY() - initialTouchY);
                            if (x < size.x - params.width + MAX_CLICK_DISTANCE && x > -MAX_CLICK_DISTANCE) {
                                params.x = x;
                            }
                            if (y < size.y - params.height + MAX_CLICK_DISTANCE && y > -MAX_CLICK_DISTANCE) {
                                params.y = y;
                            }
                            mWindowManager.updateViewLayout(container, params);
                            break;
                    }
                    gestureDetector.onTouchEvent(event);
                    return true;
                }

                private float distance(float x1, float y1, float x2, float y2) {
                    float dx = x1 - x2;
                    float dy = y1 - y2;
                    float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);
                    return pxToDp(distanceInPx);
                }

                private float pxToDp(float px) {
                    return px / getResources().getDisplayMetrics().density;
                }
            });
//            view.setOnTouchListener(new View.OnTouchListener() {
//                private static final int MAX_LONG_CLICK_DURATION = 1000;
//                private static final int MAX_CLICK_DURATION = 500;
//                private static final int MAX_CLICK_DISTANCE = 10;
//
//                private int initialX;
//                private int initialY;
//                private float initialTouchX;
//                private float initialTouchY;
//                private long pressStartTime;
//                private float pressedX;
//                private float pressedY;
//                private boolean stayedWithinClickDistance;
//
//                @Override
//                public boolean onTouch(final View v, MotionEvent event) {
//                    switch (event.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            initialX = params.x;
//                            initialY = params.y;
//                            initialTouchX = event.getRawX();
//                            initialTouchY = event.getRawY();
//                            pressStartTime = System.currentTimeMillis();
//                            pressedX = event.getX();
//                            pressedY = event.getY();
//                            stayedWithinClickDistance = true;
//
//                            break;
//                        case MotionEvent.ACTION_UP:
//                            long pressDuration = System.currentTimeMillis()
//                                    - pressStartTime;
//                            if (pressDuration < MAX_CLICK_DURATION
//                                    && stayedWithinClickDistance) {
//                                v.performClick();
//                            } else if (pressDuration < MAX_LONG_CLICK_DURATION && stayedWithinClickDistance) {
//                                v.performLongClick();
//                            }
//                            break;
//                        case MotionEvent.ACTION_MOVE:
//                            if (stayedWithinClickDistance
//                                    && distance(pressedX, pressedY, event.getX(),
//                                    event.getY()) > MAX_CLICK_DISTANCE) {
//                                stayedWithinClickDistance = false;
//                            }
//
//                            final int x = initialX
//                                    + (int) (event.getRawX() - initialTouchX),
//                                    y = initialY + (int) (event.getRawY() - initialTouchY);
//                            if (x < size.x - params.width + MAX_CLICK_DISTANCE && x > -MAX_CLICK_DISTANCE) {
//                                params.x = x;
//                            }
//                            if (y < size.y - params.height + MAX_CLICK_DISTANCE && y > -MAX_CLICK_DISTANCE) {
//                                params.y = y;
//                            }
//                            mWindowManager.updateViewLayout(v, params);
//                            break;
//                    }
//                    return true;
//                }
//
//                private float distance(float x1, float y1, float x2, float y2) {
//                    float dx = x1 - x2;
//                    float dy = y1 - y2;
//                    float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);
//                    return pxToDp(distanceInPx);
//                }
//
//                private float pxToDp(float px) {
//                    return px / getResources().getDisplayMetrics().density;
//                }
//            });
        }

        static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ErrorHolder> {
            private ArrayList<String> errorList = new ArrayList<>();
            private Handler outputHandler = new Handler();
            private StringBuilder errorStreamContainer = new StringBuilder();

            private ConsoleActivity context;

            private Runnable outputRunnable = new Runnable() {

                @Override
                public void run() {
                    if (context != null) {
                        context.addNotifConsoleError(errorStreamContainer.toString());
                    }
                    errorList.add(0, errorStreamContainer.toString());
                    errorStreamContainer = new StringBuilder();
                    notifyDataSetChanged();
                }
            };

            public RecyclerAdapter(ConsoleActivity that) {
                super();
                context = that;
            }

            @NonNull
            @Override
            public ErrorHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return ErrorHolder.hold(LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_1, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull ErrorHolder errorHolder, int i) {
                errorHolder.setErrorText(getItem(i));
            }

            @Override
            public int getItemCount() {
                return errorList.size();
            }

            public void addItem(String error) {
                errorStreamContainer.append(error);
                outputHandler.removeCallbacks(outputRunnable);
                outputHandler.postDelayed(outputRunnable, 300);
            }

            private String getItem(int position) {
                return errorList.get(position);
            }

            void clear() {
                errorList.clear();
                notifyDataSetChanged();
            }

            static class ErrorHolder extends RecyclerView.ViewHolder {
                private TextView errorView;

                static ErrorHolder hold(View itemView) {
                    return new ErrorHolder(itemView);
                }

                ErrorHolder(@NonNull View itemView) {
                    super(itemView);
                    int pad = 4;
                    errorView = itemView.findViewById(android.R.id.text1);
                    errorView.setTextSize(8);
                    errorView.setPadding(pad, pad, pad, pad * 2);
                    errorView.setGravity(Gravity.START);
                }

                void setErrorText(String error) {
                    errorView.setText(error);
                }
            }
        }

    }
}
