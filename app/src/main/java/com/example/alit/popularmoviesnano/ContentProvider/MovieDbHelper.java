package com.example.alit.popularmoviesnano.ContentProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by AliT on 9/27/17.
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movieDb.db";

    private static final int VERSION = 2;

    public MovieDbHelper(Context context) {

        super(context, DATABASE_NAME, null, VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_TABLE = "CREATE TABLE " + MovieDbContract.MovieEntry.TABLE_NAME + " (" +
                MovieDbContract.MovieEntry._ID      + " INTEGER PRIMARY KEY, " +
                MovieDbContract.MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieDbContract.MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULl, " +
                MovieDbContract.MovieEntry.COLUMN_POSTER + " BLOB NOT NULL, " +
                MovieDbContract.MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieDbContract.MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL," +
                MovieDbContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL);";

        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieDbContract.MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
