package com.mobdeve.s17.MC02;

public class Feedback {
    private String id;
    private String userId;
    private String username;
    private String text;
    private long rating;
    private long timestamp;

    public Feedback() { } // required for Firestore

    public Feedback(String id, String userId, String username, String text, long rating, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.text = text;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    // getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getRating() { return rating; }
    public void setRating(long rating) { this.rating = rating; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}