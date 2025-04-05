package com.example.fitsync;

public class TrainerVideo {
    private String details;
    private String videoUrl;
    private String createdAt;

    public TrainerVideo(String details, String videoUrl, String createdAt) {
        this.details = details;
        this.videoUrl = videoUrl;
        this.createdAt = createdAt;
    }

    public String getDetails() { return details; }
    public String getVideoUrl() { return videoUrl; }
    public String getCreatedAt() { return createdAt; }
}