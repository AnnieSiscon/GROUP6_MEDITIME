package com.example.meditime;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    public String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                loginUser(username, password);
            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent( LoginActivity.this, SignupActivity.class));
            }
        });
    }

    private void loginUser(final String username, final String password) {
        if (TextUtils.isEmpty(username)) {
            Toast.makeText( LoginActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText( LoginActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the username exists in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("your_shared_preference_name", Context.MODE_PRIVATE);
        String existingUsername = sharedPreferences.getString("username", null);

        if (existingUsername != null) {
            // Delete the old username from SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("username");
            editor.apply();
        }

        // Get a reference to the Firebase Realtime Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        // Get the user's password from the Firebase database
        DatabaseReference userRef = databaseRef.child("users").child(username);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String savedPassword = dataSnapshot.getValue(String.class);

                if (savedPassword != null && savedPassword.equals(password)) {
                    // Save the username to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);
                    editor.apply();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    // TODO: Handle successful login (navigate to the desired activity)
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Failed to read user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}