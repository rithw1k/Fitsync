package com.example.fitsync;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private Dialog progressDialog;
    private boolean isRequestInProgress = false; // Flag to prevent multiple requests

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize progress dialog
        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText username = findViewById(R.id.username);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        Button signupButton = findViewById(R.id.signupButton);
        TextView loginLink = findViewById(R.id.loginLink);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2:8000/api/signup/";

        signupButton.setOnClickListener(v -> {
            if (isRequestInProgress) {
                Log.d(TAG, "Request already in progress, ignoring click");
                return; // Prevent multiple clicks
            }

            String user = username.getText().toString().trim();
            String mail = email.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (user.isEmpty() || mail.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            isRequestInProgress = true; // Set flag to true
            signupButton.setEnabled(false); // Disable button to prevent multiple clicks
            progressDialog.show();

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        progressDialog.dismiss();
                        isRequestInProgress = false; // Reset flag
                        signupButton.setEnabled(true); // Re-enable button

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            Log.d(TAG, "Signup Response: " + response);
                            if (jsonResponse.getBoolean("success")) {
                                Toast.makeText(this, "Signup Successful! Please log in.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, jsonResponse.getString("error"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        isRequestInProgress = false; // Reset flag
                        signupButton.setEnabled(true); // Re-enable button
                        Toast.makeText(this, "Network Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Volley Error: " + error.toString());
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("username", user);
                    params.put("email", mail);
                    params.put("password", pass);
                    Log.d(TAG, "Params: " + params.toString());
                    return params;
                }
            };
            queue.add(request);
        });

        loginLink.setOnClickListener(v -> finish());
    }
}