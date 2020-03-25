package com.doodlyz.vlove.deprecated.features;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

public final class IOUtils {

    /**
     * Replicate Apache IOUtils#closeQuietly, but instead of return void, this method return boolean to detect if stream is successfully close or not.
     * @return true if close success, false otherwise.
     */
    public static boolean closeQuietly(Closeable stream) {
        if (stream == null) {
            return false;
        }
        try {
            stream.close();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     * Disconnect connection quietly.
     */
    public static void disconnectQuietly(URLConnection connection) {
        if (connection == null) return;

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = connection.getInputStream();
        }
        catch (IOException e) {
            // ignore
        }
        finally {
            closeQuietly(inputStream);
        }

        try {
            outputStream = connection.getOutputStream();
        }
        catch (IOException e) {
            // ignore
        }
        finally {
            closeQuietly(outputStream);
        }

        inputStream = null;
        outputStream = null;
    }

    /**
     * Disconnect connection quietly.
     */
    public static void disconnectQuietly(HttpURLConnection connection) {
        if (connection == null) return;

        connection.disconnect();
    }

}
