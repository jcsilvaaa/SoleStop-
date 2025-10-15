package com.mobdeve.s17.MC02;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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

        productList = new ArrayList<>();
        productList.add(new Product("Sneakers A", "$50", R.drawable.sneakers_a));
        productList.add(new Product("Sneakers B", "$70", R.drawable.sneakers_b));
        productList.add(new Product("Boots C", "$80", R.drawable.boots_c));
        productList.add(new Product("Running D", "$65", R.drawable.sneakers_a));

        adapter = new ProductAdapter(this, productList, product -> {
            Intent intent = new Intent(HomeActivity.this, ProductDetailsActivity.class);
            intent.putExtra("productName", product.getName());
            intent.putExtra("productPrice", product.getPrice());
            intent.putExtra("productImage", product.getImageResId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_profile:
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                    break;
                case R.id.nav_cart:
                    startActivity(new Intent(HomeActivity.this, CartActivity.class));
                    break;
                case R.id.nav_notifications:
                    startActivity(new Intent(HomeActivity.this, NotificationsActivity.class));
                    break;
            }
            return true;
        });
    }
}
