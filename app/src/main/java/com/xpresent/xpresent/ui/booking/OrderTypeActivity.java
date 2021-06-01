package com.xpresent.xpresent.ui.booking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.xpresent.xpresent.MainActivity;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.ui.auth.AuthorizationActivity;

public class OrderTypeActivity extends AppCompatActivity {
    private String sessionKey;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_type);
        ctx = this;

        // connect to app storage
        SharedPreferences settings = this.getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        // get sessionKey and order params from storage
        sessionKey = settings.getString("sessionKey", "");

        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        MaterialButton myCertificates = findViewById(R.id.btnMyCertificates);
        myCertificates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check authorization
                if (!sessionKey.isEmpty()) {
                    Intent intent = new Intent(ctx, BuyCertActivity.class);
                    startActivity(intent);
                } else {
                    Bundle extras = new Bundle();
                    extras.putString("redirect", "buycert");
                    Intent intent = new Intent(ctx, AuthorizationActivity.class);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
            }
        });

        MaterialButton bookBtn = findViewById(R.id.bookBtn);
        bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                startActivity(intent);
            }
        });
    }
}
