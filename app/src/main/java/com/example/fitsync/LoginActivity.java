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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private Dialog progressDialog; // Progress dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize progress dialog
        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);
        TextView signupLink = findViewById(R.id.signupLink);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2:8000/api/login/";

        loginButton.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show progress dialog
            progressDialog.show();

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        // Hide progress dialog
                        progressDialog.dismiss();

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getBoolean("success")) {
                                SharedPreferences prefs = getSharedPreferences("FitSyncPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("isLoggedIn", true);
                                editor.putString("username", user);
                                editor.apply();

                                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
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
                        // Hide progress dialog on error
                        progressDialog.dismiss();
                        Toast.makeText(this, "Network Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Volley Error: " + error.toString());
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("username", user);
                    params.put("password", pass);
                    Log.d(TAG, "Params: " + params.toString());
                    return params;
                }
            };
            queue.add(request);
        });

        signupLink.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }
}