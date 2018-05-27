package com.avivamiriammandel.watchme.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.avivamiriammandel.watchme.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class FavoriteDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorite.db";
    private static final int DATABASE_VERSION = 1;

    public static final String LOG_TAG = "FAVORITE";

    SQLiteOpenHelper dbHandler;
    SQLiteDatabase db;

    public FavoriteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public FavoriteDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    /*public void open(){
        Log.i(LOG_TAG, "DATABASE Opened: ");
        db = dbHandler.getWritableDatabase();
    }

    @Override
    public void close(){
        Log.i(LOG_TAG, "DATABASE Closed: ");
        dbHandler.close();
    }*/

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FAVORITE_TABLE =
                "CREATE TABLE " + FavoriteContract.TABLE_NAME + " ( " +
                        FavoriteContract.FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID + "INTEGER NOT NULL, " +
                        FavoriteContract.FavoriteEntry.COLUMN_TITLE + "TEXT NOT NULL, " +
                        FavoriteContract.FavoriteEntry.COLUMN_USER_RATING + "DOUBLE NOT NULL, " +
                        FavoriteContract.FavoriteEntry.COLUMN_POSTER_PATH + "TEXT NOT NULL, " +
                        FavoriteContract.FavoriteEntry.COLUMN_BACKDROP_PATH + "TEXT NOT NULL, " +
                        FavoriteContract.FavoriteEntry.COLUMN_RELEASE_DATE + "TEXT NOT NULL, " +
                        FavoriteContract.FavoriteEntry.COLUMN_PLOT_SYNOPSIS + "TEXT NOT NULL " +
                        " ); ";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE);

        }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteContract.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }

    /*public void addFavorite(Movie movie){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID, movie.getId());
        values.put(FavoriteContract.FavoriteEntry.COLUMN_TITLE, movie.getTitle());
        values.put(FavoriteContract.FavoriteEntry.COLUMN_USER_RATING, movie.getVoteAverage());
        values.put(FavoriteContract.FavoriteEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        values.put(FavoriteContract.FavoriteEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
        values.put(FavoriteContract.FavoriteEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        values.put(FavoriteContract.FavoriteEntry.COLUMN_PLOT_SYNOPSIS, movie.getOverview());
        Log.d(LOG_TAG, "addFavorite: ");
        db.insert(FavoriteContract.TABLE_NAME, null, values);
        db.close();

    }

    public void deleteFavorite(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(FavoriteContract.TABLE_NAME,
                FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID + "=" + id,
                null);
    }

    public List<Movie> getAllFavorites() {
        String[] columns = {
                FavoriteContract.FavoriteEntry._ID,
                FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID,
                FavoriteContract.FavoriteEntry.COLUMN_TITLE,
                FavoriteContract.FavoriteEntry.COLUMN_USER_RATING,
                FavoriteContract.FavoriteEntry.COLUMN_POSTER_PATH,
                FavoriteContract.FavoriteEntry.COLUMN_BACKDROP_PATH,
                FavoriteContract.FavoriteEntry.COLUMN_RELEASE_DATE,
                FavoriteContract.FavoriteEntry.COLUMN_PLOT_SYNOPSIS
        };
        String sortOrder = FavoriteContract.FavoriteEntry._ID + " ASC";

        List<Movie> favoriteList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                FavoriteContract.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                sortOrder);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Movie movie = new Movie();
                    movie.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(
                            FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID))));
                    movie.setTitle(cursor.getString(cursor.getColumnIndex(
                            FavoriteContract.FavoriteEntry.COLUMN_TITLE)));
                    movie.setVoteAverage(Double.parseDouble(cursor.getString(cursor.getColumnIndex(
                            FavoriteContract.FavoriteEntry.COLUMN_USER_RATING))));
                    movie.setPosterPath(cursor.getString(cursor.getColumnIndex(
                            FavoriteContract.FavoriteEntry.COLUMN_POSTER_PATH)));
                    movie.setBackdropPath(cursor.getString(cursor.getColumnIndex(
                            FavoriteContract.FavoriteEntry.COLUMN_BACKDROP_PATH)));
                    movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(
                            FavoriteContract.FavoriteEntry.COLUMN_RELEASE_DATE)));
                    movie.setOverview(cursor.getString(cursor.getColumnIndex(
                            FavoriteContract.FavoriteEntry.COLUMN_PLOT_SYNOPSIS)));

                    favoriteList.add(movie);

                } while (cursor.moveToNext());
                cursor.close();
                db.close();
            }
        }
        return favoriteList;
    }*/
}
