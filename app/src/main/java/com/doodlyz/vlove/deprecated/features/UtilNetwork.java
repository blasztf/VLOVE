package com.doodlyz.vlove.deprecated.features;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UtilNetwork {

    public interface Listener {
        void onProgress(int value);
    }

    public static String getResponse(String url) {
        return getResponse(url, null, null);
    }

    public static String getResponse(String url, Listener listener) {
        return getResponse(url, null, listener);
    }

    public static String getResponse(String url, String[] headers) {
        return getResponse(url, headers, null);
    }

    public static String getResponse(String url, String[] headers, Listener listener) {
        String result;
        try {
            URL mUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();
            if (headers != null) setConnectionHeaders(connection, headers);
            InputStream input = connection.getInputStream();
            int lengthFile = connection.getContentLength();
            byte[] data = getRawData(input, lengthFile, listener);
            input.close();
            connection.disconnect();
            try {
                result = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                result = new String(data);
            }
        } catch (IOException e) {
            result = null;
        }

        return result;
    }

    public static File download(String url, String savePath) {
        return download(url, savePath, null, null, null);
    }

    public static File download(String url, String savePath, String fileName) {
        return download(url, savePath, fileName, null, null);
    }

    public static File download(String url, String savePath, String[] headers) {
        return download(url, savePath, null, headers, null);
    }

    public static File download(String url, String savePath, Listener listener) {
        return download(url, savePath, null, null, listener);
    }

    public static File download(String url, String savePath, String fileName, Listener listener) {
        return download(url, savePath, fileName, null, listener);
    }

    public static File download(String url, String savePath, String fileName, String[] headers) {
        return download(url, savePath, fileName, headers, null);
    }

    public static File download(String url, String savePath, String[] headers, Listener listener) {
        return download(url, savePath, null, headers, listener);
    }

    public static File download(String url, String savePath, String filename, String[] headers, Listener listener) {
        File result = null;
        URL mUrl;
        HttpURLConnection connection = null;
        InputStream input = null;
        try {
            mUrl = new URL(url);
            connection = (HttpURLConnection) mUrl.openConnection();
            if (headers != null) setConnectionHeaders(connection, headers);
            int responseCode = connection.getResponseCode();

            // Connection success
            if (responseCode == HttpURLConnection.HTTP_OK) {

                // Get filename
                if (filename == null) {
                    String disposition = connection.getHeaderField("Content-Disposition");
                    int index;
                    if ((disposition != null) && ((index = disposition.indexOf("filename=")) >= 0)) {
                        filename = disposition.substring(index + 10, disposition.length() - 1);
                    } else {
                        filename = url.substring(url.lastIndexOf("/") + 1, url.length());
                    }
                }

                input = connection.getInputStream();
                int lengthFile = connection.getContentLength();
                result = downloadFile(new File(savePath, filename), input, lengthFile, listener);

            }
        } catch (IOException e) {
            if (result != null && result.exists() && result.isFile()) {
                result.delete();
            }
            result = null;
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.disconnectQuietly(connection);
        }

        return result;
    }

    protected static void setConnectionHeaders(HttpURLConnection connection, String[] headers) throws IllegalStateException {
        if (headers.length % 2 == 0) {
            String name, value;
            for (int i = 0, l = headers.length; i < l; i++) {
                name = headers[i];
                value = headers[++i];
                connection.setRequestProperty(name, value);
            }
        } else {
            throw new IllegalStateException("Request.addHeader() must return array with \"EVEN\" length (ex: 0,2,4,...)");
        }
    }

    private static int determineBufferSize(long lengthFile) {
        final int LIMIT_BUFFER_SIZE = 8 * 1024;
        int bufferSize = 0;
        if (lengthFile >= LIMIT_BUFFER_SIZE) {
            bufferSize = LIMIT_BUFFER_SIZE;
        } else if (lengthFile >= 1024) {
            for (int i = 1; i * 1024 < lengthFile; i *= 2) {
                bufferSize = i * 1024;
            }
        } else {
            bufferSize = 1024;
        }
        return bufferSize;
    }

    private static void stream(InputStream input, OutputStream output, long lengthFile, Listener listener) throws IOException {
        int bufferSize = determineBufferSize(lengthFile);
        byte[] data = new byte[bufferSize];
        int current, total = 0, i = 0;
        while ((current = input.read(data, 0, data.length)) != -1) {
            output.write(data, 0, current);
            total += current;

            if (total > i * 1024) {
                i++;
                output.flush();
            }

            if (listener != null) {
                listener.onProgress((int) ((total * 100d) / lengthFile));
            }
        }

        output.flush();
    }

    private static File downloadFile(File file, InputStream input, long lengthFile, Listener listener) throws IOException {
        FileOutputStream output = new FileOutputStream(file);
        byte[] data = new byte[determineBufferSize(lengthFile)];
        int current, total = 0;
        while ((current = input.read(data, 0, data.length)) != -1) {
            output.write(data, 0, current);
            total += current;

            if (listener != null) {
                listener.onProgress((int) ((total * 100d) / lengthFile));
            }
        }
        output.flush();
        output.close();
        return file;
    }

    private static byte[] getRawData(InputStream input, long lengthFile, Listener listener) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int current, total = 0;
        while ((current = input.read(data, 0, data.length)) != -1) {
            output.write(data, 0, current);
            total += current;

            if (listener != null) {
                listener.onProgress((int) ((total * 100d) / lengthFile));
            }
        }
        output.flush();
        output.close();
        return output.toByteArray();
    }
}
