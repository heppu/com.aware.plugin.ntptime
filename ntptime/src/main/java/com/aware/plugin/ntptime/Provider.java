package com.aware.plugin.ntptime;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

public class Provider extends ContentProvider {

	public static final int DATABASE_VERSION = 3;
	
	/**
	 * Provider authority: com.aware.provider.plugin.ntptime
	 */
	public static String AUTHORITY = "com.aware.plugin.ntptime.provider";
	
	private static final int NTPTIME = 1;
	private static final int NTPTIME_ID = 2;
	
	public static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/plugin_ntptime.db";
	
	public static final String[] DATABASE_TABLES = {
		"plugin_ntptime"
	};
	
	public static final String[] TABLES_FIELDS = {
		NTPtime_Data._ID + " integer primary key autoincrement," +
		NTPtime_Data.TIMESTAMP + " real default 0," +
		NTPtime_Data.DEVICE_ID + " text default ''," +
		NTPtime_Data.DRIFT + " real default 0," +
		NTPtime_Data.NTP_TIME + " real default 0," +
		"UNIQUE("+NTPtime_Data.TIMESTAMP+","+NTPtime_Data.DEVICE_ID+")"
	};
	
	public static final class NTPtime_Data implements BaseColumns {
		private NTPtime_Data(){};
		
		public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/plugin_ntptime");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.ntptime";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.ntptime";
		
		public static final String _ID = "_id";
		public static final String TIMESTAMP = "timestamp";
		public static final String DEVICE_ID = "device_id";
		public static final String DRIFT = "drift";
		public static final String NTP_TIME = "ntp_time";
	}
	
	private static UriMatcher sUriMatcher;
	private static HashMap<String, String> ntpTimeMap;
	private static DatabaseHelper databaseHelper;
	private static SQLiteDatabase database;
	
	private boolean initializeDB() {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper( getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );
        }
        if( databaseHelper != null && ( database == null || ! database.isOpen() )) {
            database = databaseHelper.getWritableDatabase();
        }
        return( database != null && databaseHelper != null);
    }
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case NTPTIME:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
	        case NTPTIME:
	            return NTPtime_Data.CONTENT_TYPE;
	        case NTPTIME_ID:
	            return NTPtime_Data.CONTENT_ITEM_TYPE;
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
	    }
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (sUriMatcher.match(uri)) {
            case NTPTIME:
                long ntp_id = database.insert(DATABASE_TABLES[0],
                        NTPtime_Data.DRIFT, values);

                if (ntp_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            NTPtime_Data.CONTENT_URI,
                            ntp_id);
                    getContext().getContentResolver().notifyChange(new_uri,
                            null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public boolean onCreate() {
		
		AUTHORITY = getContext().getPackageName() + ".provider";
		
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], NTPTIME);
		sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", NTPTIME_ID);
		
		ntpTimeMap = new HashMap<String, String>();
		ntpTimeMap.put(NTPtime_Data._ID, NTPtime_Data._ID);
		ntpTimeMap.put(NTPtime_Data.TIMESTAMP, NTPtime_Data.TIMESTAMP);
		ntpTimeMap.put(NTPtime_Data.DEVICE_ID, NTPtime_Data.DEVICE_ID);
		ntpTimeMap.put(NTPtime_Data.DRIFT, NTPtime_Data.DRIFT);
		ntpTimeMap.put(NTPtime_Data.NTP_TIME, NTPtime_Data.NTP_TIME);
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case NTPTIME:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(ntpTimeMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());

            return null;
        }
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }
		
        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case NTPTIME:
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                break;
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
}
