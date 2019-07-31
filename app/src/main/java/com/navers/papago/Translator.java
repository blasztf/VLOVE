package com.navers.papago;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Translator {

    private String mClientId, mClientSecret;

    private class Response {
        String sourceLanguageType;
        String targetLanguageType;
        String translatedText;
    }

    public Translator(String clientId, String clientSecret) {
        mClientId = clientId;
        mClientSecret = clientSecret;
    }

    /**
     * Translate a text into a text in target language.
     * @param text that want to be translate.
     * @param source language from {@code text}.
     * @param target language to translate {@code text} into.
     * @param listener for translated text.
     */
    public void translate(final String text, final Locale source, final Locale target, final TranslatorListener listener) {
        new Thread() {

            @Override
            public void run() {
                Response response = getTranslation(text, source, target);
                listener.onTranslated(response.translatedText);
            }

        }.start();
    }

    /**
     * Get translated text from papago api.
     * @param text to be translate.
     * @param source language from text that want to be translate.
     * @param target language to translate the text.
     * @return a {@linkplain Response} from papago api or an empty {@linkplain Response}.
     */
    private Response getTranslation(String text, Locale source, Locale target) {
        int responseCode;
        String encodedText;
        String postParams;
        String line;
        StringBuffer responseJson;
        BufferedReader responseReader;
        Response response;
        URL urlApi;
        HttpURLConnection urlConnection;

        try {
            encodedText = URLEncoder.encode(text, "UTF-8");
            postParams = "source=" + source.toString() + "&target=" + target.toString() + "&text=" + encodedText;
            urlApi = new URL("https://openapi.naver.com/v1/papago/n2mt");
            urlConnection = (HttpURLConnection) urlApi.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("HOST", "openapi.naver.com");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("X-Naver-Client-Id", mClientId);
            urlConnection.setRequestProperty("X-Naver-Client-Secret", mClientSecret);
            urlConnection.setDoOutput(true);

            DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.writeBytes(postParams);
            dataOutputStream.flush();
            dataOutputStream.close();

            responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                responseReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            } else {
                responseReader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
            }
            responseJson = new StringBuffer();

            while ((line = responseReader.readLine()) != null) {
                responseJson.append(line);
            }

            responseReader.close();

            response = parseResponse(responseJson.toString());
        } catch (IOException e) {
            response = new Response();
        }

        return response;
    }

    /**
     * Parse response from papago api to properly response object.
     * @param responseJson from papago api.
     * @return a properly {@linkplain Response} object.
     */
    private Response parseResponse(String responseJson) {
        Response response = new Response();
        JsonObject json = new JsonParser().parse(responseJson).getAsJsonObject();

        response.translatedText = json.get("translatedText").getAsString();
        response.sourceLanguageType = json.get("srcLangType").getAsString();
        response.targetLanguageType = json.get("tarLangType").getAsString();

        return response;
    }
}