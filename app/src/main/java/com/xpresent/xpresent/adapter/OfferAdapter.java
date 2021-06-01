/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 04.06.20 11:12
 */
package com.xpresent.xpresent.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.config.config;
import com.xpresent.xpresent.model.Item;
import java.util.List;

public class OfferAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context Activity;
    private List<Item> offersList;
    private int activeOfferPos = 0;
    private SelectedItem selectedItem;

    public OfferAdapter(Context context, List<Item> items, SelectedItem selectItem) {
        Activity = context;
        offersList = items;
        selectedItem = selectItem;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(Activity)
                .inflate(R.layout.item_impression_item_offer, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = offersList.get(position);
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        String price = config.RUB+" "+item.getParam("price");
        String old_price = item.getParam("price_old");
        if(!old_price.equals("0")) {
            String oldPrice = config.RUB + " " + old_price;
            viewHolder.oldPriceTextTV.setText(oldPrice);
        }
        viewHolder.priceTV.setText(price);
        viewHolder.numPersonTV.setText(item.getParam("human_name"));
        viewHolder.durationTV.setText(item.getParam("duration_name"));
        int borderColor, textColor;
        // if offer is active
        if(position == activeOfferPos){
            textColor = borderColor = ContextCompat.getColor(Activity, R.color.colorPrimary);
        }
        else{
            borderColor = ContextCompat.getColor(Activity, R.color.colorCardStroke);
            textColor = ContextCompat.getColor(Activity, R.color.colorOnSurface);
        }
        viewHolder.cardView.setStrokeColor(borderColor);
        viewHolder.priceTV.setTextColor(textColor);
    }

    @Override
    public long getItemId(int i) {
        return Integer.parseInt(offersList.get(i).getParam("id"));
    }

    @Override
    public int getItemCount() {
        return offersList.size();
    }

    public void setActiveOfferPos(int activeOfferPos) {
        this.activeOfferPos = activeOfferPos;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        MaterialCardView cardView;
        TextView priceTV;
        TextView oldPriceTextTV;
        TextView numPersonTV;
        TextView durationTV;

        ItemViewHolder(@NonNull View view) {
            super(view);
            cardView = view.findViewById(R.id.cardOffer);
            priceTV = view.findViewById(R.id.price);
            oldPriceTextTV = view.findViewById(R.id.oldPrice);
            numPersonTV = view.findViewById(R.id.numPerson);
            durationTV = view.findViewById(R.id.duration);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedItem.selectedItem(offersList.get(getAdapterPosition()));
                }
            });
        }
    }

}