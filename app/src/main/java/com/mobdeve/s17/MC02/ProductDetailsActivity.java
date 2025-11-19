package com.mobdeve.s17.MC02;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProductDetailsActivity extends AppCompatActivity {

    ImageView productImage;
    TextView productName, productPrice, productDescription;
    Button addToCartBtn;

    FirebaseFirestore db;
    String userId;

    Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        productImage = findViewById(R.id.productDetailImage);
        productName = findViewById(R.id.productDetailName);
        productPrice = findViewById(R.id.productDetailPrice);
        productDescription = findViewById(R.id.productDetailDescription);
        addToCartBtn = findViewById(R.id.addToCartBtn);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        // Create a Product object from intent
        String name = getIntent().getStringExtra("productName");
        String price = getIntent().getStringExtra("productPrice");
        int imageResId = getIntent().getIntExtra("productImage", R.drawable.sneakers_a);

        currentProduct = new Product(name, price, imageResId);

        // Set UI
        productName.setText(name);
        productPrice.setText(price);
        productImage.setImageResource(imageResId);
        productDescription.setText("High-quality sneakers for everyday comfort.");

        // Add to Cart button
        addToCartBtn.setOnClickListener(v -> {
            if (userId == null) {
                Toast.makeText(this, "Please log in to add to cart", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("name", currentProduct.getName());
            cartItem.put("price", currentProduct.getPrice());
            cartItem.put("imageResId", currentProduct.getImageResId());

            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .add(cartItem)
                    .addOnSuccessListener(docRef -> {
                        currentProduct.setFirestoreId(docRef.getId()); // 🔥 store Firestore ID
                        Toast.makeText(this, currentProduct.getName() + " added to cart", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }
}
