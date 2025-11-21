package com.mobdeve.s17.MC02;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderNumber.setText("Order #" + (position + 1));
        holder.orderTotal.setText("Total: $" + order.getTotalAmount());
        holder.orderPayment.setText("Payment Method: " + order.getPaymentMethod());
        holder.orderShipping.setText("Shipping Address: " + order.getShippingAddress());

        StringBuilder productsText = new StringBuilder("Products: ");
        for (Product p : order.getItems()) {
            productsText.append(p.getName()).append(" ($").append(p.getPrice().replace("$","")).append("), ");
        }
        if (productsText.length() > 0) productsText.setLength(productsText.length() - 2);
        holder.orderProducts.setText(productsText.toString());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderNumber, orderTotal, orderPayment, orderShipping, orderProducts;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNumber = itemView.findViewById(R.id.orderNumber);
            orderTotal = itemView.findViewById(R.id.orderTotal);
            orderPayment = itemView.findViewById(R.id.orderPayment);
            orderShipping = itemView.findViewById(R.id.orderShipping);
            orderProducts = itemView.findViewById(R.id.orderProducts);
        }
    }
}