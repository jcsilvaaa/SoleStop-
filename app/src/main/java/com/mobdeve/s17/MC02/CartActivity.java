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

public class CartActivity extends AppCompatActivity {

    RecyclerView cartRecyclerView;
    ProductAdapter cartAdapter;
    List<Product> cartList;
    TextView totalPrice;
    Button checkoutBtn;

    FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalPrice = findViewById(R.id.totalPrice);
        checkoutBtn = findViewById(R.id.checkoutBtn);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        cartList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        cartAdapter = new ProductAdapter(this, cartList, product -> {}, "cart");
        cartRecyclerView.setAdapter(cartAdapter);

        cartAdapter.setOnDeleteClickListener(position -> {
            Product product = cartList.get(position);

            // Remove from UI
            cartList.remove(position);
            cartAdapter.notifyItemRemoved(position);
            updateTotal();

            // Remove from Firestore
            if (product.getFirestoreId() != null) {
                db.collection("users")
                        .document(userId)
                        .collection("cart")
                        .document(product.getFirestoreId())
                        .delete();
            }
        });

        checkoutBtn.setOnClickListener(v -> goToCheckout());

        loadCartFromFirestore();
    }

    private void loadCartFromFirestore() {
        cartList.clear(); // clear old data

        db.collection("users")
                .document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener((QuerySnapshot snapshot) -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String name = doc.getString("name");
                        String price = doc.getString("price");
                        Long imageRes = doc.getLong("imageResId");
                        Product p = new Product(name, price, imageRes != null ? imageRes.intValue() : 0);
                        p.setFirestoreId(doc.getId());
                        cartList.add(p);
                    }

                    cartAdapter.notifyDataSetChanged();
                    updateTotal();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load cart: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void updateTotal() {
        int total = 0;
        for (Product p : cartList) {
            total += Integer.parseInt(p.getPrice().replace("$", ""));
        }
        totalPrice.setText("Total: $" + total);
    }

    private void goToCheckout() {
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert cart items to JSON
        Gson gson = new Gson();
        String cartJson = gson.toJson(cartList);

        Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
        intent.putExtra("cart_items_json", cartJson);
        startActivity(intent);
    }
}
