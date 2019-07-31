package com.navers.vlove.presenters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.navers.vlove.R;
import com.navers.vlove.databases.Board;
import com.navers.vlove.models.BoardHeaderModel;

public class BoardHeaderPresenter extends Presenter<BoardHeaderModel, BoardHeaderPresenter.BoardGreetHolder> {

    public BoardHeaderPresenter(int viewType) {
        super(viewType);
    }

    public BoardHeaderPresenter(int viewType, Object additionalValue) {
        super(viewType, additionalValue);
    }

    @Override
    public BoardGreetHolder createViewHolder(@NonNull ViewGroup parent) {
        return new BoardHeaderPresenter.BoardGreetHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_board, parent, false));
    }

    @Override
    public void bindView(@NonNull BoardHeaderModel model, @NonNull BoardGreetHolder holder) {
        holder.setUserNickname(model.getUserNickname());
        holder.setOnSyncClickListener(model.getOnSyncClickListener());
    }

    public interface OnSyncClickListener {
        void onSyncClick(String link);
    }

    static class BoardGreetHolder extends RecyclerView.ViewHolder {
        private TextView mGreet;
        private EditText mPostLink;
        private Button mPostSync;

        private OnSyncClickListener mListener;

        public BoardGreetHolder(@NonNull View itemView) {
            super(itemView);
            initView();
        }

        public void setUserNickname(String userNickname) {
            mGreet.setText(userNickname);
        }

        public void setOnSyncClickListener(OnSyncClickListener listener) {
            mListener = listener;
        }

        private Context getContext() {
            return itemView.getContext();
        }

        private void initView() {
            initGreet();
            initPostLink();
            initPostSync();
        }

        private void initGreet() {
            mGreet = itemView.findViewById(R.id.greet);
        }

        private void initPostLink() {
            mPostLink = itemView.findViewById(R.id.postLink);
        }

        private void initPostSync() {
            mPostSync = itemView.findViewById(R.id.postSync);
            mPostSync.setText(getContext().getString(R.string.board_sync_button));
            mPostSync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String link;
                    Editable editable = mPostLink.getText();
                    if (editable != null && isValid(link = editable.toString())) {
                        if (!Board.getInstance(getContext()).isExceed()) {
                            mListener.onSyncClick(link.trim());
                        } else {
                            Toast.makeText(getContext(), getContext().getString(R.string.board_post_exceed, Board.LIMIT), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getContext(), getContext().getString(R.string.board_link_not_valid), Toast.LENGTH_LONG).show();
                    }
                    mPostLink.setText("");
                }
            });
        }

        private boolean isValid(String link) {
            return link != null && !link.trim().isEmpty() && link.trim().matches("http(s)?://channels.vlive.tv/([A-Z0-9]+)/fan/([0-9.]+)");
        }
    }
}
