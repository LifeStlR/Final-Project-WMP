package com.example.simpleapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ActivityLogin extends AppCompatActivity {

    private EditText userEditText, passwordEditText;
    private Button loginButton, registerButton; // Declare registerButton

    // Firebase Firestore instance
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        userEditText = findViewById(R.id.userEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton); // Initialize registerButton

        // Set click listener for the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Set click listener for the register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ActivityRegister when the Register button is clicked
                Intent intent = new Intent(ActivityLogin.this, ActivityRegister.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = userEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate input
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check Firestore for user credentials
        db.collection("users")
                .whereEqualTo("password", password)
                .whereEqualTo("username", email) // Replace "username" with "email" if that's your Firestore field name
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            // User authenticated successfully
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Toast.makeText(ActivityLogin.this, "Login successful", Toast.LENGTH_SHORT).show();
                                // Redirect to main activity
                                Intent intent = new Intent(ActivityLogin.this, ActivityHome.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(ActivityLogin.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ActivityLogin.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}