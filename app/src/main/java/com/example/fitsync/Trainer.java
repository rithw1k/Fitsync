package com.example.fitsync;

public class Trainer {
    private String name;
    private String email;
    private String speciality;

    public Trainer(String name, String email, String speciality) {
        this.name = name;
        this.email = email;
        this.speciality = speciality;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getSpeciality() { return speciality; }
}