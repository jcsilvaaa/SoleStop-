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

/**
 * Activity for editing the user's profile including name, email, and address.
 */
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

        setupToolbar();
        initializeFirebase();
        initializeViews();
        setupProgressDialog();
        populateInitialData();
        setupSaveButton();
    }

    /**
     * Sets up the toolbar with back navigation.
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Initializes Firebase authentication and Firestore instances.
     */
    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
    }

    /**
     * Initializes input fields and buttons.
     */
    private void initializeViews() {
        nameInput = findViewById(R.id.nameProfileInput);
        emailInput = findViewById(R.id.emailProfileInput);
        addressInput = findViewById(R.id.addressProfileInput);
        saveBtn = findViewById(R.id.saveProfileBtn);
    }

    /**
     * Configures the progress dialog.
     */
    private void setupProgressDialog() {
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setCancelable(false);
    }

    /**
     * Populates input fields with data passed via Intent or fetches from Firestore.
     */
    private void populateInitialData() {
        if (getIntent() != null) {
            String name = getIntent().getStringExtra("name");
            String email = getIntent().getStringExtra("email");
            String address = getIntent().getStringExtra("address");

            if (name != null) nameInput.setText(name);
            if (email != null) emailInput.setText(email);
            if (address != null) addressInput.setText(address);
        }

        if (nameInput.getText().toString().isEmpty() &&
                emailInput.getText().toString().isEmpty() &&
                addressInput.getText().toString().isEmpty()) {
            loadUserDataFromFirestore();
        }
    }

    /**
     * Sets up the save button click listener.
     */
    private void setupSaveButton() {
        saveBtn.setOnClickListener(v -> saveProfile());
    }

    /**
     * Fetches user profile data from Firestore and populates the input fields.
     */
    private void loadUserDataFromFirestore() {
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

    /**
     * Saves the updated profile to Firestore and updates FirebaseAuth email if changed.
     */
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
                .addOnSuccessListener(aVoid -> updateAuthEmailIfNeeded(newEmail))
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Updates the FirebaseAuth email if it has changed.
     *
     * @param newEmail The new email to update.
     */
    private void updateAuthEmailIfNeeded(String newEmail) {
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
    }
}
