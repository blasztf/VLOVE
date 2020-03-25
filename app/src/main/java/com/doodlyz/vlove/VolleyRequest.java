package com.doodlyz.vlove;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HttpResponse;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolleyRequest {
    private RequestQueue mRequestQueue;
    private Map<String, String> mCookieStore;
    private CookieManager mCookieManager;
    private static VolleyRequest mInstance;

    public static synchronized VolleyRequest with(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyRequest(context);
        }

        return mInstance;
    }

    public static synchronized VolleyRequest with(WeakReference<Context> context) {
        return with(context.get());
    }

    private VolleyRequest(Context context) {
        mCookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(mCookieManager);
        mCookieStore = new HashMap<>();
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext(), new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpURLConnection connection = super.createConnection(url);
                connection.setInstanceFollowRedirects(false);
                return connection;
            }
        });
    }

    private RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public <T> void addToQueue(Request<T> request) {
        addToQueue(request, null);
    }

    public <T> void addToQueue(Request<T> request, Object tag) {
        request.setTag(tag);
        getRequestQueue().add(request);
    }

    public void cancelPendingRequest(Object tag) {
        if (mRequestQueue != null) {
            getRequestQueue().cancelAll(tag);
        }
    }

    public void setCookie(String url, String cookie) {
        mCookieStore.put(URI.create(url.toLowerCase()).getHost(), cookie);
    }

    private String getCookie(String url) {
        return mCookieStore.get(URI.create(url.toLowerCase()).getHost());
    }

    public static class StringRequest extends com.android.volley.toolbox.StringRequest {
        private Map<String, String> mHeaders;
        private Response.Listener<String> mListener;

        /**
         * Creates a new request with the given method.
         *
         * @param method        the request {@link Method} to use
         * @param url           URL to fetch the string at
         * @param listener      Listener to receive the String response
         * @param errorListener Error listener, or null to ignore errors
         */
        public StringRequest(int method, String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
            super(method, url, listener, errorListener);
            initialize(listener);
        }

        /**
         * Creates a new GET request.
         *
         * @param url           URL to fetch the string at
         * @param listener      Listener to receive the String response
         * @param errorListener Error listener, or null to ignore errors
         */
        public StringRequest(String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
            super(url, listener, errorListener);
            initialize(listener);
        }

        private void initialize(Response.Listener<String> listener) {
            mHeaders = new HashMap<>();
            mListener = listener;
            setHost("www.vlive.tv");
            setUserAgent(AppSettings.VLOVE_USER_AGENT);
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = super.getHeaders();
            if (headers != null && !headers.isEmpty()) {
                headers.putAll(mHeaders);
            }
            else {
                headers = mHeaders;
            }

            injectCookie(headers);

            return headers;
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            try {
                return Response.success(new String(response.data, "UTF-8"), HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            }
        }

        public Response.Listener<String> getListener() {
            return mListener;
        }

        public StringRequest setReferer(String referer) {
            setHeader("Referer", referer);
            return this;
        }

        public StringRequest setHost(String host) {
            setHeader("Host", host);
            return this;
        }

        public StringRequest setUserAgent(String userAgent) {
            setHeader("User-Agent", userAgent);
            return this;
        }

        private void setHeader(String name, String value) {
            mHeaders.put(name, value);
        }

        private void injectCookie(Map<String, String> headers) {
            headers.put("Cookie", VolleyRequest.mInstance.getCookie(getUrl()));
        }
    }
}
