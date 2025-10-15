package com.mobdeve.s17.MC02;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    RecyclerView notificationsRecyclerView;
    ProductAdapter notificationsAdapter; // Can reuse ProductAdapter for dummy notifications
    List<Product> notificationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationsList = new ArrayList<>();
        notificationsList.add(new Product("Your order #123 has been shipped", "", R.drawable.logo));
        notificationsList.add(new Product("50% off Sneakers A!", "", R.drawable.logo));
        notificationsList.add(new Product("New arrivals in your favorite brand", "", R.drawable.logo));

        notificationsAdapter = new ProductAdapter(this, notificationsList, product -> {});
        notificationsRecyclerView.setAdapter(notificationsAdapter);
    }
}
