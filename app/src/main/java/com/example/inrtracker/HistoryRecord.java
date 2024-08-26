package com.example.inrtracker;

public class HistoryRecord {

    private int id;
    private String date;
    private String inr;
    private String dosage;

    public HistoryRecord(int id, String date, String inr, String dosage) {
        this.id = id;
        this.date = date;
        this.inr = inr;
        this.dosage = dosage;
    }

    public HistoryRecord(String date, String inr, String dosage) {
        this.date = date;
        this.inr = inr;
        this.dosage = dosage;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInr() {
        return inr;
    }

    public void setInr(String inr) {
        this.inr = inr;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
}
