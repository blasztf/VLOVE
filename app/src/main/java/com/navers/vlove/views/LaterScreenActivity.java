package com.navers.vlove.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.navers.vlove.R;
import com.navers.vlove.databases.VideoOnDemand;
import com.navers.vlove.models.ItemModelAbs;
import com.navers.vlove.models.LaterHeaderModel;
import com.navers.vlove.models.LaterItemModel;
import com.navers.vlove.presenters.LaterHeaderPresenter;
import com.navers.vlove.presenters.LaterItemPresenter;
import com.navers.vlove.presenters.PresenterAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LaterScreenActivity extends AppCompatActivity implements Screen {
    private PresenterAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_later);
        setSupportToolbar();

        mAdapter = new PresenterAdapter();
        mAdapter.addPresenter(new LaterHeaderPresenter(LaterHeaderModel.TYPE));
        mAdapter.addPresenter(new LaterItemPresenter(LaterItemModel.TYPE));

        RecyclerView content = findViewById(R.id.recyclerView);
        content.setLayoutManager(new LinearLayoutManager(this));
        content.setAdapter(mAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.setItems(getItems());
    }

    @Override
    public ArrayList<ItemModelAbs> getItems() {
        ArrayList<ItemModelAbs> listItems = new ArrayList<>();

        listItems.add(getLaterHeader());
        listItems.addAll(getLaterItems());

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
        toolbar.setTitle(R.string.pref_group_vlater);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    private LaterHeaderModel getLaterHeader() {
        return new LaterHeaderModel();
    }

    private ArrayList<LaterItemModel> getLaterItems() {
        ArrayList<LaterItemModel> listItems = new ArrayList<>();
        List<VideoOnDemand.Video> videos = VideoOnDemand.getInstance(this).get();

        for (final VideoOnDemand.Video video : videos) {
            if (!isVideoExists(video)) {
                VideoOnDemand.getInstance(this).delete(video);
            } else {
                listItems.add(new LaterItemModel(video, new LaterItemPresenter.OnContentClickListener() {
                    @Override
                    public void onContentClick(VideoOnDemand.Video video) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(video.getSource()), "video/*");
                        startActivity(intent);
                    }
                }));
            }
        }

        return listItems;
    }

    private boolean isVideoExists(VideoOnDemand.Video video) {
        File videoFile = new File(video.getSource());
        return videoFile.exists() && videoFile.isFile();
    }
}
