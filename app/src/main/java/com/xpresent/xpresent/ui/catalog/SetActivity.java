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
import android.text.Html;
import android.util.Base64;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.adapter.OfferAdapter;
import com.xpresent.xpresent.adapter.ReviewAdapter;
import com.xpresent.xpresent.adapter.SelectedItem;
import com.xpresent.xpresent.adapter.SetImpressionsAdapter;
import com.xpresent.xpresent.config.config;
import com.xpresent.xpresent.lib.Utility;
import com.xpresent.xpresent.model.Item;
import com.xpresent.xpresent.requests.ServerConnector;
import com.xpresent.xpresent.ui.auth.AuthorizationActivity;
import com.xpresent.xpresent.ui.booking.BuyCertActivity;
import com.xpresent.xpresent.ui.booking.CalendarActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class SetActivity extends AppCompatActivity implements SelectedItem {
    private Context Activity = this;
    private SharedPreferences settings;
    private Set<String> favoriteSet; // favorite list of impressions
    private MaterialCheckBox favoritesCheckBox;
    private int setId, firstOfferId = 0, orderSum, priceOld;
    private String itemId;
    private String impressionCount = "";
    private String setName;
    private SetImpressionsAdapter setimpressionAdapter;
    private Button buttonBook;
    private List<Item> impressionList = new ArrayList<>();
    private boolean isClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        isClicked = false;

        settings = Activity.getSharedPreferences("xp_client", Context.MODE_PRIVATE);

        Bundle extras = getIntent().getExtras();
        setName = extras.getString("setName");
        setId = extras.getInt("setId");
        //String title = (impressionName.length()>30)? impressionName.substring(0,30)+"..." : impressionName;
        TextView titleTV = findViewById(R.id.title);
        titleTV.setText(setName);
        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onBackPressed();
            }
        });

        // Impressions
        RecyclerView impressionsRecycler = findViewById(R.id.set_impressions_list);
        impressionsRecycler.setNestedScrollingEnabled(false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(Activity, 2, GridLayoutManager.VERTICAL, false);
        impressionsRecycler.setLayoutManager(gridLayoutManager);
        setimpressionAdapter = new SetImpressionsAdapter(Activity, impressionList, this);
        impressionsRecycler.setAdapter(setimpressionAdapter);

        // Favorites
        favoritesCheckBox = findViewById(R.id.favorites);
        favoriteSet = settings.getStringSet("favorites", null);
        favoritesCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add to favorite list
                if(((MaterialCheckBox) v).isChecked()){
                    favoriteSet.add(Integer.toString(firstOfferId));
                } // remove from favorite list
                else{
                    favoriteSet.remove(Integer.toString(firstOfferId));
                }
                SharedPreferences.Editor editor = settings.edit();
                editor.remove("favorites"); // remove old favorite set
                editor.apply();
                editor.putStringSet("favorites", favoriteSet);
                editor.apply();
            }
        });

        // Get set from server
        getSetItem();

        buttonBook = findViewById(R.id.btnBook);
        buttonBook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickBook(v);
            }
        });
    }

    public void getSetItem(){
        HashMap<String,String> mapPost = new HashMap<>();
        mapPost.put("action", "set");
        mapPost.put("id", Integer.toString(setId));

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

    @SuppressLint("SetJavaScriptEnabled")
    private void showResult(String result){
        try {
            JSONObject jsonResult = new JSONObject(result);
            boolean status = jsonResult.getBoolean("status");
            if (status) {
                JSONObject set = jsonResult.getJSONObject("set");
                itemId = set.getString("id")+":"+set.getString("impression_id");
                int num = 0;
                // Impressions
                JSONArray impressions = set.getJSONArray("impressions");
                for (int i = 0; i < impressions.length(); i++) {
                    JSONObject impression = impressions.getJSONObject(i);
                    Map<String,String> paramsImpression = new HashMap<>();
                    paramsImpression.put("id", impression.getString("id"));
                    paramsImpression.put("parent_id", impression.getString("parent_id"));
                    paramsImpression.put("name", impression.getString("impression_name"));
                    paramsImpression.put("img", impression.getString("img"));
                    paramsImpression.put("content_mini", impression.getString("content_mini"));
                    paramsImpression.put("duration_name", impression.getString("d_name"));
                    paramsImpression.put("human_name", impression.getString("h_name"));

                    Item item = new Item(paramsImpression, num);
                    impressionList.add(item);
                    num++;
                }
                setimpressionAdapter.notifyDataSetChanged();
                // No offers -> book button is not available
                if(num==0) buttonBook.setVisibility(View.GONE);

                // download set main image
                String imgUrl = set.getString("img");
                ImageView imageView = findViewById(R.id.setImg);
                Glide.with(this)
                        .load(config.HOST_NAME+imgUrl)
                        //.apply(new RequestOptions().override(config.IMPRESSION_IMG_WIDTH, config.IMPRESSION_IMG_HEIGHT_ITEM))
                        .into(imageView);

                // Tag
                String tagName = set.getString("tag");
                if(!tagName.equals("")) {
                    String tagColor = set.getString("tag_color");
                    Chip tagView = findViewById(R.id.tag);
                    int color = Utility.getTagColor(tagColor);
                    tagView.setChipBackgroundColorResource(color);
                    tagView.setText(tagName);
                    tagView.setVisibility(View.VISIBLE);
                }
                // Set name
                TextView setTitle = findViewById(R.id.setTitle);
                setTitle.setText("Подарочный набор "+setName);
                // HTML mini text
                String miniTextHtml = set.getString("content_item");
                TextView miniTextView = findViewById(R.id.introText);
                if(!miniTextHtml.equals("")) {
                    String miniText = android.text.Html.fromHtml(miniTextHtml).toString();
                    miniText = miniText.replaceAll("\\r\\n|\\r|\\n", " ");
                    miniTextView.setText(miniText);
                }
                else miniTextView.setVisibility(View.GONE);
                // Impression count
                TextView impressionCountTV = findViewById(R.id.impressionCount);
                impressionCount = set.getString("impression_count");
                impressionCountTV.setText("В набор входит "+impressionCount+" впечатление на выбор:");
                // Cart price
                orderSum = Integer.parseInt(set.getString("price"));
                TextView cartCashback = findViewById(R.id.cartCashback);
                TextView cartPrice = findViewById(R.id.cartPrice);
                cartPrice.setText(Objects.requireNonNull(config.RUB+" "+set.getString("price")));
                int cashBack = (int) Math.round(orderSum*0.05);
                String cashtxt = getResources().getString(R.string.cashback)+" +"+cashBack+" "+config.RUB;
                cartCashback.setText(cashtxt);

                // Is set in Favorites
                if(favoriteSet != null && favoriteSet.size() > 0) {
                    String[] favorites = favoriteSet.toArray(new String[favoriteSet.size()]);
                    for (String favorite : favorites) {
                        if (favorite.equals(Integer.toString(firstOfferId))) {
                            favoritesCheckBox.setChecked(true);
                        }
                    }
                }
                else favoriteSet = new HashSet<>();
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

    public void onClickBook(View v){
        // record Order params to storage
        SharedPreferences.Editor editor = settings.edit();
        // set cart
        editor.putString("ordType", "set");
        editor.putString("itemId", itemId);
        editor.putString("itemName", setName);
        editor.putString("impressionCount", impressionCount);

        editor.putInt("orderSum", orderSum);
        editor.putInt("price_old", priceOld);
        editor.putString("human_name", "");
        editor.putString("duration_name", "");
        editor.apply();
        // connect to app storage
        SharedPreferences settings = this.getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        // get sessionKey and order params from storage
        String sessionKey = settings.getString("sessionKey", "");
        //check authorization
        if (!sessionKey.isEmpty()) {
            Intent intent = new Intent(this, BuyCertActivity.class);
            startActivity(intent);
        } else {
            Bundle extras = new Bundle();
            extras.putString("redirect", "buycert");
            Intent intent = new Intent(this, AuthorizationActivity.class);
            intent.putExtras(extras);
            startActivity(intent);
        }
    }

    /**
     * onClick on Offer Recyclerview
     * @param item instance
     */
    @Override
    public void selectedItem(Item item) {
        // restrict double clicking
        if(!isClicked) {
            isClicked = true;
            Bundle extras = new Bundle();
            extras.putInt("impressionId", Integer.parseInt(item.getParam("parent_id")));
            extras.putString("impressionName", item.getParam("name"));
            Intent intent = new Intent(Activity, ImpressionActivity.class);
            intent.putExtras(extras);
            startActivity(intent);
            isClicked = false;
        }
    }

}

