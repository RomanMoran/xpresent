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

public class SetsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final int VIEW_TYPE_ITEM=0, VIEW_TYPE_LOADING=1;
    private LoadMoreInterface loadMoreInterface;
    private SelectedItem selectedItem;
    private Context activity;
    private boolean isLoading;
    private List<Item> items;
    private int visibleTreshold=3;
    private int lastVisibleItem, totalCountItem;

    public SetsAdapter(RecyclerView recyclerView, Context activity, List<Item> items, SelectedItem selectImp) {
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
                        .inflate(R.layout.item_set_preview, parent, false);
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
            if(!oldPrice.equals("0")) {
                viewHolder.oldPriceTV.setText(oldPrc);
            }
            else{
                viewHolder.oldPriceTV.setVisibility(View.GONE);
            }
            String impressionCount = item.getParam("impression_count");
            viewHolder.impressionCountTV.setText("Впечатлений: "+impressionCount);

            Glide.with(viewHolder.itemView)
                    .load(config.HOST_NAME+item.getParam("img"))
                    .apply(new RequestOptions().override(config.SET_IMG_WIDTH, config.SET_IMG_HEIGHT))
                    //.placeholder(R.drawable.placeholder_details)
                    .centerCrop()
                    .into(viewHolder.getImpressionImageView());

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
        TextView priceTV;
        TextView oldPriceTV;
        TextView impressionCountTV;
        ImageView setImage;
        Chip tag;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.setName);
            setImage = itemView.findViewById(R.id.setImg);
            priceTV = itemView.findViewById(R.id.tv_price);
            oldPriceTV = itemView.findViewById(R.id.tv_old_price);
            impressionCountTV = itemView.findViewById(R.id.tv_impression_count);
            tag = itemView.findViewById(R.id.tag);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedItem.selectedItem(items.get(getAdapterPosition()));
                }
            });
        }

        ImageView getImpressionImageView(){
            return setImage;
        }
    }
}