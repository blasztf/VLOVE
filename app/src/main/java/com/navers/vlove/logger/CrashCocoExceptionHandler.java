package com.navers.vlove.logger;

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

    private final boolean unlock = true;
    private final boolean disable = false;

    private boolean isErrorSet = false;

    private static CrashCocoExceptionHandler mInstance;

    public static synchronized CrashCocoExceptionHandler with(String id) {
        if (mInstance == null) {

            mInstance = new CrashCocoExceptionHandler();

        }

        mInstance.id = id;

        return mInstance;
    }

    private CrashCocoExceptionHandler() {

    }

    public CrashCocoExceptionHandler(String id) {
        this.id = id;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        crashCoco();
    }

    private void crashCoco() {
        if (unlock) {
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
        } else {
            if (disable) {
                System.err.close();
            }
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (unlock) {
            try {
                writeLog(getMessageLog(throwable));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        defaultHandler.uncaughtException(thread, throwable);
    }

    public void toast(Context ctx, String msg) {
        if (unlock) {
            Toast.makeText(ctx, id + " : " + msg, Toast.LENGTH_LONG).show();
        }
    }

    public void debugLog(String msg) {
        if (unlock) {
            try {
                writeLog(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void debugLog(Exception ex) {
        if (unlock) {
            try {
                writeLog(ex.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeLog(String msg) throws IOException {
        if (unlock) {
            File reportFile = getFileLog();
            FileWriter reportWriter = new FileWriter(reportFile, true);
            reportWriter.append(formatLog(msg));
            reportWriter.flush();
            reportWriter.close();
        }
    }

    private String getMessageLog(Throwable e) {
        String message;
        if (unlock) {
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
        if (unlock) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd, HH:mm:ss", Locale.US);
            Date date = new Date();
            log = dateFormat.format(date) + "\n\n[STACKTRACE]\n\t|\n\tV\n" + log + "\n\n" + TStrings.repeat("=", 15) + "\n\n";
        }
        return log;
    }

    private File getFileLog() {
        File file;

        if (unlock) {
            file = new File(Environment.getExternalStorageDirectory(), String.format("%s%s.%s.%s.%s", ENV, "cc", id, "log", "txt"));
            if (!file.exists() || !file.getParentFile().isDirectory()) {
                file.getParentFile().mkdirs();
            }
        }

        return file;
    }
}
