package com.example.arpaul.ids.DataAccess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ARPaul on 04-01-2016.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;

    static final String CREATE_DB_TABLE =
            " CREATE TABLE IF NOT EXISTS " + PhoneRecordCPConstants.CALL_RECEIVE_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PhoneRecordCPConstants.COLUMN_CONTACT_NO + " TEXT NOT NULL, " +
                    PhoneRecordCPConstants.COLUMN_CONTACT_TYPE + " INT NOT NULL, " +
                    PhoneRecordCPConstants.COLUMN_CALL_DATETIME + " DATETIME NOT NULL, " +
                    PhoneRecordCPConstants.COLUMN_CHECHED + " INT NOT NULL);";

    DataBaseHelper(Context context){
        super(context, PhoneRecordCPConstants.DATABASE_NAME, null, PhoneRecordCPConstants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PhoneRecordCPConstants.CALL_RECEIVE_TABLE_NAME);
        onCreate(db);
    }
}
