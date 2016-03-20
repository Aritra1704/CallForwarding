package com.example.arpaul.ids.DataAccess;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by ARPaul on 04-01-2016.
 */
public class ContentProviderHelper extends ContentProvider {

    public static final int CALLRECEIVE = 1;

    public static final String TAG_ID = "/#";

    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PhoneRecordCPConstants.PROVIDER_NAME, PhoneRecordCPConstants.CALL_RECEIVE_TABLE_NAME + TAG_ID, CALLRECEIVE);
    }

    private DataBaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DataBaseHelper(getContext());
        return (mOpenHelper == null) ? false : true;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        return 0;
    }

    /**
     * Create a write able database which will trigger its
     * creation if it doesn't already exist.
     *//*
        mOpenHelper = dbHelper.getWritableDatabase();
    }*/

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        Uri returnURI;
                long _id = db.insert(PhoneRecordCPConstants.CALL_RECEIVE_TABLE_NAME, null, values);
                if (_id > 0) {
                    returnURI = PhoneRecordCPConstants.buildMoviesUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into: " + uri);
                }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnURI;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        retCursor = mOpenHelper.getReadableDatabase().query(
                PhoneRecordCPConstants.CALL_RECEIVE_TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        return retCursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;

        count = db.delete(PhoneRecordCPConstants.CALL_RECEIVE_TABLE_NAME,
                PhoneRecordCPConstants.COLUMN_CONTACT_NO + " = " + selection +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);


        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        count = db.update(PhoneRecordCPConstants.CALL_RECEIVE_TABLE_NAME, values, selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {

        final int match = uriMatcher.match(uri);

        return PhoneRecordCPConstants.CONTENT_NAME_TYPE;
    }
}