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

    private static final String DATABASE_NAME = "inrtracker.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PATIENTS = "patients";
    private static final String TABLE_RECORDS = "records";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DOSAGE = "dosage";

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
                + COLUMN_DOSAGE + " TEXT" + ")";
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDS);
        onCreate(db);
    }

    // Metodo per aggiungere un paziente
    public void addPatient(Patient patient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, patient.getName());
        values.put(COLUMN_DOSAGE, patient.getDosage());

        db.insert(TABLE_PATIENTS, null, values);
        db.close();
    }

    // Metodo per ottenere tutti i pazienti
    public List<Patient> getAllPatients() {
        List<Patient> patientList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PATIENTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Patient patient = new Patient(cursor.getString(1));
                patient.setDosage(cursor.getString(2));
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

    // Metodo per aggiungere un record
    public void addRecord(int patientId, HistoryRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PATIENT_ID, patientId);
        values.put(COLUMN_DATE, record.getDate());
        values.put(COLUMN_INR, record.getInr());
        values.put(COLUMN_CUMADIN, record.getDosage());

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
                HistoryRecord record = new HistoryRecord(
                        cursor.getString(2),  // date
                        cursor.getString(3),  // INR
                        cursor.getString(4)); // Cumadin
                recordList.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return recordList;
    }
}
