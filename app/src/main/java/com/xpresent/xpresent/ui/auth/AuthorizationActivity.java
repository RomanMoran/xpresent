package com.xpresent.xpresent.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xpresent.xpresent.R;
import com.xpresent.xpresent.ui.catalog.ReviewsActivity;

public class AuthorizationActivity extends AppCompatActivity {
    private String redirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        TextView toolbarTitle = findViewById(R.id.Title);
        toolbarTitle.setText(R.string.title_authorize_please);

        Bundle extras = getIntent().getExtras();
        redirect = extras.getString("redirect");

        ImageView btnBack = findViewById(R.id.backBtn);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        CardView buttonPhoneAuth = findViewById(R.id.btnSignInByPhone);
        buttonPhoneAuth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickSingInByPhone();
            }
        });

        /*CardView buttonVKAuth = findViewById(R.id.btnSignInVK);
        buttonVKAuth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickSingInByVK();
            }
        });*/
    }

    public void onClickSingInByPhone(){
        Bundle extras = new Bundle();
        extras.putString("redirect", redirect);
        Intent intent = new Intent(this, AuthByPhoneActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    public void onClickSingInByVK(){
        Bundle extras = new Bundle();
        extras.putString("redirect", redirect);
        Intent intent = new Intent(this, AuthVKActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }
}