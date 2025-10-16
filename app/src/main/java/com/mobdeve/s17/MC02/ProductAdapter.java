package com.mobdeve.s17.MC02;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> productList;
    private Context context;
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;
    private String mode; // "home", "cart", or "notifications"

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

        holder.itemView.setOnClickListener(v -> listener.onItemClick(product));

        // Control visibility of delete/clear button
        if (mode.equals("cart")) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setImageResource(R.drawable.ic_delete); // trash icon
        } else if (mode.equals("notifications")) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setImageResource(R.drawable.ic_clear); // clear icon
        } else {
            holder.deleteBtn.setVisibility(View.GONE);
        }

        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price;
        ImageButton deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.productImage);
            name = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}