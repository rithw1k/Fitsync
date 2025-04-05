package com.example.fitsync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class ReviewsActivity extends AppCompatActivity {

    private static final String TAG = "ReviewsActivity";
    private TextView statusMessage;
    private ProgressBar progressBar;
    private RatingBar ratingBar;
    private EditText commentEditText;
    private Button submitButton;
    private RequestQueue requestQueue;
    private String username;
    private String trainerEmail;
    private boolean hasReview = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Reviews");
        toolbar.setNavigationOnClickListener(v -> finish());

        statusMessage = findViewById(R.id.statusMessage);
        progressBar = findViewById(R.id.progressBar);
        ratingBar = findViewById(R.id.ratingBar);
        commentEditText = findViewById(R.id.commentEditText);
        submitButton = findViewById(R.id.submitButton);

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
                            trainerEmail = booking.getString("trainer_email");
                            Log.d(TAG, "Trainer email: " + trainerEmail);
                            fetchReview();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing booking response: " + e.getMessage(), e);
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

    private void fetchReview() {
        progressBar.setVisibility(View.VISIBLE);
        String url = "http://10.0.2.2:8000/api/get-review/?username=" + username + "&trainer_email=" + trainerEmail;
        Log.d(TAG, "Fetching review with URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        Log.d(TAG, "Fetch review response: " + response.toString());
                        if (!response.getBoolean("success")) {
                            Toast.makeText(this, response.getString("error"), Toast.LENGTH_SHORT).show();
                            statusMessage.setText("Error loading review.");
                            statusMessage.setVisibility(View.VISIBLE);
                            return;
                        }

                        hasReview = response.getBoolean("has_review");
                        Log.d(TAG, "Has review: " + hasReview);
                        if (hasReview) {
                            JSONObject review = response.getJSONObject("review");
                            ratingBar.setRating(review.getInt("rating"));
                            commentEditText.setText(review.getString("comment"));
                            submitButton.setText("Update Review");
                            Log.d(TAG, "Loaded existing review - Rating: " + review.getInt("rating") + ", Comment: " + review.getString("comment"));
                        } else {
                            ratingBar.setRating(0);
                            commentEditText.setText("");
                            submitButton.setText("Submit Review");
                            Log.d(TAG, "No review found, showing form to create a new review");
                        }

                        View reviewForm = findViewById(R.id.reviewForm);
                        reviewForm.setVisibility(View.VISIBLE);
                        statusMessage.setVisibility(View.GONE);
                        Log.d(TAG, "Review form visibility set to VISIBLE");
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing review: " + e.getMessage(), e);
                        Toast.makeText(this, "Error parsing review", Toast.LENGTH_SHORT).show();
                        statusMessage.setText("Error loading review.");
                        statusMessage.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error fetching review: " + error.toString(), error);
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(this, "Error fetching review: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    statusMessage.setText("Error fetching review.");
                    statusMessage.setVisibility(View.VISIBLE);
                });

        requestQueue.add(request);
    }

    public void submitReview(View view) {
        float rating = ratingBar.getRating();
        String comment = commentEditText.getText().toString().trim();

        if (rating < 1) {
            Toast.makeText(this, "Please provide a rating (1-5)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (comment.isEmpty()) {
            Toast.makeText(this, "Please provide a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String url = hasReview ? "http://10.0.2.2:8000/api/update-review/" : "http://10.0.2.2:8000/api/create-review/";
        Log.d(TAG, "Submitting review with URL: " + url);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            requestBody.put("trainer_email", trainerEmail);
            requestBody.put("rating", (int) rating);
            requestBody.put("comment", comment);
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
                        Log.d(TAG, "Submit review response: " + response.toString());
                        if (!response.getBoolean("success")) {
                            Toast.makeText(this, response.getString("error"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        fetchReview(); // Refresh the form after submission
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error submitting review: " + error.toString(), error);
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(this, "Error submitting review: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });

        requestQueue.add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}