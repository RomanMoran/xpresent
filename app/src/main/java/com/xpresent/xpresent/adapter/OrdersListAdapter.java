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
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.config.config;
import com.xpresent.xpresent.lib.Utility;
import com.xpresent.xpresent.model.Item;
import java.util.List;

public class OrdersListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private SelectedItem selectedItem;
    private Context activity;
    private List<Item> orders;

    public OrdersListAdapter(Context activity, List<Item> items, SelectedItem selectCert) {
        this.activity = activity;
        this.orders = items;
        this.selectedItem = selectCert;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity)
                .inflate(R.layout.item_order, parent, false);
        ItemViewHolder viewHolder = new ItemViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item orderItem = orders.get(position);
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        Glide.with(viewHolder.itemView)
                .load(config.HOST_NAME+orderItem.getParam("img"))
                .apply(new RequestOptions().override(config.IMPRESSION_IMG_WIDTH, config.IMPRESSION_IMG_HEIGHT))
                .placeholder(R.drawable.placeholder_details)
                .centerCrop()
                .into(viewHolder.impressionImage);
        int statusBank = Integer.parseInt(orderItem.getParam("status_bank"));
        String status;
        switch (statusBank) {
            case 1:
                status = "В процессе";
                break;
            case 2:
                status = "Оплачен";
                break;
            case 6:
                status = "Отклонен";
                break;
            default:
                status = "Не оплачено";
                break;
        }
        viewHolder.orderId.setText(orderItem.getParam("order_id"));
        viewHolder.orderStatus.setText(status);
        String dateTime = Utility.formatDate(orderItem.getParam("date_insert"), "", "");
        viewHolder.orderDate.setText(dateTime);
        String itemSum = orderItem.getParam("item_sum")+" "+ config.RUB;
        viewHolder.orderSum.setText(itemSum);
        viewHolder.impressionName.setText(orderItem.getParam("item_name"));
        viewHolder.certNumber.setText(orderItem.getParam("cert_number"));
        viewHolder.certCode.setText(orderItem.getParam("secret_code"));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView orderId, orderStatus, orderDate, orderSum, impressionName, certNumber, certCode;
        ImageView impressionImage;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            // order
            orderId = itemView.findViewById(R.id.orderId);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderDate = itemView.findViewById(R.id.orderDate);
            orderSum = itemView.findViewById(R.id.orderSum);
            // certificate
            impressionName = itemView.findViewById(R.id.impressionName);
            certNumber = itemView.findViewById(R.id.certNumber);
            certCode = itemView.findViewById(R.id.certCode);
            impressionImage = itemView.findViewById(R.id.impressionImage);
            // certificate is clicked
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedItem.selectedItem(orders.get(getAdapterPosition()));
                }
            });
        }
    }
}