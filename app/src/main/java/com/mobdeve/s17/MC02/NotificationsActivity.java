package com.mobdeve.s17.MC02;

import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display notifications for the user, including simulated order status updates.
 */
public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView notificationsRecyclerView;
    private ProductAdapter notificationsAdapter;
    private List<Product> notificationsList;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        setupToolbar();
        initializeViews();
        initializeFirebase();
        setupRecyclerView();
        listenForNewOrders();
    }

    /**
     * Sets up the toolbar with back navigation.
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Initializes UI elements.
     */
    private void initializeViews() {
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Initializes Firestore and user ID.
     */
    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();
    }

    /**
     * Sets up the RecyclerView and adapter for notifications.
     */
    private void setupRecyclerView() {
        notificationsList = new ArrayList<>();
        notificationsAdapter = new ProductAdapter(this, notificationsList, p -> {}, "notifications");
        notificationsRecyclerView.setAdapter(notificationsAdapter);

        notificationsAdapter.setOnDeleteClickListener(position -> {
            notificationsList.remove(position);
            notificationsAdapter.notifyItemRemoved(position);
        });
    }

    /**
     * Listens for new orders in Firestore and triggers simulated notifications.
     */
    private void listenForNewOrders() {
        db.collection("users")
                .document(userId)
                .collection("orders")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;

                    for (DocumentChange dc : snapshot.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String orderId = dc.getDocument().getId();
                            simulateOrderStatusUpdates(orderId);
                        }
                    }
                });
    }

    /**
     * Simulates order status updates every 10 seconds for a given order.
     *
     * @param orderId The order ID to simulate notifications for.
     */
    private void simulateOrderStatusUpdates(String orderId) {
        String[] statuses = {
                "Order " + orderId + " is now PREPARING",
                "Order " + orderId + " has been SHIPPED",
                "Order " + orderId + " is OUT FOR DELIVERY",
                "Order " + orderId + " has been DELIVERED"
        };

        Handler handler = new Handler();

        for (int i = 0; i < statuses.length; i++) {
            int index = i;
            handler.postDelayed(() -> {
                notificationsList.add(0,
                        new Product(statuses[index], "", R.drawable.logo));

                notificationsAdapter.notifyItemInserted(0);
                notificationsRecyclerView.scrollToPosition(0);
            }, (i + 1) * 10000L); // 10 seconds interval
        }
    }
}
