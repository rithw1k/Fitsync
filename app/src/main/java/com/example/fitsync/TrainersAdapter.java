package com.example.fitsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TrainersAdapter extends RecyclerView.Adapter<TrainersAdapter.TrainerViewHolder> {

    private List<Trainer> trainerList;
    private Consumer<Trainer> onBookClickListener;
    private Supplier<Boolean> hasBookingSupplier;
    private String bookingStatus; // To store the booking status

    public TrainersAdapter(List<Trainer> trainerList, Consumer<Trainer> onBookClickListener, Supplier<Boolean> hasBookingSupplier) {
        this.trainerList = trainerList;
        this.onBookClickListener = onBookClickListener;
        this.hasBookingSupplier = hasBookingSupplier;
    }

    // Method to update the booking status
    public void setBookingStatus(String status) {
        this.bookingStatus = status;
        notifyDataSetChanged();
    }

    @Override
    public TrainerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trainer, parent, false);
        return new TrainerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrainerViewHolder holder, int position) {
        Trainer trainer = trainerList.get(position);
        holder.nameText.setText(trainer.getName());
        holder.emailText.setText("Email: " + trainer.getEmail());
        holder.specialityText.setText("Speciality: " + trainer.getSpeciality());

        if (hasBookingSupplier.get()) {
            holder.bookButton.setVisibility(View.GONE);
            holder.statusText.setVisibility(View.VISIBLE);
            holder.statusText.setText("Status: " + (bookingStatus != null ? bookingStatus : "Pending"));
        } else {
            holder.bookButton.setVisibility(View.VISIBLE);
            holder.statusText.setVisibility(View.GONE);
            holder.bookButton.setOnClickListener(v -> onBookClickListener.accept(trainer));
        }
    }

    @Override
    public int getItemCount() {
        return trainerList.size();
    }

    static class TrainerViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText, specialityText, statusText;
        Button bookButton;

        TrainerViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            emailText = itemView.findViewById(R.id.emailText);
            specialityText = itemView.findViewById(R.id.specialityText);
            statusText = itemView.findViewById(R.id.statusText);
            bookButton = itemView.findViewById(R.id.bookButton);
        }
    }
}