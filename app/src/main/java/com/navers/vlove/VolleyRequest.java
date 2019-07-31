package com.navers.vlove;

import android.content.Context;
import android.support.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

public class VolleyRequest {
    private RequestQueue mRequestQueue;

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
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
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

    public static class StringRequest extends com.android.volley.toolbox.StringRequest {

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
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            try {
                return Response.success(new String(response.data, "UTF-8"), HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            }
        }
    }

}
