package com.example.fitsync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddFoodFragment extends Fragment {

    private EditText detailsInput;
    private Button addButton;
    private ProgressBar progressBar;
    private View overlay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_food, container, false);

        detailsInput = view.findViewById(R.id.detailsInput);
        addButton = view.findViewById(R.id.addButton);
        progressBar = view.findViewById(R.id.progressBar);
        overlay = view.findViewById(R.id.overlay);

        addButton.setOnClickListener(v -> addFoodLog());

        return view;
    }

    private void addFoodLog() {
        SharedPreferences prefs = getContext().getSharedPreferences("FitSyncPrefs", getContext().MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");

        String details = detailsInput.getText().toString().trim();

        if (details.isEmpty()) {
            Toast.makeText(getContext(), "Details are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set the current date programmatically
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        progressBar.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.VISIBLE);

        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("date", currentDate);
        params.put("added_details", details);
        params.put("comments", new JSONArray()); // Empty array for trainers to add comments later

        String url = "http://10.0.2.2:8000/api/add-food-log/";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(getContext(), json.getString("message"), Toast.LENGTH_SHORT).show();
                            detailsInput.setText(""); // Clear details input
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
            public byte[] getBody() {
                return new JSONObject(params).toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        Volley.newRequestQueue(getContext()).add(request);
    }
}