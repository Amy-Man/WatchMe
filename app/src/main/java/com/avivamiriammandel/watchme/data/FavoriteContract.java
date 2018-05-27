package com.avivamiriammandel.watchme.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavoriteContract {

    public static final String TABLE_NAME = "favorite";
    public static final String CONTENT_AUTHORITY = "com.avivamiriammandel.watchme";
    public static final Uri CONTENT_URI = new Uri.Builder()
            .scheme("content")
            .authority(CONTENT_AUTHORITY)
            .appendPath(TABLE_NAME)
            .build();

    public static final class FavoriteEntry implements BaseColumns {
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_USER_RATING = "user_rating";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_PLOT_SYNOPSIS = "plot_synopsis";
    }
}

