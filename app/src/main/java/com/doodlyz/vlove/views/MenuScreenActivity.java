package com.doodlyz.vlove.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.doodlyz.vlove.ui.dialogs.Login;
import com.doodlyz.vlove.ui.dialogs.Popup;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.EmptyPermissionRequestErrorListener;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.doodlyz.vlove.VloveSettings;
import com.doodlyz.vlove.R;
import com.doodlyz.vlove.SettingsActivity;
import com.doodlyz.vlove.apis.VAPIS;
import com.doodlyz.vlove.models.ItemModelAbs;
import com.doodlyz.vlove.models.MenuItemModel;
import com.doodlyz.vlove.presenters.MenuItemPresenter;
import com.doodlyz.vlove.presenters.PresenterAdapter;
import com.doodlyz.vlove.services.SaverService;

import java.util.ArrayList;

public class MenuScreenActivity extends ScreenAbs {
    private PresenterAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_menu);
        setSupportToolbar();

        stopService(new Intent(this, SaverService.class));

        if (!VAPIS.isExpired(this)) {
            if (isVLiveInstalled()) {
                mAdapter = new PresenterAdapter();
                mAdapter.addPresenter(new MenuItemPresenter(MenuItemModel.TYPE));

                RecyclerView content = findViewById(R.id.recyclerView);
                content.setLayoutManager(new LinearLayoutManager(this));
                content.setAdapter(mAdapter);

                if (!VloveSettings.getInstance(this).isPopupEnabled()) {
                    openPopup();
                } else {
                    mAdapter.setItems(getItems());
                }
            } else {
                Popup.with(this, Popup.ID_INSTALL).make(R.string.vlive_not_installed).show();
            }
        } else {
            showExpiredAlert();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isVLiveInstalled()) {
            if (VloveSettings.getInstance(this).isPopupEnabled()) {
                if (mAdapter != null) mAdapter.setItems(getItems());
            }
        }
        else {
            Popup.with(this, Popup.ID_INSTALL).make(R.string.vlive_not_installed).show();
        }
    }

    @Override
    public ArrayList<ItemModelAbs> getItems() {
        return getListMenu();
    }

    private void setSupportToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    private ArrayList<ItemModelAbs> getListMenu() {
        ArrayList<ItemModelAbs> listMenu = new ArrayList<>();

        listMenu.add(new MenuItemModel(getString(R.string.pref_group_vpopup), View.class) {
            @Override
            public void onAction(boolean state) {
                super.onAction(state);
                openPopup();
            }
        }.tooltip(getString(R.string.tooltip_v_popup)));
        listMenu.add(new MenuItemModel(getString(R.string.pref_group_vboard), ImageView.class) {
            @Override
            public void onAction(boolean state) {
                super.onAction(state);
                openBoard();
            }
        }.tooltip(getString(R.string.tooltip_v_board)));
        listMenu.add(new MenuItemModel(getString(R.string.pref_group_vlater), ImageView.class) {
            @Override
            public void onAction(boolean state) {
                super.onAction(state);
                openLater();
            }
        }.tooltip(getString(R.string.tooltip_v_later)));
        listMenu.add(new MenuItemModel(getString(R.string.pref_group_vsaver), Button.class, getString(R.string.btntxt_start)) {
            @Override
            public void onAction(boolean state) {
                super.onAction(state);
                openSaver();
            }
        }.tooltip(getString(R.string.tooltip_v_saver)));
        listMenu.add(new MenuItemModel(getString(R.string.pref_group_vtrans), View.class) {
            @Override
            public void onAction(boolean state) {
                super.onAction(state);
                openTrans();
            }
        }.tooltip(getString(R.string.tooltip_v_trans)));
        listMenu.add(new MenuItemModel(getString(R.string.action_settings), ImageView.class) {
            @Override
            public void onAction(boolean state) {
                super.onAction(state);
                openSettings();
            }
        }.tooltip(getString(R.string.tooltip_settings)));
        listMenu.add(new MenuItemModel(getString(R.string.action_login), View.class) {
            @Override
            public void onAction(boolean state) {
                super.onAction(state);
                Login.with(MenuScreenActivity.this).show();
            }
        });

        return listMenu;
    }

    private boolean isVLiveInstalled() {
        return VloveSettings.getInstance(this).isVLiveInstalled();
    }

    private void openVApp() {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.naver.vapp");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Popup.with(this, Popup.ID_INFO).make(R.string.vlive_not_opened).show();
        }
    }

    private void openPopup() {
        Toast.makeText(MenuScreenActivity.this, getString(R.string.pref_choose, getString(R.string.pref_title_enable_popup)), Toast.LENGTH_LONG).show();
        openSettings();
    }

    private void openBoard() {
        Intent intent = new Intent(MenuScreenActivity.this, BoardScreenActivity.class);
        startActivity(intent);
    }

    private void openLater() {
        Intent intent = new Intent(MenuScreenActivity.this, LaterScreenActivity.class);
        startActivity(intent);
    }

    @SuppressLint("NewApi")
    private void openSaver() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new BasePermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        super.onPermissionGranted(response);
                        Intent intent = new Intent(MenuScreenActivity.this, SaverService.class);
                        if (Settings.canDrawOverlays(MenuScreenActivity.this)) {
                            intent.putExtra(SaverService.INTENT_EXTRA_BUBBLE, true);
                        }
                        startService(intent);
                        openVApp();
                        finish();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        super.onPermissionDenied(response);
                        DialogOnDeniedPermissionListener.Builder
                                .withContext(MenuScreenActivity.this)
                                .withButtonText(R.string.perm_write_ext_denied_button)
                                .withIcon(R.mipmap.ic_launcher_round)
                                .withMessage(R.string.perm_write_ext_denied_message)
                                .withTitle(R.string.perm_write_ext_denied_title)
                                .build()
                                .onPermissionDenied(response);
                    }
                })
                .withErrorListener(new EmptyPermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        super.onError(error);
                    }
                })
                .check();
    }

    private void openTrans() {
        openSettings();
    }

    private void openSettings() {
        Intent intent = new Intent(MenuScreenActivity.this, SettingsActivity.class);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_INIT_FRAGMENT, true);
        startActivity(intent);
    }

    private void showNewVersionAlert() {
        Toast.makeText(this, "There is newer version of this app!", Toast.LENGTH_LONG).show();
        Toast.makeText(this, "Check at : https://github.com/doodlyz/VLOVE", Toast.LENGTH_LONG).show();

        finish();
    }

    private void showExpiredAlert() {
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);

        Toast.makeText(this, "App is already expired...", Toast.LENGTH_LONG).show();
        if (!sp.getBoolean("msgExpired1", false)) {
            sp.edit().putBoolean("msgExpired1", true).apply();
            Toast.makeText(this, "By the way, thank you for installing & using this app...", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "So many memories we created together in a week...", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "I feel like we can spend a little more time together... but...", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "I just can't...", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "So this is our goodbye...", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Hopefully we will meet again... Goodbye...\n(Run towards the sun).", Toast.LENGTH_LONG).show();
        }

        finish();
    }

//    private void test(ArrayList<ItemModel> listMenu) {
//        listMenu.add(new MenuItemModel("Test", ImageView.class) {
//            @Override
//            public void onAction(boolean state) {
//                super.onAction(state);
//                Intent intent = new Intent(MenuScreenActivity.this, TestActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        listMenu.add(new MenuItemModel("Test2", ImageView.class) {
//            @Override
//            public void onAction(boolean state) {
//                super.onAction(state);
//                Intent intent = new Intent(MenuScreenActivity.this, TestActivity2.class);
//                startActivity(intent);
//            }
//        });
//    }
}
