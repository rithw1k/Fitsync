package com.example.fitsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        SharedPreferences prefs = getSharedPreferences("FitSyncPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Hi, " + username);

        // Trainers CardView
        findViewById(R.id.TrainersCard).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrainersActivity.class);
            startActivity(intent);
        });

        // Products CardView
        findViewById(R.id.productsCard).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProductsActivity.class);
            startActivity(intent);
        });

        // Orders CardView
        findViewById(R.id.OrdersCard).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OrdersActivity.class);
            startActivity(intent);
        });

        // Daily Food CardView
        findViewById(R.id.DailyCard).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DailyFoodActivity.class);
            startActivity(intent);
        });

        // Sessions CardView
        findViewById(R.id.SessionCard).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SessionsActivity.class);
            startActivity(intent);
        });

        // Reviews CardView
        findViewById(R.id.ReviewCard).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReviewsActivity.class);
            startActivity(intent);
        });

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}