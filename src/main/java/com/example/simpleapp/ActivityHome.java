package com.example.simpleapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;  // Import SetOptions

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityHome extends AppCompatActivity {

    private Button packageButton, electiveButton, logoutButton;
    private ListView courseListView;

    private FirebaseFirestore db;
    private String currentMenu = "package"; // Default menu
    private ArrayList<Map<String, Object>> courses = new ArrayList<>();
    private CourseAdapter adapter;

    private String userId;  // Store the current user's ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve userId passed from the previous activity
        userId = getIntent().getStringExtra("userId");

        // Initialize UI elements
        packageButton = findViewById(R.id.packageButton);
        electiveButton = findViewById(R.id.electiveButton);
        courseListView = findViewById(R.id.courseListView);
        logoutButton = findViewById(R.id.logoutButton);

        // Set up adapter
        adapter = new CourseAdapter(this, courses);
        courseListView.setAdapter(adapter);

        // Set click listeners for buttons
        packageButton.setOnClickListener(v -> loadCourses("package"));
        electiveButton.setOnClickListener(v -> loadCourses("elective"));
        logoutButton.setOnClickListener(v -> logout());

        // Set item click listener for ListView
        courseListView.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, Object> selectedCourse = courses.get(position);
            // Show a confirmation dialog
            showConfirmationDialog(selectedCourse);
        });

        // Load default menu (package)
        loadCourses("package");
    }

    private void loadCourses(String menu) {
        currentMenu = menu;
        courses.clear();
        adapter.notifyDataSetChanged();

        db.collection(menu)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> course = new HashMap<>();
                            course.put("Name", document.get("Name"));
                            String creditsString = document.getString("Credits");
                            if (creditsString != null) {
                                try {
                                    long credits = Long.parseLong(creditsString);
                                    course.put("Credits", credits);
                                } catch (NumberFormatException e) {
                                    course.put("Credits", 0L);
                                }
                            } else {
                                course.put("Credits", 0L);
                            }
                            courses.add(course);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ActivityHome.this, "Error loading courses", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showConfirmationDialog(Map<String, Object> selectedCourse) {
        new AlertDialog.Builder(this)
                .setTitle("Enroll in " + selectedCourse.get("Name"))
                .setMessage("Do you want to enroll in this course?")
                .setPositiveButton("Yes", (dialog, which) -> enrollCourse(selectedCourse))
                .setNegativeButton("No", null)
                .show();
    }

    private void enrollCourse(Map<String, Object> course) {
        if (userId == null) {
            Toast.makeText(ActivityHome.this, "User ID is not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the student's data
        db.collection("students").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> student = documentSnapshot.getData();
                        if (student != null) {
                            // Retrieve current credits
                            long currentCredits = (long) student.get("credits");

                            // Check if the total credits are already >= 24
                            if (currentCredits >= 24) {
                                Toast.makeText(ActivityHome.this, "You have reached the maximum allowed credits (24).", Toast.LENGTH_SHORT).show();
                                return; // Restrict further enrollment
                            }

                            // Check if the student is already enrolled in this specific type
                            String currentEnrollment = (String) student.get("enrollment");
                            if (currentEnrollment != null && !currentEnrollment.equals(currentMenu) && !currentEnrollment.isEmpty()) {
                                Toast.makeText(ActivityHome.this, "You can only enroll in " + currentEnrollment + " subjects.", Toast.LENGTH_SHORT).show();
                                return; // Prevent enrollment in a different type
                            }

                            // Increment credits
                            long courseCredits = (long) course.get("Credits");
                            student.put("credits", currentCredits + courseCredits);

                            // Add the course to the student's subjects (array)
                            ArrayList<String> subjects = (ArrayList<String>) student.get("subjects");
                            if (subjects == null) {
                                subjects = new ArrayList<>();
                            }
                            if (subjects.contains(course.get("Name").toString())) {
                                Toast.makeText(ActivityHome.this, "You are already enrolled in this course.", Toast.LENGTH_SHORT).show();
                                return; // Prevent duplicate enrollment
                            }
                            subjects.add(course.get("Name").toString());
                            student.put("subjects", subjects);

                            // Set the enrollment type (package or elective)
                            student.put("enrollment", currentMenu); // "package" or "elective"

                            // Update the student document in Firestore
                            db.collection("students").document(userId)
                                    .set(student, SetOptions.merge()) // Merge to avoid overwriting other fields
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(ActivityHome.this, "Enrolled in " + course.get("Name"), Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ActivityHome.this, "Error enrolling: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // If the student document doesn't exist
                        Toast.makeText(ActivityHome.this, "Student document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ActivityHome.this, "Error retrieving student data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Logout function
    private void logout() {
        // Clear any session or user data (use SharedPreferences or similar approach)
        // Redirect to login screen
        Intent intent = new Intent(ActivityHome.this, ActivityLogin.class);
        startActivity(intent);
        finish();  // Finish the current activity so the user cannot go back to it using the back button
    }
}