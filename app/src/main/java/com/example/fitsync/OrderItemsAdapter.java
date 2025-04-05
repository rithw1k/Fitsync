package com.example.fitsync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.ItemViewHolder> {

    private List<OrderItem> itemList;
    private Context context;

    public OrderItemsAdapter(List<OrderItem> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        OrderItem item = itemList.get(position);
        holder.nameText.setText(item.getName());
        holder.priceText.setText("₹" + String.format("%.2f", item.getPrice()));
        holder.quantityText.setText("Qty: " + item.getQuantity());
        if (!item.getImageUrl().isEmpty()) {
            Glide.with(context).load(item.getImageUrl()).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameText, priceText, quantityText;

        ItemViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.itemImage);
            nameText = itemView.findViewById(R.id.itemName);
            priceText = itemView.findViewById(R.id.itemPrice);
            quantityText = itemView.findViewById(R.id.itemQuantity);
        }
    }
}