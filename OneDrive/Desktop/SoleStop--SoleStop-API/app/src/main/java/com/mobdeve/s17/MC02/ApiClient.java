package com.mobdeve.s17.MC02;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * API client to fetch product data from fakestoreapi.com
 */
public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "https://fakestoreapi.com/products";
    private static final OkHttpClient client = new OkHttpClient();

    public interface ApiCallback {
        void onSuccess(List<Product> products);
        void onFailure(String error);
    }

    /**
     * Fetches products from the API asynchronously.
     *
     * @param ctx Context (unused, included for consistency)
     * @param cb  Callback to handle success or failure
     */
    public static void fetchProducts(Context ctx, ApiCallback cb) {
        Request req = new Request.Builder().url(BASE_URL).build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API request failed", e);
                cb.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    cb.onFailure("HTTP " + response.code());
                    return;
                }

                String body = response.body().string();
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<FakeStoreProduct>>() {}.getType();
                    List<FakeStoreProduct> apiProducts = gson.fromJson(body, listType);

                    List<Product> mappedProducts = new ArrayList<>();
                    for (FakeStoreProduct f : apiProducts) {
                        Product p = new Product();
                        p.setName(f.title != null ? f.title : "Unnamed");
                        p.setPrice("$" + (f.price != null ? Math.round(f.price) : "0"));
                        p.setImageResId(0); // 0 indicates remote image
                        p.setDescription(f.description != null ? f.description : "");
                        p.setImageUrl(f.image);
                        p.setBrand(detectBrand(f.title));
                        mappedProducts.add(p);
                    }

                    cb.onSuccess(mappedProducts);
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to parse JSON", ex);
                    cb.onFailure(ex.getMessage());
                }
            }
        });
    }

    /**
     * Attempts to detect brand based on product title.
     *
     * @param title Product title
     * @return Brand name or "Other" if no match
     */
    private static String detectBrand(String title) {
        if (title == null) return "Other";
        String t = title.toLowerCase();
        if (t.contains("nike")) return "Nike";
        if (t.contains("adidas")) return "Adidas";
        if (t.contains("puma")) return "PUMA";
        if (t.contains("new balance") || t.contains("newbalance")) return "New Balance";
        if (t.contains("onitsuka") || t.contains("tiger")) return "Onitsuka";
        return "Other";
    }

    /**
     * Internal helper class representing the JSON structure from fakestoreapi.com
     */
    private static class FakeStoreProduct {
        Integer id;
        String title;
        Double price;
        String description;
        String category;
        String image;
    }
}
