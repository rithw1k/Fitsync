package com.example.fitsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SessionsActivity extends AppCompatActivity {

    private static final String TAG = "SessionsActivity";
    private RecyclerView videosRecyclerView;
    private TextView statusMessage;
    private ProgressBar progressBar;
    private List<TrainerVideo> videoList;
    private VideoAdapter videoAdapter;
    private RequestQueue requestQueue;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sessions");
        toolbar.setNavigationOnClickListener(v -> finish());

        videosRecyclerView = findViewById(R.id.videosRecyclerView);
        statusMessage = findViewById(R.id.statusMessage);
        progressBar = findViewById(R.id.progressBar);

        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        videoList = new ArrayList<>();
        videoAdapter = new VideoAdapter(videoList, this::playVideo);
        videosRecyclerView.setAdapter(videoAdapter);

        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("FitSyncPrefs", MODE_PRIVATE);
        username = prefs.getString("username", "User");
        Log.d(TAG, "Username: " + username);

        checkBookingStatus();
    }

    private void checkBookingStatus() {
        progressBar.setVisibility(View.VISIBLE);
        String url = "http://10.0.2.2:8000/api/check-booking/?username=" + username;
        Log.d(TAG, "Checking booking status with URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        Log.d(TAG, "Check booking response: " + response.toString());
                        if (!response.getBoolean("success")) {
                            Toast.makeText(this, response.getString("error"), Toast.LENGTH_SHORT).show();
                            statusMessage.setText("No booking found.");
                            statusMessage.setVisibility(View.VISIBLE);
                            return;
                        }

                        if (!response.getBoolean("has_booking")) {
                            statusMessage.setText("No booking found. Please book a trainer first.");
                            statusMessage.setVisibility(View.VISIBLE);
                            return;
                        }

                        JSONObject booking = response.getJSONObject("booking");
                        String status = booking.getString("status");
                        if (status.equals("Pending")) {
                            statusMessage.setText("Trainer not Approved you.");
                            statusMessage.setVisibility(View.VISIBLE);
                        } else if (status.equals("Approved")) {
                            String trainerEmail = booking.getString("trainer_email");
                            fetchTrainerVideos(trainerEmail);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                        Toast.makeText(this, "Error parsing booking response", Toast.LENGTH_SHORT).show();
                        statusMessage.setText("Error loading booking status.");
                        statusMessage.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error checking booking: " + error.toString(), error);
                    Toast.makeText(this, "Error checking booking: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    statusMessage.setText("Error checking booking status.");
                    statusMessage.setVisibility(View.VISIBLE);
                });

        requestQueue.add(request);
    }

    private void fetchTrainerVideos(String trainerEmail) {
        progressBar.setVisibility(View.VISIBLE);
        String url = "http://10.0.2.2:8000/api/get-trainer-videos/?trainer_email=" + trainerEmail;
        Log.d(TAG, "Fetching trainer videos with URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        Log.d(TAG, "Fetch trainer videos response: " + response.toString());
                        if (!response.getBoolean("success")) {
                            Toast.makeText(this, response.getString("error"), Toast.LENGTH_SHORT).show();
                            statusMessage.setText("No videos found for this trainer.");
                            statusMessage.setVisibility(View.VISIBLE);
                            return;
                        }

                        JSONArray videos = response.getJSONArray("videos");
                        videoList.clear();
                        for (int i = 0; i < videos.length(); i++) {
                            JSONObject video = videos.getJSONObject(i);
                            videoList.add(new TrainerVideo(
                                    video.getString("details"),
                                    video.getString("video_url"),
                                    video.getString("created_at")
                            ));
                        }
                        videoAdapter.notifyDataSetChanged();
                        videosRecyclerView.setVisibility(View.VISIBLE);
                        statusMessage.setVisibility(View.GONE);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing videos: " + e.getMessage(), e);
                        Toast.makeText(this, "Error parsing videos", Toast.LENGTH_SHORT).show();
                        statusMessage.setText("Error loading videos.");
                        statusMessage.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error fetching videos: " + error.toString(), error);
                    Toast.makeText(this, "Error fetching videos: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    statusMessage.setText("Error fetching videos.");
                    statusMessage.setVisibility(View.VISIBLE);
                });

        requestQueue.add(request);
    }

    private void playVideo(String videoUrl) {
        try {
            Uri videoUri = Uri.parse("http://10.0.2.2:8000" + videoUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(videoUri, "video/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error playing video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}