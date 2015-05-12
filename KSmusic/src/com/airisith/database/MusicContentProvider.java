package com.airisith.database;

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
import android.text.TextUtils;


/**
 * 操作数据库Person表的ContentProvider
 * @author
 *
 */
public class MusicContentProvider extends ContentProvider {

    private static HashMap<String, String> sPersonsProjectionMap;

    private static final int TABLE = 1;
    private static final int MUSIC_ID = 2;

    private static final UriMatcher sUriMatcher;

    private MusicDatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new MusicDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(MusicProvider.MusicColumns.TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
        case TABLE:
            qb.setProjectionMap(sPersonsProjectionMap);
            break;

        case MUSIC_ID:
            qb.setProjectionMap(sPersonsProjectionMap);
            qb.appendWhere(MusicProvider.MusicColumns._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = MusicProvider.MusicColumns.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case TABLE:
            return MusicProvider.CONTENT_TYPE;
        case MUSIC_ID:
            return MusicProvider.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != TABLE) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        // Make sure that the fields are all set
        if (values.containsKey(MusicProvider.MusicColumns.TITLE) == false) {
            values.put(MusicProvider.MusicColumns.TITLE, "");
        }

        if (values.containsKey(MusicProvider.MusicColumns.URL) == false) {
            values.put(MusicProvider.MusicColumns.URL, "");
        }
        
        if (values.containsKey(MusicProvider.MusicColumns.TYPE) == false) {
            values.put(MusicProvider.MusicColumns.TYPE, 0);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(MusicProvider.MusicColumns.TABLE_NAME, MusicProvider.MusicColumns.TITLE, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(MusicProvider.MusicColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case TABLE:
            count = db.delete(MusicProvider.MusicColumns.TABLE_NAME, where, whereArgs);
            break;

        case MUSIC_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(MusicProvider.MusicColumns.TABLE_NAME, MusicProvider.MusicColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case TABLE:
            count = db.update(MusicProvider.MusicColumns.TABLE_NAME, values, where, whereArgs);
            break;

        case MUSIC_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.update(MusicProvider.MusicColumns.TABLE_NAME, values, MusicProvider.MusicColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 这个地方的musics要和MusicColumns.CONTENT_URI中最后面的一个Segment一致
        sUriMatcher.addURI(MusicProvider.AUTHORITY, "musics", TABLE);
        sUriMatcher.addURI(MusicProvider.AUTHORITY, "musics/#", MUSIC_ID);

        sPersonsProjectionMap = new HashMap<String, String>();
        sPersonsProjectionMap.put(MusicProvider.MusicColumns._ID, MusicProvider.MusicColumns._ID);
        sPersonsProjectionMap.put(MusicProvider.MusicColumns.TITLE, MusicProvider.MusicColumns.TITLE);
        sPersonsProjectionMap.put(MusicProvider.MusicColumns.URL, MusicProvider.MusicColumns.URL);
        sPersonsProjectionMap.put(MusicProvider.MusicColumns.TYPE, MusicProvider.MusicColumns.TYPE);
    }
}
