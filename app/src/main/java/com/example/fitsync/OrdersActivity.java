package com.example.fitsync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrdersAdapter adapter;
    private List<Order> orderList;
    private TextView emptyOrdersText;
    private ProgressBar progressBar;
    private View overlay;
    private SwipeRefreshLayout swipeRefreshLayout; // For pull-to-refresh
    private Handler handler; // For polling
    private Runnable pollingRunnable; // Polling task
    private static final long POLLING_INTERVAL = 10000; // 10 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Orders");
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.ordersRecyclerView);
        emptyOrdersText = findViewById(R.id.emptyOrdersText);
        progressBar = findViewById(R.id.progressBar);
        overlay = findViewById(R.id.overlay);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        adapter = new OrdersAdapter(orderList, this);
        recyclerView.setAdapter(adapter);

        // Set up pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchOrders(true); // Manual refresh
        });

        // Set up polling
        handler = new Handler(Looper.getMainLooper());
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                fetchOrders(false); // Auto-refresh
                handler.postDelayed(this, POLLING_INTERVAL);
            }
        };

        // Initial fetch
        fetchOrders(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start polling when the activity is visible
        handler.postDelayed(pollingRunnable, POLLING_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop polling when the activity is not visible
        handler.removeCallbacks(pollingRunnable);
    }

    private void fetchOrders(boolean showLoading) {
        SharedPreferences prefs = getSharedPreferences("FitSyncPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");

        if (showLoading) {
            progressBar.setVisibility(View.VISIBLE);
            overlay.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyOrdersText.setVisibility(View.GONE);
        }

        String url = "http://10.0.2.2:8000/api/orders/?username=" + username;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            orderList.clear();
                            JSONArray orders = json.getJSONArray("orders");
                            for (int i = 0; i < orders.length(); i++) {
                                JSONObject orderJson = orders.getJSONObject(i);
                                JSONArray itemsJson = orderJson.getJSONArray("items");
                                List<OrderItem> items = new ArrayList<>();
                                for (int j = 0; j < itemsJson.length(); j++) {
                                    JSONObject itemJson = itemsJson.getJSONObject(j);
                                    items.add(new OrderItem(
                                            itemJson.getInt("product_id"),
                                            itemJson.getString("name"),
                                            (float) itemJson.getDouble("price"),
                                            itemJson.getInt("quantity"),
                                            itemJson.isNull("image") ? "" : "http://10.0.2.2:8000" + itemJson.getString("image")
                                    ));
                                }
                                orderList.add(new Order(
                                        orderJson.getInt("order_id"),
                                        (float) orderJson.getDouble("total_price"),
                                        orderJson.getString("added_at"),
                                        orderJson.getString("status"),
                                        orderJson.getString("location"),
                                        items
                                ));
                            }
                            adapter.notifyDataSetChanged();

                            if (orderList.isEmpty()) {
                                recyclerView.setVisibility(View.GONE);
                                emptyOrdersText.setVisibility(View.VISIBLE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                emptyOrdersText.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(this, json.getString("error"), Toast.LENGTH_SHORT).show();
                            recyclerView.setVisibility(View.GONE);
                            emptyOrdersText.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        recyclerView.setVisibility(View.GONE);
                        emptyOrdersText.setVisibility(View.VISIBLE);
                    }
                    if (showLoading) {
                        progressBar.setVisibility(View.GONE);
                        overlay.setVisibility(View.GONE);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    recyclerView.setVisibility(View.GONE);
                    emptyOrdersText.setVisibility(View.VISIBLE);
                    if (showLoading) {
                        progressBar.setVisibility(View.GONE);
                        overlay.setVisibility(View.GONE);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
        );
        Volley.newRequestQueue(this).add(request);
    }
}