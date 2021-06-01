/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 26.05.20
 */

package com.xpresent.xpresent.ui.catalog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.adapter.OfferAdapter;
import com.xpresent.xpresent.adapter.ReviewAdapter;
import com.xpresent.xpresent.adapter.SelectedItem;
import com.xpresent.xpresent.config.config;
import com.xpresent.xpresent.model.Item;
import com.xpresent.xpresent.requests.ServerConnector;
import com.xpresent.xpresent.ui.booking.CalendarActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ReviewsActivity extends AppCompatActivity {
    private Context Activity = this;
    private int impressionId;
    private ReviewAdapter reviewAdapter;
    private List<Item> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        Bundle extras = getIntent().getExtras();
        impressionId = extras.getInt("impressionId");
        String impressionName = extras.getString("impressionName");

        TextView titleTV = findViewById(R.id.Title);
        //impressionName = (impressionName.length()>23)? impressionName.substring(0,23)+"..." : impressionName;
        String title = getResources().getString(R.string.reviews)+": "+ impressionName;
        titleTV.setText(title);
        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onBackPressed();
            }
        });

        // Reviews
        RecyclerView reviewsRecycler = findViewById(R.id.impression_reviews_list);
        reviewsRecycler.setNestedScrollingEnabled(false);
        reviewsRecycler.setLayoutManager(new LinearLayoutManager(Activity));
        reviewAdapter = new ReviewAdapter(Activity, reviewList, null, true);
        reviewsRecycler.setAdapter(reviewAdapter);

        getAllReviews();
    }

    public void getAllReviews(){
        // get city from storage
        SharedPreferences settings = Activity.getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        int cityId = settings.getInt("cityId", 1);

        HashMap<String,String> mapPost = new HashMap<>();
        mapPost.put("action", "impression");
        mapPost.put("id", Integer.toString(impressionId));
        mapPost.put("city", Integer.toString(cityId));

        ServerConnector Connector = new ServerConnector(Activity, new ServerConnector.AsyncResponse() {
            @Override
            public void processFinish(boolean success, String output) {
                if(success) {
                    showResult(output);
                }
                else Toast.makeText(Activity, getResources().getString(R.string.no_server_connection), Toast.LENGTH_LONG).show();
            }
        }, true);
        Connector.execute(mapPost);
    }

   private void showResult(String result){
        try {
            JSONObject jsonResult = new JSONObject(result);
            boolean status = jsonResult.getBoolean("status");
            if (status) {
                JSONObject impression = jsonResult.getJSONObject("impression");

                // Reviews
                JSONArray reviewJson = impression.getJSONArray("review");
                int reviewCount = reviewJson.length();
                if(reviewCount > 0) {
                    int num=0;
                    for (int i = 0; i < reviewCount; i++) {
                        JSONObject review = reviewJson.getJSONObject(i);
                        Map<String,String> paramsReview = new HashMap<>();
                        String advantages = review.getString("advantages");
                        //if(!advantages.equals("")) {
                            paramsReview.put("id", review.getString("id"));
                            paramsReview.put("advantages", advantages);
                            paramsReview.put("disadvantages", review.getString("disadvantages"));
                            paramsReview.put("recommendation", review.getString("recommendation"));
                            paramsReview.put("name", review.getString("name"));
                            paramsReview.put("date_insert", review.getString("date_insert"));
                            paramsReview.put("stars", review.getString("stars"));
                            for(int j=1; j <= config.NUM_IMG_IN_REVIEW; j++){
                                String foto_key = "foto"+j;
                                String foto_key_min = foto_key+"_min";
                                String foto = review.getString(foto_key);
                                if(!foto.equals("")) {
                                    paramsReview.put(foto_key, foto);
                                    paramsReview.put(foto_key_min, review.getString(foto_key_min));
                                }
                            }
                            Item item = new Item(paramsReview, num);
                            reviewList.add(item);
                        //}
                    }
                    reviewAdapter.notifyDataSetChanged();
                }
                else{
                    CardView blockReviews = findViewById(R.id.reviews_block);
                    blockReviews.setVisibility(View.GONE);
                }

            }
            else{
                String message = jsonResult.getString("message");
                Toast.makeText(Activity, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e){
            Toast.makeText(Activity, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /*public void onClickBook(View v){
        // record Order params to storage
        SharedPreferences settings = getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        // set cart
        selectedOfferId = Integer.parseInt(Objects.requireNonNull(offerList.get(position).getParam("id")));
        orderSum = Integer.parseInt(Objects.requireNonNull(offerList.get(position).getParam("price")));
        priceOld = Objects.requireNonNull(offerList.get(position).getParam("price_old"));
        duration_name = Objects.requireNonNull(offerList.get(position).getParam("duration_name"));
        human_name = Objects.requireNonNull(offerList.get(position).getParam("human_name"));
        editor.putInt("offerId", selectedOfferId);
        editor.putInt("orderSum", orderSum);
        editor.putString("price_old", priceOld);
        editor.putString("impressionName", impressionName);
        editor.putString("human_name", human_name);
        editor.putString("duration_name", duration_name);
        editor.apply();
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
    }*/

}

