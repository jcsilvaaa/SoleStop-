package com.mobdeve.s17.MC02;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    RecyclerView cartRecyclerView;
    ProductAdapter cartAdapter;
    List<Product> cartList;
    TextView totalPrice;
    Button checkoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Toolbar with back button
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
        cartList.add(new Product("Sneakers A", "$50", R.drawable.sneakers_a));
        cartList.add(new Product("Boots C", "$80", R.drawable.boots_c));

     
        cartAdapter = new ProductAdapter(this, cartList, product -> {}, "cart");
        cartRecyclerView.setAdapter(cartAdapter);

       
        cartAdapter.setOnDeleteClickListener(position -> {
            cartList.remove(position);
            cartAdapter.notifyItemRemoved(position);
            updateTotal();
        });

       
        updateTotal();

      
        checkoutBtn.setOnClickListener(v ->
                startActivity(new Intent(CartActivity.this, CheckoutActivity.class))
        );
    }

    private void updateTotal() {
        int total = 0;
        for (Product p : cartList) {
            total += Integer.parseInt(p.getPrice().replace("$", ""));
        }
        totalPrice.setText("Total: $" + total);
    }
}
