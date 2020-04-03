package com.doodlyz.vlove;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class VloveDownload {
    private static VloveDownload mInstance;

    public static synchronized VloveDownload with(Context context) {
        if (mInstance == null) {
            mInstance = new VloveDownload();
        }

        return mInstance;
    }

    private VloveDownload() {

    }

    private HttpURLConnection openConnection(String url) throws IOException {
        return (HttpURLConnection) new URL(url).openConnection();
    }

    private void injectHeaders(HttpURLConnection connection, HashMap<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    private void process(HttpURLConnection connection, MediaDownload media) throws IOException {
        int lengthFile = connection.getContentLength();
        InputStream input = connection.getInputStream();
        FileOutputStream output = new FileOutputStream(media.getSaveLocation());
        byte[] data = new byte[determineBufferSize(lengthFile)];
        int current, total = 0;
        while ((current = input.read(data, 0, data.length)) != -1) {
            output.write(data, 0, current);
            total += current;

            if (media.listener != null) {
                media.listener.onProgress((int) ((total * 100d) / lengthFile));
            }

            if (media.shouldCancel) {
                output.flush();
                output.close();
                input.close();
                return;
            }
        }
        output.flush();
        output.close();
        input.close();
        media.downloaded = true;
    }

    private int determineBufferSize(long lengthFile) {
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

    private String determineFileName(HttpURLConnection connection) {
        String filename;
        int index;
        String disposition = connection.getHeaderField("Content-Disposition");
        String url = connection.getURL().toString();
        if ((disposition != null) && ((index = disposition.indexOf("filename=")) >= 0)) {
            filename = disposition.substring(index + 10, disposition.length() - 1);
        } else {
            filename = url.substring(url.lastIndexOf("/") + 1);
        }

        return filename;
    }

    public void download(MediaDownload media) {
        HttpURLConnection connection;
        int responseCode;
        try {
            connection = openConnection(media.url);
            injectHeaders(connection, media.getHeaders());
            responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if (media.fileName == null) {
                    media.fileName = determineFileName(connection);
                }
                process(connection, media);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class MediaDownload {
        private String url;
        private String savePath;
        private String fileName;
        private OnProgressListener listener;
        private boolean downloaded;
        private boolean shouldCancel;

        public interface OnProgressListener {
            void onProgress(int value);
        }

        private MediaDownload() {

        }

        public MediaDownload(String url) {
            this.url = url;
            this.downloaded = false;
            this.shouldCancel = false;
        }

        public MediaDownload setSavePath(String path) {
            this.savePath = path;
            return this;
        }

        public MediaDownload setFileName(String name) {
            this.fileName = name;
            return this;
        }

        public MediaDownload setOnProgressListener(OnProgressListener listener) {
            this.listener = listener;
            return this;
        }

        public void cancel() {
            this.shouldCancel = true;
        }

        public boolean isDownloaded() {
            return downloaded;
        }

        HashMap<String, String> getHeaders() {
            return null;
        }

        public File getSaveLocation() {
            return new File(savePath, fileName);
        }
    }
}
