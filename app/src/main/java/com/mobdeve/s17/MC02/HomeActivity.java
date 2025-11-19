package com.mobdeve.s17.MC02;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProductAdapter adapter;
    List<Product> productList;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.productRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Sample products
        productList = new ArrayList<>();
        productList.add(new Product("Sneakers A", "$50", R.drawable.sneakers_a));
        productList.add(new Product("Sneakers B", "$70", R.drawable.sneakers_b));
        productList.add(new Product("Boots C", "$80", R.drawable.boots_c));
        productList.add(new Product("Running D", "$65", R.drawable.sneakers_a));

        // Adapter → Item click only opens details
        adapter = new ProductAdapter(this, productList, product -> {
            // Open product details
            Intent intent = new Intent(HomeActivity.this, ProductDetailsActivity.class);
            intent.putExtra("productName", product.getName());
            intent.putExtra("productPrice", product.getPrice());
            intent.putExtra("productImage", product.getImageResId());
            startActivity(intent);
        }, "home");

        recyclerView.setAdapter(adapter);

        bottomNav = findViewById(R.id.bottomNav);

        Button loginHomeBtn = findViewById(R.id.loginHomeBtn);
        Button registerHomeBtn = findViewById(R.id.registerHomeBtn);
        Button logoutHomeBtn = findViewById(R.id.logoutHomeBtn);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loginHomeBtn.setVisibility(Button.GONE);
            registerHomeBtn.setVisibility(Button.GONE);
            logoutHomeBtn.setVisibility(Button.VISIBLE);
        } else {
            logoutHomeBtn.setVisibility(Button.GONE);
        }

        loginHomeBtn.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, LoginActivity.class)));
        registerHomeBtn.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, RegisterActivity.class)));

        logoutHomeBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(HomeActivity.this, CartActivity.class));
                return true;
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(HomeActivity.this, NotificationsActivity.class));
                return true;
            } else if (id == R.id.nav_home) {
                return true;
            }
            return false;
        });
    }
}
