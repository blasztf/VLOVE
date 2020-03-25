package com.doodlyz.vlove;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.android.volley.toolbox.RequestFuture;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.doodlyz.vlove", appContext.getPackageName());
    }

    @Test
    public void mockFanship() {
        String videoId = "169233";
        String channelCode = "E203A5";
        String url = String.format("https://www.vlive.tv/video/init/view?videoSeq=%s&channelCode=%s", videoId, channelCode);
        String referer = String.format("https://www.vlive.tv/video/%s?channelCode=%s", videoId, channelCode);
        String host = "www.vlive.tv";
        String cookie = "timezoneOffset=-120; _ga=GA1.2.22056447.1575832836; userLanguage=id; _gid=GA1.2.1436269726.1584434214; userCountry=ID; webp=true; JSESSIONID=3E9FA548622F593D5102378689CC14A3; _gat=1; snsCd=facebook; NEO_SES=\"sDsIbxEaIVQLGwFJpn0YESVwzHhLr/ggpYIFPzJ2EqagYCQGfDXq/poZ2y43Qc7IQ67BTVMofDV0RVifT7WpvGIHUHUzW92CBty8Qd0qOROezmgT0UezRNvyilwGZ468Q4cnbX1sRdAEc66uhv6WTE24xykPq48jVVys7cm0Rnved9HG5rCjZTv1725KsMAgWL46V0WfamTctAooeBkFTzZieOWaflp9z6sQedwHpBs8qcIQySvMOR8XGCB7wHt7RN6dGoOBEKxc8YTnHVKUuRkYf1LGrsRFIvacm6R0HqqLUb+Dj4jV8a6wuI9JY3wfUWvAtAGEv/meX4P9W5OgCOllABro3i9WI6/FzcXzwQsMtfDCZua00HFoomYmHgeVwAuCRd74YP0uw4vSffNL3pkdNSZpSR1WkYbTATkuGes9LKEH+MlxorfxQVHG2Fr7K8WAQJ5hoi3fs/C5DsNu8A==\"; NEO_CHK=\"PZTO+AJBfWvbsLj1CVg9su1K19FPXRavdMkCx9keMn9FreYjHtUXDFkuFLtGgR6ZvwwAcDDJTjTSdCfzG2lYojMV/VMVDLmYxpxtPummantS/0HavZcrPbZ7MyU9Q64HzcSnW+gjzKxcOXMsnkD9GQ==\"; emailAuthStatus=NOT-NOTIFIED";

        VolleyRequest.StringRequest request;
        String response;

        RequestFuture<String> future = RequestFuture.newFuture();

        request = new VolleyRequest.StringRequest(url, future, future);
        request.setHost(host);
        request.setReferer(referer);

        VolleyBall.getInstance().addToQueue(request);

        try {
            response = future.get();
            print(response);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void print(String s) {
        Log.d("TAG", s);
    }

    @Test
    public void test1() throws ExecutionException, InterruptedException {
        String cookie = "timezoneOffset=-120; userLanguage=id; webp=true; userCountry=ID; snsCd=facebook; NEO_SES=\"vnT3FsB5qXAb4ehyjxsKh6R/e+2JsmZiHSR+RuGpblIM6iaaPT2DNVKvdWCn1dI+etYQ6CCiQeDUYH6fx/OUBXmPptmqpQwqqmvGMjrKEKKdtPL++nnOwu3UsXRcT3AatbuIggEUDTrt0bBgHVTv1ev5JKgR9/uAzdrXtCGkiUEqwjrpLKx5Whs94j5lPzET14cpyEOvWGsjXsCgkUzYvg3YEFFNEqANsEtl+TuHSKFw0FAPW11D8ONNYQW1v/hoYZ040FLlqFAwkbba8sSLTzT9vb9bO/DBRL3LJdmwDxE9i74hPDm/nCffhRLPTXRSFgfSCzTZmufB0j1NrAz4N24ikh3VKnXI5nCHS5lQdd5wPFGVRMrK4uWqG8rfpB/RkWrd+jvBoDojVAGTsIF5c7D1b5SW702a9NmHgYuYZKn/UQmjq9gK5F/cJy/fREUofe5aibXPjk0LHW+ZnZoF1Q==\"; NEO_CHK=\"PZTO+AJBfWvbsLj1CVg9su1K19FPXRavdMkCx9keMn9FreYjHtUXDFkuFLtGgR6ZvwwAcDDJTjTSdCfzG2lYojMV/VMVDLmYxpxtPummant+KTtePm2JaAOe6bc3nRVJVWHznqR/RMCp2rgZjNC7TA==\"; emailAuthStatus=NOT-NOTIFIED; _gat=1; _ga=GA1.2.22056447.1575832836; _gid=GA1.2.1436269726.1584434214";
        Context context = InstrumentationRegistry.getTargetContext();

        VolleyRequest.with(context).setCookie("https://www.vlive.tv", cookie);

        RequestFuture<String> future = RequestFuture.newFuture();

        String videoId = "169233";
        String channelCode = "E203A5";

        String api = String.format("https://www.vlive.tv/video/init/view?videoSeq=%s&channelCode=%s", videoId, channelCode);

        VolleyRequest.StringRequest request = new VolleyRequest.StringRequest(api, future, future);
        request.setReferer(String.format("https://www.vlive.tv/video/%s?channelCode=%s", videoId, channelCode))
                .setHost("www.vlive.tv");

        VolleyRequest.with(context).addToQueue(request);
        String res = future.get();
        log(res);
    }

    private void log(String s) {
        System.out.println(s);
    }
}
