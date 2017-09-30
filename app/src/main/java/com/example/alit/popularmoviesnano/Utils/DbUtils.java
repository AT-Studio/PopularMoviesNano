package com.example.alit.popularmoviesnano.Utils;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.example.alit.popularmoviesnano.ContentProvider.MovieDbContract;
import com.example.alit.popularmoviesnano.MyDatastructures.MovieListItem;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by AliT on 9/28/17.
 */

public class DbUtils {

    public static byte[] getByteArray(Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();

    }

    public static byte[] getByteArray(Bitmap bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();

    }

    public static Bitmap getBitmap(byte[] bytes) {

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

    }

    public static ArrayList<MovieListItem> getMovieListItems(Cursor cursor) {

        ArrayList<MovieListItem> movieListItems = new ArrayList<>();

        while (cursor.moveToNext()) {

            int movieID = cursor.getInt(cursor.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_MOVIE_ID));
            String title = cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_ORIGINAL_TITLE));
            Bitmap poster = getBitmap(cursor.getBlob(cursor.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_POSTER)));
            String overview = cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_OVERVIEW));
            Float rating = cursor.getFloat(cursor.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_VOTE_AVERAGE));
            String releaseDate = cursor.getString(cursor.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_RELEASE_DATE));

            movieListItems.add(new MovieListItem(movieID, title, poster, overview, rating, releaseDate));

        }

        return movieListItems;

    }

}
