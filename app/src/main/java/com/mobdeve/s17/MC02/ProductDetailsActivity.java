package com.mobdeve.s17.MC02;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProductDetailsActivity extends AppCompatActivity {

    ImageView productImage;
    TextView productName, productPrice, productDescription;
    Button addToCartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        productImage = findViewById(R.id.productDetailImage);
        productName = findViewById(R.id.productDetailName);
        productPrice = findViewById(R.id.productDetailPrice);
        productDescription = findViewById(R.id.productDetailDescription);
        addToCartBtn = findViewById(R.id.addToCartBtn);

        // Set dummy data from Intent
        productName.setText(getIntent().getStringExtra("productName"));
        productPrice.setText(getIntent().getStringExtra("productPrice"));
        productImage.setImageResource(getIntent().getIntExtra("productImage", R.drawable.sneakers_a));
        productDescription.setText("High-quality sneakers for everyday comfort.");

        addToCartBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProductDetailsActivity.this, CartActivity.class));
        });
    }
}
