package com.example.simpleapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;  // Add this import

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ActivityRegister extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, confirmPasswordEditText, fullnameEditText;
    private Button registerButton;

    // Firebase Firestore instance
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        usernameEditText = findViewById(R.id.nameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        fullnameEditText = findViewById(R.id.fullnameEditText); // Assuming you added this to the layout
        registerButton = findViewById(R.id.registerButton);

        // Set click listener for the register button
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String fullname = fullnameEditText.getText().toString().trim();

        // Validate input
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fullname.isEmpty()) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a user object
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);

        // Save user data to Firestore
        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    // Get the generated user ID
                    String userId = documentReference.getId();

                    // Add to students collection
                    Map<String, Object> student = new HashMap<>();
                    student.put("fullname", fullname);
                    student.put("enrollment", "");
                    student.put("subject", new ArrayList<String>());
                    student.put("credits", 0); // Initial credits
                    student.put("userId", userId);

                    db.collection("students")
                            .document(userId)
                            .set(student)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ActivityRegister.this, "Registration successful", Toast.LENGTH_SHORT).show();

                                // Redirect to ActivityHome
                                Intent intent = new Intent(ActivityRegister.this, ActivityHome.class);
                                intent.putExtra("userId", userId); // Pass user ID to the next activity
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ActivityRegister.this, "Error saving student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ActivityRegister.this, "Error registering user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}