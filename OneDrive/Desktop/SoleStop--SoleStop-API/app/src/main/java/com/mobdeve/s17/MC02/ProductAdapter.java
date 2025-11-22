package com.mobdeve.s17.MC02;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecyclerView Adapter for displaying products in home, cart, or notifications mode.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> productList;
    private Context context;
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;
    private String mode; // "home", "cart", or "notifications"

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public ProductAdapter(Context context, List<Product> productList,
                          OnItemClickListener listener, String mode) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
        this.mode = mode;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // Display product info
        holder.name.setText(product.getName());
        holder.price.setText(product.getPrice());
        holder.brand.setText(product.getBrand() != null ? product.getBrand() : "Other");

        // Load image (prefer remote URL if available)
        setupImage(holder, product);

        // Item click opens product details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(product);
        });

        // Add to cart button (only for home mode)
        setupAddToCartButton(holder, product);

        // Delete button handling
        setupDeleteButton(holder, product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    private void setupImage(ViewHolder holder, Product product) {
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            // Placeholder: if using Glide, replace below with Glide loading
            holder.image.setImageResource(R.drawable.logo);
            holder.image.setOnClickListener(v -> {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(product.getImageUrl()));
                context.startActivity(browser);
            });
        } else if (product.getImageResId() != 0) {
            holder.image.setImageResource(product.getImageResId());
            holder.image.setOnClickListener(null);
        } else {
            holder.image.setImageResource(R.drawable.logo);
            holder.image.setOnClickListener(null);
        }
    }

    private void setupAddToCartButton(ViewHolder holder, Product product) {
        if ("home".equals(mode)) {
            holder.addToCartBtn.setVisibility(View.VISIBLE);
            holder.addToCartBtn.setOnClickListener(v -> {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(context, "Please log in to add to cart", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Map<String, Object> cartItem = new HashMap<>();
                cartItem.put("name", product.getName());
                cartItem.put("price", product.getPrice());
                cartItem.put("imageResId", product.getImageResId());
                cartItem.put("brand", product.getBrand());
                cartItem.put("imageUrl", product.getImageUrl());

                db.collection("users")
                        .document(userId)
                        .collection("cart")
                        .add(cartItem)
                        .addOnSuccessListener(docRef -> {
                            product.setFirestoreId(docRef.getId());
                            Toast.makeText(context, product.getName() + " added to cart", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        } else {
            holder.addToCartBtn.setVisibility(View.GONE);
        }
    }

    private void setupDeleteButton(ViewHolder holder, Product product) {
        if ("cart".equals(mode)) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setImageResource(R.drawable.ic_delete);
        } else if ("notifications".equals(mode)) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setImageResource(R.drawable.ic_clear);
        } else {
            holder.deleteBtn.setVisibility(View.GONE);
        }

        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(holder.getAdapterPosition());

            if (product.getFirestoreId() != null && "cart".equals(mode)) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                db.collection("users")
                        .document(userId)
                        .collection("cart")
                        .document(product.getFirestoreId())
                        .delete()
                        .addOnSuccessListener(unused ->
                                Toast.makeText(context, "Removed from cart!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price, brand;
        ImageButton deleteBtn;
        Button addToCartBtn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.productImage);
            name = itemView.findViewById(R.id.productName);
            brand = itemView.findViewById(R.id.productBrand);
            price = itemView.findViewById(R.id.productPrice);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            addToCartBtn = itemView.findViewById(R.id.addToCartBtn);
        }
    }
}
