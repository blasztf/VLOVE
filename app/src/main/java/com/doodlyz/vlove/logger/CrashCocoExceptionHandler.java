package com.doodlyz.vlove.logger;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import toolkit.util.TIO;
import toolkit.util.TStrings;

public class CrashCocoExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String ENV = File.separator + "CrashCoco_Reports" + File.separator;

    private String id;
    private Thread.UncaughtExceptionHandler defaultHandler;

    private static final boolean enable = false;

    private boolean isErrorSet = false;

    private static CrashCocoExceptionHandler mInstance;

    public static synchronized CrashCocoExceptionHandler with(String id) {
        if (mInstance == null) {

            mInstance = new CrashCocoExceptionHandler();

        }

        mInstance.id = id;

        return mInstance;
    }

    public static Thread.UncaughtExceptionHandler asDefault(String id) {
        if (enable) {
            return new CrashCocoExceptionHandler(id);
        }
        else {
            return Thread.getDefaultUncaughtExceptionHandler();
        }
    }

    private CrashCocoExceptionHandler() {

    }

    private CrashCocoExceptionHandler(String id) {
        this.id = id;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        crashCoco();
    }

    private void crashCoco() {
        if (enable) {
            if (!isErrorSet) {
                try {
                    File fileErr = new File(getFileLog().getParent(), String.format("%s.%s.%s", id, "stacktrace", "txt"));
                    PrintStream err = new PrintStream(new FileOutputStream(fileErr, true));
                    System.setErr(err);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isErrorSet = true;
            }
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (enable) {
            try {
                writeLog(getMessageLog(throwable));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        defaultHandler.uncaughtException(thread, throwable);
    }

    public void toast(Context ctx, String msg) {
        if (enable) {
            Toast.makeText(ctx, id + " : " + msg, Toast.LENGTH_LONG).show();
        }
    }

    public void debugLog(String msg) {
        if (enable) {
            try {
                writeLog(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void debugLog(Exception ex) {
        if (enable) {
            try {
                writeLog(ex.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeLog(String msg) throws IOException {
        if (enable) {
            File reportFile = getFileLog();
            FileWriter reportWriter = new FileWriter(reportFile, true);
            reportWriter.append(formatLog(msg));
            reportWriter.flush();
            reportWriter.close();
        }
    }

    private String getMessageLog(Throwable e) {
        String message = null;
        if (enable) {
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            message = writer.toString();
            printWriter.close();
            TIO.closeQuietly(writer);
        }
        return message;
    }

    private String formatLog(String log) {
        if (enable) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd, HH:mm:ss", Locale.US);
            Date date = new Date();
            log = dateFormat.format(date) + "\n\n[STACKTRACE]\n\t|\n\tV\n" + log + "\n\n" + TStrings.repeat("=", 15) + "\n\n";
        }
        return log;
    }

    private File getFileLog() {
        File file = null;

        if (enable) {
            file = new File(Environment.getExternalStorageDirectory(), String.format("%s%s.%s.%s.%s", ENV, "cc", id, "log", "txt"));
            if (!file.exists() || !file.getParentFile().isDirectory()) {
                file.getParentFile().mkdirs();
            }
        }

        return file;
    }
}
