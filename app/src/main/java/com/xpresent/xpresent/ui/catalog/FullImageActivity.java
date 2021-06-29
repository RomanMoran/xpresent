/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 07.07.20 19:55
 */
package com.xpresent.xpresent.ui.catalog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.config.config;
import com.xpresent.xpresent.ui.LoadingDialog;

import java.lang.ref.WeakReference;

public class FullImageActivity extends AppCompatActivity {
    private LoadingDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        // show progress bar
        progressDialog = new LoadingDialog(this, true);
        progressDialog.setCancelable(true);
        progressDialog.show();
        // load full image
        ImageView fullImage = findViewById(R.id.imgDisplay);

        Glide.with(this)
                .load(imgUrl())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressDialog.cancel();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressDialog.cancel();
                        return false;
                    }
                })
                .into(fullImage);
        // close button
        findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private String imgUrl() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) return "";
        String url = extras.getString("imgFullUrl");
        return url.startsWith(config.HOST_NAME) ? url : config.HOST_NAME + url;
    }
}