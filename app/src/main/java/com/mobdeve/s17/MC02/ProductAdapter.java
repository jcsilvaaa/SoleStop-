package com.mobdeve.s17.MC02;

import android.content.Context;
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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> productList;
    private Context context;
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;
    private String mode; // "home" OR "cart" OR "notifications"

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Interfaces
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

        holder.name.setText(product.getName());
        holder.price.setText(product.getPrice());
        holder.image.setImageResource(product.getImageResId());

        // -----------------------------
        // ITEM CLICK → Open Product Details ONLY
        // -----------------------------
        holder.itemView.setOnClickListener(v -> listener.onItemClick(product));

        // -----------------------------
        // ADD TO CART BUTTON
        // -----------------------------
        if (mode.equals("home")) {
            holder.addToCartBtn.setVisibility(View.VISIBLE);
            holder.addToCartBtn.setOnClickListener(v -> {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Map<String, Object> cartItem = new HashMap<>();
                cartItem.put("name", product.getName());
                cartItem.put("price", product.getPrice());
                cartItem.put("imageResId", product.getImageResId());

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

        // -----------------------------
        // DELETE BUTTON (Cart / Notifications)
        // -----------------------------
        if (mode.equals("cart")) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setImageResource(R.drawable.ic_delete);
        } else if (mode.equals("notifications")) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setImageResource(R.drawable.ic_clear);
        } else {
            holder.deleteBtn.setVisibility(View.GONE);
        }

        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(holder.getAdapterPosition());
            }

            if (product.getFirestoreId() != null && mode.equals("cart")) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                db.collection("users")
                        .document(userId)
                        .collection("cart")
                        .document(product.getFirestoreId())
                        .delete()
                        .addOnSuccessListener(unused -> Toast.makeText(context, "Removed from cart!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // -----------------------------
    // UPDATE LIST (for CartActivity)
    // -----------------------------
    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price;
        ImageButton deleteBtn;
        Button addToCartBtn; // 🔥 new button

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.productImage);
            name = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            addToCartBtn = itemView.findViewById(R.id.addToCartBtn); // 🔥 initialize
        }
    }
}
