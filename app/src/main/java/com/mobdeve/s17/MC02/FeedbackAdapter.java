package com.mobdeve.s17.MC02;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.VH> {

    public interface Listener {
        void onEdit(Feedback feedback);
        void onDelete(Feedback feedback);
    }

    private List<Feedback> items;
    private String currentUserId;
    private Listener listener;

    public FeedbackAdapter(List<Feedback> items, String currentUserId, Listener listener) {
        this.items = items;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Feedback f = items.get(position);
        holder.username.setText(f.getUsername());
        holder.text.setText(f.getText());
        holder.ratingBar.setRating((float) f.getRating());

        // format timestamp
        String dateStr = "";
        try {
            dateStr = DateFormat.getDateTimeInstance().format(new Date(f.getTimestamp()));
        } catch (Exception ignored){ }
        holder.date.setText(dateStr);

        // show edit/delete only for owner
        if (currentUserId != null && currentUserId.equals(f.getUserId())) {
            holder.edit.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
        } else {
            holder.edit.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);
        }

        holder.edit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(f);
        });
        holder.delete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(f);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<Feedback> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView username, date, text, edit, delete;
        RatingBar ratingBar;
        VH(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.feedbackUsername);
            date = itemView.findViewById(R.id.feedbackDate);
            text = itemView.findViewById(R.id.feedbackText);
            ratingBar = itemView.findViewById(R.id.feedbackRating);
            edit = itemView.findViewById(R.id.editFeedbackBtn);
            delete = itemView.findViewById(R.id.deleteFeedbackBtn);
        }
    }
}