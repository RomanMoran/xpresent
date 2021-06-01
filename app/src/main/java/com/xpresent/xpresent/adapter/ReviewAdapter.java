/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 03.06.20 11:12
 */
package com.xpresent.xpresent.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.config.config;
import com.xpresent.xpresent.lib.Utility;
import com.xpresent.xpresent.model.Item;
import com.xpresent.xpresent.ui.catalog.FullImageActivity;
import com.xpresent.xpresent.ui.catalog.ReviewsActivity;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context Activity;
    private List<Item> reviewsList;
    private SelectedItem selectedItem;
    private boolean allReviews;
    private int impressionId;
    private String impressionName;

    public ReviewAdapter(Context context, List<Item> items, SelectedItem selectItem, boolean allRev) {
        Activity = context;
        reviewsList = items;
        selectedItem = selectItem;
        allReviews = allRev;
    }

    public void setImpressionParams(int id, String name){
        impressionId = id;
        impressionName = name;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int template = allReviews ? R.layout.item_review : R.layout.item_impression_item_review;
        View view = LayoutInflater.from(Activity)
                .inflate(template, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = reviewsList.get(position);
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        // author name
        String authorName = item.getParam("name");
        if(!authorName.equals("")){
            viewHolder.authorName.setText(authorName);
        }
        else viewHolder.authorName.setVisibility(View.GONE);
        // text
        String review = android.text.Html.fromHtml(item.getParam("advantages")).toString();
        review = (review.length()>200 && !allReviews)? review.substring(0,200) : review;
        viewHolder.reviewText.setText(Html.fromHtml(review));
        // date
        String date_insert = item.getParam("date_insert");
        viewHolder.reviewDate.setText(Utility.formatDate(date_insert, "", ""));
        // grade
        float grade = Float.parseFloat(item.getParam("stars"));
        grade = (grade > 5) ? (grade / 2) : grade;
        viewHolder.reviewGrade.setText(Float.toString(grade));
        // author image
        // TODO insert image of the user
        String foto_url = item.getParam("foto1_min");
        if(foto_url != null) {
            Glide.with(Activity)
                    .load(config.HOST_NAME + foto_url)
                    .placeholder(R.drawable.placeholder_details)
                    .apply(new RequestOptions().override(config.REVIEW_MIN_IMG_WIDTH, config.REVIEW_MIN_IMG_HEIGHT))
                    .into(viewHolder.reviewImg);
        }
        // ReviewsActivity displays all reviews
        if(allReviews){
            for(int i=1; i <= config.NUM_IMG_IN_REVIEW; i++){
                final String imgUrl = item.getParam("foto"+i);
                if(imgUrl != null){
                    String imgSmallUrl = item.getParam("foto"+i+"_min");
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions = requestOptions.transforms(new CenterCrop());
                    Glide.with(Activity)
                            .load(config.HOST_NAME+imgSmallUrl)
                            .transform(new CenterCrop(),
                                       new RoundedCorners(16)  )
                            .into(viewHolder.getViewImg(i));
                    viewHolder.getViewImg(i).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Activity, FullImageActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString("imgFullUrl", imgUrl);
                            intent.putExtras(extras);
                            Activity.startActivity(intent);
                        }
                    });
                }
                else {
                    viewHolder.getViewImg(i).setVisibility(View.GONE);
                }
            }
        }
        else{
            viewHolder.review.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle extras = new Bundle();
                    extras.putInt("impressionId", impressionId);
                    extras.putString("impressionName", impressionName);
                    Intent intent = new Intent(Activity, ReviewsActivity.class);
                    intent.putExtras(extras);
                    Activity.startActivity(intent);
                }
            });
        }
    }

    @Override
    public long getItemId(int i) {
        return Integer.parseInt(reviewsList.get(i).getParam("id"));
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout review;
        TextView authorName, reviewText, reviewDate, reviewGrade;
        ImageView reviewImg, img1, img2, img3;

        ItemViewHolder(@NonNull View view) {
            super(view);
            review = view.findViewById(R.id.review);
            authorName = view.findViewById(R.id.authorName);
            reviewText = view.findViewById(R.id.reviewText);
            reviewDate = view.findViewById(R.id.reviewDate);
            reviewImg = view.findViewById(R.id.reviewImg);
            reviewGrade = view.findViewById(R.id.reviewGrade);
            if(allReviews) {
                img1 = view.findViewById(R.id.img1);
                img2 = view.findViewById(R.id.img2);
                img3 = view.findViewById(R.id.img3);
            }
            else{
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedItem.selectedItem(reviewsList.get(getAdapterPosition()));
                    }
                });
            }
        }

        ImageView getViewImg(int index){
            switch (index){
                default:
                case 1:
                    return img1;
                case 2:
                    return img2;
                case 3:
                    return img3;
            }
        }


    }

}