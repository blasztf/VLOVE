package com.doodlyz.vlove.presenters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.doodlyz.vlove.R;
import com.doodlyz.vlove.databases.Board;
import com.doodlyz.vlove.models.BoardItemModel;
import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.util.Date;

public class BoardItemPresenter extends Presenter<BoardItemModel, BoardItemPresenter.BoardItemHolder> {

    public BoardItemPresenter(int viewType) {
        super(viewType);
    }

    public BoardItemPresenter(int viewType, Object additionalValue) {
        super(viewType, additionalValue);
    }

    @Override
    public BoardItemHolder createViewHolder(@NonNull ViewGroup parent) {
        return new BoardItemPresenter.BoardItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board, parent, false));
    }

    @Override
    public void bindView(@NonNull BoardItemModel model, @NonNull BoardItemHolder holder) {
        holder.setPost(model.getPost());
        holder.setPostUser(model.getPostUser());
        holder.setPostUserImage(model.getPostUserImage());
        holder.setPostCreated(model.getPostCreatedTime());

        Board.Comment latestComment = model.getLatestComment();

        if (latestComment != null) {
            holder.setLastComment(latestComment.getContent());
            holder.setLastCommentUser(latestComment.getUser());
            holder.setLastCommentUserImage(latestComment.getUserImage());
            holder.setLastCommentCreated(latestComment.getCreatedAt());
            holder.setLastCommentSticker(model.getStickerSource());
        }

        holder.setTotalComment(model.getTotalComment());
        holder.setOnContentClickListener(model.getOnContentClickListener());
        holder.setOnRemoveClickListener(model.getOnRemoveClickListener());
        holder.setModel(model);
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(BoardItemModel model);
    }

    public interface OnContentClickListener {
        void onContentClick(BoardItemModel model);
    }

    static class BoardItemHolder extends RecyclerView.ViewHolder {
        private TextView mPostUser, mPost, mPostCreated, mLastCommentUser, mLastComment, mLastCommentCreated, mTotalComment;
        private ImageView mPostUserImage, mLastCommentUserImage, mLastCommentSticker;
        private ImageButton mRemove;

        private OnRemoveClickListener mOnRemoveClickListener;
        private OnContentClickListener mOnContentClickListener;

        private BoardItemModel mModel;

        public BoardItemHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnContentClickListener != null) mOnContentClickListener.onContentClick(mModel);
                }
            });
            initView();
        }

        private Context getContext() {
            return itemView.getContext();
        }

        private void initView() {
            initPost();
            initLastComment();
            initRemove();
        }

        private void initPost() {
            mPostUserImage = itemView.findViewById(R.id.postUserImage);
            mPostUser = itemView.findViewById(R.id.postUser);
            mPost = itemView.findViewById(R.id.post);
            mPostCreated = itemView.findViewById(R.id.postCreated);
        }

        private void initLastComment() {
            mLastCommentUserImage = itemView.findViewById(R.id.lastCommentUserImage);
            mLastCommentUser = itemView.findViewById(R.id.lastCommentUser);
            mLastComment = itemView.findViewById(R.id.lastComment);
            mLastCommentCreated = itemView.findViewById(R.id.lastCommentCreated);
            mLastCommentSticker = itemView.findViewById(R.id.lastCommentSticker);
            mTotalComment = itemView.findViewById(R.id.totalComment);
        }

        private void initRemove() {
            mRemove = itemView.findViewById(R.id.remove);
            mRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnRemoveClickListener != null) mOnRemoveClickListener.onRemoveClick(mModel);
                }
            });
        }

        public void setPostUser(String postUser) {
            mPostUser.setText(postUser);
        }

        public void setPostUserImage(String postUserImage) {
            RequestOptions requestOptions = new RequestOptions().circleCrop().error(R.drawable.ui_unknown).fallback(R.drawable.ui_unknown);
            Glide.with(getContext()).load(postUserImage).apply(requestOptions).into(mPostUserImage);
        }

        public void setPost(String post) {
            mPost.setText(post);
        }

        public void setPostCreated(long postCreated) {
            mPostCreated.setText(DateFormat.getDateTimeInstance().format(new Date(postCreated)));
        }

        public void setLastCommentUser(String lastCommentUser) {
            mLastCommentUser.setText(lastCommentUser);
        }

        public void setLastCommentUserImage(String lastCommentUserImage) {
            RequestOptions requestOptions = new RequestOptions().circleCrop().error(R.drawable.ui_unknown).fallback(R.drawable.ui_unknown);
            Glide.with(getContext()).load(lastCommentUserImage).apply(requestOptions).into(mLastCommentUserImage);
        }

        public void setLastComment(String lastComment) {
            mLastComment.setText(lastComment);
        }

        public void setLastCommentCreated(long lastCommentCreated) {
            mLastCommentCreated.setText(DateFormat.getDateTimeInstance().format(new Date(lastCommentCreated)));
        }

        public void setLastCommentSticker(String lastCommentSticker) {
            if (lastCommentSticker != null) {
                mLastCommentSticker.setVisibility(View.VISIBLE);
                Glide.with(getContext()).load(lastCommentSticker).into(mLastCommentSticker);
            }
            else {
                mLastCommentSticker.setVisibility(View.GONE);
            }
        }

        public void setTotalComment(int totalComment) {
            mTotalComment.setText(getContext().getString(R.string.board_total_comment, totalComment));
        }

        public void setOnRemoveClickListener(OnRemoveClickListener listener) {
            mOnRemoveClickListener = listener;
        }

        public void setOnContentClickListener(OnContentClickListener listener) {
            mOnContentClickListener = listener;
        }

        public void setModel(BoardItemModel model) {
            mModel = model;
        }
    }
}
