package com.example.meditime;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends AppCompatActivity {

    // Splash screen duration in milliseconds
    private static final int SPLASH_SCREEN_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Delayed execution of the next activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if the user is logged in
                if (isLoggedIn()) {
                    // User is logged in, start the MainActivity
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // User is not logged in, start the LoginActivity
                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

                // Finish the current activity
                finish();
            }
        }, SPLASH_SCREEN_DURATION);
    }

    // Check if the user is logged in
    private boolean isLoggedIn() {
        // Implement your logic to check if the user is logged in
        // Return true if the user is logged in, false otherwise
        // You can use SharedPreferences, database, or any other method to check the login status
        // For now, let's assume the user is not logged in
        return false;
    }
}
