package com.mobdeve.s17.MC02;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    EditText shippingAddressInput;
    RadioGroup paymentOptions;
    Button confirmOrderBtn;
    TextView orderTotalText;

    List<Product> cartItems; // items passed from CartActivity
    int totalAmount = 0;

    FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        shippingAddressInput = findViewById(R.id.shippingAddressInput);
        paymentOptions = findViewById(R.id.paymentOptions);
        confirmOrderBtn = findViewById(R.id.confirmOrderBtn);
        orderTotalText = findViewById(R.id.orderTotalText);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupToolbar();
        receiveCartItems();
        computeTotal();
        updateTotalUI();

        confirmOrderBtn.setOnClickListener(v -> placeOrder());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void receiveCartItems() {
        String cartJson = getIntent().getStringExtra("cart_items_json");
        if (cartJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Product>>(){}.getType();
            cartItems = gson.fromJson(cartJson, type);
        }
    }

    private void computeTotal() {
        if (cartItems == null) return;
        totalAmount = 0;
        for (Product p : cartItems) {
            totalAmount += Integer.parseInt(p.getPrice().replace("$", ""));
        }
    }

    private void updateTotalUI() {
        orderTotalText.setText("Total Payment: $" + totalAmount);
    }

    private void placeOrder() {
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        String shippingAddress = shippingAddressInput.getText().toString().trim();
        if (shippingAddress.isEmpty()) {
            shippingAddressInput.setError("Enter shipping address");
            return;
        }

        // Payment method
        String paymentMethod = "Unknown";
        int selectedId = paymentOptions.getCheckedRadioButtonId();
        if (selectedId == R.id.codOption) paymentMethod = "Cash on Delivery";
        else if (selectedId == R.id.creditCardOption) paymentMethod = "Credit Card";
        else if (selectedId == R.id.ewalletOption) paymentMethod = "E-Wallet";

        // Create order
        Order order = new Order(cartItems, shippingAddress, paymentMethod, totalAmount);

        db.collection("users")
                .document(userId)
                .collection("orders")
                .add(order)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // close checkout
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
