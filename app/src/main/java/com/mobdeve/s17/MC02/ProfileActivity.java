package com.mobdeve.s17.MC02;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfileActivity extends AppCompatActivity {

    EditText nameProfileInput, emailProfileInput, addressProfileInput;
    Button saveProfileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // 

        nameProfileInput = findViewById(R.id.nameProfileInput);
        emailProfileInput = findViewById(R.id.emailProfileInput);
        addressProfileInput = findViewById(R.id.addressProfileInput);
        saveProfileBtn = findViewById(R.id.saveProfileBtn);

        saveProfileBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
        });
    }
}
