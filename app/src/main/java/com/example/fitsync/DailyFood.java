package com.example.fitsync;

import java.util.List;

public class DailyFood {
    private int id;
    private String date;
    private String addedDetails;
    private List<String> comments;
    private String createdAt;

    public DailyFood(int id, String date, String addedDetails, List<String> comments, String createdAt) {
        this.id = id;
        this.date = date;
        this.addedDetails = addedDetails;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getDate() { return date; }
    public String getAddedDetails() { return addedDetails; }
    public List<String> getComments() { return comments; }
    public String getCreatedAt() { return createdAt; }
    public int getCommentsCount() { return comments != null ? comments.size() : 0; }
}