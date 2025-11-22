package com.mobdeve.s17.MC02;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying and managing the user's shopping cart.
 */
public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private ProductAdapter cartAdapter;
    private List<Product> cartList;
    private TextView totalPrice;
    private Button checkoutBtn;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        setupToolbar();
        initializeViews();
        initializeFirebase();
        setupRecyclerView();
        setupCheckoutButton();

        loadCartFromFirestore();
    }

    /**
     * Configures the toolbar with back navigation.
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
     * Initializes UI elements.
     */
    private void initializeViews() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalPrice = findViewById(R.id.totalPrice);
        checkoutBtn = findViewById(R.id.checkoutBtn);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Initializes Firebase Firestore and retrieves the current user ID.
     */
    private void initializeFirebase() {
        cartList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    /**
     * Configures the RecyclerView adapter and delete action.
     */
    private void setupRecyclerView() {
        cartAdapter = new ProductAdapter(this, cartList, product -> {}, "cart");
        cartRecyclerView.setAdapter(cartAdapter);

        cartAdapter.setOnDeleteClickListener(position -> {
            Product product = cartList.get(position);
            cartList.remove(position);
            cartAdapter.notifyItemRemoved(position);
            updateTotal();

            if (product.getFirestoreId() != null) {
                db.collection("users")
                        .document(userId)
                        .collection("cart")
                        .document(product.getFirestoreId())
                        .delete();
            }
        });
    }

    /**
     * Configures the checkout button to start the checkout process.
     */
    private void setupCheckoutButton() {
        checkoutBtn.setOnClickListener(v -> goToCheckout());
    }

    /**
     * Loads cart items from Firestore for the current user.
     */
    private void loadCartFromFirestore() {
        cartList.clear();

        db.collection("users")
                .document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener((QuerySnapshot snapshot) -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String name = doc.getString("name");
                        String price = doc.getString("price");
                        Long imageRes = doc.getLong("imageResId");
                        String brand = doc.getString("brand");

                        Product product = new Product(name, price, imageRes != null ? imageRes.intValue() : 0);
                        product.setBrand(brand);
                        product.setFirestoreId(doc.getId());
                        cartList.add(product);
                    }

                    cartAdapter.notifyDataSetChanged();
                    updateTotal();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load cart: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Updates the total price displayed based on current cart contents.
     */
    private void updateTotal() {
        int total = 0;
        for (Product product : cartList) {
            total += Integer.parseInt(product.getPrice().replace("$", ""));
        }
        totalPrice.setText("Total: $" + total);
    }

    /**
     * Initiates checkout by converting the cart to JSON and launching CheckoutActivity.
     */
    private void goToCheckout() {
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        String cartJson = new Gson().toJson(cartList);
        Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
        intent.putExtra("cart_items_json", cartJson);
        startActivity(intent);
    }
}
