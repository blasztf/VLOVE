package com.doodlyz.vlove.ui.dialogs;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.doodlyz.vlove.VloveUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.doodlyz.vlove.Action;
import com.doodlyz.vlove.R;
import com.doodlyz.vlove.VloveRequest;
import com.doodlyz.vlove.apis.VAPIS;
import com.doodlyz.vlove.logger.CrashCocoExceptionHandler;
import com.doodlyz.vpago.Locale;
import com.doodlyz.vpago.TranslatorListener;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public final class TranslatorAct extends BaseDialogAct {

    private static final String TAG_VOLLEY_REQUEST = TranslatorAct.class.getSimpleName() + ".TAG_VOLLEY_REQUEST";
    private static final String POST_NOT_EXIST = "This post does not exist.";

    private static final int REQUEST_MAX_RETRIES = 3;
    private static final int ERROR_POST_NOT_EXIST = 1000;

    static final String EXTRA_POST = TranslatorAct.class.getSimpleName() + ".POST:LString";

    private Spinner mTranslatorFrom, mTranslatorTo;
    private EditText mTranslatorFromText, mTranslatorToText;
    private Button mTranslatorTranslate;

    private ArrayAdapter<CharSequence> mTranslatorAdapterFrom, mTranslatorAdapterTo;

    private String[] mTranslatorLocaleCode;

    private com.doodlyz.vpago.Translator mTranslator;

    private Locale mSourceLocale, mTargetLocale;

    private TranslatorListener mListener = new TranslatorListener() {
        @Override
        public void onTranslated(com.doodlyz.vpago.Translator.Response result) {
            setSourceValue(result.sourceLanguageType, result.text);
            setTargetValue(result.targetLanguageType, result.translatedText);
        }

        @Override
        public void onError(String errorText) {
            setTargetValue(new Locale("", "Error"), errorText);
        }
    };

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
        Thread.setDefaultUncaughtExceptionHandler(CrashCocoExceptionHandler.asDefault("vl_tl"));

        mTranslatorLocaleCode = getResources().getStringArray(R.array.pref_locale_code);
        mTranslatorAdapterFrom = ArrayAdapter.createFromResource(this, R.array.pref_locale_lang, android.R.layout.simple_spinner_item);
        mTranslatorAdapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTranslatorAdapterTo = ArrayAdapter.createFromResource(this, R.array.pref_locale_lang, android.R.layout.simple_spinner_item);
        mTranslatorAdapterTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mTranslatorFrom.setAdapter(mTranslatorAdapterFrom);
        mTranslatorTo.setAdapter(mTranslatorAdapterTo);

        mTranslatorFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                ((TextView) adapterView.getChildAt(0)).setTextColor(getResources().getColor(R.color.color_light_black));
                mSourceLocale = new Locale(mTranslatorLocaleCode[pos]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                ((TextView) adapterView.getChildAt(0)).setTextColor(getResources().getColor(R.color.color_light_black));
            }
        });

        mTranslatorTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                ((TextView) adapterView.getChildAt(0)).setTextColor(getResources().getColor(R.color.color_light_black));
                mTargetLocale = new Locale(mTranslatorLocaleCode[pos]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                ((TextView) adapterView.getChildAt(0)).setTextColor(getResources().getColor(R.color.color_light_black));
            }

        });

        mTranslatorTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doTranslation();
            }
        });

        mTranslator = new com.doodlyz.vpago.Translator();

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
        mTranslatorFrom.setSelection(getLocaleCodePosition(sourceLocale), true);

        mTranslatorFromText.setText(sourceValue);
        mSourceLocale = sourceLocale;
    }

    private void setTargetValue(Locale targetLocale, String targetValue) {
        mTranslatorTo.setSelection(getLocaleCodePosition(targetLocale), true);

        mTranslatorToText.setText(targetValue);
        mTargetLocale = targetLocale;
    }

    private int getLocaleCodePosition(Locale locale) {
        for (int i = 0; i < mTranslatorLocaleCode.length; i++) {
            if (locale.equals(new Locale(mTranslatorLocaleCode[i]))) {
                return i;
            }
        }

        return 0;  //-1;
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
        String[] pidNcid = VloveUtils.getPIDandCC(url);
        String postId = pidNcid[0];
//        String channelCode = pidNcid[1];

        VloveRequest.ApiRequest request = new VloveRequest.ApiRequest(
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
        VloveRequest.with(this).addToQueue(request, TAG_VOLLEY_REQUEST);
    }

}
