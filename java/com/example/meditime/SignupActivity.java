package com.example.meditime;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private TextView loginTextView;
    private EditText usernameEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.password2);
        registerButton = findViewById(R.id.register);
        loginTextView = findViewById(R.id.login);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                registerUser(username, password, confirmPassword);
            }
        });
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });

    }



    private void registerUser(String username, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(SignupActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(SignupActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get a reference to the Firebase Realtime Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        // Create a child node under "users" with the username as the key
        DatabaseReference userRef = databaseRef.child("users").child(username);

        // Save the password as the value of the child node
        userRef.setValue(password)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignupActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(SignupActivity.this, "Failed to register: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
