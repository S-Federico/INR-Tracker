package com.example.inrtracker;

public class Patient {

    private String name;
    private String dosage; // Consideriamo il dosaggio come stringa, può essere modificato a seconda dei requisiti

    public Patient(String name) {
        this.name = name;
        this.dosage = "N/A";
    }

    public String getName() {
        return name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
}
