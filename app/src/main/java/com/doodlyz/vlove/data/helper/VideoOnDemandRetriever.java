package com.doodlyz.vlove.data.helper;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.doodlyz.vlove.AppSettings;
import com.doodlyz.vlove.R;
import com.doodlyz.vlove.VolleyRequest;
import com.doodlyz.vlove.apis.VAPIS;
import com.doodlyz.vlove.databases.VideoOnDemand;
import com.doodlyz.vlove.logger.CrashCocoExceptionHandler;
import com.doodlyz.vlove.ui.dialogs.Popup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoOnDemandRetriever {
    private WeakReference<Context> mContext;

    private OnRetrievedListener mListener;

    private Data mData = new Data();

    // Data extras.
    private int mVideoViewCount, mVideoLikeCount;
    private String mVideoTitle, mVideoChannelName;

    private boolean mCancel = false;
    private final String tag = UUID.randomUUID().toString();

    public static VideoOnDemandRetriever retrieve(Context context, String url, OnRetrievedListener listener) {
        return new VideoOnDemandRetriever(context, url, listener);
    }

    public void cancel() {
        mCancel = true;
        VolleyRequest.with(mContext.get()).cancelPendingRequest(tag);
        clearContext();
    }

    private VideoOnDemandRetriever(Context context, String url, final OnRetrievedListener listener) {
        Thread.setDefaultUncaughtExceptionHandler(CrashCocoExceptionHandler.asDefault("VODR.U"));
        mContext = new WeakReference<>(context);
        mListener = new OnRetrievedListener() {
            @Override
            public void onRetrieved(Data data) {
                listener.onRetrieved(data);
                clearContext();
            }

            @Override
            public void onError(String message) {
                listener.onError(message);
                clearContext();
            }
        };
        execute(url);
    }

    private void clearContext() {
        mContext.clear();
        mContext = null;
    }

    private void execute(final String url) {
        execute0(url);
    }

    private void execute0(final String url) {
        final String videoId = getId(url);
        VolleyRequest.StringRequest request = new VolleyRequest.StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] extraParams = getParams(response);

                        if (extraParams.length != 4) {
                            mListener.onError("extraParams not valid!");
                        }
                        else {
                            if (FanshipHelper.help().isPlus(extraParams)) {
                                FanshipHelper.help().addVideoIdAndKey(
                                        mContext.get(),
                                        videoId,
                                        extraParams,
                                        (extraParams1, status) -> {
                                            // Not login?
                                            if (extraParams1[2].isEmpty() || extraParams1[3].isEmpty()) {
                                                Popup
                                                        .with(mContext.get(), Popup.ID_INFO)
                                                        .make(R.string.alert_error_title, R.string.alert_video_is_vlive_plus)
                                                        .show();
                                                Popup
                                                        .with(mContext.get(), Popup.ID_INFO)
                                                        .make("Status", status)
                                                        .show();
                                            }
                                            else {
                                                next(response, extraParams1);
                                            }
                                        });
                            }
                            else {
                                next(response, extraParams);
                            }
                        }
                    }

                    private void next(String response, String[] extraParams) {
                        mVideoTitle = getTitle(response);
                        mVideoChannelName = getChannelName(response);

                        if (!mCancel) {
                            execute1(videoId, extraParams[0], extraParams[2], extraParams[3]);
                        }
                    }
                },
                error -> {
                    if (error.getMessage() != null) {
                        mListener.onError("execute0: " + error.getMessage());
                    }
                    else {
                        String response;
                        try {
                            response = new String(error.networkResponse.data, "UTF-8");
                        }
                        catch (UnsupportedEncodingException e) {
                            response = new String(error.networkResponse.data);
                        }
                        mListener.onError("execute0: \n\n response: " + response + "\n\n status code: " + error.networkResponse.statusCode);
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Host", "www.vlive.tv");
                headers.put("User-Agent", AppSettings.VLOVE_USER_AGENT);
                return headers;
            }
        } ;
        VolleyRequest.with(mContext).addToQueue(request);
    }

    /**
     * Get video count.
     * @param videoId
     * @param channelCode
     */
    private void execute1(final String videoId, final String channelCode, final String longVideoId, final String key) {
        String url = VAPIS.getAPIVideoCount(mContext.get(), videoId, channelCode, System.currentTimeMillis());
        VolleyRequest.StringRequest request = new VolleyRequest.StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (parseVideoCount(response)) {
                            if (!mCancel) {
                                execute2(videoId, channelCode, longVideoId, key);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mListener.onError("execute1: " + error.getMessage());
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Host", "www.vlive.tv");
                headers.put("Referer", "http://www.vlive.tv/video/" + videoId);
                headers.put("User-Agent", AppSettings.VLOVE_USER_AGENT);
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }
        };
        VolleyRequest.with(mContext).addToQueue(request);
    }

    /**
     * Get video info.
     * @param videoId
     * @param channelCode
     * @param longVideoId
     * @param key
     */
    private void execute2(final String videoId, final String channelCode, final String longVideoId, final String key) {
        String url = VAPIS.getAPIVideoInfo(mContext.get(), longVideoId, key);
        VolleyRequest.StringRequest request = new VolleyRequest.StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (parseVideoInfo(response)) {
                            if (!mCancel) {
                                execute3();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mListener.onError("execute2: " + error.getMessage());
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Host", "global.apis.naver.com");
                headers.put("User-Agent", AppSettings.VLOVE_USER_AGENT);
                return headers;
            }
        };
        VolleyRequest.with(mContext).addToQueue(request);
    }

    /**
     * Final action.
     */
    private void execute3() {
        if (!mCancel) mListener.onRetrieved(mData);
    }

    private boolean parseVideoCount(String webpage) {
        if (webpage == null) {
            mListener.onError("webpage can't be null");
            return false;
        }
        Pattern validUrl = Pattern.compile("<span class=\"txt\">([0-9,]+)</span>");
        Matcher match = validUrl.matcher(webpage);
        int s = 0;
        String count;
        while (match.find()) {
            count = match.group(1).replaceAll(",", "");
            switch(s) {
                case 0: // viewCount
                    mVideoViewCount = Integer.parseInt(count);
                    break;
                case 1: // heartCount
                    mVideoLikeCount = Integer.parseInt(count);
                    break;
            }
            s++;
        }

        if (mVideoViewCount > -1 && mVideoLikeCount > -1) {
            return true;
        }
        else {
            mListener.onError("Failed to parse video count");
            return false;
        }
    }

    private boolean parseVideoInfo(String jsonString) {
        if (jsonString == null) {
            mListener.onError("jsonString can't be null");
            return false;
        }
        try {
            JSONObject container = new JSONObject(jsonString);
            JSONObject meta = container.getJSONObject("meta");
            JSONArray list = container.getJSONObject("videos").getJSONArray("list");

            String id = meta.getString("masterVideoId");
            String cover = meta.getJSONObject("cover").getString("source");

            VideoOnDemand.Video.Builder builder;
            for (int i = 0, l = list.length(); i < l; i++) {
                meta = list.getJSONObject(i);

                builder = new VideoOnDemand.Video.Builder(id)
                .setTitle(mVideoTitle)
                .setSource(meta.getString("source"))
                .setChannel(mVideoChannelName)
                .setCover(cover)
                .setSize(meta.getLong("size"))
                .setDuration(meta.getDouble("duration"));

                meta = meta.getJSONObject("encodingOption");
                builder.setResolution(meta.getString("name").replaceAll("[pP]", "").trim())
                .setWidth(meta.getInt("width"))
                .setHeight(meta.getInt("height"))
                .setViewCount(mVideoViewCount)
                .setLikeCount(mVideoLikeCount);

                mData.videos.add(builder.build());
            }

            Collections.sort(mData.videos, new Comparator<VideoOnDemand.Video>() {

                @Override
                public int compare(VideoOnDemand.Video o1, VideoOnDemand.Video o2) {
                    // TODO Auto-generated method stub
                    return Integer.compare(Integer.parseInt(o1.getResolution()), Integer.parseInt(o2.getResolution()));
                }

            });

            if (container.has("captions")) {
                list = container.getJSONObject("captions").getJSONArray("list");

                VideoOnDemand.Caption caption;
                for (int i = 0, l = list.length(); i < l; i++) {
                    meta = list.getJSONObject(i);

                    caption = new VideoOnDemand.Caption.Builder()
                    .setLabel(meta.getString("label"))
                    .setLocale(meta.getString("locale"))
                    .setLanguage(meta.getString("language"))
                    .setCountry(meta.getString("country"))
                    .setSource(meta.getString("source"))
                    .setCategory(getCaptionCategory(meta.getString("source")))
                    .build();

                    mData.captions.add(caption);
                }
            }

            return true;
        }
        catch (JSONException e) {
            mListener.onError("Failed to parse video info");
            return false;
        }
    }

    private static String getCaptionCategory(String captionSource) {
        String info = "";
        if (captionSource != null) {
            Pattern regex = Pattern.compile("_(fan|cp|auto)\\.vtt");
            Matcher match = regex.matcher(captionSource);
            if (match.find()) {
                info = match.group(1).trim();
                info = info.equalsIgnoreCase("fan") ? "(By Fan)" : info.equalsIgnoreCase("auto") ? "(Auto)" : "";
            }
        }

        return info;
    }

    private static String getId(String url) {
        String id = "";
        Pattern validUrl = Pattern.compile("https?://(www\\.|m\\.|mobile\\.)?vlive\\.tv/video/(\\d+)");
        Matcher match = validUrl.matcher(url);
        if (match.find()) {
            id = match.group(2);
        }
        return id;
    }

    @NonNull
    private static String getChannelName(String webpage) {
        String channelName = "";
        Pattern regex = Pattern.compile("<div[^>]+class=\"info_area\"[^>]*>\\s*<a\\s+[^>]*>([^<]+)");
        Matcher match = regex.matcher(webpage);
        if (match.find()) {
            channelName = match.group(1);
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 ? Html.fromHtml(channelName, Html.FROM_HTML_MODE_COMPACT).toString() : Html.fromHtml(channelName).toString();
    }

    @NonNull
    private static String getTitle(String webpage) {
        String title = "";
        Pattern regex = Pattern.compile("\"og:title\" content=\"(.+)\"/>");
        Matcher match = regex.matcher(webpage);
        if (match.find()) {
            title = match.group(1);
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 ? Html.fromHtml(title, Html.FROM_HTML_MODE_COMPACT).toString() : Html.fromHtml(title).toString();
    }

    private static String[] getParams(String webpage) {
        return parseParams(webpage);
    }

    private static String[] parseParams(String webpage) {
        String[] result;
        JSONArray container;
        Pattern regex = Pattern.compile("vlive\\.video\\.init\\(([^)]+)");
        Matcher match = regex.matcher(webpage);
        if (match.find()) {
            try {
                container = new JSONObject("{container: [" + match.group(1) + "]}").getJSONArray("container");
                result = new String[4];
                result[0] = container.optString(4); // channelCode
                result[1] = container.optString(2); // status
                result[2] = container.optString(5); // longVideoId
                result[3] = container.optString(6); // key
            }
            catch (JSONException e) {
                result = null;
            }
        }
        else {
            result = null;
        }
        return result;
    }

    public static class Data implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = -7702252378428917368L;

        public ArrayList<VideoOnDemand.Video> videos = new ArrayList<>();
        public ArrayList<VideoOnDemand.Caption> captions = new ArrayList<>();

        private Data() {}
    }

    public interface OnRetrievedListener {
        void onRetrieved(Data data);
        void onError(String message);
    }
}
