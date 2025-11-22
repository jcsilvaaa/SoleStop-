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
 * Simple API client to fetch products from fakestoreapi.com
 */
public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "https://fakestoreapi.com/products";
    private static final OkHttpClient client = new OkHttpClient();

    public interface ApiCallback {
        void onSuccess(List<Product> products);
        void onFailure(String error);
    }

    public static void fetchProducts(Context ctx, ApiCallback cb) {
        Request req = new Request.Builder().url(BASE_URL).build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API call failed", e);
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

                    // Map to our Product class
                    List<Product> mapped = new ArrayList<>();
                    for (FakeStoreProduct f : apiProducts) {
                        String name = f.title != null ? f.title : "Unnamed";
                        // Format price as $xx (FakeStore delivers numeric price)
                        String price = "$" + (f.price != null ? Math.round(f.price) : "0");
                        // We don't have image resource ids for remote images; store image URL in imageResId = 0 and description in product field
                        Product p = new Product();
                        p.setName(name);
                        p.setPrice(price);
                        p.setImageResId(0); // use 0 to indicate remote image
                        p.setDescription(f.description != null ? f.description : "");
                        p.setImageUrl(f.image != null ? f.image : null); // new field in Product for remote images
                        p.setBrand(detectBrand(name));
                        mapped.add(p);
                    }

                    cb.onSuccess(mapped);
                } catch (Exception ex) {
                    Log.e(TAG, "JSON parse error", ex);
                    cb.onFailure(ex.getMessage());
                }
            }
        });
    }

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

    // internal helper matching the API JSON
    private static class FakeStoreProduct {
        Integer id;
        String title;
        Double price;
        String description;
        String category;
        String image;
    }
}
