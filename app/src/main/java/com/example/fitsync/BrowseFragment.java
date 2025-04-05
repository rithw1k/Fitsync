package com.example.fitsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class BrowseFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private RequestQueue requestQueue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productList = new ArrayList<>();
        adapter = new ProductAdapter(productList, getContext());
        recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(getContext());
        fetchProducts();

        return view;
    }

    private void fetchProducts() {
        String url = "http://10.0.2.2:8000/api/products/";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray products = response.getJSONArray("products");
                            for (int i = 0; i < products.length(); i++) {
                                JSONObject obj = products.getJSONObject(i);
                                Product product = new Product(
                                        obj.getInt("id"),  // Add this
                                        obj.getString("name"),
                                        obj.getString("description"),
                                        (float) obj.getDouble("price"),
                                        obj.getInt("stock"),
                                        obj.isNull("image") ? "" : "http://10.0.2.2:8000" + obj.getString("image")
                                );
                                productList.add(product);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );
        requestQueue.add(request);
    }
}