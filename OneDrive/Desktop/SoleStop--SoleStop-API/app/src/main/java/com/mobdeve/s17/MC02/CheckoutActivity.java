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

/**
 * Activity for handling checkout process including order confirmation, shipping address, and payment method.
 */
public class CheckoutActivity extends AppCompatActivity {

    private EditText shippingAddressInput;
    private RadioGroup paymentOptions;
    private Button confirmOrderBtn;
    private TextView orderTotalText;

    private List<Product> cartItems;
    private int totalAmount = 0;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initializeViews();
        initializeFirebase();
        setupToolbar();
        receiveCartItems();
        computeTotal();
        updateTotalUI();

        confirmOrderBtn.setOnClickListener(v -> placeOrder());
    }

    /**
     * Initializes UI elements.
     */
    private void initializeViews() {
        shippingAddressInput = findViewById(R.id.shippingAddressInput);
        paymentOptions = findViewById(R.id.paymentOptions);
        confirmOrderBtn = findViewById(R.id.confirmOrderBtn);
        orderTotalText = findViewById(R.id.orderTotalText);
    }

    /**
     * Initializes Firebase Firestore and current user ID.
     */
    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
     * Retrieves cart items passed from CartActivity.
     */
    private void receiveCartItems() {
        String cartJson = getIntent().getStringExtra("cart_items_json");
        if (cartJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Product>>() {}.getType();
            cartItems = gson.fromJson(cartJson, type);
        }
    }

    /**
     * Computes the total payment amount from the cart items.
     */
    private void computeTotal() {
        if (cartItems == null) return;
        totalAmount = 0;
        for (Product p : cartItems) {
            totalAmount += Integer.parseInt(p.getPrice().replace("$", ""));
        }
    }

    /**
     * Updates the UI with the total payment amount.
     */
    private void updateTotalUI() {
        orderTotalText.setText("Total Payment: $" + totalAmount);
    }

    /**
     * Validates input and places the order in Firestore.
     */
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

        String paymentMethod = getSelectedPaymentMethod();
        Order order = new Order(cartItems, shippingAddress, paymentMethod, totalAmount);

        db.collection("users")
                .document(userId)
                .collection("orders")
                .add(order)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Determines the selected payment method.
     *
     * @return Payment method as a string.
     */
    private String getSelectedPaymentMethod() {
        int selectedId = paymentOptions.getCheckedRadioButtonId();
        if (selectedId == R.id.codOption) return "Cash on Delivery";
        if (selectedId == R.id.creditCardOption) return "Credit Card";
        if (selectedId == R.id.ewalletOption) return "E-Wallet";
        return "Unknown";
    }
}
