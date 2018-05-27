package com.avivamiriammandel.watchme.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class FavoriteProvider extends ContentProvider {

    private static final String TAG = FavoriteProvider.class.getSimpleName();

    private static final int FAVORITES = 100;
    private static final int FAVORITES_WITH_ID = 101;

    private FavoriteDbHelper dbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(FavoriteContract.CONTENT_AUTHORITY,
                FavoriteContract.TABLE_NAME,
                FAVORITES);

        sUriMatcher.addURI(FavoriteContract.CONTENT_AUTHORITY,
                FavoriteContract.TABLE_NAME + "/#",
                FAVORITES_WITH_ID);

    }



        @Override
        public boolean onCreate() {
            dbHelper = new FavoriteDbHelper(getContext());
            return true;
        }

        @Nullable
        @Override public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                                      @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            Cursor cursor = null;

            int match = sUriMatcher.match(uri);
            switch (match){
                case FAVORITES:
                    cursor = database.query(FavoriteContract.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );
                    break;
                case FAVORITES_WITH_ID:
                    selection = FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID + "=?";
                    selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                    cursor = database.query(FavoriteContract.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );
                    break;
                default:
                    throw new IllegalArgumentException("Cannot query unknown Uri" + uri);
            }
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

            int match = sUriMatcher.match(uri);
            switch (match) {
                case FAVORITES:
                    return insertFavorite(uri, values);
            }
            throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }


        private Uri insertFavorite(Uri uri, ContentValues values){
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            long id = database.insert(FavoriteContract.TABLE_NAME, null, values);
            if (id == -1){
                Log.e(TAG, "Failed to insert Favorite for  " + uri );
                return null;
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, id);
        }

        @Override
        public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
            int match = sUriMatcher.match(uri);
            switch (match) {
                case FAVORITES:
                    selection = (selection == null) ? "1" : selection;
                    break;
                case FAVORITES_WITH_ID:
                    selection = String.format("%s = ?", FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID);
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    break;
                default:
                    throw new IllegalArgumentException("Delete is not supported for " + uri);
            }

            SQLiteDatabase database = dbHelper.getWritableDatabase();
            int rowsDeleted = database.delete(FavoriteContract.TABLE_NAME,
                    selection,
                    selectionArgs);
            if (rowsDeleted > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsDeleted;
        }

        @Override
        public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
            int match = sUriMatcher.match(uri);
            switch (match) {
                case FAVORITES:
                    selection = FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID+ "=?";
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    return updateFavorite(uri, values, selection, selectionArgs);
                default:
                    throw new IllegalArgumentException("Update is not supported for " + uri);
            }
        }

        private int updateFavorite(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
            if (values.size() == 0)
                return 0;
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            int rowsUpdated = database.update(FavoriteContract.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
        }
}


