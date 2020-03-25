package com.doodlyz.vlove.models;

public abstract class ItemModelAbs implements ItemModel {
    private String mTooltip;

    public ItemModelAbs tooltip(String tips) {
        mTooltip = tips;
        return this;
    }

    public boolean isTooltipEnabled() {
        return mTooltip != null && !mTooltip.isEmpty();
    }

    public String getTooltip() {
        return mTooltip;
    }
}
