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
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.config.config;
import com.xpresent.xpresent.model.Item;
import java.util.List;

public class CatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private SelectedItem selectedCategory;
    private Context activity;
    private List<Item> items;

    public CatAdapter(Context activity, List<Item> items, SelectedItem selectCat) {
        this.activity = activity;
        this.items = items;
        this.selectedCategory = selectCat;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity)
                .inflate(R.layout.item_category_preview, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ItemViewHolder) {
            Item item = items.get(position);
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            viewHolder.categoryName.setText(item.getParam("categoryName"));
            Glide.with(viewHolder.itemView)
                    .load(config.HOST_NAME + item.getParam("imageUrl"))
                    .placeholder(R.drawable.placeholder_details)
                    .apply(new RequestOptions().override(config.CATEGORY_IMG_WIDTH, config.CATEGORY_IMG_HEIGHT))
                    .into(viewHolder.getCategoryImageView());
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
        TextView categoryName;
        ImageView categoryImage;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.mainText);
            categoryImage = itemView.findViewById(R.id.categoryImg);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedCategory.selectedItem(items.get(getAdapterPosition()));
                }
            });
        }

        ImageView getCategoryImageView(){
            return categoryImage;
        }
    }
}