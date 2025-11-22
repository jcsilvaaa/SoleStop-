package com.mobdeve.s17.MC02;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;

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
    List<Product> filteredList;

    Spinner filterSpinner;
    SearchView searchView;
    Button mapsBtn;

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.productRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        searchView = findViewById(R.id.searchBar);
        filterSpinner = findViewById(R.id.spinnerBrand);
        mapsBtn = findViewById(R.id.mapsBtn);

        // Local product list (your existing hardcoded products)
        productList = new ArrayList<>();
        productList.add(new Product("Adidas Tokyo", "$50", R.drawable.adidas_tokyo, "Adidas"));
        productList.add(new Product("Adidas Gazelle", "$70", R.drawable.gazelle, "Adidas"));
        productList.add(new Product("New Balance 1906r", "$70", R.drawable.newbalance, "New Balance"));
        productList.add(new Product("New Balance X Miu Miu", "$70", R.drawable.new_balance, "New Balance"));
        productList.add(new Product("PUMA H-Street", "$80", R.drawable.puma, "PUMA"));
        productList.add(new Product("PUMA Speedcat", "$60", R.drawable.speedcat, "PUMA"));
        productList.add(new Product("Onitsuka Tiger Kill Bill", "$65", R.drawable.onitsuka, "Onitsuka"));
        productList.add(new Product("Onitsuka Tiger Tokuten", "$55", R.drawable.tokuten, "Onitsuka"));
        productList.add(new Product("Nike P6000", "$45", R.drawable.nike, "Nike"));
        productList.add(new Product("Nike Dunk Low", "$60", R.drawable.dunk, "Nike"));

        filteredList = new ArrayList<>(productList);

        // Spinner options
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All", "Adidas", "PUMA", "New Balance", "Onitsuka", "Nike", "Other"}
        );
        filterSpinner.setAdapter(spinAdapter);

        // Adapter (use filteredList initially)
        adapter = new ProductAdapter(this, filteredList, product -> {
            Intent intent = new Intent(HomeActivity.this, ProductDetailsActivity.class);
            intent.putExtra("productName", product.getName());
            intent.putExtra("productPrice", product.getPrice());
            intent.putExtra("productImage", product.getImageResId());
            // add description & imageUrl too
            intent.putExtra("productDesc", product.getDescription());
            intent.putExtra("productImageUrl", product.getImageUrl());
            startActivity(intent);
        }, "home");
        recyclerView.setAdapter(adapter);

        // Wire listeners
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String query) {
                filterProducts();
                return true;
            }
        });

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) { filterProducts(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        mapsBtn.setOnClickListener(v -> {
            // Open Google Maps search for sneaker stores near me
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=sneaker+store+near+me");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // If Google Maps not installed, open generic geo intent or browser search
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/sneaker+store+near+me"));
                startActivity(intent);
            }
        });

        // Bottom nav & auth UI
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
            }
            return id == R.id.nav_home;
        });

        // Finally: fetch API products and merge with local list
        fetchAndMergeApiProducts();
    }

    private void fetchAndMergeApiProducts() {
        ApiClient.fetchProducts(this, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                runOnUiThread(() -> {
                    // Merge: keep local products first, then add API ones
                    // but avoid duplicates by name
                    for (Product p : products) {
                        boolean exists = false;
                        for (Product local : productList) {
                            if (local.getName().equalsIgnoreCase(p.getName())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) productList.add(p);
                    }
                    // update filtered list and refresh UI
                    filteredList.clear();
                    filteredList.addAll(productList);
                    adapter.updateList(filteredList);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Failed to load online products: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void filterProducts() {
        String query = searchView.getQuery().toString().toLowerCase().trim();
        String brand = filterSpinner.getSelectedItem().toString();

        filteredList.clear();

        for (Product p : productList) {
            boolean matchesSearch = p.getName() != null && p.getName().toLowerCase().contains(query);
            boolean matchesBrand = "All".equals(brand) || (p.getBrand() != null && p.getBrand().equalsIgnoreCase(brand));
            if (matchesSearch && matchesBrand) filteredList.add(p);
        }
        adapter.updateList(filteredList);
    }
}