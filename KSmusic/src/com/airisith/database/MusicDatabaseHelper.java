package com.airisith.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * 操作数据库
 * @author jacp
 *
 */
public class MusicDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "musicinfo.db";
	private static final int DATABASE_VERSION = 1;

    public MusicDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + MusicProvider.MusicColumns.TABLE_NAME + " ("
                + MusicProvider.MusicColumns._ID + " INTEGER PRIMARY KEY,"
                + MusicProvider.MusicColumns.TITLE + " TEXT,"
                + MusicProvider.MusicColumns.URL + " TEXT,"
                + MusicProvider.MusicColumns.TYPE + " INTEGER"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TITLE IF EXISTS " + MusicProvider.MusicColumns.TABLE_NAME);
        onCreate(db);
    }
}