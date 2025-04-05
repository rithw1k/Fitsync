package com.example.fitsync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;

    public OrdersAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Format date
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = isoFormat.parse(order.getAddedAt());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            holder.dateText.setText("Ordered on: " + displayFormat.format(date));
        } catch (Exception e) {
            holder.dateText.setText("Ordered on: " + order.getAddedAt());
        }

        holder.orderIdText.setText("Order #" + order.getOrderId());
        holder.statusText.setText("Status: " + order.getStatus());
        holder.totalPriceText.setText("Total: ₹" + String.format("%.2f", order.getTotalPrice()));
        holder.locationText.setText("Location: " + order.getLocation());

        // Set up nested RecyclerView for order items
        holder.itemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        OrderItemsAdapter itemsAdapter = new OrderItemsAdapter(order.getItems(), context);
        holder.itemsRecyclerView.setAdapter(itemsAdapter);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText, dateText, statusText, totalPriceText,locationText;
        RecyclerView itemsRecyclerView;

        OrderViewHolder(View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            dateText = itemView.findViewById(R.id.dateText);
            statusText = itemView.findViewById(R.id.statusText);
            totalPriceText = itemView.findViewById(R.id.totalPriceText);
            locationText=itemView.findViewById(R.id.locationText);
            itemsRecyclerView = itemView.findViewById(R.id.itemsRecyclerView);
        }
    }
}