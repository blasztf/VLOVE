package com.navers.vlove.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.navers.vlove.AppSettings;
import com.navers.vlove.R;
import com.navers.vlove.deprecated.features.StorageUtils;
import com.navers.vlove.deprecated.features.UtilNetwork;
import com.navers.vlove.broadcasters.SaverBroadcaster;
import com.navers.vlove.data.helper.VideoOnDemandRetriever;
import com.navers.vlove.databases.VideoOnDemand;
import com.navers.vlove.logger.CrashCocoExceptionHandler;
import com.navers.vlove.ui.helper.NotificationHelper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

import static com.navers.vlove.broadcasters.SaverBroadcaster.BRIDGE_FILTER_DATA_PROGRESS;
import static com.navers.vlove.broadcasters.SaverBroadcaster.BRIDGE_FILTER_DATA_STATUS;
import static com.navers.vlove.broadcasters.SaverBroadcaster.BRIDGE_STATUS_FAILED;
import static com.navers.vlove.broadcasters.SaverBroadcaster.BRIDGE_STATUS_SUCCESS;

public class DownloaderService extends Service {
    public static final String KEY_VODDATA = DownloaderService.class.getSimpleName() + ".KEY_VODDATA:LVODData";
    public static final String KEY_SELECTED_VIDEO = DownloaderService.class.getSimpleName() + ".KEY_SELECTED_VIDEO:int";
    public static final String KEY_SELECTED_CAPTION = DownloaderService.class.getSimpleName() + ".KEY_SELECTED_CAPTION:int";

    private NotificationCompat.Builder mNotificationBuilder;
    private int mNotificationId;

    private int selectedVideo, selectedCaption;
    private String savePath;

    private Task mTask;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        ExEater.eat(this);
        final VideoOnDemandRetriever.Data data = (VideoOnDemandRetriever.Data) intent.getSerializableExtra(KEY_VODDATA);

        selectedVideo = intent.getIntExtra(KEY_SELECTED_VIDEO, -1);
        selectedCaption = intent.getIntExtra(KEY_SELECTED_CAPTION, -1);

        initializeSavePath();

        if (data != null && selectedVideo >= 0) {
            mNotificationId = data.videos.get(selectedVideo).getTitle().hashCode();
            mNotificationBuilder = NotificationHelper.getBuilder(this, data.videos.get(selectedVideo).getTitle(), "Downloading...")
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX);

            NotificationHelper.notify(this, mNotificationId, mNotificationBuilder.build());

            mTask = new Task(this);
            mTask.execute(data);

            return START_STICKY;
        }
        else {
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTask != null) {
            mTask.cancel(true);
        }
        mTask = null;
    }

    private void initializeSavePath() {
        savePath = AppSettings.getInstance(this).getSaverDownloadPath() + File.separator + "VLIVE" + File.separator;
        //noinspection ResultOfMethodCallIgnored
        StorageUtils.isDirectoryExists(savePath, true);
    }

    private void sendBridgeData(String status, int progress) {
        Intent bridgeIntent = new Intent(SaverBroadcaster.BROADCAST_FILTER_SAVER);
        bridgeIntent.putExtra(BRIDGE_FILTER_DATA_STATUS, status);
        bridgeIntent.putExtra(BRIDGE_FILTER_DATA_PROGRESS, progress);
        sendBroadcast(bridgeIntent);
    }

    private void saveVideoData(VideoOnDemandRetriever.Data data, int selectedVideo, int selectedCaption, String videoPath, String captionPath) {
        VideoOnDemand.Video video = data.videos.get(selectedVideo);
        VideoOnDemand.Video.Builder builder = VideoOnDemand.Video.Builder.copy(video);
        builder.setSource(videoPath);

        if (selectedCaption >= 0 && captionPath != null) {
            builder.setCaption(captionPath);
        }

        if (!VideoOnDemand.getInstance(this).add(builder.build())) {
            VideoOnDemand.getInstance(this).update(builder.build());
        }
    }

    private static class Task extends AsyncTask<VideoOnDemandRetriever.Data, Integer, Boolean> {
        private WeakReference<DownloaderService> serviceContext;

        private long lastTimeMillis;
        private String videoPath, captionPath;

        private VideoOnDemandRetriever.Data mData;

        private Task(DownloaderService service) {
            serviceContext = new WeakReference<>(service);
        }

        private DownloaderService getContext() {
            return serviceContext.get();
        }

        private void clearContext() {
            serviceContext.get().mTask = null;
            serviceContext.clear();
            serviceContext = null;
        }

        private void setData(VideoOnDemandRetriever.Data data) {
            mData = data;
        }

        private VideoOnDemandRetriever.Data getData() {
            return mData;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                getContext().saveVideoData(getData(), getContext().selectedVideo, getContext().selectedCaption, videoPath, captionPath);

                getContext().sendBridgeData(getContext().getString(R.string.downloaded_content, "Video"), BRIDGE_STATUS_SUCCESS);

                getContext().mNotificationBuilder
                        .setContentText(getContext().getString(R.string.downloaded_content, "Video"))
                        .setProgress(0, 0, false)
                        .setOngoing(false);
                NotificationHelper.notify(getContext(), getContext().mNotificationId, getContext().mNotificationBuilder.build());
                getContext().stopSelf();
                clearContext();
            } else {
                onCancelled(result);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if ((System.currentTimeMillis() - lastTimeMillis) >= 1000) {
                lastTimeMillis = System.currentTimeMillis();
                getContext().mNotificationBuilder.setProgress(100, values[0], false);
                NotificationHelper.notify(getContext(), getContext().mNotificationId, getContext().mNotificationBuilder.build());
            }
        }

        @Override
        protected void onCancelled(Boolean result) {
            if (result) {
                onPostExecute(true);
            } else {
                getContext().sendBridgeData(getContext().getString(R.string.download_content_fail, "Video"), BRIDGE_STATUS_FAILED);

                getContext().mNotificationBuilder
                        .setContentText(getContext().getString(R.string.download_content_fail, "Video"))
                        .setProgress(0, 0, false)
                        .setOngoing(false);
                NotificationHelper.notify(getContext(), getContext().mNotificationId, getContext().mNotificationBuilder.build());
                getContext().stopSelf();
                clearContext();
            }
        }

        @Override
        protected Boolean doInBackground(VideoOnDemandRetriever.Data... data) {
            setData(data[0]);
            CrashCocoExceptionHandler.with("ds").debugLog(getContext().selectedVideo + "");
            if (getContext().selectedVideo >= 0) {
                FutureTarget<Bitmap> largeIcon = Glide.with(getContext())
                        .asBitmap()
                        .load(getData().videos.get(getContext().selectedVideo).getCover())
                        .submit();

                try {
                    getContext().mNotificationBuilder
                            .setContentText(getContext().getString(R.string.downloading_content, "video"))
                            .setLargeIcon(largeIcon.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                publishProgress(0);
                CrashCocoExceptionHandler.with("ds").debugLog(getData().videos.get(getContext().selectedVideo).getSource());
                File video = UtilNetwork.download(getData().videos.get(getContext().selectedVideo).getSource(), getContext().savePath, getData().videos.get(getContext().selectedVideo).getTitle() + ".mp4", new UtilNetwork.Listener() {

                    @Override
                    public void onProgress(int value) {
                        // TODO Auto-generated method stub
                        publishProgress(value);
                        getContext().sendBridgeData(getContext().getString(R.string.downloading_content, "video"), value);
                    }
                });

                if (video != null) {
                    videoPath = video.getAbsolutePath();

                    if (getContext().selectedCaption >= 0) {
                        getContext().mNotificationBuilder.setContentText(getContext().getString(R.string.downloading_content, "caption"));
                        publishProgress(0);
                        File caption = UtilNetwork.download(getData().captions.get(getContext().selectedCaption).getSource(), getContext().savePath, getData().videos.get(getContext().selectedVideo).getTitle() + "-" + getData().captions.get(getContext().selectedCaption).getLocale() + ".vtt", new UtilNetwork.Listener() {

                            @Override
                            public void onProgress(int value) {
                                // TODO Auto-generated method stub
                                publishProgress(value);
                                getContext().sendBridgeData(getContext().getString(R.string.downloading_content, "caption"), value);
                            }
                        });

                        if (caption != null) {
                            captionPath = caption.getAbsolutePath();

                            getContext().sendBridgeData(getContext().getString(R.string.downloaded_content, "Caption"), 100);
                            getContext().sendBridgeData(getContext().getString(R.string.downloaded_content, "Video"), 100);
                            return true;
                        } else {
                            getContext().sendBridgeData(getContext().getString(R.string.download_content_fail, "caption"), 0);
                            return false;
                        }
                    } else {
                        getContext().sendBridgeData(getContext().getString(R.string.downloaded_content, "Video"), 100);
                        return true;
                    }
                } else {
                    getContext().sendBridgeData(getContext().getString(R.string.download_content_fail, "video"), 0);
                    return false;
                }
            } else {
                getContext().sendBridgeData(getContext().getString(R.string.download_content_fail, "video"), 0);
                return false;
            }
        }
    }
}
