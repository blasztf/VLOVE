package com.navers.vlove.presenters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.navers.vlove.models.ItemModel;

import java.util.ArrayList;

public class PresenterAdapter extends RecyclerView.Adapter {
    @NonNull
    private final ArrayList<ItemModel> mItems = new ArrayList<>();

    @NonNull
    private final SparseArray<Presenter> mPresenters = new SparseArray<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int itemViewType) {
        final Presenter presenter = mPresenters.get(itemViewType);
        if (presenter != null) {
            return presenter.createViewHolder(parent);
        }
        else {
            throw new RuntimeException("Not supported Item View Type: " + itemViewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ItemModel item = getItem(position);
        final Presenter presenter = mPresenters.get(item.getType());
        if (presenter != null) {
            presenter.bindView(item, holder);
        }
        else {
            throw new RuntimeException("Not supported View Holder: " + holder);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(final int position) {
        final ItemModel item = getItem(position);
        return item.getType();
    }

    public void addPresenter(@NonNull final Presenter presenter) {
        final int type = presenter.getType();
        if (mPresenters.get(type) == null) {
            mPresenters.put(type, presenter);
        }
        else {
            throw new RuntimeException("Presenter already exist with this type: " + type);
        }
    }

    public void setItems(@NonNull final ArrayList<ItemModel> items) {
        mItems.clear();
        mItems.addAll(items);
    }

    public void addItem(@NonNull final ItemModel item) {
        mItems.add(item);
    }

    public void removeItem(@NonNull final ItemModel item) {
        mItems.remove(item);
    }

    private ItemModel getItem(final int position) {
        return mItems.get(position);
    }
}
