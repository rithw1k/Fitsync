package com.example.fitsync;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartList;
    private TextView totalPriceText;
    private Button buyButton;
    private TextView emptyCartText; // New TextView for empty cart message
    private ProgressBar progressBar; // New ProgressBar for loading
    private View overlay; // Blurry background overlay
    private float totalPrice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.cartRecyclerView);
        totalPriceText = view.findViewById(R.id.totalPriceText);
        buyButton = view.findViewById(R.id.buyButton);
        emptyCartText = view.findViewById(R.id.emptyCartText);
        progressBar = view.findViewById(R.id.progressBar);
        overlay = view.findViewById(R.id.overlay);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartList = new ArrayList<>();
        adapter = new CartAdapter(cartList, getContext(), this);
        recyclerView.setAdapter(adapter);

        fetchCart(); // Initial fetch

        buyButton.setOnClickListener(v -> showPaymentDialog());

        return view;
    }

    public void fetchCart() {
        SharedPreferences prefs = getContext().getSharedPreferences("FitSyncPrefs", getContext().MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");

        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyCartText.setVisibility(View.GONE);
        totalPriceText.setVisibility(View.GONE);
        buyButton.setVisibility(View.GONE);

        String url = "http://10.0.2.2:8000/api/cart/?username=" + username;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            cartList.clear();
                            JSONArray cart = json.getJSONArray("cart");
                            totalPrice = (float) json.getDouble("total_price");
                            for (int i = 0; i < cart.length(); i++) {
                                JSONObject item = cart.getJSONObject(i);
                                cartList.add(new CartItem(
                                        item.getInt("id"),
                                        item.getInt("product_id"),
                                        item.getString("name"),
                                        (float) item.getDouble("price"),
                                        item.getInt("quantity"),
                                        item.isNull("image") ? "" : "http://10.0.2.2:8000" + item.getString("image")
                                ));
                            }
                            adapter.notifyDataSetChanged();
                            updateTotalPrice();

                            // Show/hide views based on cart contents
                            if (cartList.isEmpty()) {
                                recyclerView.setVisibility(View.GONE);
                                emptyCartText.setVisibility(View.VISIBLE);
                                totalPriceText.setVisibility(View.GONE);
                                buyButton.setVisibility(View.GONE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                emptyCartText.setVisibility(View.GONE);
                                totalPriceText.setVisibility(View.VISIBLE);
                                buyButton.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getContext(), json.getString("error"), Toast.LENGTH_SHORT).show();
                            recyclerView.setVisibility(View.GONE);
                            emptyCartText.setVisibility(View.VISIBLE);
                            totalPriceText.setVisibility(View.GONE);
                            buyButton.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error parsing cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        recyclerView.setVisibility(View.GONE);
                        emptyCartText.setVisibility(View.VISIBLE);
                        totalPriceText.setVisibility(View.GONE);
                        buyButton.setVisibility(View.GONE);
                    }
                    // Hide loading indicator
                    progressBar.setVisibility(View.GONE);
                    overlay.setVisibility(View.GONE);
                },
                error -> {
                    Toast.makeText(getContext(), "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    recyclerView.setVisibility(View.GONE);
                    emptyCartText.setVisibility(View.VISIBLE);
                    totalPriceText.setVisibility(View.GONE);
                    buyButton.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    overlay.setVisibility(View.GONE);
                }
        );
        Volley.newRequestQueue(getContext()).add(request);
    }

    private void updateTotalPrice() {
        totalPrice = 0;
        for (CartItem item : cartList) {
            totalPrice += item.getPrice() * item.getQuantity();
        }
        totalPriceText.setText("Total Price: ₹" + String.format("%.2f", totalPrice));
    }

    private void showPaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_payment, null);
        builder.setView(dialogView);

        EditText creditCardInput = dialogView.findViewById(R.id.creditCardInput);
        EditText cvvInput = dialogView.findViewById(R.id.cvvInput);
        EditText expiryDateInput = dialogView.findViewById(R.id.expiryDateInput);
        EditText locationInput = dialogView.findViewById(R.id.locationInput);
        Button submitButton = dialogView.findViewById(R.id.submitPaymentButton);

        AlertDialog dialog = builder.create();

        submitButton.setOnClickListener(v -> {
            String creditCard = creditCardInput.getText().toString().trim();
            String cvv = cvvInput.getText().toString().trim();
            String expiryDate = expiryDateInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();

            if (creditCard.isEmpty() || cvv.isEmpty() || expiryDate.isEmpty() || location.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getContext().getSharedPreferences("FitSyncPrefs", getContext().MODE_PRIVATE);
            String username = prefs.getString("username", "Guest");

            Map<String, String> params = new HashMap<>();
            params.put("username", username);
            params.put("credit_card", creditCard);
            params.put("cvv", cvv);
            params.put("expiry_date", expiryDate);
            params.put("location", location);

            // Show loading for order submission
            progressBar.setVisibility(View.VISIBLE);
            overlay.setVisibility(View.VISIBLE);

            String url = "http://10.0.2.2:8000/api/create-order/";
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject json = new JSONObject(response);
                            if (json.getBoolean("success")) {
                                Toast.makeText(getContext(), json.getString("message"), Toast.LENGTH_SHORT).show();
                                fetchCart(); // Refresh cart after order
                                dialog.dismiss();
                            } else {
                                Toast.makeText(getContext(), json.getString("error"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                        overlay.setVisibility(View.GONE);
                    },
                    error -> {
                        Toast.makeText(getContext(), "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        overlay.setVisibility(View.GONE);
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            Volley.newRequestQueue(getContext()).add(request);
        });

        dialog.show();
    }
}