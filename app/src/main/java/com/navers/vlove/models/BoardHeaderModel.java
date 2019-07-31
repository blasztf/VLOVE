package com.navers.vlove.models;

import com.navers.vlove.presenters.BoardHeaderPresenter;

public class BoardHeaderModel implements ItemModel {
    public static final int TYPE = 1;

    private String mUserNickname;
    private BoardHeaderPresenter.OnSyncClickListener mListener;

    public BoardHeaderModel(String userNickname, BoardHeaderPresenter.OnSyncClickListener listener) {
        mUserNickname = userNickname;
        mListener = listener;
    }

    @Override
    public int getType() {
        return TYPE;
    }

    public String getUserNickname() {
        return mUserNickname;
    }

    public BoardHeaderPresenter.OnSyncClickListener getOnSyncClickListener() {
        return mListener;
    }
}
