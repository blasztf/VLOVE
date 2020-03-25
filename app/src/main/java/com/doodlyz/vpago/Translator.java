package com.doodlyz.vpago;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Translator {

    private static final String SECRET_KEY = "rlWxMKMcL2IWMPV6ImUwMWMwZWFkLWMyNDUtNDg2YS05ZTdiLWExZTZmNzc2OTc0MyIsImRpY3QiOnRydWUsImRpY3REaXNwbGF5Ijoz";
    private static final String QUERY_TRTE = "0,\"honorific\":false,\"instant\":false,\"source\":\"%s\",\"target\":\"%s\",\"text\":\"%s\"}";
    private static final String QUERY_DECT = "0,\"honorific\":false,\"instant\":false,\"query\":\"%s\"}";
    private static final String URL_TRTE = "https://papago.naver.com/apis/n2mt/translate";
    private static final String URL_DECT   = "https://papago.naver.com/apis/langs/dect";

    public class Response {
        public final Locale sourceLanguageType;
        public final Locale targetLanguageType;
        public final String translatedText;
        public final String text;

        private Response(String slt, String tlt, String tt, String t) {
            sourceLanguageType = new Locale(slt);
            targetLanguageType = new Locale(tlt);
            translatedText     = tt;
            text               = t;
        }

        private Response() {
            sourceLanguageType = null;
            targetLanguageType = null;
            translatedText     = null;
            text               = null;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format("{ sourceLanguageType : \"%s\", targetLanguageType : \"%s\", translatedText : \"%s\", text : \"%s\" }", sourceLanguageType, targetLanguageType, translatedText, text);
        }
    }

    public Translator() {}

    /**
     * Translate a text into a text in target language.
     * @param text that want to be translate.
     * @param source language from {@code text}.
     * @param target language to translate {@code text} into.
     * @param listener for translated text.
     */
    public void translate(final String text, final Locale source, final Locale target, final TranslatorListener listener) {

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Integer, Response> task = new AsyncTask<Void, Integer, Response>() {
            @Override
            protected Response doInBackground(Void... strings) {
                return getTranslation(text, source, target);
            }

            @Override
            protected void onPostExecute(Response response) {
                listener.onTranslated(response);
            }
        };

        task.execute();
    }

    /**
     * Translate a text into a text in target language in synchronous way.
     * @param text that want to be translate.
     * @param source language from {@code text}.
     * @param target language to translate {@code text} into.
     * @return translated text.
     */
    public Response translate(final String text, final Locale source, final Locale target) {
        return getTranslation(text, source, target);
    }

    /**
     * Build query by placing values into query template specified in {@value QUERY_TRTE}.
     * @param text that want to be translated.
     * @param source locale from what language.
     * @param target locale to what language.
     * @return raw query.
     */
    private String buildTransQuery(String text, Locale source, Locale target) {
        return sanitizeText(String.format(Translator.QUERY_TRTE, source, target, text));
    }

    private String detectLocale(String text) {
        text = sanitizeText(text);
        JsonObject json;

        String data = String.format(Translator.QUERY_DECT, text);
        String responseJson = getAPIResponse(Translator.URL_DECT, data);

        if (responseJson != null) {
            try {
                json = new JsonParser().parse(responseJson).getAsJsonObject();
                return json.get("langCode").getAsString();
            }
            catch (NullPointerException e) {
                return null;
            }
        }
        else return null;
    }

    private String sanitizeText(String text) {
        return text.replaceAll("\\n", "\\n").replaceAll("\\r", "\\r");
    }

    /**
     * Encode query with Base64 encoder.
     * @param value that want to be encoded.
     * @return encoded value in Base64.
     */
    private String encodeWithBase64(String value) {
        return new String(Base64.encodeBase64(value.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
//    private void bypassSSL() {
//        X509ExtendedTrustManager trustManager;
//        SSLSocketFactory sslSocketFactory;
//        try {
//            trustManager =
//                    new X509ExtendedTrustManager() {
//                        @Override
//                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
//
//                        }
//
//                        @Override
//                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
//
//                        }
//
//                        @Override
//                        public X509Certificate[] getAcceptedIssuers() {
//                            return new X509Certificate[]{};
//                        }
//
//                        @Override
//                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
//
//                        }
//
//                        @Override
//                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
//
//                        }
//
//                        @Override
//                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
//
//                        }
//
//                        @Override
//                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
//
//                        }
//                    };
//            SSLContext sslContext = SSLContext.getInstance("SSL");
//            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
//            sslSocketFactory = sslContext.getSocketFactory();
//
//            HostnameVerifier allHostsValid = new HostnameVerifier() {
//                @Override
//                public boolean verify(String hostname, SSLSession session) {
//                    return true;
//                }
//            };
//
//            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
//            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
//            HttpsURLConnection.setFollowRedirects(true);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Get translated text from papago api.
     * @param text to be translate.
     * @param source language from text that want to be translate.
     * @param target language to translate the text.
     * @return a {@linkplain Response} from papago api or an empty {@linkplain Response}.
     */
    private Response getTranslation(String text, Locale source, Locale target) {

        String data;

        if (source.useAutoDetection()) {
            source = new Locale(detectLocale(text));
        }

        data = buildTransQuery(text, source, target);
        data = getAPIResponse(URL_TRTE, data);

        return data == null ? null : parseResponse(data, text);
    }

    private String getAPIResponse(String url, String data) {
        int responseCode;
        String line;
        StringBuffer responseJson;
        BufferedReader responseReader;
        URL urlApi;
        HttpURLConnection urlConnection;

        try {
            data = encodeWithBase64(data);
            data = Translator.SECRET_KEY + data;
            data = "data=" + data;

            urlApi = new URL(url);
            urlConnection = (HttpURLConnection) urlApi.openConnection();
            urlConnection.setInstanceFollowRedirects(true);

            urlConnection.setRequestMethod("POST");

            urlConnection.setRequestProperty("accept", "application/json");
            urlConnection.setRequestProperty("accept-encoding", "gzip, deflate, br");
            urlConnection.setRequestProperty("accept-language", "ko");
            urlConnection.setRequestProperty("authority", "papago.naver.com");
            urlConnection.setRequestProperty("cache-control", "no-cache");
            urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
            urlConnection.setRequestProperty("device-type", "pc");
            urlConnection.setRequestProperty("dnt", "1");
//            urlConnection.setRequestProperty("host", "openapi.naver.com");
            urlConnection.setRequestProperty("origin", "https://papago.naver.com");
            urlConnection.setRequestProperty("pragma", "no-cache");
            urlConnection.setRequestProperty("referer", "https://papago.naver.com/");
            urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
            urlConnection.setRequestProperty("x-apigw-partnerid", "papago");

            urlConnection.setDoOutput(true);

            DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.writeBytes(data);
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

            return responseJson.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Parse response from papago api to properly response object.
     * @param responseJson from papago api.
     * @param text source.
     * @return a properly {@linkplain Response} object.
     */
    private Response parseResponse(String responseJson, String text) {
        String source = null, target = null, trans = null;

        JsonObject json = new JsonParser().parse(responseJson).getAsJsonObject();
        JsonElement element = json.get("srcLangType");

        if (element != null) {
            source = element.getAsString();
        }

        element = json.get("tarLangType");

        if (element != null) {
            target = element.getAsString();
        }

        element = json.get("translatedText");

        if (element != null) {
            trans = element.getAsString();
        }

        Response response;

        try {
            response = new Response(
                    source,
                    target != null ? target : source,
                    trans,
                    text
            );
        }
        catch (NullPointerException e) {
            response = new Response(
                    null,
                    null,
                    "Error parse response:\n" + responseJson,
                    text
            );
        }

        return response;
    }
}