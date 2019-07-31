package com.navers.vlove.presenters;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.navers.vlove.R;
import com.navers.vlove.models.MenuItemModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class MenuItemPresenter extends Presenter<MenuItemModel, MenuItemPresenter.MenuItemHolder> {
    public MenuItemPresenter(int viewType) {
        super(viewType);
    }

    @Override
    public MenuItemHolder createViewHolder(@NonNull ViewGroup parent) {
        return new MenuItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false));
    }

    @Override
    public void bindView(@NonNull MenuItemModel model, @NonNull MenuItemHolder holder) {
        holder.setTitle(model.getTitle());
        holder.setOnActionListener(model.getOnActionListener());
        holder.embedAction(model.getActionType(), model.getInitValue());
    }

    public interface OnActionListener {
        void onAction(boolean state);
    }

    static class MenuItemHolder extends RecyclerView.ViewHolder {
        private TextView mTitle;
        private View mAction;
        private OnActionListener mActionListener;

        public MenuItemHolder(@NonNull View itemView) {
            super(itemView);
            initView();
        }

        public void setTitle(String title) {
            if (!mTitle.getText().toString().equals(title)) {
                mTitle.setText(title);
            }
        }

        public void setOnActionListener(OnActionListener listener) {
            mActionListener = listener;
        }

        private void initView() {
            initTitle();
        }

        private void initTitle() {
            mTitle = itemView.findViewById(R.id.title);
        }

        public void embedAction(Class<? extends View> actionType, Object initValue) {
            if (actionType.equals(Button.class)) {
                mAction = asButton(initValue);
            } else if (actionType.equals(Switch.class)) {
                mAction = asSwitch(initValue);
            } else if (actionType.equals(ImageView.class)) {
                mAction = asImageView(initValue);
            }
            else if (actionType.equals(View.class)) {
                // Behave like ImageView but without image action.
                mAction = asNormal();
                return;
            } else {
                throw new RuntimeException("Action type is not supported!");
            }
            replaceView((ViewGroup) itemView, actionType.cast(mAction));
        }

        private void replaceView(ViewGroup parent, View newChild) {
            View currentChild;
            for (int i = 0, l = parent.getChildCount(); i < l; i++) {
                if ((currentChild = parent.getChildAt(i)).getId() == R.id.action) {
                    newChild.setLayoutParams(currentChild.getLayoutParams());
                    newChild.setId(currentChild.getId());
                    parent.removeViewAt(i);
                    parent.addView(newChild, i);
                    break;
                }
            }
        }

        private Button asButton(Object initValue) {
            Button viewCast = new Button(itemView.getContext());
            viewCast.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mActionListener != null) mActionListener.onAction(true);
                }
            });
            if (initValue != null && initValue instanceof String) {
                viewCast.setText((String) initValue);
            }
            viewCast.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.color_btn_selector));
            viewCast.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_light_black));
            return viewCast;
        }

        private Switch asSwitch(Object initValue) {
            Switch viewCast = new Switch(itemView.getContext());
            viewCast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean state) {
                    if (mActionListener != null) mActionListener.onAction(state);
                }
            });
            if (initValue != null && initValue instanceof Boolean) {
                viewCast.setChecked((Boolean) initValue);
            }
            return viewCast;
        }

        private ImageView asImageView(Object initValue) {
            ImageView viewCast = new ImageView(itemView.getContext());
            viewCast.setScaleType(ImageView.ScaleType.CENTER);
            viewCast.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mActionListener != null) mActionListener.onAction(true);
                }
            });
            if (initValue != null && (initValue instanceof String || initValue instanceof Integer)) {
                Glide.with(itemView)
                        .load(initValue instanceof String ?
                                (String) initValue :
                                (Integer) initValue
                        )
                        .apply(new RequestOptions()
                                .fitCenter()
                                .dontTransform()
                        )
                        .into(viewCast);
            }
            else {
                Glide.with(itemView)
                        .load(R.drawable.chev_r)
                        .apply(new RequestOptions()
                                .fitCenter()
                                .centerInside()
                        )
                        .into(viewCast);
//                viewCast.setImageResource(R.drawable.chev_r);
            }
            return viewCast;
        }

        private View asNormal() {
            View viewCast = itemView.findViewById(R.id.action);
            viewCast.setVisibility(View.GONE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mActionListener != null) mActionListener.onAction(true);
                }
            });
            return viewCast;
        }
    }
}
