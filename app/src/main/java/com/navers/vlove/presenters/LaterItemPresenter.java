package com.navers.vlove.presenters;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.navers.vlove.R;
import com.navers.vlove.databases.VideoOnDemand;
import com.navers.vlove.models.LaterItemModel;

public class LaterItemPresenter extends Presenter<LaterItemModel, LaterItemPresenter.LaterItemHolder> {
    public LaterItemPresenter(int viewType) {
        super(viewType);
    }

    public LaterItemPresenter(int viewType, Object additionalValue) {
        super(viewType, additionalValue);
    }

    @Override
    public LaterItemPresenter.LaterItemHolder createViewHolder(@NonNull ViewGroup parent) {
        return new LaterItemPresenter.LaterItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_later, parent, false));
    }

    @Override
    public void bindView(@NonNull LaterItemModel model, @NonNull LaterItemPresenter.LaterItemHolder holder) {
        holder.setThumbnail(model.getThumbnail());
        holder.setTitle(model.getTitle());
        holder.setChannel(model.getChannel());
        holder.setDuration(model.getDuration());
        holder.setQuality(model.getQuality());
        holder.setOnContentClickListener(model.getOnContentClickListener());
        holder.setModel(model);
    }

    public interface OnContentClickListener {
        void onContentClick(VideoOnDemand.Video video);
    }

    static class LaterItemHolder extends RecyclerView.ViewHolder {
        private ImageView videoThumb;
        private TextView tvVodLength, title, channelName, downloadQuality;

        private OnContentClickListener listener;
        private LaterItemModel model;

        public LaterItemHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) listener.onContentClick(null);
                }
            });
            initView();
        }

        private Context getContext() {
            return itemView.getContext();
        }

        private View findViewById(@IdRes int id) {
            return itemView.findViewById(id);
        }

        private void initView() {
            videoThumb = (ImageView) findViewById(R.id.videoThumb);
            tvVodLength = (TextView) findViewById(R.id.tvVodLength);
            title = (TextView) findViewById(R.id.title);
            channelName = (TextView) findViewById(R.id.channelName);
            downloadQuality = (TextView) findViewById(R.id.downloadQuality);
        }

        private void setThumbnail(String thumbnail) {
            Glide.with(getContext()).load(thumbnail).into(videoThumb);
        }

        private void setDuration(double duration) {
            tvVodLength.setText(formatSecToTime(duration));
        }

        private void setTitle(String title) {
            this.title.setText(title);
        }

        private void setChannel(String channel) {
            channelName.setText(channel);
        }

        private void setQuality(String quality) {
            downloadQuality.setText(quality);
        }

        private void setOnContentClickListener(OnContentClickListener listener) {
            this.listener = listener;
        }

        private void setModel(LaterItemModel model) {
            this.model = model;
        }

        private String formatSecToTime(double seconds) {
            int hour = (int) seconds / 3600,
                    minute = (int) (seconds % 3600) / 60,
                    second = (int) seconds % 60;

            String time = "";
            if (hour > 0) {
                time += hour + ":";
            }
            time += (minute < 10 ? ("0" + minute) : minute) + ":";
            time += (second < 10 ? ("0" + second) : second);

            return time;
        }
    }
}
