package com.example.fitsync;

public class Booking {
    private String trainerName;
    private String trainerEmail;
    private String speciality;
    private String status;

    public Booking(String trainerName, String trainerEmail, String speciality, String status) {
        this.trainerName = trainerName;
        this.trainerEmail = trainerEmail;
        this.speciality = speciality;
        this.status = status;
    }

    public String getTrainerName() { return trainerName; }
    public String getTrainerEmail() { return trainerEmail; }
    public String getSpeciality() { return speciality; }
    public String getStatus() { return status; }
}