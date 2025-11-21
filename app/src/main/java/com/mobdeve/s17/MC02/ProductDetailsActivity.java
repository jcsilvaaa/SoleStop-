package com.mobdeve.s17.MC02;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class ProductDetailsActivity extends AppCompatActivity {

    ImageView productImage;
    TextView productName, productPrice, productDescription;
    Button addToCartBtn;

    // feedback UI
    RecyclerView feedbackRecycler;
    EditText feedbackInput;
    Button postFeedbackBtn;
    FeedbackAdapter feedbackAdapter;
    List<Feedback> feedbackList = new ArrayList<>();

    FirebaseFirestore db;
    String userId;
    String userFullName;
    String productId; // derived from product name

    Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        productImage = findViewById(R.id.productDetailImage);
        productName = findViewById(R.id.productDetailName);
        productPrice = findViewById(R.id.productDetailPrice);
        productDescription = findViewById(R.id.productDetailDescription);
        addToCartBtn = findViewById(R.id.addToCartBtn);

        // Feedback views (IDs from updated layout)
        feedbackRecycler = findViewById(R.id.feedbackRecycler);
        feedbackInput = findViewById(R.id.feedbackInput);
        postFeedbackBtn = findViewById(R.id.postFeedbackBtn);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        userFullName = null;

        // Build product object & id
        String name = getIntent().getStringExtra("productName");
        String price = getIntent().getStringExtra("productPrice");
        int imageResId = getIntent().getIntExtra("productImage", R.drawable.sneakers_a);

        currentProduct = new Product(name, price, imageResId);
        productId = name.trim().replaceAll("\\s+","_").toLowerCase();

        // set UI
        productName.setText(name);
        productPrice.setText(price);
        productImage.setImageResource(imageResId);
        productDescription.setText("High-quality sneakers for everyday comfort.");

        // get user full name (if logged in) for username in feedback
        if (userId != null) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) userFullName = doc.getString("name");
                    });
        }

        // setup feedback RecyclerView
        feedbackAdapter = new FeedbackAdapter(feedbackList, userId, new FeedbackAdapter.Listener() {
            @Override
            public void onEdit(Feedback feedback) {
                showEditDialog(feedback);
            }

            @Override
            public void onDelete(Feedback feedback) {
                confirmDelete(feedback);
            }
        });
        feedbackRecycler.setLayoutManager(new LinearLayoutManager(this));
        feedbackRecycler.setAdapter(feedbackAdapter);

        // load feedback in real-time
        listenToFeedback();

        // post feedback button
        postFeedbackBtn.setOnClickListener(v -> {
            if (userId == null) {
                Toast.makeText(this, "Please log in to post feedback", Toast.LENGTH_SHORT).show();
                return;
            }
            String text = feedbackInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Please write something", Toast.LENGTH_SHORT).show();
                return;
            }
            // Ask for rating via dialog
            showRatingDialogAndPost(text);
        });

        // Add to Cart (keeps your existing logic)
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
                        currentProduct.setFirestoreId(docRef.getId());
                        Toast.makeText(this, currentProduct.getName() + " added to cart", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void listenToFeedback() {
        CollectionReference fbCol = db.collection("products").document(productId).collection("feedback");
        fbCol.orderBy("timestamp").addSnapshotListener((@Nullable QuerySnapshot snapshots, @Nullable com.google.firebase.firestore.FirebaseFirestoreException e) -> {
            if (e != null) {
                return;
            }
            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                switch (dc.getType()) {
                    case ADDED:
                    case MODIFIED:
                    case REMOVED:
                        // rebuild list from snapshot for simplicity
                        List<Feedback> updated = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot ds : snapshots.getDocuments()) {
                            Feedback f = ds.toObject(Feedback.class);
                            if (f != null) {
                                f.setId(ds.getId());
                                updated.add(f);
                            }
                        }
                        feedbackList = updated;
                        feedbackAdapter.updateList(feedbackList);
                        break;
                }
            }
        });
    }

    private void showRatingDialogAndPost(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rate this product");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_rating_input, null);
        final RatingBar ratingBar = view.findViewById(R.id.dialogRatingBar);
        builder.setView(view);

        builder.setPositiveButton("Post", (dialog, which) -> {
            long rating = Math.round(ratingBar.getRating());
            postFeedback(text, rating);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void postFeedback(String text, long rating) {
        CollectionReference fbCol = db.collection("products").document(productId).collection("feedback");

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", userFullName == null ? "Anonymous" : userFullName);
        data.put("text", text);
        data.put("rating", rating);
        data.put("timestamp", System.currentTimeMillis());

        fbCol.add(data).addOnSuccessListener(doc -> {
            Toast.makeText(this, "Feedback posted", Toast.LENGTH_SHORT).show();
            feedbackInput.setText("");
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showEditDialog(Feedback feedback) {
        // dialog with EditText + RatingBar
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Feedback");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_feedback, null);
        final EditText input = view.findViewById(R.id.editFeedbackInput);
        final RatingBar ratingBar = view.findViewById(R.id.editFeedbackRatingBar);

        input.setText(feedback.getText());
        ratingBar.setRating(feedback.getRating());

        builder.setView(view);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newText = input.getText().toString().trim();
            long newRating = Math.round(ratingBar.getRating());
            updateFeedback(feedback.getId(), newText, newRating);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void updateFeedback(String feedbackId, String text, long rating) {
        DocumentReference docRef = db.collection("products").document(productId).collection("feedback").document(feedbackId);
        Map<String, Object> patch = new HashMap<>();
        patch.put("text", text);
        patch.put("rating", rating);
        patch.put("timestamp", System.currentTimeMillis());
        docRef.update(patch)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Feedback updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void confirmDelete(Feedback feedback) {
        new AlertDialog.Builder(this)
                .setTitle("Delete feedback")
                .setMessage("Are you sure you want to delete this feedback?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteFeedback(feedback.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteFeedback(String feedbackId) {
        db.collection("products").document(productId).collection("feedback").document(feedbackId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}