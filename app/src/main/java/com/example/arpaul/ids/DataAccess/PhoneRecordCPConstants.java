package com.example.arpaul.ids.DataAccess;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by ARPaul on 07-01-2016.
 */
public class PhoneRecordCPConstants {
    public static final String CONTENT_AUTHORITY = "com.example.arpaul.ids.DataAccess.ContentProviderHelper";

    static final String DATABASE_NAME = "PhoneRecord.db";
    public static final String CALL_RECEIVE_TABLE_NAME = "Call";
    static final int DATABASE_VERSION = 1;

    public static final String DELIMITER = "/";

    public static final String CONTENT = "content://";
    public static final Uri BASE_CONTENT_URI = Uri.parse(CONTENT + CONTENT_AUTHORITY);

    public static final Uri CONTENT_URI = Uri.parse(CONTENT + CONTENT_AUTHORITY + DELIMITER + CALL_RECEIVE_TABLE_NAME);

    public static final String PROVIDER_NAME = CONTENT_AUTHORITY;

    // create cursor of base type directory for multiple entries
    public static final String CONTENT_NAME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + DATABASE_NAME;
    // create cursor of base type item for single entry
    public static final String CONTENT_ID_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +"/" + CONTENT_AUTHORITY + "/" + DATABASE_NAME;

    public static Uri buildMoviesUri(long id){
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    public static final String COLUMN_CONTACT_NO = "ContactNumber";
    public static final String COLUMN_CONTACT_TYPE = "ContactType";
    public static final String COLUMN_CALL_DATETIME = "CallTime";
    public static final String COLUMN_CHECHED = "Checked";

    public static int get_Not_Checked(){
        return 0;
    }

    public static int get_Checked(){
        return 1;
    }

    public static int get_Contact_Type_Call(){
        return 0;
    }

    public static int get_Contact_Type_SMS(){
        return 1;
    }
}
