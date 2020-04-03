package com.doodlyz.vlove.views;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.doodlyz.vlove.VloveSettings;
import com.doodlyz.vlove.R;
import com.doodlyz.vlove.databases.Board;
import com.doodlyz.vlove.logger.CrashCocoExceptionHandler;
import com.doodlyz.vlove.models.BoardHeaderModel;
import com.doodlyz.vlove.models.BoardItemModel;
import com.doodlyz.vlove.models.ItemModelAbs;
import com.doodlyz.vlove.presenters.BoardHeaderPresenter;
import com.doodlyz.vlove.presenters.BoardItemPresenter;
import com.doodlyz.vlove.presenters.PresenterAdapter;
import com.doodlyz.vlove.services.BoardService;
import com.doodlyz.vlove.ui.dialogs.Popup;

import java.util.ArrayList;
import java.util.List;

public class BoardScreenActivity extends AppCompatActivity implements Screen {
    private View contentView;
    private PresenterAdapter mAdapter;
    private ServiceConnection mServiceConnection;
    private boolean mStillSync;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(CrashCocoExceptionHandler.asDefault("vl"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_board);
        setSupportToolbar();
        contentView = findViewById(android.R.id.content);

        mAdapter = new PresenterAdapter();
        mAdapter.addPresenter(new BoardHeaderPresenter(BoardHeaderModel.TYPE));
        mAdapter.addPresenter(new BoardItemPresenter(BoardItemModel.TYPE));

        RecyclerView content = findViewById(R.id.recyclerView);
        content.setLayoutManager(new LinearLayoutManager(this));
        content.setAdapter(mAdapter);

        mAdapter.setItems(getItems());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStillSync) {
            mStillSync = false;
            unbindService(mServiceConnection);
            stopService(new Intent(BoardScreenActivity.this, BoardService.class));
        }
    }

    @Override
    public ArrayList<ItemModelAbs> getItems() {
        ArrayList<ItemModelAbs> listItems = new ArrayList<>();

        listItems.add(getBoardHeader());
        listItems.addAll(getBoardItems());

        return listItems;
    }

    private void setSupportToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolbar.setTitle(R.string.pref_group_vboard);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    private BoardHeaderModel getBoardHeader() {
        return new BoardHeaderModel(getString(R.string.board_greet, VloveSettings.getInstance(this).getBoardUsername()), new BoardHeaderPresenter.OnSyncClickListener() {
            @Override
            public void onSyncClick(String link) {
                if (!isStillSync()) {
                    startSync(link);
                } else {
                    Snackbar.make(contentView, R.string.board_sync_wait, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private ArrayList<BoardItemModel> getBoardItems() {
        ArrayList<BoardItemModel> listItems = new ArrayList<>();
        Board.getInstance(this).close();

        if (!Board.getInstance(this).isEmpty()) {
            List<Board.Post> posts = Board.getInstance(this).retrieve();
            for (final Board.Post post : posts) {
                listItems.add(buildBoardItemModel(post));
            }
        }

        return listItems;
    }

    private BoardItemModel buildBoardItemModel(Board.Post post) {
        return new BoardItemModel(post, new BoardItemPresenter.OnRemoveClickListener() {
            @Override
            public void onRemoveClick(BoardItemModel model) {
                if (Board.getInstance(BoardScreenActivity.this).delete(model.getPostObject())) {
                    mAdapter.removeItem(model);
                    mAdapter.notifyDataSetChanged();
                } else {
                    Popup.with(BoardScreenActivity.this, Popup.ID_INFO).make(R.string.board_post_delete_fail).show();
                }
            }
        }, new BoardItemPresenter.OnContentClickListener() {

            @Override
            public void onContentClick(BoardItemModel model) {
                Board.Post post = model.getPostObject();
                if (post.getChannelCode() != null) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(String.format("https://channels.vlive.tv/%s/fan/%s", post.getChannelCode(), post.getId())));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Snackbar.make(contentView, R.string.board_open_post_fail_1, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                } else {
                    Snackbar.make(contentView, R.string.board_open_post_fail_2, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startSync(final String link) {
        mStillSync = true;
        final Intent service = new Intent(BoardScreenActivity.this, BoardService.class);
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                final BoardService.BoardBinder binder = (BoardService.BoardBinder) iBinder;
                binder.startSync(link, new BoardService.OnSyncListener() {
                    @Override
                    public void onSync(Board.Post post) {
                        if (Board.getInstance(BoardScreenActivity.this).add(post)) {
                            if (mAdapter != null) {
                                mAdapter.addItem(buildBoardItemModel(post));
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                        onFinish();
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Snackbar.make(contentView, errorMsg, Snackbar.LENGTH_LONG).show();
                        onFinish();
                    }

                    private void onFinish() {
                        mStillSync = false;
                        unbindService(mServiceConnection);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                if (!mStillSync) {
                    unbindService(mServiceConnection);
                    mStillSync = false;
                    mServiceConnection = null;
                }
            }
        };
        startService(service);
        bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private boolean isStillSync() {
        return mStillSync;
    }
}
