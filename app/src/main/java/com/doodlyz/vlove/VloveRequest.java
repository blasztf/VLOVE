package com.doodlyz.vlove;

import android.content.Context;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class VloveRequest {
    private RequestQueue mRequestQueue;
    private Map<String, String> mCookieStore;
    private CookieManager mCookieManager;
    private static VloveRequest mInstance;

    public static synchronized VloveRequest with(Context context) {
        if (mInstance == null) {
            mInstance = new VloveRequest(context);
        }

        return mInstance;
    }

    public static synchronized void addToQueue(Context context, ApiRequest request)  {
        with(context).addToQueue(request);
    }

    private VloveRequest(Context context) {
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

    public static class ApiRequest extends com.android.volley.toolbox.StringRequest {
        private static final String HEADER_USER_AGENT = "User-Agent";
        private static final String HEADER_REFERER = "Referer";
        private static final String HEADER_HOST = "Host";

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
        public ApiRequest(int method, String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
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
        public ApiRequest(String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
            super(url, listener, errorListener);
            initialize(listener);
        }

        private void initialize(Response.Listener<String> listener) {
            mHeaders = new HashMap<>();
            mListener = listener;
            setUserAgent(VloveSettings.VLOVE_USER_AGENT);
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
            return Response.success(new String(response.data, StandardCharsets.UTF_8), HttpHeaderParser.parseCacheHeaders(response));
        }

        public Response.Listener<String> getListener() {
            return mListener;
        }

        public ApiRequest addHeader(String name, String value) {
            setHeader(name, value);
            return this;
        }

        public ApiRequest setReferer(String referer) {
            setHeader(HEADER_REFERER, referer);
            return this;
        }

        public ApiRequest setHost(String host) {
            setHeader(HEADER_HOST, host);
            return this;
        }

        public ApiRequest setUserAgent(String userAgent) {
            setHeader(HEADER_USER_AGENT, userAgent);
            return this;
        }

        private void setHeader(String name, String value) {
            if (value == null) {
                mHeaders.remove(name);
            }
            else {
                mHeaders.put(name, value);
            }
        }

        private void injectCookie(Map<String, String> headers) {
            headers.put("Cookie", VloveRequest.mInstance.getCookie(getUrl()));
        }
    }
}
