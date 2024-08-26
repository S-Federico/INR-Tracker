// File: java/com/tuo/package/name/HistoryRecord.java
package com.example.inrtracker;

public class HistoryRecord {

    private String date;
    private String inr;
    private String dosage;

    public HistoryRecord(String date, String inr, String dosage) {
        this.date = date;
        this.inr = inr;
        this.dosage = dosage;
    }

    public String getDate() {
        return date;
    }

    public String getInr() {
        return inr;
    }

    public String getDosage() {
        return dosage;
    }
}
