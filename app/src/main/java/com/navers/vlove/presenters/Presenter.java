package com.navers.vlove.presenters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.navers.vlove.models.ItemModel;

public abstract class Presenter<M extends ItemModel, VH extends RecyclerView.ViewHolder> {
    private final int mViewType;
    private final Object mAdditionalValue;

    public Presenter(final int viewType) {
        this(viewType, null);
    }

    public Presenter(final int viewType, final Object additionalValue) {
        mViewType = viewType;
        mAdditionalValue = additionalValue;
    }

    public int getType() {
        return mViewType;
    }

    public final <T extends Object> T getAdditionalValue() {
        //noinspection unchecked
        return (T) mAdditionalValue;
    }

    public abstract VH createViewHolder(@NonNull ViewGroup parent);
    public abstract void bindView(@NonNull M model, @NonNull VH holder);

    protected void unbindViewHolder(@NonNull VH holder) {}
    protected void attachViewHolder(@NonNull VH holder) {}
    protected void detachViewHolder(@NonNull VH holder) {}
}
