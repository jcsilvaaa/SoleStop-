package com.mobdeve.s17.MC02;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CheckoutActivity extends AppCompatActivity {

    EditText shippingAddressInput;
    RadioGroup paymentOptions;
    Button confirmOrderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        shippingAddressInput = findViewById(R.id.shippingAddressInput);
        paymentOptions = findViewById(R.id.paymentOptions);
        confirmOrderBtn = findViewById(R.id.confirmOrderBtn);

        confirmOrderBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Order Confirmed!", Toast.LENGTH_SHORT).show();
            finish(); // Return to previous screen
        });
    }
}
