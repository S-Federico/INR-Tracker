// File: java/com/example/inrtracker/DatabaseHelper.java
package com.example.inrtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String COLUMN_MAINTENANCE_DOSAGE = "maintenance_dosage";

    private static final String COLUMN_DAYS_TO_TARGET = "days_to_target";

    private static final String DATABASE_NAME = "inrtracker.db";
    private static final int DATABASE_VERSION = 2; // Incrementa la versione del database

    private static final String TABLE_PATIENTS = "patients";
    private static final String TABLE_RECORDS = "records";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DOSAGE = "dosage";
    private static final String COLUMN_TARGET_INR = "target_inr";

    private static final String COLUMN_PATIENT_ID = "patient_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_INR = "inr";
    private static final String COLUMN_CUMADIN = "cumadin";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PATIENTS_TABLE = "CREATE TABLE " + TABLE_PATIENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_DOSAGE + " TEXT,"
                + COLUMN_TARGET_INR + " REAL,"
                + COLUMN_DAYS_TO_TARGET + " INTEGER,"
                + COLUMN_MAINTENANCE_DOSAGE + " REAL" + ")";
        db.execSQL(CREATE_PATIENTS_TABLE);

        String CREATE_RECORDS_TABLE = "CREATE TABLE " + TABLE_RECORDS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PATIENT_ID + " INTEGER,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_INR + " TEXT,"
                + COLUMN_CUMADIN + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_PATIENT_ID + ") REFERENCES " + TABLE_PATIENTS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_RECORDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_PATIENTS + " ADD COLUMN " + COLUMN_TARGET_INR + " REAL DEFAULT 2.5");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_PATIENTS + " ADD COLUMN " + COLUMN_DAYS_TO_TARGET + " INTEGER DEFAULT 7");
            db.execSQL("ALTER TABLE " + TABLE_PATIENTS + " ADD COLUMN " + COLUMN_MAINTENANCE_DOSAGE + " REAL DEFAULT 0.0");
        }
    }

    // Metodo per aggiungere un paziente
    public void addPatient(Patient patient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, patient.getName());
        values.put(COLUMN_DOSAGE, patient.getDosage());
        values.put(COLUMN_TARGET_INR, 2.5); // Imposta un target INR predefinito

        db.insert(TABLE_PATIENTS, null, values);
        db.close();
    }

    public List<Patient> getAllPatients() {
        List<Patient> patientList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PATIENTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));  // Ottieni l'ID del paziente
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String dosage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOSAGE));
                Patient patient = new Patient(id, name);  // Usa il nuovo costruttore con l'ID
                patient.setDosage(dosage);
                patientList.add(patient);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return patientList;
    }


    // Metodo per ottenere l'ID del paziente in base al nome
    public int getPatientIdByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + COLUMN_ID + " FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_NAME + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{name});

        int patientId = -1;
        if (cursor.moveToFirst()) {
            patientId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }
        cursor.close();
        db.close();
        return patientId;
    }

    // Metodo per aggiornare il target INR
    public void updateTargetINR(int patientId, double targetINR) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TARGET_INR, targetINR);

        db.update(TABLE_PATIENTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(patientId)});
        db.close();
    }

    // Metodo per ottenere il target INR
    public double getTargetINR(int patientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + COLUMN_TARGET_INR + " FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(patientId)});

        double targetINR = 2.5; // Valore di default
        if (cursor.moveToFirst()) {
            targetINR = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TARGET_INR));
        }
        cursor.close();
        db.close();
        return targetINR;
    }

    // Metodo per aggiornare il dosaggio stimato
    public void updateDosage(int patientId, double dosage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DOSAGE, dosage);

        db.update(TABLE_PATIENTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(patientId)});
        db.close();
    }

    // Aggiungi un nuovo metodo per aggiungere un record INR o Cumadin
    public void addRecord(int patientId, HistoryRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PATIENT_ID, patientId);
        values.put(COLUMN_DATE, record.getDate());

        if (record.getInr() != null && !record.getInr().isEmpty()) {
            values.put(COLUMN_INR, record.getInr());
        } else if (record.getDosage() != null && !record.getDosage().isEmpty()) {
            values.put(COLUMN_CUMADIN, record.getDosage());
        }

        db.insert(TABLE_RECORDS, null, values);
        db.close();
    }

    // Metodo per ottenere i record di un paziente
    public List<HistoryRecord> getRecordsForPatient(int patientId) {
        List<HistoryRecord> recordList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_RECORDS + " WHERE " + COLUMN_PATIENT_ID + " = " + patientId;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String inr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INR));
                String cumadin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUMADIN));
                HistoryRecord record = new HistoryRecord(id, date, inr, cumadin);
                recordList.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return recordList;
    }

    // Metodo per cancellare un record
    public void deleteRecord(int recordId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECORDS, COLUMN_ID + " = ?", new String[]{String.valueOf(recordId)});
        db.close();
    }

    // Modifica dell'update del database per evitare di aggiornare INR e Cumadin nello stesso record
    public void updateRecord(HistoryRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", record.getDate());

        if (record.getInr() != null && !record.getInr().isEmpty()) {
            values.put("inr", record.getInr());
            values.putNull("cumadin");  // Rimuove Cumadin se si sta aggiornando INR
        } else if (record.getDosage() != null && !record.getDosage().isEmpty()) {
            values.put("cumadin", record.getDosage());
            values.putNull("inr");  // Rimuove INR se si sta aggiornando Cumadin
        }

        db.update("records", values, "id = ?", new String[]{String.valueOf(record.getId())});
        db.close();
    }

    // Metodo per cancellare un paziente
// Metodo per cancellare un paziente dal database
    public void deletePatient(int patientId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PATIENTS, COLUMN_ID + " = ?", new String[]{String.valueOf(patientId)});
        db.delete(TABLE_RECORDS, COLUMN_PATIENT_ID + " = ?", new String[]{String.valueOf(patientId)}); // Cancella anche i record associati al paziente
        db.close();
    }

    // Aggiungi metodi per aggiornare e ottenere days_to_target
    public void updateDaysToTarget(int patientId, int daysToTarget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DAYS_TO_TARGET, daysToTarget);

        db.update(TABLE_PATIENTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(patientId)});
        db.close();
    }

    public int getDaysToTarget(int patientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + COLUMN_DAYS_TO_TARGET + " FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(patientId)});

        int daysToTarget = 7; // Default 7 giorni
        if (cursor.moveToFirst()) {
            daysToTarget = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAYS_TO_TARGET));
        }
        cursor.close();
        db.close();
        return daysToTarget;
    }

    // Metodo per aggiornare la dose di mantenimento
    public void updateMaintenanceDosage(int patientId, double maintenanceDosage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MAINTENANCE_DOSAGE, maintenanceDosage);

        db.update(TABLE_PATIENTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(patientId)});
        db.close();
    }

    // Metodo per ottenere la dose di mantenimento
    public double getMaintenanceDosage(int patientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + COLUMN_MAINTENANCE_DOSAGE + " FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(patientId)});

        double maintenanceDosage = 0.0; // Default 0.0 mg
        if (cursor.moveToFirst()) {
            maintenanceDosage = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_MAINTENANCE_DOSAGE));
        }
        cursor.close();
        db.close();
        return maintenanceDosage;
    }
}
