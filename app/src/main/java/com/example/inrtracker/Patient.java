package com.example.inrtracker;

public class Patient {

    private int id;  // Aggiungi questo campo
    private String name;
    private String dosage;  // Consideriamo il dosaggio come stringa, può essere modificato a seconda dei requisiti

    // Modifica il costruttore per includere l'id
    public Patient(int id, String name) {
        this.id = id;
        this.name = name;
        this.dosage = "N/A";  // Imposta un valore predefinito per il dosaggio
    }

    // Se non vuoi sempre passare l'id, puoi avere un costruttore senza id per la creazione
    public Patient(String name) {
        this.name = name;
        this.dosage = "N/A";  // Imposta un valore predefinito per il dosaggio
    }

    // Aggiungi il metodo getter per l'id
    public int getId() {
        return id;
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
