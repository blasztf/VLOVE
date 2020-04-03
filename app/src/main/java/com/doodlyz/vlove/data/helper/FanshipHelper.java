package com.doodlyz.vlove.data.helper;

import android.content.Context;

import com.doodlyz.vlove.VloveSettings;
import com.doodlyz.vlove.VloveRequest;
import com.doodlyz.vlove.logger.CrashCocoExceptionHandler;

import org.json.JSONException;
import org.json.JSONObject;

class FanshipHelper {
    static final String STATUS_VOD_ON_AIR = "VOD_ON_AIR";
    static final String STATUS_NEED_CHANNEL_PLUS = "NEED_CHANNEL_PLUS";

    private static FanshipHelper mInstance;

    static synchronized FanshipHelper help() {
        if (mInstance == null) {
            mInstance = new FanshipHelper();
        }

        return mInstance;
    }

    private FanshipHelper() {
        Thread.setDefaultUncaughtExceptionHandler(CrashCocoExceptionHandler.asDefault("FH.U"));
    }

    boolean isPlus(String[] extraParams) {
        return extraParams[2].isEmpty() || extraParams[3].isEmpty();
    }

    void addVideoIdAndKey(Context context, String videoId, String[] extraParams, Listener listener) {
        String channelCode = extraParams[0];
        getVideoStatus(context, videoId, channelCode, extraParams, listener);
    }

    private void getVideoStatus(Context context, String videoId, String channelCode, String[] extraParams, Listener listener) {
        String api = String.format("https://www.vlive.tv/video/init/view?videoSeq=%s&channelCode=%s", videoId, channelCode);
        String vpdid2 = VloveSettings.getInstance(context).getVLivePlusDeviceId();
        if (!vpdid2.isEmpty()) {
            api += "&vpdid2=" + vpdid2;
        }
        VloveRequest.ApiRequest request = new VloveRequest.ApiRequest(
                api,
                response -> {
                    String[] result = parseVideoStatus(response);
                    if (result.length == 4) {
                        extraParams[2] = result[1];
                        extraParams[3] = result[2];
                        VloveSettings.getInstance(context).setVLivePlusDeviceId(result[3]);
                    }
                    listener.onSuccess(extraParams, result[0]);
                    CrashCocoExceptionHandler.with("FH.R").debugLog(response);
                },
                error -> {
//                    error.printStackTrace();
                    CrashCocoExceptionHandler.with("FH").debugLog(error);
                    listener.onSuccess(extraParams, error.getMessage());
                })
                .setHost(VloveSettings.VLIVE_HOST)
                .setReferer(String.format("https://www.vlive.tv/video/%s?channelCode=%s", videoId, channelCode));
        VloveRequest.with(context).addToQueue(request);
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
            try {
                CrashCocoExceptionHandler.with("FH.PVS").debugLog(response.substring(beginIndex, endIndex));
                videoStatus = new JSONObject(response.substring(beginIndex, endIndex));
                status = videoStatus.getString("status");

                res += status;
                // User logged in and is membership level.
                if (STATUS_VOD_ON_AIR.equals(status)) {
                    res += "|";
                    res += videoStatus.getString("vid");
                    res += "|";
                    res += videoStatus.getString("inkey");
                    res += "|";
                    res += videoStatus.getString("vpdid");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return res.split("\\|");
    }

    interface Listener {
        void onSuccess(String[] extraParams, String status);
    }
}
