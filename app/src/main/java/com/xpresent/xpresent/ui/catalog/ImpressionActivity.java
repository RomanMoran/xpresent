/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 26.05.20
 */

package com.xpresent.xpresent.ui.catalog;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.jivosite.sdk.ui.chat.JivoChatFragment;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.adapter.OfferAdapter;
import com.xpresent.xpresent.adapter.ReviewAdapter;
import com.xpresent.xpresent.adapter.SelectedItem;
import com.xpresent.xpresent.config.config;
import com.xpresent.xpresent.lib.Utility;
import com.xpresent.xpresent.model.Item;
import com.xpresent.xpresent.requests.ServerConnector;
import com.xpresent.xpresent.ui.booking.CalendarActivity;
import com.xpresent.xpresent.ui.booking.OrderTypeActivity;
import com.xpresent.xpresent.util.UtilKt;

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

import kotlin.collections.CollectionsKt;


public class ImpressionActivity extends AppCompatActivity implements SelectedItem {
    private Context Activity = this;
    private SharedPreferences settings;
    private Set<String> favoriteSet; // favorite list of impressions
    private MaterialCheckBox favoritesCheckBox;
    private int impressionId, firstOfferId = 0, selectedOfferId, orderSum, priceOld, position = 0;
    private String impressionName;
    private OfferAdapter offerAdapter;
    private ReviewAdapter reviewAdapter;
    private MaterialButton materialButton;
    private Button buttonBook;
    private List<Item> offerList = new ArrayList<>();
    private List<Item> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impression);

        settings = Activity.getSharedPreferences("xp_client", Context.MODE_PRIVATE);

        Bundle extras = getIntent().getExtras();
        impressionName = extras.getString("impressionName");
        impressionId = extras.getInt("impressionId");
        //String title = (impressionName.length()>30)? impressionName.substring(0,30)+"..." : impressionName;
        TextView titleTV = findViewById(R.id.title);
        titleTV.setText(impressionName);
        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> onBackPressed());

        // Offers
        RecyclerView offersRecycler = findViewById(R.id.impression_offer_list);
        offersRecycler.setNestedScrollingEnabled(false);
        offersRecycler.setLayoutManager(new LinearLayoutManager(Activity));
        offerAdapter = new OfferAdapter(Activity, offerList, this);
        offersRecycler.setAdapter(offerAdapter);

        // Reviews
        RecyclerView reviewsRecycler = findViewById(R.id.impression_reviews_list);
        reviewsRecycler.setNestedScrollingEnabled(false);
        reviewsRecycler.setLayoutManager(new LinearLayoutManager(Activity));
        reviewAdapter = new ReviewAdapter(Activity, reviewList, this, false);
        reviewAdapter.setImpressionParams(impressionId, impressionName);
        reviewsRecycler.setAdapter(reviewAdapter);
        materialButton = findViewById(R.id.allReviewsBtn);
        materialButton.setOnClickListener(v -> onClickAllReviews(v));
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

        // Get impression from server
        getImpressionItem();

        buttonBook = findViewById(R.id.btnBook);
        buttonBook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickBook(v);
            }
        });

        // Jivo chat
        View jivoView = findViewById(R.id.jivoBtn);
        jivoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity, JivoChatActivity.class);
                startActivity(intent);
            }
        });
    }

    public void getImpressionItem(){
        // get city from storage
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

    private void initReviewList(int count) {
        CollectionsKt.sortBy(reviewList, it -> UtilKt
                .toCalendar(it.getParam("date_insert"), "yyyy-MM-dd hh:mm:ss")
                .getTimeInMillis()
        );
        CollectionsKt.reverse(reviewList);
        List<Item> cache = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cache.add(reviewList.get(i));
        }
        reviewList.clear();
        reviewList.addAll(cache);
        reviewAdapter.notifyDataSetChanged();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showResult(String result){
        try {
            JSONObject jsonResult = new JSONObject(result);
            boolean status = jsonResult.getBoolean("status");
            if (status) {
                JSONObject impression = jsonResult.getJSONObject("impression");
                int num = 0;
                // Offers
                JSONArray offers = impression.getJSONArray("offer");
                for (int i = 0; i < offers.length(); i++) {
                    JSONObject offer = offers.getJSONObject(i);
                    String available_for_sale = offer.getString("available_for_sale");
                    // don't add offer if not available for sale
                    if(Integer.parseInt(available_for_sale) == 0) continue;
                    Map<String,String> paramsOffer = new HashMap<>();
                    paramsOffer.put("id", offer.getString("id"));
                    paramsOffer.put("price", offer.getString("price"));
                    paramsOffer.put("price_format", offer.getString("price_format"));
                    paramsOffer.put("price_old", offer.getString("price_old"));
                    paramsOffer.put("price_old_format", offer.getString("price_old_format"));
                    paramsOffer.put("duration_name", offer.getString("duration_name"));
                    paramsOffer.put("human_name", offer.getString("human_name"));
                    paramsOffer.put("available_for_sale", available_for_sale);

                    Item item = new Item(paramsOffer, num);
                    offerList.add(item);
                    // set label for Book button
                    if(num==0){
                        firstOfferId= selectedOfferId = Integer.parseInt(Objects.requireNonNull(paramsOffer.get("id")));
                        orderSum = Integer.parseInt(Objects.requireNonNull(paramsOffer.get("price")));
                        TextView cartCashback = findViewById(R.id.cartCashback);
                        TextView cartPrice = findViewById(R.id.cartPrice);
                        cartPrice.setText(Objects.requireNonNull(config.RUB+" "+paramsOffer.get("price")));
                        int cashBack = (int) Math.round(orderSum*0.05);
                        String cashtxt = getResources().getString(R.string.cashback)+" +"+cashBack+" "+config.RUB;
                        cartCashback.setText(cashtxt);
                    }
                    num++;
                }
                offerAdapter.notifyDataSetChanged();
                // No offers -> book button is not available
                if(num==0) buttonBook.setVisibility(View.GONE);

                // Reviews
                JSONArray reviewJson = impression.getJSONArray("review");
                int reviewCount = reviewJson.length();
                if(reviewCount > 0) {
                    num=0;
                    for (int i = 0; i < reviewCount; i++) {
                        JSONObject review = reviewJson.getJSONObject(i);
                        Map<String,String> paramsReview = new HashMap<>();
                        String advantages = review.getString("advantages");
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
                            paramsReview.put(foto_key, foto);
                            if(!foto.equals("")) {
                                paramsReview.put(foto_key_min, review.getString(foto_key_min));
                            }
                        }
                        reviewList.add(new Item(paramsReview, num++));
                    }
                    initReviewList(Math.min(reviewCount, config.REVIEWS_ON_PAGE));
                    if(reviewCount > config.REVIEWS_ON_PAGE){
                        String watchAllBtn = getResources().getString(R.string.reviews_all)+" ("+reviewCount+")";
                        materialButton.setText(watchAllBtn);
                        materialButton.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    CardView blockReviews = findViewById(R.id.reviews_block);
                    blockReviews.setVisibility(View.GONE);
                }

                // download impression main image
                String imgUrl = impression.getString("img");
                ImageView imageView = findViewById(R.id.impressionImg);
                Glide.with(this)
                        .load(config.HOST_NAME+imgUrl)
                        .apply(new RequestOptions().override(config.IMPRESSION_IMG_WIDTH, config.IMPRESSION_IMG_HEIGHT_ITEM))
                        .into(imageView);

                // Tag
                String tagName = impression.getString("tag");
                if(!tagName.equals("")) {
                    String tagColor = impression.getString("tag_color");
                    Chip tagView = findViewById(R.id.tag);
                    int color = Utility.getTagColor(tagColor);
                    tagView.setChipBackgroundColorResource(color);
                    tagView.setText(tagName);
                    tagView.setVisibility(View.VISIBLE);
                }
                // Impression Stars
                String star = impression.getString("star");
                String reviews_count = impression.getString("reviews_count");
                TextView starsText = findViewById(R.id.grade);
                TextView numReviews = findViewById(R.id.numReviews);
                starsText.setText(star);
                if(!reviews_count.equals("0")) {
                    String reviews = " (отзывов: " + reviews_count + ")";
                    numReviews.setText(reviews);
                    numReviews.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickAllReviews(v);
                        }
                    });
                }
                // Impression name
                TextView impressionTitle = findViewById(R.id.impressionTitle);
                impressionTitle.setText(impressionName);
                String mimeType = "text/html";
                String encoding = "base64";
                // HTML mini text
                String miniTextHtml = impression.getString("item_text");
                TextView miniTextView = findViewById(R.id.introText);
                if(!miniTextHtml.equals("")) {
                    String miniText = android.text.Html.fromHtml(miniTextHtml).toString();
                    miniText = miniText.replaceAll("\\r\\n|\\r|\\n", " ");
                    miniTextView.setText(miniText);
                }
                else miniTextView.setVisibility(View.GONE);
                // HTML full text
                String fullText = impression.getString("text");
                WebView wvFullText = findViewById(R.id.webViewFullText);
                WebSettings webSettings = wvFullText.getSettings();
                webSettings.setJavaScriptEnabled(true);
                if(!fullText.equals("")) {
                    String html = Html.fromHtml(fullText).toString();
                    String fullHtmlText = html.replace("src=\"/filecache", "width=\"100%\" src=\"https://xpresent.ru/filecache");
                    fullHtmlText = fullHtmlText.replaceAll("height=\"\\d{3}\"", "height=\"200\"");
                    String encodedHtml = Base64.encodeToString(fullHtmlText.getBytes(), Base64.NO_PADDING);
                    wvFullText.loadData(encodedHtml, mimeType, encoding);
                }
                else{
                    LinearLayout full_txt_block = findViewById(R.id.full_txt_block);
                    full_txt_block.setVisibility(View.GONE);
                }
                // HTML additional info
                String additionalInfo = impression.getString("cont_text");
                if(!additionalInfo.equals("")) {
                    WebView wvAddInfo = findViewById(R.id.webViewAdditionalInfo);
                    WebSettings InfoSettings = wvAddInfo.getSettings();
                    InfoSettings.setJavaScriptEnabled(true);
                    String additionalInfoHtml = Html.fromHtml(additionalInfo).toString();
                    String encodedAddInfoHtml = Base64.encodeToString(additionalInfoHtml.getBytes(), Base64.NO_PADDING);
                    wvAddInfo.loadData(encodedAddInfoHtml, mimeType, encoding);
                }
                else{
                    LinearLayout add_info_block = findViewById(R.id.add_info_block);
                    add_info_block.setVisibility(View.GONE);
                }
                // HTML impression address
                String impAddress = impression.getString("cont_text_where");
                if(!impAddress.equals("")){
                    WebView wvImpAddress = findViewById(R.id.webViewImpressionAddress);
                    WebSettings AddressSettings = wvImpAddress.getSettings();
                    AddressSettings.setJavaScriptEnabled(true);
                    String addressText = Html.fromHtml(impAddress).toString();
                    String addressHtmlText = addressText.replaceAll("height=\\d{3}", "height=200");
                    String encodedAddressHtmlText = Base64.encodeToString(addressHtmlText.getBytes(), Base64.NO_PADDING);
                    wvImpAddress.loadData(encodedAddressHtmlText, mimeType, encoding);
                }
                else{
                    LinearLayout place_block = findViewById(R.id.place_block);
                    place_block.setVisibility(View.GONE);
                }
                // Is impression in Favorites
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
        selectedOfferId = Integer.parseInt(Objects.requireNonNull(offerList.get(position).getParam("id")));
        orderSum = Integer.parseInt(Objects.requireNonNull(offerList.get(position).getParam("price")));
        priceOld = Integer.parseInt(Objects.requireNonNull(offerList.get(position).getParam("price_old")));
        String duration_name = Objects.requireNonNull(offerList.get(position).getParam("duration_name"));
        String human_name = Objects.requireNonNull(offerList.get(position).getParam("human_name"));
        editor.putString("itemId", Integer.toString(selectedOfferId));
        editor.putString("ordType", "impression");
        editor.putInt("orderSum", orderSum);
        editor.putInt("price_old", priceOld);
        editor.putString("itemName", impressionName);
        editor.putString("human_name", human_name);
        editor.putString("duration_name", duration_name);
        editor.apply();
        Intent intent = new Intent(this, OrderTypeActivity.class);
        startActivity(intent);
    }

    public void onClickAllReviews(View v){
        Bundle extras = new Bundle();
        extras.putInt("impressionId", impressionId);
        extras.putString("impressionName", impressionName);
        Intent intent = new Intent(this, ReviewsActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * onClick on Offer Recyclerview
     * @param item instance
     */
    @Override
    public void selectedItem(Item item) {
        position = item.getPosition();
        // change active offer
        offerAdapter.setActiveOfferPos(position);
        offerAdapter.notifyDataSetChanged();
        // change price near book button
        TextView cartPrice = findViewById(R.id.cartPrice);
        TextView cartCashback = findViewById(R.id.cartCashback);

        String price = Objects.requireNonNull(offerList.get(position).getParam("price"));
        String priceTxt = config.RUB+" "+price;
        int cashBack = (int) Math.round(Integer.parseInt(price)*0.05);
        String cashbackTxt = getResources().getString(R.string.cashback)+" +"+cashBack+" "+config.RUB;

        cartPrice.setText(priceTxt);
        cartCashback.setText(cashbackTxt);
    }

}

