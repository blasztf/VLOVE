package com.doodlyz.vlove.deprecated.features;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.doodlyz.vlove.R;
import com.doodlyz.vlove.databases.Board;

public class TestActivity extends AppCompatActivity {
    private EditText editText;
    private Button button;
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText(getString(R.string.loading));
                String query = editText.getText().toString();
                String result = Board.getInstance(TestActivity.this).testQuery(query);
                textView.setText(result);
            }
        });
    }
}
