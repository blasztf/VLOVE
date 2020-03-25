package com.doodlyz.vlove.presenters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.TooltipCompat;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.doodlyz.vlove.models.ItemModel;
import com.doodlyz.vlove.models.ItemModelAbs;

import java.util.ArrayList;

public class PresenterAdapter extends RecyclerView.Adapter {
    @NonNull
    private final ArrayList<ItemModelAbs> mItems = new ArrayList<>();

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
        final ItemModelAbs item = getItem(position);
        final Presenter presenter = mPresenters.get(item.getType());
        if (presenter != null) {
            presenter.bindView(item, holder);
            shouldAddTooltip(item, holder.itemView);
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

    public void setItems(@NonNull final ArrayList<ItemModelAbs> items) {
        mItems.clear();
        mItems.addAll(items);
    }

    public void addItem(@NonNull final ItemModelAbs item) {
        mItems.add(item);
    }

    public void removeItem(@NonNull final ItemModelAbs item) {
        mItems.remove(item);
    }

    private ItemModelAbs getItem(final int position) {
        return mItems.get(position);
    }

    private void shouldAddTooltip(ItemModelAbs item, View view) {
        if (item.isTooltipEnabled()) {
            TooltipCompat.setTooltipText(view, item.getTooltip());
        }
    }
}
