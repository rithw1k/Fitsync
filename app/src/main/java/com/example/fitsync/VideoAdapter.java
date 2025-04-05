package com.example.fitsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.function.Consumer;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<TrainerVideo> videoList;
    private Consumer<String> onPlayClickListener;

    public VideoAdapter(List<TrainerVideo> videoList, Consumer<String> onPlayClickListener) {
        this.videoList = videoList;
        this.onPlayClickListener = onPlayClickListener;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        TrainerVideo video = videoList.get(position);
        holder.detailsText.setText(video.getDetails());
        holder.createdAtText.setText("Uploaded: " + video.getCreatedAt());
        holder.playButton.setOnClickListener(v -> onPlayClickListener.accept(video.getVideoUrl()));
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView detailsText, createdAtText;
        Button playButton;

        VideoViewHolder(View itemView) {
            super(itemView);
            detailsText = itemView.findViewById(R.id.detailsText);
            createdAtText = itemView.findViewById(R.id.createdAtText);
            playButton = itemView.findViewById(R.id.playButton);
        }
    }
}