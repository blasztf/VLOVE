package com.navers.vlove.deprecated.features;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blaszt.exeater.ExEater;
import com.navers.vlove.R;
import com.navers.vlove.databases.Board;
import com.navers.vlove.logger.CrashCocoExceptionHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestActivity2 extends AppCompatActivity implements ExEater.Foodtastic {
    private EditText editText;
    private Button button;
    private TextView textView;
    private TextViewWrapper tv;

    public static final String foodtasticID = "sms";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ExEater.eat(this);

        Thread.setDefaultUncaughtExceptionHandler(new CrashCocoExceptionHandler("vltest2"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);

        tv = TextViewWrapper.wrap(textView);

        try {
            System.setErr(new ParseStream(ExEater.getFileLog("vltest212")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            tv.appendText(e.getMessage());
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                testComment();
//                try {
//                    throw new Exception("Hi Test Exception for ExEater");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                tv.appendText(getString(R.string.loading));
                String action = editText.getText().toString();
                deepLink();
//                exec();
//                exec(action);
//                textView.setText(result);
            }
        });
    }

    private class ParseStream extends PrintStream {

        ParseStream(@NonNull OutputStream out) {
            super(out);
        }

        public ParseStream(@NonNull File file) throws FileNotFoundException {
            super(file);
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
            if (tv != null) {
                tv.appendText(s);
            }
        }
    }

    private static class TextViewWrapper {
        private TextView tv;

        static TextViewWrapper wrap(TextView tv) {
            return new TextViewWrapper(tv);
        }

        TextViewWrapper(TextView tv) {
            this.tv = tv;
        }

        TextViewWrapper setText(String text) {
            tv.setText(text);
            return this;
        }

        TextViewWrapper appendText(String text) {
            setText(tv.getText() + text + "\n\n");
            return this;
        }
    }

    private void deepLink() {
        Intent explicitIntent ;
        Intent implicitIntent = new Intent("android.intent.action.VIEW");
//        implicitIntent.setComponent(new ComponentName("tv.vlive.activity", "tv.vlive.activity.DeepLinkActivity"));
        implicitIntent.addCategory(Intent.CATEGORY_DEFAULT);
        implicitIntent.addCategory(Intent.CATEGORY_BROWSABLE);

        explicitIntent = implicitIntent;
//        PackageManager pm = getApplicationContext().getPackageManager();
//        List<ResolveInfo> resolveInfoList = pm.queryIntentServices(implicitIntent, 0);
//        if (resolveInfoList != null && resolveInfoList.size() == 1) {
//            ResolveInfo resolveInfo = resolveInfoList.get(0);
//            ComponentName componentName = new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
//            explicitIntent = new Intent(implicitIntent);
//            explicitIntent.setComponent(componentName);
//        } else {
//            explicitIntent = implicitIntent;
//        }

        explicitIntent.setData(Uri.parse("https://www.vlive.tv/video/79735"));
        startActivity(explicitIntent);
    }

    private void exec() {
        try {
            Context context = getApplicationContext().createPackageContext("com.naver.vapp", Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            Class<?> clazz = context.getClassLoader().loadClass("com.campmobile.vfan.feature.board.detail.PostViewActivity");
            Intent intent;
            intent = new Intent(context, clazz);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            tv.appendText(e.getMessage());
        } finally {
            try {
                Context context = getApplicationContext().createPackageContext("com.naver.vapp", Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
                Class<?> clazz = context.getClassLoader().loadClass("com.campmobile.vfan.feature.board.detail.PostViewActivity");
                Intent intent;
                intent = new Intent(context, clazz);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ResolveInfo resolveInfo = getApplicationContext().getPackageManager().resolveActivity(intent, 0);
//                resolveInfo.activityInfo.exported = true;
//                context.startActivity(intent);
                tv.appendText(resolveInfo.activityInfo.applicationInfo.name)
                        .appendText(resolveInfo.activityInfo.applicationInfo.packageName)
                .appendText(resolveInfo.activityInfo.applicationInfo.uid + "");

                startActivityAsCaller(intent, null, true, resolveInfo.activityInfo.applicationInfo.uid);
            } catch (Exception e) {
                e.printStackTrace();
                tv.appendText(e.getMessage());
            }
        }
    }

    private void startActivityAsCaller(Intent intent, Bundle options, boolean ignoreTargetSecurity, int userId) {
        try {
            Class c = getClass();
            Method m = c.getMethod("startActivityAsCaller", Intent.class, Bundle.class, boolean.class, int.class);
            m.invoke(this, intent, options, ignoreTargetSecurity, userId);
        } catch (Exception e) {
            e.printStackTrace();
            tv.appendText(e.getMessage());
        }
    }

    private void exec(String action) {
        String[] actions = action.split(":");
        if ("a".equals(actions[0])) {
            startActivity(new Intent(actions[1]));
        } else if ("s".equals(actions[0])) {
            startService(explicitIntent(new Intent(actions[1])));
        } else if ("b".equals(actions[0])) {
            sendBroadcast(new Intent(actions[1]));
        } else {
            textView.setText("Penanganan aksi tidak dapat dilakukan");
        }
    }

//    private Intent explicitActivity() {
//        PackageManager pm = getApplicationContext().getPackageManager();
//
//        Intent intent = new Intent(getApplicationContext().createPackageContext("com.naver.vapp", Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE), );
//        List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(, )
//    }

    private Intent explicitIntent(Intent implicitIntent) {
        PackageManager pm = getApplicationContext().getPackageManager();
        List<ResolveInfo> resolveInfoList = pm.queryIntentServices(implicitIntent, 0);
        if (resolveInfoList != null && resolveInfoList.size() == 1) {
            ResolveInfo resolveInfo = resolveInfoList.get(0);
            ComponentName componentName = new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
            Intent explicitIntent = new Intent(implicitIntent);
            explicitIntent.setComponent(componentName);
            return explicitIntent;
        } else {
            return implicitIntent;
        }
    }

    private void testComment() {
        textView.setText(getString(R.string.loading));
        List<Board.Comment> oldComments, newComments;
        oldComments = new ArrayList<>();
        newComments = new ArrayList<>();

        for (int i = 0, l = 5; i < l; i++) {
            oldComments.add(new Board.Comment.Builder(Integer.toString(i))
            .setContent("Comment " + i)
            .setUser("User " + i)
            .build());
        }

        for (int i = 0, l = 3; i < l; i++) {
            newComments.add(new Board.Comment.Builder(Integer.toString(i))
                    .setContent("Comment " + i)
                    .setUser("User " + i)
                    .build());
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0, l = oldComments.size(); i < l; i++) {
            if (!newComments.contains(oldComments.get(i))) {
                builder.append("new comments not contain old commnet with id ").append(i).append("\n");
            }
        }

        List<Board.Comment> tempComments = newComments;

        int i = 5;
        tempComments.add(new Board.Comment.Builder(Integer.toString(i))
                .setContent("Comment " + i)
                .setUser("User " + i)
                .build());

        i = 6;
        tempComments.add(new Board.Comment.Builder(Integer.toString(i))
                .setContent("Comment " + i)
                .setUser("User " + i)
                .build());
        builder.append("size new comments = ").append(newComments.size()).append("\n");
        builder.append("size temp comments = ").append(tempComments.size()).append("\n");

        textView.setText(builder.toString());
    }
}
