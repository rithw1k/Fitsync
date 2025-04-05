package com.example.fitsync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class FoodHistoryAdapter extends RecyclerView.Adapter<FoodHistoryAdapter.FoodViewHolder> {

    private List<DailyFood> foodList;
    private Context context;
    private Consumer<List<String>> onItemClickListener;

    public FoodHistoryAdapter(List<DailyFood> foodList, Context context, Consumer<List<String>> onItemClickListener) {
        this.foodList = foodList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_log, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FoodViewHolder holder, int position) {
        DailyFood food = foodList.get(position);

        // Format date
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = isoFormat.parse(food.getDate());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.dateText.setText("Date: " + displayFormat.format(date));
        } catch (Exception e) {
            holder.dateText.setText("Date: " + food.getDate());
        }

        // Format time from created_at
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC")); // Ensure parsing as UTC
            Date createdAt = isoFormat.parse(food.getCreatedAt());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            holder.timeText.setText("Time: " + timeFormat.format(createdAt));
        } catch (Exception e) {
            holder.timeText.setText("Time: Unknown");
            e.printStackTrace(); // Log the error for debugging
        }

        holder.detailsText.setText("Details: " + food.getAddedDetails());
        holder.commentsCountText.setText("Comments: " + food.getCommentsCount());

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.accept(food.getComments());
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, timeText, detailsText, commentsCountText;

        FoodViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            detailsText = itemView.findViewById(R.id.detailsText);
            commentsCountText = itemView.findViewById(R.id.commentsCountText);
        }
    }
}