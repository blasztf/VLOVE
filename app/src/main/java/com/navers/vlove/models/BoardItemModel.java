package com.navers.vlove.models;

import com.navers.vlove.databases.Board;
import com.navers.vlove.presenters.BoardItemPresenter;

public class BoardItemModel extends ItemModelAbs {
    public static final int TYPE = 2;
    private static final String STICKER_SOURCE = "http://v.phinf.naver.net/";

    private Board.Post mPost;
    private BoardItemPresenter.OnContentClickListener mOnContentClickListener;
    private BoardItemPresenter.OnRemoveClickListener mOnRemoveClickListener;

    public BoardItemModel(Board.Post post, BoardItemPresenter.OnRemoveClickListener onRemoveClickListener) {
        this(post, onRemoveClickListener, null);
    }

    public BoardItemModel(Board.Post post, BoardItemPresenter.OnRemoveClickListener onRemoveClickListener, BoardItemPresenter.OnContentClickListener onContentClickListener) {
        mPost = post;
        mOnRemoveClickListener = onRemoveClickListener;
        mOnContentClickListener = onContentClickListener;
    }

    @Override
    public int getType() {
        return TYPE;
    }

    public Board.Post getPostObject() {
        return mPost;
    }

    public String getPostUser() {
        return mPost.getUser();
    }

    public String getPostUserImage() {
        return mPost.getUserImage();
    }

    public String getPost() {
        return mPost.getContent();
    }

    public long getPostCreatedTime() {
        return mPost.getCreatedAt();
    }

    public String getStickerSource() {
        if (getLatestComment() != null) {
            Board.Comment latestComment = getLatestComment();
            if (
                    (latestComment.getStickerPack() != null && !latestComment.getStickerPack().isEmpty()) &&
                    (latestComment.getStickerId() != null && !latestComment.getStickerId().isEmpty())
                    ) {
                return STICKER_SOURCE + getLatestComment().getStickerPack() + "/" + getLatestComment().getStickerId() + ".png";
            }
        }
        return null;
    }

    public int getTotalComment() {
        return mPost.getTotalComment();
    }

    public BoardItemPresenter.OnContentClickListener getOnContentClickListener() {
        return mOnContentClickListener;
    }

    public BoardItemPresenter.OnRemoveClickListener getOnRemoveClickListener() {
        return mOnRemoveClickListener;
    }

    public Board.Comment getLatestComment() {
        if (mPost.getComments().isEmpty()) return null;
        else return mPost.getComments().get(mPost.getComments().size()-1);
    }
}
