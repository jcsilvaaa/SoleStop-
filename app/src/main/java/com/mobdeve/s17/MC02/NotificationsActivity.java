package com.mobdeve.s17.MC02;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    RecyclerView notificationsRecyclerView;
    ProductAdapter notificationsAdapter;
    List<Product> notificationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

       
        notificationsList = new ArrayList<>();
        notificationsList.add(new Product("Your order #123 has been shipped", "", R.drawable.logo));
        notificationsList.add(new Product("50% off Sneakers A!", "", R.drawable.logo));
        notificationsList.add(new Product("New arrivals in your favorite brand", "", R.drawable.logo));

      
        notificationsAdapter = new ProductAdapter(this, notificationsList, product -> {}, "notifications");
        notificationsRecyclerView.setAdapter(notificationsAdapter);

      
        notificationsAdapter.setOnDeleteClickListener(position -> {
            notificationsList.remove(position);
            notificationsAdapter.notifyItemRemoved(position);
        });
    }
}
