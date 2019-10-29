package com.navers.vlove.ui.dialogs;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.navers.vlove.R;
import com.navers.vlove._.deprecated.features.StorageUtils;
import com.navers.vlove.apis.VAPIS;
import com.navers.vlove.broadcasters.SaverBroadcaster;
import com.navers.vlove.data.helper.VideoOnDemandRetriever;
import com.navers.vlove.databases.VideoOnDemand;
import com.navers.vlove.services.DownloaderService;
import com.navers.vlove.ui.helper.RecyclerViewCompat;
import com.navers.vlove.views.MenuScreenActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Saver extends BaseDialog implements View.OnClickListener {
    private static final String VIDEO_URL = Saver.class.getSimpleName() + ".VIDEO_URL:LString";

    private int selectedVideo, selectedCaption;
    private VideoOnDemandRetriever.Data selectedVOD;

    private SaverBroadcaster broadcaster;

    private TextView title, loadingStatus;
    private LinearLayout dialogScreen, loadingScreen;
    private ProgressBar loadingProgress;
    private LottieAnimationView loadingImage;
    private RecyclerView videoRes;
    private NumberPicker captionList;
    private Button cancel, save;

    public static Saver with(Context context, String videoURI) {
        return new Saver(context).setVideo(videoURI);
    }

    private Saver(Context context) {
        super(context);
    }

    @BuilderMethod
    private Saver setVideo(String uri) {
        getIntent().putExtra(Saver.VIDEO_URL, uri);
        return this;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_dialog_saver;
    }

    @Override
    protected void onPrepareContentViewElement() {
        dialogScreen    = findViewById(R.id.dialogScreen);
        title           = findViewById(R.id.title);
        loadingScreen   = findViewById(R.id.loadingScreen);
        loadingProgress = findViewById(R.id.loadingProgress);
        loadingImage    = findViewById(R.id.loadingImage);
        loadingStatus   = findViewById(R.id.loadingStatus);
        videoRes        = findViewById(R.id.videoRes);
        captionList     = findViewById(R.id.captionList);
        cancel          = findViewById(R.id.cancel);
        save            = findViewById(R.id.save);
    }

    @Override
    protected void onReady(Intent intent) {
        if (!VAPIS.isExpired(this)) {
            main();
        } else {
            startActivity(new Intent(this, MenuScreenActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (broadcaster != null) {
            unregisterReceiver(broadcaster);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.save:
                downloadVideo();
                break;
            case R.id.cancel:
                finish();
                break;
        }
    }

    private void main() {
        cancel.setOnClickListener(this);
        save.setOnClickListener(this);

        loadingProgress.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.color_light_blue_alt), PorterDuff.Mode.SRC_IN);

        videoRes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        videoRes.addOnItemTouchListener(new RecyclerViewCompat.OnItemClickListener(videoRes) {

            @Override
            public void onItemClick(View view, int position) {
                // TODO Auto-generated method stub
                super.onItemClick(view, position);
                selectedVideo = position;
            }

        });

        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(videoRes);

        String url = getIntent().getStringExtra(VIDEO_URL);
        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            String type;
            if ((type = getIntent().getType()) != null && type.startsWith("text/")) {
                url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            }
        }
        url = filterShareText(url);

        if (url == null) {
            Toast.makeText(this, R.string.share_text_not_valid_video, Toast.LENGTH_LONG).show();
            finish();
        } else {
            VideoOnDemandRetriever.retrieve(this, url, new VideoOnDemandRetriever.OnRetrievedListener() {
                @Override
                public void onRetrieved(VideoOnDemandRetriever.Data data) {
                    selectedVOD = data;
                    title.setText(data.videos.get(0).getTitle());

                    VideoResAdapter adapter = new VideoResAdapter(data.videos.get(0).getCover(), data.videos);
                    videoRes.setAdapter(adapter);

                    if (data.captions.size() > 0) {
                        ArrayList<String> listCaption = new ArrayList<>();

                        listCaption.add(Saver.this.getString(R.string.saver_no_subs));
                        for (VideoOnDemand.Caption caption : data.captions) {
                            listCaption.add(caption.getLabel() + " " + caption.getCategory());
                        }

                        captionList.setWrapSelectorWheel(false);
                        captionList.setMinValue(0);
                        captionList.setMaxValue(listCaption.size() - 1);
                        captionList.setDisplayedValues(listCaption.toArray(new String[0]));
                        captionList.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

                            @Override
                            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                                // TODO Auto-generated method stub
                                selectedCaption = newVal - 1;
                            }
                        });

                        listCaption.clear();
                    } else {
                        selectedCaption = -1;
                        captionList.setVisibility(View.GONE);
                    }

                    RecyclerView.ViewHolder vh = videoRes.findViewHolderForAdapterPosition(0);
                    if (vh != null) {
                        vh.itemView.performClick();
                    }

                    end();
                }

                @Override
                public void onError(String message) {
                    showToast(message);
                    end();
                    finish();
                }

                private void end() {
                    loadingScreen.setVisibility(View.GONE);
                    loadingImage.pauseAnimation();
                }
            });
        }
    }

    private String filterShareText(String shareText) {
        if (shareText != null) {
            Pattern pattern = Pattern.compile("(https?://(www\\.|m\\.)?vlive\\.tv/video/([0-9A-Za-z]+))");
            Matcher matcher = pattern.matcher(shareText);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private boolean isMemoryAvailable(VideoOnDemand.Video video) {
        // offset 2mb
        return (video.getSize() + (2L * 1024L * 1024L)) < StorageUtils.getAvailableExternalStorageSize();
    }

    private void downloadVideo() {
        if (StorageUtils.isExternalStorageAvailable()) {
            if (isMemoryAvailable(selectedVOD.videos.get(selectedVideo))) {
                Intent intent = new Intent(this, DownloaderService.class);
                intent.putExtra(DownloaderService.KEY_SELECTED_VIDEO, selectedVideo);
                intent.putExtra(DownloaderService.KEY_SELECTED_CAPTION, selectedCaption);
                intent.putExtra(DownloaderService.KEY_VODDATA, selectedVOD);
                showToast("Downloading...");
                startService(intent);
                setBridge();
            }
            else {
                Toast.makeText(this, "Memory is not enough, please free some memory first.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        else {
            Toast.makeText(this, "External storage not available.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setBridge() {
        IntentFilter filter = new IntentFilter(SaverBroadcaster.BROADCAST_FILTER_SAVER);
        dialogScreen.setVisibility(View.GONE);
        loadingScreen.setVisibility(View.VISIBLE);
        loadingProgress.setVisibility(View.VISIBLE);
        loadingImage.playAnimation();
        broadcaster = new SaverBroadcaster();
        broadcaster.setListener(new SaverBroadcaster.OnSaverListener() {
            @Override
            public void onDownload(int progress, String status) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    loadingProgress.setProgress(progress, true);
                }
                else {
                    loadingProgress.setProgress(progress);
                }

                if (status != null && !loadingStatus.getText().equals(status)) {
                    loadingStatus.setText(status);
                }
            }

            @Override
            public void onError() {
                end();
            }

            @Override
            public void onSuccess() {
                end();
            }

            private void end() {
                loadingImage.pauseAnimation();
                loadingImage.setImageResource(R.drawable.ic_finger_heart_alt_material);
                loadingProgress.setVisibility(View.GONE);
                unregisterReceiver(broadcaster);
                broadcaster = null;
            }
        });

        registerReceiver(broadcaster, filter);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

    }

    private static class VideoResAdapter extends RecyclerView.Adapter<VideoResAdapter.VH> {
        private String cover;
        private List<VideoOnDemand.Video> listVideo;

        private int highlightedItem = -1;

        private VideoResAdapter(String cover, List<VideoOnDemand.Video> listVideo) {
            this.cover = cover;
            this.listVideo = listVideo;
        }

        class VH extends RecyclerView.ViewHolder {
            private ImageView thumbnail;
            private TextView resolution;

            private VH(View itemView) {
                super(itemView);
                // TODO Auto-generated constructor stub
                thumbnail = itemView.findViewById(R.id.thumbnail);
                resolution = itemView.findViewById(R.id.resolution);

                itemView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        highlightedItem = getAdapterPosition();
                        highlightItem(v, highlightedItem);
                        notifyDataSetChanged();
                    }
                });
            }

            private void highlightItem(View itemView, int currentItem) {
                if (currentItem == highlightedItem) {
                    itemView.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.rounded_corner_highlight));
                }
                else {
                    itemView.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.rounded_corner));
                }
            }

            private void setThumbnail(String url) {
                Glide.with(itemView.getContext())
                        .load(url)
                        .apply(new RequestOptions()
                                .centerCrop()
                                .override(320, 240)
                                .placeholder(R.drawable.symbol_s)
                        ).into(thumbnail);
            }

            private void setResolution(String res) {
                resolution.setText(res);
            }
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // TODO Auto-generated method stub
            int resource = R.layout.item_video_saver;
            View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            // TODO Auto-generated method stub
            VideoOnDemand.Video video = getVideo(position);
            holder.setThumbnail(cover);
            holder.setResolution(video.getResolution());
            holder.highlightItem(holder.itemView, position);
        }

        @Override
        public int getItemCount() {
            // TODO Auto-generated method stub
            return listVideo.size();
        }

        private VideoOnDemand.Video getVideo(int position) {
            return listVideo.get(position);
        }
    }
}
