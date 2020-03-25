package com.doodlyz.vlove.models;

import android.view.View;

import com.doodlyz.vlove.presenters.MenuItemPresenter;

public class MenuItemModel extends ItemModelAbs implements MenuItemPresenter.OnActionListener {
    public static final int TYPE = 0;

    private String mTitle;
    private Class<? extends View> mActionType;
    private Object mInitValue;

    public MenuItemModel(String title, Class<? extends View> actionType) {
        mTitle = title;
        mActionType = actionType;
    }

    public MenuItemModel(String title, Class<? extends View> actionType, Object initValue) {
        this(title, actionType);
        mInitValue = initValue;
    }

    @Override
    public int getType() {
        return TYPE;
    }

    public String getTitle() {
        return mTitle;
    }

    public Class<? extends View> getActionType() {
        return mActionType;
    }

    public Object getInitValue() {
        return mInitValue;
    }

    public MenuItemPresenter.OnActionListener getOnActionListener() {
        return this;
    }

    @Override
    public void onAction(boolean state) {

    }
}
