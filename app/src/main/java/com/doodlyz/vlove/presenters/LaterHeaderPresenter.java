package com.doodlyz.vlove.presenters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doodlyz.vlove.R;
import com.doodlyz.vlove.models.ItemModel;

public class LaterHeaderPresenter extends Presenter {
    public LaterHeaderPresenter(int viewType) {
        super(viewType);
    }

    public LaterHeaderPresenter(int viewType, Object additionalValue) {
        super(viewType, additionalValue);
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(@NonNull ViewGroup parent) {
        return new LaterHeaderPresenter.LaterHeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_later, parent, false));
    }

    @Override
    public void bindView(@NonNull ItemModel model, @NonNull RecyclerView.ViewHolder holder) {

    }

    static class LaterHeaderHolder extends RecyclerView.ViewHolder {
        public LaterHeaderHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
