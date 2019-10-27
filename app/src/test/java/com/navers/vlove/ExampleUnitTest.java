package com.navers.vlove;

import com.navers.vlove.apis.VAPIS;
import com.navers.vpago.Locale;
import com.navers.vpago.Translator;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    static boolean loop = true;

    @Test
    public void addition_isCorrect() {
        assertEquals( 4, 2 + 2);
    }

    private String btos(String text) {
        return new String(Base64.decodeBase64(Base64.decodeBase64(text.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
    }

    @Test
    public void b64test() throws Exception {
        String m = "WyoTSgLKDtHTSanFW9rlWkqJIlrFV6VyA";
        String b64 = "WyoTSgLKDtHTSanFW9rlWkqyA";

        String b1 = "eTSanFW9rlWkqJIlrFV6VyAyoTSgLKDtH";
        String b11 = "eTSanFW9yAyoKDtH";
        String b2 = "oFV6VyAyoTSgLKDtHTSanFW9rlWkqJIlr";
        String b12 = "TSanFW9rlWkqJIlr";
        String b13 = "FV6VyAyoTSgLKDtH";
        String b14 = "TSgLrlWkqJIlrFV6V";
        String b15 = "yoTSgLKDtHTSanF";
        String b16 = "ITRtn2SvLKVtMUIhnJRvsGSnrlWkqJIlrFV6VyAyoTSgLKDtpTSanFOup";
        String b167 = "ITRtn2SvLKVtMUIhnJRvsGSnyAyoKDtpTSanFOup";
        String b17 = "3IlrFV6VxgyoJSlnJ4tpTSgLJ4tMTS0LJ5aVa1ZA2yErlWkqJ";


        System.out.println(btos(b11));
    }

    @Test
    public void trans_test() throws Exception {
        String url = "https://papago.naver.com/apis/langs/dect";
        String dataValue = "";

        final String SECRET_KEY = "rlWxMKMcL2IWMPV6ImUwMWMwZWFkLWMyNDUtNDg2YS05ZTdiLWExZTZmNzc2OTc0MyIsImRpY3QiOnRydWUsImRpY3REaXNwbGF5Ijoz";
        final String QUERY      = "0,\"honorific\":false,\"instant\":false,\"query\":\"%s\"}";

        Map<String, String> headers = new HashMap<>();
        Map<String, String> data    = new HashMap<>();

        headers.put("accept", "application/json");
        headers.put("accept-encoding", "gzip, deflate, br");
        headers.put("accept-language", "ko");
        headers.put("authority", "papago.naver.com");
        headers.put("cache-control", "no-cache");
        headers.put("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("device-type", "pc");
        headers.put("dnt", "1");
        headers.put("origin", "https://papago.naver.com");
        headers.put("pragma", "no-cache");
        headers.put("referer", "https://papago.naver.com/");
        headers.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
        headers.put("x-apigw-partnerid", "papago");

        dataValue = String.format(QUERY, "Selamat Pagi");
        dataValue = encodeWithBase64(dataValue);
        dataValue = SECRET_KEY + dataValue;

        data.put("data", dataValue);

        String response = http_request(url, "POST", data, headers);
        System.out.println(response);
    }

    @Test
    public void test_auto() {
        Translator t = new Translator();
        Translator.Response r = t.translate("Selamat Pagi\nApa kabar?", Locale.AUTO, Locale.ENGLISH);
        System.out.println(r);
    }

//    private String buildQuery(String text, Locale source, Locale target) {
//        return String.format(Translator.QUERY, source, target, text).replaceAll("\\n", "\\n").replaceAll("\\r", "\\r");
//    }

    private String encodeWithBase64(String value) {
        return new String(Base64.encodeBase64(value.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    private String http_request(String url, String method, Map<String, String> data, Map<String,String> headers) throws Exception {
        URL urlApi;
        HttpURLConnection urlConnection;
        int responseCode;
        StringBuilder formatData = new StringBuilder();
        BufferedReader responseReader;
        StringBuffer response;

        urlApi = new URL(url);
        urlConnection = (HttpURLConnection) urlApi.openConnection();
        urlConnection.setInstanceFollowRedirects(true);

        urlConnection.setRequestMethod(method);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                formatData.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            formatData.deleteCharAt(formatData.length() - 1);
        }

        if ("POST".equals(method) && data != null) {
            urlConnection.setDoOutput(true);

            DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.writeBytes(formatData.toString());
            dataOutputStream.flush();
            dataOutputStream.close();
        }


        responseCode = urlConnection.getResponseCode();
        if (responseCode == 200) {
            responseReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        } else {
            responseReader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
        }
        response = new StringBuffer();

        String line;
        while ((line = responseReader.readLine()) != null) {
            response.append(line);
        }

        responseReader.close();

        return response.toString();
    }
}