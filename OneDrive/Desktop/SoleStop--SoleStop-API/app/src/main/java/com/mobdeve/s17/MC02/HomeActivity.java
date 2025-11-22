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

/**
 * Home activity displaying the product catalog with search, filtering, and navigation.
 */
public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private List<Product> filteredList;

    private Spinner filterSpinner;
    private SearchView searchView;
    private Button mapsBtn;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupProductList();
        setupFilterSpinner();
        setupRecyclerView();
        setupSearchAndFilter();
        setupMapsButton();
        setupBottomNavigation();
        fetchAndMergeApiProducts();
    }

    /**
     * Initializes UI components.
     */
    private void initializeViews() {
        recyclerView = findViewById(R.id.productRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        searchView = findViewById(R.id.searchBar);
        filterSpinner = findViewById(R.id.spinnerBrand);
        mapsBtn = findViewById(R.id.mapsBtn);
        bottomNav = findViewById(R.id.bottomNav);
    }

    /**
     * Sets up local product list and initializes the filtered list.
     */
    private void setupProductList() {
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
    }

    /**
     * Sets up the brand filter spinner.
     */
    private void setupFilterSpinner() {
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All", "Adidas", "PUMA", "New Balance", "Onitsuka", "Nike", "Other"}
        );
        filterSpinner.setAdapter(spinAdapter);
    }

    /**
     * Initializes the RecyclerView and adapter.
     */
    private void setupRecyclerView() {
        adapter = new ProductAdapter(this, filteredList, product -> {
            Intent intent = new Intent(HomeActivity.this, ProductDetailsActivity.class);
            intent.putExtra("productName", product.getName());
            intent.putExtra("productPrice", product.getPrice());
            intent.putExtra("productImage", product.getImageResId());
            intent.putExtra("productDesc", product.getDescription());
            intent.putExtra("productImageUrl", product.getImageUrl());
            startActivity(intent);
        }, "home");

        recyclerView.setAdapter(adapter);
    }

    /**
     * Configures search bar and brand filter listeners.
     */
    private void setupSearchAndFilter() {
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
    }

    /**
     * Filters products by search query and selected brand.
     */
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

    /**
     * Sets up the Google Maps button for finding nearby sneaker stores.
     */
    private void setupMapsButton() {
        mapsBtn.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=sneaker+store+near+me");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/sneaker+store+near+me"));
                startActivity(intent);
            }
        });
    }

    /**
     * Sets up bottom navigation buttons and login/logout logic.
     */
    private void setupBottomNavigation() {
        Button loginBtn = findViewById(R.id.loginHomeBtn);
        Button registerBtn = findViewById(R.id.registerHomeBtn);
        Button logoutBtn = findViewById(R.id.logoutHomeBtn);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loginBtn.setVisibility(Button.GONE);
            registerBtn.setVisibility(Button.GONE);
            logoutBtn.setVisibility(Button.VISIBLE);
        } else {
            logoutBtn.setVisibility(Button.GONE);
        }

        loginBtn.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, LoginActivity.class)));
        registerBtn.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, RegisterActivity.class)));
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            else if (id == R.id.nav_cart) startActivity(new Intent(HomeActivity.this, CartActivity.class));
            else if (id == R.id.nav_notifications) startActivity(new Intent(HomeActivity.this, NotificationsActivity.class));
            return id == R.id.nav_home;
        });
    }

    /**
     * Fetches online products from API and merges with local list, avoiding duplicates.
     */
    private void fetchAndMergeApiProducts() {
        ApiClient.fetchProducts(this, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                runOnUiThread(() -> {
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
                    filteredList.clear();
                    filteredList.addAll(productList);
                    adapter.updateList(filteredList);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(HomeActivity.this, "Failed to load online products: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
