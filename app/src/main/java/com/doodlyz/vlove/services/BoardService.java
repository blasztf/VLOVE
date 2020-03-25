package com.doodlyz.vlove.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.doodlyz.vlove.AppSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.doodlyz.vlove.Action;
import com.doodlyz.vlove.R;
import com.doodlyz.vlove.VolleyRequest;
import com.doodlyz.vlove.apis.VAPIS;
import com.doodlyz.vlove.databases.Board;
import com.doodlyz.vlove.logger.CrashCocoExceptionHandler;
import com.doodlyz.vlove.ui.helper.NotificationHelper;
import com.doodlyz.vlove.views.BoardScreenActivity;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoardService extends Service {
    private static final String TAG_VOLLEY_REQUEST = BoardService.class.getSimpleName() + ".TAG_VOLLEY_REQUEST";
    private static final String POST_NOT_EXIST = "This post does not exist.";

    private static final int REQUEST_MAX_RETRIES = 3;
    private static final int ERROR_POST_NOT_EXIST = 1000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new BoardBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread.setDefaultUncaughtExceptionHandler(CrashCocoExceptionHandler.asDefault("vl"));

        String action = intent.getAction();
        if (action != null && action.equals(Action.BOARD_SYNC)) {
            startSync();
        }
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VolleyRequest.with(this).cancelPendingRequest(TAG_VOLLEY_REQUEST);
    }

    private void startSync() {
        final int init = 0;
        final List<Board.Post> posts = Board.getInstance(this).retrieve(); // Get posts from board.

        // If there are any posts on board, begin synchronizing...
        if (!posts.isEmpty()) {
            syncPost(posts.get(init).getId(), posts.get(init).getChannelCode(), new OnSyncListener() {
                int next = init;
                Board.Post oldPost = posts.get(init);

                @Override
                public void onSync(Board.Post post) {
                    // Update this post.
                    checkComments(post);
                    Board.getInstance(BoardService.this).update(post);
                    notifyUserIfNeccessary(oldPost, post);
                    next();
                }

                @Override
                public void onError(String errorMsg) {
                    // Post already has been deleted, therefore delete this post on board and begin synchronizing next post.
                    if (errorMsg.equals(POST_NOT_EXIST)) {
                        Board.getInstance(BoardService.this).delete(oldPost);
                    }
                    next();
                }

                private void checkComments(Board.Post post) {
                    // someone delete their comment, mark comment as deleted.
                    if (post.getTotalComment() < oldPost.getTotalComment()) {
                        List<Board.Comment> comments = post.getComments();
                        for (Board.Comment comment : oldPost.getComments()) {
                            if (!comments.contains(comment)) {
                                comment.setContent(comment.getId());
                                comment.setId(Board.MARK_DELETED);

                                comments.add(comment);
                            }
                        }
                    }
                }

                private void next() {
                    next++;
                    // it is end of the post, therefore stop the board service.
                    if (next >= posts.size()) {
                        stopSelf();
                    } else { // begin synchronizing next post.
                        oldPost = posts.get(next);
                        syncPost(oldPost.getId(), oldPost.getChannelCode(),this);
                    }
                }
            });
        }
    }

    private void syncPost(final String postId, final String channelCode, final OnSyncListener listener) {
        VolleyRequest.StringRequest request = new VolleyRequest.StringRequest(
                VAPIS.getAPIPosts(this, postId),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JsonObject root = new JsonParser().parse(response).getAsJsonObject();
                            String content = root.get("body").getAsString();

                            if (root.has("photo") || root.has("video")) {
                                content = content.replaceAll("<v:attachment type=\\\"(photo|video)\\\" id=\\\"([0-9.]+)\\\" />", "");
                                content += "\n\n(" + getString(R.string.board_post_contains_media) + ")";
                            }

                            Board.Post postBuild = new Board.Post.Builder(postId)
                                    .setUser(root.get("author").getAsJsonObject().get("nickname").getAsString())
                                    .setUserImage(root.get("author").getAsJsonObject().get("profile_image").getAsString())
                                    .setContent(content)
                                    .setTotalComment(root.get("comment_count").getAsInt())
                                    .setChannelCode(channelCode)
                                    .setCreatedAt(root.get("created_at").getAsLong())
                                    .build();

                            syncComment(postBuild, listener);
                        } catch (Exception e) {
                            listener.onError(getString(R.string.board_sync_fail));
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                            String response;
                            try {
                                response = new String(error.networkResponse.data, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                response = new String(error.networkResponse.data);
                            }
                            JsonObject root = new JsonParser().parse(response).getAsJsonObject();
                            if (root.has("error")) {
                                if (root.get("error").getAsJsonObject().get("error_code").getAsInt() == ERROR_POST_NOT_EXIST) {
                                    listener.onError(POST_NOT_EXIST);
                                    return;
                                }
                            }
                        }
                        listener.onError(getString(R.string.board_sync_fail));
                        error.printStackTrace();
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, REQUEST_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyRequest.with(this).addToQueue(request, TAG_VOLLEY_REQUEST);
    }

    private void syncComment(final Board.Post post, final OnSyncListener listener) {
        VolleyRequest.StringRequest request = new VolleyRequest.StringRequest(
                VAPIS.getAPIComments(this, post.getId()),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JsonObject root = new JsonParser().parse(response).getAsJsonObject();
                            JsonArray comments = root.getAsJsonArray("data");

                            if (comments.size() > 0) {
                                ArrayList<Board.Comment> postComments = new ArrayList<>();
                                Board.Comment.Builder comment;
                                JsonArray stickerContainer;
                                for (int i = 0, l = comments.size(); i < l; i++) {
                                    comment = new Board.Comment.Builder(comments.get(i).getAsJsonObject().get("comment_id").getAsString())
                                            .setUser(comments.get(i).getAsJsonObject().get("author").getAsJsonObject().get("nickname").getAsString())
                                            .setUserImage(comments.get(i).getAsJsonObject().getAsJsonObject("author").has("profile_image") ? comments.get(i).getAsJsonObject().getAsJsonObject("author").get("profile_image").getAsString() : "")
                                            .setContent(comments.get(i).getAsJsonObject().get("body").getAsString())
                                            .setTotalComment(comments.get(i).getAsJsonObject().get("comment_count").getAsInt())
                                            .setPostId(post.getId())
                                            .setCreatedAt(comments.get(i).getAsJsonObject().get("created_at").getAsLong());

                                    if ((stickerContainer = comments.get(i).getAsJsonObject().get("sticker").getAsJsonArray()).size() != 0) {
                                        comment.setStickerPack(stickerContainer.get(0).getAsJsonObject().get("sticker_info").getAsJsonObject().get("pack_code").getAsString())
                                                .setStickerId(stickerContainer.get(0).getAsJsonObject().get("v_sticker_id").getAsString());
                                    }

                                    postComments.add(comment.build());
                                }
                                post.setComments(postComments);
                            }
                            listener.onSync(post);
                        } catch (Exception e) {
                            listener.onError(getString(R.string.board_sync_fail));
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(getString(R.string.board_sync_fail));
                        error.printStackTrace();
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, REQUEST_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyRequest.with(this).addToQueue(request, TAG_VOLLEY_REQUEST);
    }

    /**
     * Find and return "Post ID" + "Channel Code" based on the given <i><b>link</b></i>.
     * @param link to process.
     * @return array of string consist of:<ul><li>[0] => Post ID</li><li>[1] => Channel Code</li></ul>
     * *note: returned output always not null, <i>but</i> items on the output can be null.
     */
    private String[] getPIDandCC(String link) {
        String postId, channelCode;
        postId = channelCode = null;
        Pattern regex = Pattern.compile("https?://channels\\.vlive\\.tv/([A-Z0-9]+)/fan/([0-9.]+)");
        Matcher matcher = regex.matcher(link);

        if (matcher.find()) {
            postId = matcher.group(2);
            channelCode = matcher.group(1);
        }

        return new String[] { postId, channelCode };
    }

    private void notifyUserIfNeccessary(Board.Post oldPost, Board.Post newPost) {
        Board.Comment newLatestComment;

        if (newPost.getTotalComment() > oldPost.getTotalComment()) {
            newLatestComment = newPost.getTotalComment() > 0 ? newPost.getComments().get(newPost.getTotalComment() - 1) : new Board.Comment.Builder("monkeyid").build();
            notifyUser(String.format(getString(R.string.board_notification_comment), newLatestComment.getUser()), newLatestComment.getContent(), newLatestComment.getUserImage());
        }
    }

    private void notifyUser(final String title, final String message, final String userProfile) {
        Glide.with(this).asBitmap().load(userProfile).into(new SimpleTarget<Bitmap>() {
            NotificationCompat.Builder builder = NotificationHelper.getBuilder(BoardService.this, title, message).setContentIntent(getContentIntent());

            /**
             * Sets the given {@link Drawable} on the view using {@link
             * ImageView#setImageDrawable(Drawable)}.
             *
             * @param errorDrawable {@inheritDoc}
             */
            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                NotificationHelper.notify(BoardService.this, userProfile.hashCode(), builder.build());
            }

            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                builder.setLargeIcon(resource);
                NotificationHelper.notify(BoardService.this, userProfile.hashCode(), builder.build());
            }

            private PendingIntent getContentIntent() {
                Intent intent = new Intent(BoardService.this, BoardScreenActivity.class);
                return PendingIntent.getActivity(BoardService.this, 0, intent, 0);
            }
        });
    }

    public interface OnSyncListener {
        void onSync(Board.Post post);

        void onError(String errorMsg);
    }

    /**
     * Bridge for binding board service on any activity.
     */
    public class BoardBinder extends Binder {
        private boolean isOnSynchronizing = false;

        /**
         * Begin synchronizing requested post.
         *
         * @param link     of post that will be synchronize.
         * @param listener for handling the result, where:<br/><ul><li>{@linkplain OnSyncListener#onSync(Board.Post)} will be trigger on success.</li><li>{@linkplain OnSyncListener#onError(String)} if any error occurred.</li></ul>
         */
        public void startSync(String link, OnSyncListener listener) {
            OnSyncListener newListener = bindListener(listener);
            if (!isOnSynchronizing) {
                String[] pidncc = getPIDandCC(link);
                if (!Board.getInstance(getService()).contain(pidncc[0])) {
                    setOnSynchronizing(true);
                    getService().syncPost(pidncc[0], pidncc[1], newListener);
                } else {
                    newListener.onError(getString(R.string.board_post_already_sync));
                }
            } else {
                newListener.onError(getString(R.string.board_sync_wait));
            }
        }

        private void setOnSynchronizing(boolean isOnSynchronizing) {
            this.isOnSynchronizing = isOnSynchronizing;
        }

        private OnSyncListener bindListener(final OnSyncListener listener) {
            return new OnSyncListener() {
                @Override
                public void onSync(Board.Post post) {
                    setOnSynchronizing(false);
                    listener.onSync(post);
                }

                @Override
                public void onError(String errorMsg) {
                    setOnSynchronizing(false);
                    listener.onError(errorMsg);
                }
            };
        }

        private BoardService getService() {
            return BoardService.this;
        }
    }
}
