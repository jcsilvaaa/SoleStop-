package com.mobdeve.s17.MC02;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, addressInput;
    private Button saveBtn;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity_profile);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        // Inputs
        nameInput = findViewById(R.id.nameProfileInput);
        emailInput = findViewById(R.id.emailProfileInput);
        addressInput = findViewById(R.id.addressProfileInput);
        saveBtn = findViewById(R.id.saveProfileBtn);

        // Progress dialog
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setCancelable(false);

        // Load data from Intent first
        if (getIntent() != null) {
            String name = getIntent().getStringExtra("name");
            String email = getIntent().getStringExtra("email");
            String address = getIntent().getStringExtra("address");

            if (name != null) nameInput.setText(name);
            if (email != null) emailInput.setText(email);
            if (address != null) addressInput.setText(address);
        }

        // If no data from Intent, fetch from Firestore
        if (nameInput.getText().toString().isEmpty() &&
                emailInput.getText().toString().isEmpty() &&
                addressInput.getText().toString().isEmpty()) {
            loadUserData();
        }

        // Save button click
        saveBtn.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        loadingDialog.setMessage("Loading profile...");
        loadingDialog.show();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    loadingDialog.dismiss();
                    if (doc.exists()) {
                        nameInput.setText(doc.getString("name"));
                        emailInput.setText(doc.getString("email"));
                        addressInput.setText(doc.getString("address"));
                    }
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveProfile() {
        String newName = nameInput.getText().toString().trim();
        String newEmail = emailInput.getText().toString().trim();
        String newAddress = addressInput.getText().toString().trim();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Name and email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.setMessage("Saving...");
        loadingDialog.show();

        String uid = currentUser.getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("email", newEmail);
        updates.put("address", newAddress);

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (!newEmail.equals(currentUser.getEmail())) {
                        currentUser.updateEmail(newEmail)
                                .addOnSuccessListener(x -> {
                                    loadingDialog.dismiss();
                                    Toast.makeText(this, "Profile updated.", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    loadingDialog.dismiss();
                                    Toast.makeText(this, "Saved, but Auth email failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "Profile updated.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
