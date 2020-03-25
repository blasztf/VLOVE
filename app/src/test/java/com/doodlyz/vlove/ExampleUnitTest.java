package com.doodlyz.vlove;

import com.doodlyz.vlove.data.helper.RequestHelper;
import com.doodlyz.vlove.logger.CrashCocoExceptionHandler;
import com.doodlyz.vpago.Locale;
import com.doodlyz.vpago.Translator;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    static boolean loop = true;

    @Test
    public void testCookie() throws IOException {
//        CookieSyncManager.
//        System.out.println(VAPIS.getAPIVideoInfo(null, "abc", null));
//        print("host" + URI.create("https://www.vlive.tv/video/init/view?videoSeq=223&channelCode=88998").getHost());
        String videoId = "169233";
        String channelCode = "E203A5";
        String vpdid = "64b6c6f365bf49e70ac0d557796779501b955b53f85775e2b253a2de152f79de";
        String vpdid2 = "e54cf662ad4b5d2b9d76a846e15c9ef9eb7512b369144f397a434320f1cd9fcb";
        String url = String.format("https://www.vlive.tv/video/init/view?videoSeq=%s&channelCode=%s&src=%s", videoId, channelCode, "vlive.video.js");
        String referer = String.format("https://www.vlive.tv/video/%s?channelCode=%s", videoId, channelCode);
        String host = "www.vlive.tv";
        String cookie = "timezoneOffset=-120; userLanguage=id; userCountry=ID; webp=true; JSESSIONID=C0378073475D311DFB140C0796709267; _ga=GA1.2.738573834.1584897536; _gid=GA1.2.454853547.1584897536; snsCd=facebook; NEO_SES=\"2ptFp3pEszMSTjBlUXKOJNO4kyogkW39rv2Psah7dJO8hcGDDeSzQl5ojVFnnV7P9h77IOLWiJnDhbcx41itfkMOsFXXr1bbsAX/LUUYbtsdetVL+93N531UJUaHU1eeWGp/gsRXia9thhNBOHhMdw7JQPBViE/20FTia8m35XvQxk5op8Gdd9eGv7cJ92ue9bCBO/kfiHLN3HaT2izSXYn9xedejrVMjgON6p0k50rZiZSNQj0JDDn7Fhxei/wM8G89GGtuGD4re5tVLasw0nmXmUNv9fLlmyd+qQiAH3lbzLZTDw4fAuPr/qe8Iomtahz8dq8BlXDbD+NGlMB43owri1wExscDPlQya7TW9VvFTBWFP35z+WAlbMUto9KHO02WCTBTrEYTlKijyBtYJgolX7YYXMZO0tIk6ub39ye9jH/hlzHU1nU1ORmsqkK8eRsK8RMfHz9H0RnAg2cOdA==\"; NEO_CHK=\"PZTO+AJBfWvbsLj1CVg9su1K19FPXRavdMkCx9keMn9FreYjHtUXDFkuFLtGgR6ZvwwAcDDJTjTSdCfzG2lYojMV/VMVDLmYxpxtPummantu027rVj6eNwMIdVO+7KfjV67xdvMPzDqYJinjDRtJKg==\"; emailAuthStatus=NOT-NOTIFIED";

        RequestHelper.setCookie(url, cookie);
        StringRequest request = new StringRequest(url,
                new StringRequest.Response.Listener() {
                    @Override
                    public void onResponse(String response) {
                        print(response);
                    }
                },
                new StringRequest.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Exception error) {
                        error.printStackTrace();
                    }
                })
                .setReferer(referer)
                .setHost(host);
        RequestHelper.with(request).sendNow();
    }

    private void print(String s) {
        System.out.println(s);
    }

    private String[] parseVideoStatus(String response) {
        String var = "oVideoStatus = ";
        String res = "";
        String status;
        JSONObject videoStatus;
        int beginIndex, endIndex;
        if (response != null) {
            beginIndex = response.indexOf(var) + var.length();
            endIndex = response.indexOf('}', beginIndex) + 1;
            return new String[] { response.substring(beginIndex, endIndex)};
//            try {
//                videoStatus = new JSONObject(response.substring(beginIndex, endIndex));
//                status = videoStatus.getString("status");
//
//                // User logged in and a membership level.
//                if ("VOD_ON_AIR".equals(status)) {
//                    res += videoStatus.getString("vid");
//                    res += "|";
//                    res += videoStatus.getString("inkey");
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
        }
        return res.split("\\|");
    }

    @Test
    public void testParse() {
        String a = "<div class=\"_status_area\" style=\"display:none;\">\n" +
                "\t\t<span class=\"info_tx\">\n" +
                "\t\t\t<span class=\"like\">\n" +
                "\t\t\t\t<em class=\"icon_like\"><span class=\"blind\">좋아요</span></em>\n" +
                "\t\t\t\t<span class=\"txt\">144.177</span>\n" +
                "\t\t\t</span>\n" +
                "\t\t\t<span class=\"date\">1 bulan yang lalu</span>\n" +
                "\t\t</span>\n" +
                "\t</div>\n" +
                "<script type=\"text/javascript\" charset=\"utf-8\">\n" +
                "var oVideoStatus = {\n" +
                "\t\"videoSeq\" : 169233,\n" +
                "\t\"status\" : \"VOD_ON_AIR\",\n" +
                "\t\"vpdid\" : \"ecb3f0a09f4b2e955c35c9680bf7aecacca538125d75cd731f53006da6a08372\",\n" +
                "\t\"vid\" : \"B0BE88F3BAE7975D6F0E66354C71C8FC0FDC\", \"inkey\" : \"V12773d046c2d92bee190e64a08116773375e89155793574cc333d84138bbfac9a6cce64a08116773375e\",\n" +
                "\t\"disableAd\" : true,\n" +
                "\t\"startTime\" : 4446712,\n" +
                "\t\"viewType\" : \"vod\",\n" +
                "\t\"rentalYn\" : false}\n" +
                "</script>\n";

        print(parseVideoStatus(a)[0]);
    }

    @Test
    public void test() {
//        String res = VAPIS.getAPIPosts(null, "AAA");
//        print(res);
        String enc0 = "knT9Qwr/5UIpS7+HQ7bss9IXVn+YZbbUOs+ck5rPZiyDfJl/YcItWnU6WCmM4Ce/wyBrXZ9T+Nd3XfGYx/qSJomR0Mq7I3QwePaktb1nS5iWYSqlD//UUZS4ynXmCc0CAjN4JWptoD5b/z/jXCM+GoH/c8iY9llJ1Z4prw8TFj0gA/cz4tC+b+eBO289JsZ8";
////        String res1 = "http://api.vfan.vlive.tv/post.%s?gcc=KR&locale=en&app_id=8c6cc7b45d2568fb668be6e05b6e5a3b&on_complete=redirect_to_location";
        String key1 = "tOWwmGR0QLEBCHtpuFrf9u+LZtWODMxIoFHyl2+8BTE=";
        String enc1 = generator.generate(enc0, key1);
        String r1 = generator.crypt(enc1, key1);
        String enc2 = generator.generate(r1, key1);
        print(enc1);
        print(r1);
        print(enc2);
        print(new String(Base64.decodeBase64("QUVTL0NCQy9QS0NTNVBBRERJTkc="), StandardCharsets.UTF_8));

    }

    @Test
    public void testClass() {
        Class<? extends ABC> clazz = DEF.class;
        ABC def = new DEF();
        print("Name : " + def.getClassName());
        print("Canonical Name : " + clazz.getCanonicalName());
        print("Simple Name : " + clazz.getSimpleName());
        print("Type Name : " + clazz.getTypeName());

    }

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