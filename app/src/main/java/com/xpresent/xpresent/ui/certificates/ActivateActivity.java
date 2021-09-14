package com.xpresent.xpresent.ui.certificates;

import androidx.appcompat.app.AppCompatActivity;
import com.xpresent.xpresent.R;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);

        TextView toolbarTitle = findViewById(R.id.Title);
        toolbarTitle.setText("Активировать сертификат");

        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}