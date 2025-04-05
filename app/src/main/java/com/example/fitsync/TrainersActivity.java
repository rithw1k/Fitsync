package com.example.fitsync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class TrainersActivity extends AppCompatActivity {

    private static final String TAG = "TrainersActivity";
    private RecyclerView recyclerView;
    private TrainersAdapter adapter;
    private List<Trainer> trainerList;
    private ProgressBar progressBar;
    private TextView bookingStatus;
    private Button removeBookingButton;
    private RequestQueue requestQueue;
    private String username;
    private boolean hasBooking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainers);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Initialize views
        recyclerView = findViewById(R.id.trainersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        bookingStatus = findViewById(R.id.bookingStatus);
        removeBookingButton = findViewById(R.id.removeBookingButton);

        // Set the OnClickListener here to ensure it's always set
        removeBookingButton.setOnClickListener(v -> {
            Log.d(TAG, "Remove Booking button clicked");
            removeBooking();
        });

        // Get username from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("FitSyncPrefs", MODE_PRIVATE);
        username = prefs.getString("username", "User");
        Log.d(TAG, "Username: " + username);

        // Setup RecyclerView
        trainerList = new ArrayList<>();
        adapter = new TrainersAdapter(trainerList, this::bookTrainer, () -> hasBooking);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        // Initial check for existing booking
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
                            fetchTrainers();
                            return;
                        }

                        hasBooking = response.getBoolean("has_booking");
                        if (hasBooking) {
                            JSONObject booking = response.getJSONObject("booking");
                            String trainerName = booking.getString("trainer_name");
                            String trainerEmail = booking.getString("trainer_email");
                            String speciality = booking.getString("speciality");
                            String status = booking.getString("status");

                            bookingStatus.setText("You have booked:\n" +
                                    "Trainer: " + trainerName + "\n" +
                                    "Email: " + trainerEmail + "\n" +
                                    "Speciality: " + speciality + "\n" +
                                    "Status: " + status);
                            bookingStatus.setVisibility(View.VISIBLE);
                            removeBookingButton.setVisibility(View.VISIBLE);

                            // Show only the booked trainer
                            trainerList.clear();
                            trainerList.add(new Trainer(trainerName, trainerEmail, speciality));
                            adapter.setBookingStatus(status);
                            adapter.notifyDataSetChanged();
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            bookingStatus.setVisibility(View.GONE);
                            removeBookingButton.setVisibility(View.GONE);
                            fetchTrainers();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                        Toast.makeText(this, "Error parsing booking response", Toast.LENGTH_SHORT).show();
                        fetchTrainers();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error checking booking: " + error.toString(), error);
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(this, "Error checking booking: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    fetchTrainers();
                });

        requestQueue.add(request);
    }

    private void fetchTrainers() {
        progressBar.setVisibility(View.VISIBLE);
        String url = "http://10.0.2.2:8000/api/get-trainers/";
        Log.d(TAG, "Fetching trainers with URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        Log.d(TAG, "Fetch trainers response: " + response.toString());
                        if (!response.getBoolean("success")) {
                            Toast.makeText(this, response.getString("error"), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray trainers = response.getJSONArray("trainers");
                        trainerList.clear();
                        for (int i = 0; i < trainers.length(); i++) {
                            JSONObject trainer = trainers.getJSONObject(i);
                            trainerList.add(new Trainer(
                                    trainer.getString("name"),
                                    trainer.getString("email"),
                                    trainer.getString("speciality")
                            ));
                        }
                        adapter.setBookingStatus(null);
                        adapter.notifyDataSetChanged();
                        recyclerView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing trainers: " + e.getMessage(), e);
                        Toast.makeText(this, "Error parsing trainers", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error fetching trainers: " + error.toString(), error);
                    Toast.makeText(this, "Error fetching trainers: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });

        requestQueue.add(request);
    }

    private void bookTrainer(Trainer trainer) {
        progressBar.setVisibility(View.VISIBLE);
        String url = "http://10.0.2.2:8000/api/create-booking/";
        Log.d(TAG, "Booking trainer with URL: " + url);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            requestBody.put("trainer_name", trainer.getName());
            requestBody.put("trainer_email", trainer.getEmail());
            requestBody.put("speciality", trainer.getSpeciality());
        } catch (Exception e) {
            Log.e(TAG, "Error creating request: " + e.getMessage(), e);
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        Log.d(TAG, "Book trainer response: " + response.toString());
                        if (!response.getBoolean("success")) {
                            Toast.makeText(this, response.getString("error"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(this, "Trainer booked successfully!", Toast.LENGTH_SHORT).show();
                        checkBookingStatus();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing booking response: " + e.getMessage(), e);
                        Toast.makeText(this, "Error parsing booking response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error booking trainer: " + error.toString(), error);
                    Toast.makeText(this, "Error booking trainer: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });

        requestQueue.add(request);
    }

    private void removeBooking() {
        Log.d(TAG, "removeBooking method called");
        progressBar.setVisibility(View.VISIBLE);
        String url = "http://10.0.2.2:8000/api/delete-booking/";
        Log.d(TAG, "Removing booking with URL: " + url);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            Log.d(TAG, "Request body: " + requestBody.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error creating request body: " + e.getMessage(), e);
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        Log.d(TAG, "Remove booking response: " + response.toString());
                        if (!response.getBoolean("success")) {
                            Toast.makeText(this, response.getString("error"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(this, "Booking removed successfully!", Toast.LENGTH_SHORT).show();
                        hasBooking = false;
                        bookingStatus.setVisibility(View.GONE);
                        removeBookingButton.setVisibility(View.GONE);
                        fetchTrainers();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing remove booking response: " + e.getMessage(), e);
                        Toast.makeText(this, "Error parsing remove booking response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error removing booking: " + error.toString(), error);
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(this, "Error removing booking: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });

        Log.d(TAG, "Adding remove booking request to queue");
        requestQueue.add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}