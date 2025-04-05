package com.example.fitsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Check SharedPreferences for logged-in user
        SharedPreferences prefs = getSharedPreferences("FitSyncPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        // Delay for splash screen, then navigate
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isLoggedIn) {
                // Go to MainActivity (Home Page)
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // Go to LoginActivity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish(); // Close SplashActivity
        }, SPLASH_DELAY);
    }
}
