package com.mobdeve.s17.MC02;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.AdapterView;

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

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.productRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        searchView = findViewById(R.id.searchBar);
        filterSpinner = findViewById(R.id.spinnerBrand);

        // ✅ Importing your Product class
        // productList initialization
        productList = new ArrayList<>();
        productList.add(new Product("Adidas Tokyo", "$50", R.drawable.adidas_tokyo, "Adidas"));
        productList.add(new Product("New Balance X Miu Miu", "$70", R.drawable.new_balance, "New Balance"));
        productList.add(new Product("PUMA H-Street", "$80", R.drawable.puma, "PUMA"));
        productList.add(new Product("Onitsuka Tiger Kill Bill", "$65", R.drawable.onitsuka, "Onitsuka"));

        filteredList = new ArrayList<>(productList);

        // Brand filter options
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All", "Adidas", "PUMA", "New Balance", "Onitsuka"}
        );
        filterSpinner.setAdapter(spinAdapter);

        // Adapter
        adapter = new ProductAdapter(this, filteredList, product -> {
            Intent intent = new Intent(HomeActivity.this, ProductDetailsActivity.class);
            intent.putExtra("productName", product.getName());
            intent.putExtra("productPrice", product.getPrice());
            intent.putExtra("productImage", product.getImageResId());
            startActivity(intent);
        }, "home");

        recyclerView.setAdapter(adapter);

        // SEARCH LISTENER
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String query) {
                filterProducts();
                return true;
            }
        });

        // BRAND FILTER LISTENER
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                filterProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        bottomNav = findViewById(R.id.bottomNav);

        Button loginHomeBtn = findViewById(R.id.loginHomeBtn);
        Button registerHomeBtn = findViewById(R.id.registerHomeBtn);
        Button logoutHomeBtn = findViewById(R.id.logoutHomeBtn);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loginHomeBtn.setVisibility(View.GONE);
            registerHomeBtn.setVisibility(View.GONE);
            logoutHomeBtn.setVisibility(View.VISIBLE);
        } else {
            logoutHomeBtn.setVisibility(View.GONE);
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
    }

    // Filtering Logic (brand + search)
    private void filterProducts() {
        String query = searchView.getQuery().toString().toLowerCase();
        String brand = filterSpinner.getSelectedItem().toString();

        filteredList.clear();

        for (Product p : productList) {
            boolean matchesSearch = p.getName().toLowerCase().contains(query);
            boolean matchesBrand = brand.equals("All") || p.getBrand().equalsIgnoreCase(brand);

            if (matchesSearch && matchesBrand) {
                filteredList.add(p);
            }
        }

        adapter.updateList(filteredList);
    }
}