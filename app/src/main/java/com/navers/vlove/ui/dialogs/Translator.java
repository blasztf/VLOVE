package com.navers.vlove.ui.dialogs;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.navers.vlove.Action;
import com.navers.vlove.R;
import com.navers.vlove.VolleyRequest;
import com.navers.vlove.apis.VAPIS;
import com.navers.vlove.logger.CrashCocoExceptionHandler;
import com.navers.vpago.Locale;
import com.navers.vpago.TranslatorListener;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator extends BaseDialog {

    private static final String TAG_VOLLEY_REQUEST = Translator.class.getSimpleName() + ".TAG_VOLLEY_REQUEST";
    private static final String POST_NOT_EXIST = "This post does not exist.";

    private static final int REQUEST_MAX_RETRIES = 3;
    private static final int ERROR_POST_NOT_EXIST = 1000;

    private static final String EXTRA_POST = Translator.class.getSimpleName() + ".POST:LString";

    private TextView mTranslatorFrom, mTranslatorTo;
    private EditText mTranslatorFromText, mTranslatorToText;
    private Button mTranslatorTranslate;

    private com.navers.vpago.Translator mTranslator;

    private Locale mSourceLocale, mTargetLocale;

    private TranslatorListener mListener = new TranslatorListener() {
        @Override
        public void onTranslated(com.navers.vpago.Translator.Response result) {
            setSourceValue(result.sourceLanguageType, result.text);
            setTargetValue(result.targetLanguageType, result.translatedText);
        }

        @Override
        public void onError(String errorText) {
            setTargetValue(new Locale("", "Error"), errorText);
        }
    };

    public static synchronized Translator with(Context context) {
        return new Translator(context);
    }

    private Translator(Context context) {
        super(context);
    }

    @BuilderMethod
    public void translate(String urlOrContent) {
        getIntent().putExtra(Translator.EXTRA_POST, urlOrContent);
        show();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_dialog_translator;
    }

    @Override
    protected void onPrepareContentViewElement() {
        mTranslatorFrom      = findViewById(R.id.translatorFrom);
        mTranslatorTo        = findViewById(R.id.translatorTo);
        mTranslatorFromText  = findViewById(R.id.translatorFromText);
        mTranslatorToText    = findViewById(R.id.translatorToText);
        mTranslatorTranslate = findViewById(R.id.translatorTranslate);
    }

    @Override
    protected void onReady(Intent intent) {
        main();
    }

    private void main() {
        Thread.setDefaultUncaughtExceptionHandler(new CrashCocoExceptionHandler("vl_tl"));

        mTranslatorTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doTranslation();
            }
        });

        mTranslator = new com.navers.vpago.Translator();

        Intent intent = getIntent();

        String action = intent.getAction();
        String urlOrContent   = null;

        if (action != null && (action.equals(Action.TRANS_TRANSLATE) || action.equals(Intent.ACTION_SEND))) {

            String type;
            if ((type = intent.getType()) != null && type.startsWith("text/")) {
                urlOrContent = intent.getStringExtra(Intent.EXTRA_TEXT);
            }

            doTranslation(urlOrContent);
            return;
        }
        else if ((urlOrContent = intent.getStringExtra(EXTRA_POST)) != null) {
            doTranslation(urlOrContent);
            return;
        }

        finish();
    }

    private void setSourceValue(Locale sourceLocale, String sourceValue) {
        mTranslatorFrom.setText(sourceLocale.toCountryString());
        mTranslatorFromText.setText(sourceValue);
        mSourceLocale = sourceLocale;
    }

    private void setTargetValue(Locale targetLocale, String targetValue) {
        mTranslatorTo.setText(targetLocale.toCountryString());
        mTranslatorToText.setText(targetValue);
        mTargetLocale = targetLocale;
    }

    /**
     * Find and return "Post ID" + "Channel Code" based on the given <i><b>link</b></i>.
     * @param link to process.
     * @return array of string consist of:<ul><li>[0] => Post ID</li><li>[1] => Channel Code</li></ul>
     * *note: returned output always not null, <i>but</i> items on the output can be null.
     */
    private String[] getPIDandCC(String link) {
        String postId, channelCode;
        postId = channelCode = null;
        Pattern regex = Pattern.compile("https?://channels\\.vlive\\.tv/([A-Z0-9]+)/fan/([0-9.]+)");
        Matcher matcher = regex.matcher(link);

        if (matcher.find()) {
            postId = matcher.group(2);
            channelCode = matcher.group(1);
        }

        return new String[] { postId, channelCode };
    }

    private void doTranslation() {
        if (mSourceLocale.equals(mTargetLocale)) {
            setTargetValue(mSourceLocale, mTranslatorFromText.getText().toString());
        }
        else {
            mTranslator.translate(mTranslatorFromText.getText().toString(), mSourceLocale, mTargetLocale, mListener);
        }
    }

    private void doTranslation(String url) {
        String[] pidNcid = getPIDandCC(url);
        String postId = pidNcid[0];
//        String channelCode = pidNcid[1];

        VolleyRequest.StringRequest request = new VolleyRequest.StringRequest(
                VAPIS.getAPIPosts(this, postId),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {

                            JsonObject root = new JsonParser().parse(response).getAsJsonObject();
                            String content = root.get("body").getAsString();
                            String locale = root.get("written_in").getAsString();
                            Locale objLocale = new Locale(locale);

                            if (root.has("photo") || root.has("video")) {
                                content = content.replaceAll("<v:attachment type=\\\"(photo|video)\\\" id=\\\"([0-9.]+)\\\" />", "");
//                                content += "\n\n(" + getString(R.string.board_post_contains_media) + ")";
                            }

                            setSourceValue(Locale.AUTO, content);
                            setTargetValue(Locale.ENGLISH, "Translating...");

                            doTranslation();

                        } catch (Exception e) {
                            mListener.onError(getString(R.string.board_sync_fail));
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                            String response = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            JsonObject root = new JsonParser().parse(response).getAsJsonObject();
                            if (root.has("error")) {
                                if (root.get("error").getAsJsonObject().get("error_code").getAsInt() == ERROR_POST_NOT_EXIST) {
                                    mListener.onError(POST_NOT_EXIST);
                                    return;
                                }
                            }
                        }
                        mListener.onError(getString(R.string.trans_fail));
                        error.printStackTrace();
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, REQUEST_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyRequest.with(this).addToQueue(request, TAG_VOLLEY_REQUEST);
    }

}
