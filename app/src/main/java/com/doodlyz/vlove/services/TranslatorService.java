package com.doodlyz.vlove.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.doodlyz.vlove.VloveUtils;
import com.doodlyz.vlove.ui.dialogs.Popup;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.doodlyz.vlove.Action;
import com.doodlyz.vlove.R;
import com.doodlyz.vlove.VloveRequest;
import com.doodlyz.vlove.apis.VAPIS;
import com.doodlyz.vpago.Locale;
import com.doodlyz.vpago.Translator;
import com.doodlyz.vpago.TranslatorListener;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Objects;

public class TranslatorService extends IntentService {

    private static final String TAG_VOLLEY_REQUEST = TranslatorService.class.getSimpleName() + ".TAG_VOLLEY_REQUEST";
    private static final String POST_NOT_EXIST = "This post does not exist.";

    private static final int REQUEST_MAX_RETRIES = 3;
    private static final int ERROR_POST_NOT_EXIST = 1000;

    public TranslatorService(String name) {
        super(name);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        begin(Objects.requireNonNull(intent));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_REDELIVER_INTENT;
    }

    private void begin(Intent intent) {
        String action = intent.getAction();
        String url   = null;

        Popup.with(TranslatorService.this, Popup.ID_INFO)
                .make("Translating...")
                .show();

        if (action != null && (action.equals(Action.TRANS_TRANSLATE) || action.equals(Intent.ACTION_SEND))) {

            String type;
            if ((type = intent.getType()) != null && type.startsWith("text/")) {
                url = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }
    }

    private void translate(String url, final TranslatorListener listener) {
        String[] pidNcid = VloveUtils.getPIDandCC(url);
        String postId = pidNcid[0];
//        String channelCode = pidNcid[1];

        VloveRequest.ApiRequest request = new VloveRequest.ApiRequest(
                VAPIS.getAPIPosts(this, postId),
                new Response.Listener<String>() {

                    Translator translator;

                    @Override
                    public void onResponse(String response) {
                        try {
                            JsonObject root = new JsonParser().parse(response).getAsJsonObject();
                            String content = root.get("body").getAsString();
                            String locale = root.get("written_in").getAsString();

                            if (root.has("photo") || root.has("video")) {
                                content = content.replaceAll("<v:attachment type=\\\"(photo|video)\\\" id=\\\"([0-9.]+)\\\" />", "");
//                                content += "\n\n(" + getString(R.string.board_post_contains_media) + ")";
                            }

                            translator.translate(content, new Locale(locale), Locale.ENGLISH, listener);

                        } catch (Exception e) {
                            listener.onError(getString(R.string.board_sync_fail));
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                            String response;
                            try {
                                response = new String(error.networkResponse.data, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                response = new String(error.networkResponse.data);
                            }
                            JsonObject root = new JsonParser().parse(response).getAsJsonObject();
                            if (root.has("error")) {
                                if (root.get("error").getAsJsonObject().get("error_code").getAsInt() == ERROR_POST_NOT_EXIST) {
                                    listener.onError(POST_NOT_EXIST);
                                    return;
                                }
                            }
                        }
                        listener.onError(getString(R.string.trans_fail));
                        error.printStackTrace();
                    }
                })
                .setHost(null);
        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, REQUEST_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VloveRequest.with(this).addToQueue(request, TAG_VOLLEY_REQUEST);
    }

}
