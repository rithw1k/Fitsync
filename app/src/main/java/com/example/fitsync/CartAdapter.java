package com.example.fitsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartList;
    private Context context;
    private CartFragment cartFragment;

    public CartAdapter(List<CartItem> cartList, Context context, CartFragment cartFragment) {
        this.cartList = cartList;
        this.context = context;
        this.cartFragment = cartFragment;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);
        holder.nameText.setText(item.getName());
        holder.priceText.setText("₹" + String.format("%.2f", item.getPrice()));
        holder.quantityText.setText(String.valueOf(item.getQuantity()));
        if (!item.getImageUrl().isEmpty()) {
            Glide.with(context).load(item.getImageUrl()).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        SharedPreferences prefs = context.getSharedPreferences("FitSyncPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");

        holder.incrementButton.setOnClickListener(v -> updateCart(item.getId(), "increment", position));
        holder.decrementButton.setOnClickListener(v -> updateCart(item.getId(), "decrement", position));
        holder.removeButton.setOnClickListener(v -> updateCart(item.getId(), "remove", position));
    }

    private void updateCart(int cartId, String action, int position) {
        SharedPreferences prefs = context.getSharedPreferences("FitSyncPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("cart_id", String.valueOf(cartId));
        params.put("action", action);

        String url = "http://10.0.2.2:8000/api/update-cart/";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            cartFragment.fetchCart(); // Refresh cart from server
                        } else {
                            Toast.makeText(context, json.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(context, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameText, priceText, quantityText;
        Button incrementButton, decrementButton, removeButton;

        CartViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cartImage);
            nameText = itemView.findViewById(R.id.cartName);
            priceText = itemView.findViewById(R.id.cartPrice);
            quantityText = itemView.findViewById(R.id.cartQuantity);
            incrementButton = itemView.findViewById(R.id.incrementButton);
            decrementButton = itemView.findViewById(R.id.decrementButton);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}