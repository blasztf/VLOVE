package com.doodlyz.vlove.data.helper;

import android.os.AsyncTask;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.doodlyz.vlove.StringRequest;
import com.doodlyz.vlove.VolleyRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class RequestHelper<E extends StringRequest> {
    private HttpURLConnection connection;
    private E request;
    private static final Map<String, String> mCookieStore = new HashMap<>();

    public static void setCookie(String url, String cookie) {
        mCookieStore.put(URI.create(url.toLowerCase()).getHost(), cookie);
    }

    public static String getCookie(String url) {
        return mCookieStore.get(URI.create(url.toLowerCase()).getHost());
    }

    public static synchronized <T extends StringRequest> RequestHelper with(T request) {
        return new RequestHelper<>(request);
    }

    private RequestHelper(E request) {
        this.request = request;
    }

    private RequestHelper() {}

    private void createConnection() throws IOException {
        if (connection != null) {
            connection.disconnect();
        }
        connection = (HttpURLConnection) new URL(request.getUrl()).openConnection();
    }

    private void setMethod() throws ProtocolException {
        connection.setRequestMethod(parseMethod(request.getMethod()));
    }

    private String parseMethod(int method) {
        String methodString;
        switch (method) {
            case Request.Method.GET:
                methodString = "GET";
                break;
            case Request.Method.POST:
                methodString = "POST";
                break;
            case Request.Method.DELETE:
                methodString = "DELETE";
                break;
            case Request.Method.OPTIONS:
                methodString = "OPTIONS";
                break;
                default:
                    methodString = "GET";
                    break;
        }

        return methodString;
    }

    private void setHeaders() {
        Map<String, String> headers;
        headers = request.getHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    private Response execute() {
        try {
            createConnection();
            setMethod();
            setHeaders();
            return new Response(this);
        }
        catch (IOException e) {
            return new Response(e);
        }
    }

    public void sendNow() {
        Response response = execute();
        if (response.err == null) {
            request.getListener().onResponse(response.result);
        }
        else {
            request.getErrorListener().onErrorResponse(response.err);
        }
    }

    public void send() {
        new RequestHelperTask().execute(this);
    }

    class Response {
        int statusCode;
        public String result;

        Exception err = null;

        Response(Exception e) {
            err = e;
        }

        Response(RequestHelper requestHelper) throws IOException {
            statusCode = requestHelper.connection.getResponseCode();
            result     = parse(requestHelper.connection.getInputStream());
        }

        private String parse(InputStream is) throws IOException {
            String result = null;
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder builder = new StringBuilder();

                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append('\n');
                    }
                    result = builder.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    err = e;
                    result = builder.toString();
                }

                reader.close();
                is.close();
            }

            return result;
        }
    }

    class RequestHelperTask extends AsyncTask<RequestHelper, Void, Response> {
        private RequestHelper task;

        @Override
        protected Response doInBackground(RequestHelper... requestHelpers) {
            task = requestHelpers[0];

            return task.execute();
        }

        @Override
        protected void onPostExecute(Response e) {
            super.onPostExecute(e);
            if (e.err == null) {
                task.request.getListener().onResponse(e.result);
            }
            else {
                task.request.getErrorListener().onErrorResponse(new VolleyError(e.err));
            }
        }
    }
}
