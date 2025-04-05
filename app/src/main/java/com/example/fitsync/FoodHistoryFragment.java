package com.example.fitsync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;

public class FoodHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private FoodHistoryAdapter adapter;
    private List<DailyFood> foodList;
    private TextView emptyHistoryText;
    private ProgressBar progressBar;
    private View overlay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_history, container, false);

        recyclerView = view.findViewById(R.id.foodRecyclerView);
        emptyHistoryText = view.findViewById(R.id.emptyHistoryText);
        progressBar = view.findViewById(R.id.progressBar);
        overlay = view.findViewById(R.id.overlay);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        foodList = new ArrayList<>();
        adapter = new FoodHistoryAdapter(foodList, getContext(), this::showCommentsDialog);
        recyclerView.setAdapter(adapter);

        fetchFoodHistory();

        return view;
    }

    private void fetchFoodHistory() {
        SharedPreferences prefs = getContext().getSharedPreferences("FitSyncPrefs", getContext().MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");

        progressBar.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyHistoryText.setVisibility(View.GONE);

        String url = "http://10.0.2.2:8000/api/food-history/?username=" + username;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            foodList.clear();
                            JSONArray history = json.getJSONArray("history");
                            for (int i = 0; i < history.length(); i++) {
                                JSONObject log = history.getJSONObject(i);
                                JSONArray commentsJson = log.getJSONArray("comments");
                                List<String> comments = new ArrayList<>();
                                for (int j = 0; j < commentsJson.length(); j++) {
                                    comments.add(commentsJson.getString(j));
                                }
                                foodList.add(new DailyFood(
                                        log.getInt("id"),
                                        log.getString("date"),
                                        log.getString("added_details"),
                                        comments,
                                        log.getString("created_at")
                                ));
                            }
                            adapter.notifyDataSetChanged();

                            if (foodList.isEmpty()) {
                                recyclerView.setVisibility(View.GONE);
                                emptyHistoryText.setVisibility(View.VISIBLE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                emptyHistoryText.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(getContext(), json.getString("error"), Toast.LENGTH_SHORT).show();
                            recyclerView.setVisibility(View.GONE);
                            emptyHistoryText.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        recyclerView.setVisibility(View.GONE);
                        emptyHistoryText.setVisibility(View.VISIBLE);
                    }
                    progressBar.setVisibility(View.GONE);
                    overlay.setVisibility(View.GONE);
                },
                error -> {
                    Toast.makeText(getContext(), "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    recyclerView.setVisibility(View.GONE);
                    emptyHistoryText.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    overlay.setVisibility(View.GONE);
                }
        );
        Volley.newRequestQueue(getContext()).add(request);
    }

    private void showCommentsDialog(List<String> comments) {
        if (comments.isEmpty()) {
            return; // Don't show dialog if comments are empty
        }

        // Create a dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_comments, null);
        builder.setView(dialogView);

        RecyclerView commentsRecyclerView = dialogView.findViewById(R.id.commentsRecyclerView);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        CommentsAdapter commentsAdapter = new CommentsAdapter(comments);
        commentsRecyclerView.setAdapter(commentsAdapter);

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
}