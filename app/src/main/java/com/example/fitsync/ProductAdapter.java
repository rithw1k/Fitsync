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
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;

    public ProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.nameText.setText(product.getName());
        holder.priceText.setText("₹" + product.getPrice());
        if (product.getStock() == 0) {
            holder.stockText.setText("Out of Stock");
            holder.addToCartButton.setEnabled(false);
        }
        if (!product.getImageUrl().isEmpty()) {
            Glide.with(context).load(product.getImageUrl()).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.addToCartButton.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("FitSyncPrefs", Context.MODE_PRIVATE);
            String username = prefs.getString("username", "Guest");

            Map<String, String> params = new HashMap<>();
            params.put("username", username);
            params.put("product_id", String.valueOf(product.getId()));
            params.put("quantity", "1");

            String url = "http://10.0.2.2:8000/api/add-to-cart/";
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getBoolean("success")) {
                                Toast.makeText(context, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                if (context instanceof ProductsActivity) {
                                    ProductsActivity activity = (ProductsActivity) context;
                                    CartFragment cartFragment = (CartFragment) activity.getSupportFragmentManager()
                                            .findFragmentByTag("f1"); // Tag for CartFragment (position 1)
                                    if (cartFragment != null) {
                                        cartFragment.fetchCart();
                                    }
                                }
                            } else {
                                Toast.makeText(context, jsonResponse.getString("error"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
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
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameText, priceText, stockText;
        Button addToCartButton;

        ProductViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.productImage);
            nameText = itemView.findViewById(R.id.productName);
            priceText = itemView.findViewById(R.id.productPrice);
            stockText = itemView.findViewById(R.id.productStock);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
        }
    }
}