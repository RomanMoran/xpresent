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
import android.widget.RelativeLayout;
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

public class SetImpressionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private SelectedItem selectedItem;
    private Context activity;
    private List<Item> items;
    private int activeImpressionPos;

    public SetImpressionsAdapter(Context activity, List<Item> items, SelectedItem selectImp) {
        this.activity = activity;
        this.items = items;
        this.selectedItem = selectImp;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity)
                .inflate(R.layout.item_set_item_impression, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ItemViewHolder) {
            Item item = items.get(position);
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            viewHolder.nameTV.setText(item.getParam("name"));
            // HTML mini text
            String miniTextHtml = item.getParam("content_mini");
            if(!miniTextHtml.equals("")) {
                String miniText = android.text.Html.fromHtml(miniTextHtml).toString();
                miniText = miniText.replaceAll("\\r\\n|\\r|\\n", " ");
                viewHolder.descriptionTV.setText(miniText);
            }
            if(!item.getParam("duration_name").equals("")) {
                viewHolder.dNameTV.setText(item.getParam("duration_name"));
            }
            else{
                viewHolder.dNameTV.setVisibility(View.GONE);
            }
            if(!item.getParam("human_name").equals("")) {
                viewHolder.hNameTV.setText(item.getParam("human_name"));
            }
            else{
                viewHolder.hNameTV.setVisibility(View.GONE);
            }
            Glide.with(viewHolder.itemView)
                    .load(config.HOST_NAME+item.getParam("img"))
                    .apply(new RequestOptions().override(config.SET_IMG_WIDTH, config.SET_IMP_IMG_HEIGHT))
                    .placeholder(R.drawable.placeholder_details)
                    //.centerCrop()
                    .into(viewHolder.getImpressionImageView());

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

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView nameTV;
        TextView descriptionTV;
        ImageView impressionImage;
        TextView dNameTV;
        TextView hNameTV;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.impressionName);
            impressionImage = itemView.findViewById(R.id.impressionImg);
            descriptionTV = itemView.findViewById(R.id.tv_description);
            dNameTV = itemView.findViewById(R.id.dNameTV);
            hNameTV = itemView.findViewById(R.id.hNameTV);
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