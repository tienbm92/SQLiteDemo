package com.sqlitedemo.tienbm.sqlitedemo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Created by TienBM on 10/13/2015.
 */
public class CustomContentProviders extends ContentProvider {
    static final String _ID = "_id";
    static final String PROVIDER_NAME = "com.tienbm.customproviders";
    static final String URL = "content://" + PROVIDER_NAME + "/contact";
    static final Uri CONTENT_URI = Uri.parse(URL);
    static final String NAME = "name";
    private static HashMap<String, String> CONTACT_PROJECTION_MAP;
    static final int CONTACT = 1;
    static final int CONTACT_ID = 2;
    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "contacts", CONTACT);
        uriMatcher.addURI(PROVIDER_NAME, "contacts/#", CONTACT_ID);
    }
//    static UriMatcher uriMatcher;

    private SQLiteDatabase db;
    @Override
    public boolean onCreate() {
        Context mContext = getContext();
        DBHelper dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DBHelper.CONTACTS_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case CONTACT:
                qb.setProjectionMap(CONTACT_PROJECTION_MAP);
                break;

            case CONTACT_ID:
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder == ""){

            sortOrder = NAME;
        }
        Cursor c = qb.query(db,	projection,	selection, selectionArgs,null, null, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * Get all student records
             */
            case CONTACT:
                return "vnd.android.cursor.dir/vnd.example.students";

            /**
             * Get a particular student
             */
            case CONTACT_ID:
                return "vnd.android.cursor.item/vnd.example.students";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = db.insert(DBHelper.CONTACTS_TABLE_NAME, "", values);
        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case CONTACT:
                count = db.delete(DBHelper.CONTACTS_TABLE_NAME, selection, selectionArgs);
                break;

            case CONTACT_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( DBHelper.CONTACTS_TABLE_NAME, _ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case CONTACT:
                count = db.update(DBHelper.CONTACTS_TABLE_NAME, values, selection, selectionArgs);
                break;

            case CONTACT_ID:
                count = db.update(DBHelper.CONTACTS_TABLE_NAME, values, _ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
