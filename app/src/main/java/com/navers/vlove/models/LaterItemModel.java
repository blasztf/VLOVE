package com.navers.vlove.models;

import com.navers.vlove.databases.VideoOnDemand;
import com.navers.vlove.presenters.LaterItemPresenter;

public class LaterItemModel extends ItemModelAbs {
    public static final int TYPE = 4;

    private VideoOnDemand.Video mVideo;
    private LaterItemPresenter.OnContentClickListener mListener;

    public LaterItemModel(VideoOnDemand.Video video) {
        this(video, null);
    }

    public LaterItemModel(VideoOnDemand.Video video, LaterItemPresenter.OnContentClickListener listener) {
        final LaterItemPresenter.OnContentClickListener finalListener = listener;
        mVideo = video;
        mListener = new LaterItemPresenter.OnContentClickListener() {
            @Override
            public void onContentClick(VideoOnDemand.Video video) {
                finalListener.onContentClick(mVideo);
            }
        };
    }

    @Override
    public int getType() {
        return TYPE;
    }

    public String getThumbnail() {
        return mVideo.getCover();
    }

    public String getTitle() {
        return mVideo.getTitle();
    }

    public String getChannel() {
        return mVideo.getChannel();
    }

    public double getDuration() {
        return mVideo.getDuration();
    }

    public String getQuality() {
        return mVideo.getResolution();
    }

    public LaterItemPresenter.OnContentClickListener getOnContentClickListener() {
        return mListener;
    }
}
