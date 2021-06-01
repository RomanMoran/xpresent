/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 25.05.20 10:14
 */
package com.xpresent.xpresent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.chip.Chip;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.config.config;
import com.xpresent.xpresent.lib.Utility;
import com.xpresent.xpresent.model.Item;
import java.util.List;

public class ImpressionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final int VIEW_TYPE_ITEM=0, VIEW_TYPE_LOADING=1;
    private LoadMoreInterface loadMoreInterface;
    private SelectedItem selectedItem;
    private Context activity;
    private boolean isLoading;
    private List<Item> items;
    private int visibleTreshold=3;
    private int lastVisibleItem, totalCountItem;

    public ImpressionsAdapter(RecyclerView recyclerView, Context activity, List<Item> items, SelectedItem selectImp) {
        this.activity = activity;
        this.items = items;
        this.selectedItem = selectImp;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalCountItem = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalCountItem <= (lastVisibleItem + visibleTreshold)) {
                    if (loadMoreInterface != null) {
                        loadMoreInterface.onLoadMoreInterface();
                        isLoading = true;
                    }
                }
            }
        });

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TYPE_ITEM:
                View view = LayoutInflater.from(activity)
                        .inflate(R.layout.item_impression_preview, parent, false);
                return new ItemViewHolder(view);
            default:
            case VIEW_TYPE_LOADING:
                View load_view = LayoutInflater.from(activity)
                        .inflate(R.layout.item_loading, parent, false);
                return new LoadingViewHolder(load_view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ItemViewHolder) {
            Item item = items.get(position);
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            viewHolder.nameTV.setText(item.getParam("name"));
            String price = config.RUB+" "+item.getParam("price");
            viewHolder.priceTV.setText(price);
            String oldPrice = item.getParam("old_price");
            String oldPrc = oldPrice.equals("0") ? "" : config.RUB + " " + oldPrice;
            viewHolder.oldPriceTV.setText(oldPrc);
            String miniText = android.text.Html.fromHtml(item.getParam("content_item")).toString();
            String miniTextFormat = miniText.replaceAll("\\r\\n|\\r|\\n", " ");
            viewHolder.descriptionTV.setText(miniTextFormat);
            Glide.with(viewHolder.itemView)
                    .load(config.HOST_NAME+item.getParam("img"))
                    .apply(new RequestOptions().override(config.IMPRESSION_IMG_WIDTH, config.IMPRESSION_IMG_HEIGHT))
                    .placeholder(R.drawable.placeholder_details)
                    .centerCrop()
                    .into(viewHolder.getImpressionImageView());

            String reviews_count = item.getParam("reviews_count");
            String reviewsStr = "";
            if(!reviews_count.equals("0") && !reviews_count.equals("")) {
                reviewsStr = activity.getResources().getString(R.string.review_list) + ": " + reviews_count;
            }
            viewHolder.tv_reviews.setText(reviewsStr);
            viewHolder.tv_star.setText(item.getParam("grade"));
            String tagName = item.getParam("tag");
            if(!tagName.equals("")){
                String tagColor = item.getParam("tag_color");
                int color= Utility.getTagColor(tagColor);
                viewHolder.tag.setChipBackgroundColorResource(color);
                viewHolder.tag.setText(tagName);
                viewHolder.tag.setVisibility(View.VISIBLE);
            }
            else{
                viewHolder.tag.setVisibility(View.GONE);
            }
        }
        else if(holder instanceof LoadingViewHolder){
            LoadingViewHolder viewHolder = (LoadingViewHolder) holder;
            viewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setLoadMoreInterface(LoadMoreInterface loadMoreInterface) {
        this.loadMoreInterface = loadMoreInterface;
    }

    public void setLoaded() {  isLoading = false; }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView nameTV;
        TextView descriptionTV;
        TextView priceTV;
        TextView oldPriceTV;
        TextView tv_reviews, tv_star;
        ImageView impressionImage;
        Chip tag;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.impressionName);
            impressionImage = itemView.findViewById(R.id.impressionImg);
            priceTV = itemView.findViewById(R.id.tv_price);
            oldPriceTV = itemView.findViewById(R.id.tv_old_price);
            descriptionTV = itemView.findViewById(R.id.tv_description);
            tv_reviews = itemView.findViewById(R.id.tv_reviews);
            tv_star = itemView.findViewById(R.id.tv_star);
            tag = itemView.findViewById(R.id.tag);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedItem.selectedItem(items.get(getAdapterPosition()));
                }
            });
        }

        ImageView getImpressionImageView(){
            return impressionImage;
        }
    }
}