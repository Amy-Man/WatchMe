package com.avivamiriammandel.watchme.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

import static com.avivamiriammandel.watchme.data.FavoriteContract.TABLE_NAME;

public class FavoriteProvider extends ContentProvider {

    private static final String TAG = FavoriteProvider.class.getSimpleName();

    private static final int FAVORITES = 100;
    private static final int FAVORITES_WITH_ID = 101;

    private static HashMap<String, String> fAVORITE_MOVIES_PROJECTION_MAP;

    private FavoriteDbHelper dbHelper;

    private SQLiteDatabase db;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(FavoriteContract.CONTENT_AUTHORITY,
                TABLE_NAME,
                FAVORITES);

        sUriMatcher.addURI(FavoriteContract.CONTENT_AUTHORITY,
                TABLE_NAME + "/#",
                FAVORITES_WITH_ID);

    }

    @Override
     public boolean onCreate() {
            Context context = getContext();
            dbHelper = new FavoriteDbHelper(context);

            /**
             * Create a write able database which will trigger its
             * creation if it doesn't already exist.
             */

            db = dbHelper.getWritableDatabase();
        Log.d(TAG, "onCreate: Database in Content Provider");
            
            return (db == null)? false:true;
        }


    @Nullable
    @Override public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                                      @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        db = dbHelper.getWritableDatabase();


            int match = sUriMatcher.match(uri);
            switch (match){
                case FAVORITES:
                    qb.setProjectionMap(fAVORITE_MOVIES_PROJECTION_MAP);
                    break;
                case FAVORITES_WITH_ID:
                    qb.appendWhere( FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID + "=" + uri.getPathSegments().get(1));
                    break;
                default:
                    throw new IllegalArgumentException("Cannot query unknown Uri" + uri);
            }

        if (sortOrder == null || sortOrder.equals("")){
            sortOrder = FavoriteContract.FavoriteEntry.COLUMN_TITLE;
        }

        Cursor cursor = qb.query(db,	projection,	selection,
                selectionArgs,null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }

        @Nullable
        @Override
        public String getType(@NonNull Uri uri) {
            return null;
        }

        @Nullable
        @Override
        public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            int match = sUriMatcher.match(uri);
            switch (match) {
                case FAVORITES:
                    long id = db.insert(TABLE_NAME, null, values);
                    if (id == -1){
                        Log.e(TAG, "Failed to insert Favorite for  " + uri );
                        return null;
                    }
                    getContext().getContentResolver().notifyChange(uri, null);
                    return ContentUris.withAppendedId(uri, id);
            }
            throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

        @Override
        public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        db = dbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
            int rowsDeleted;
            switch (match) {
                case FAVORITES:
                    rowsDeleted = db.delete(FavoriteContract.TABLE_NAME, selection, selectionArgs);
                    break;
                case FAVORITES_WITH_ID:
                    String id = uri.getPathSegments().get(1);
                    rowsDeleted = db.delete(FavoriteContract.TABLE_NAME, FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID +  " = " + id +
                                    (!TextUtils.isEmpty(selection) ?
                             "AND (" + selection + ')' : ""), selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Delete is not supported for " + uri);
            }

            if (rowsDeleted > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsDeleted;
        }

        @Override
        public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
            db = dbHelper.getWritableDatabase();
            int match = sUriMatcher.match(uri);
            int rowsUpdated;
            switch (match) {
                case FAVORITES:
                    rowsUpdated = db.delete(FavoriteContract.TABLE_NAME, selection, selectionArgs);
                    break;
                case FAVORITES_WITH_ID:
                    String id = uri.getPathSegments().get(1);
                    rowsUpdated = db.delete(FavoriteContract.TABLE_NAME, FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID +  " = " + id +
                            (!TextUtils.isEmpty(selection) ?
                                    "AND (" + selection + ')' : ""), selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Update is not supported for " + uri);
            }

            if (rowsUpdated > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
        }
}


