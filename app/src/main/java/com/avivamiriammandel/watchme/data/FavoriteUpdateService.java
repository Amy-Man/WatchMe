package com.avivamiriammandel.watchme.data;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

public class FavoriteUpdateService extends IntentService {

    private static final String TAG = FavoriteUpdateService.class.getSimpleName();

    public static final String ACTION_INSERT = TAG + ".INSERT";
    public static final String ACTION_DELETE = TAG + ".DELETE";
    public static final String ACTION_UPDATE = TAG + ".UPDATE";

    public static final String EXTRA_VALUES = TAG + ".ContentValues";

    public static void insertNewFavorite(Context context, ContentValues values){
        Intent intent = new Intent(context, FavoriteUpdateService.class);
        intent.setAction(ACTION_INSERT);
        intent.putExtra(EXTRA_VALUES, values);
        context.startService(intent);
    }

    public static void updateFavorite(Context context, Uri uri, ContentValues values){
        Intent intent = new Intent(context, FavoriteUpdateService.class);
        intent.setAction(ACTION_UPDATE);
        intent.setData(uri);
        intent.putExtra(EXTRA_VALUES, values);
        context.startService(intent);
    }

    public static void deleteFavorite(Context context, Uri uri, ContentValues values){
        Intent intent = new Intent(context, FavoriteUpdateService.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(EXTRA_VALUES, values);
        intent.setData(uri);
        context.startService(intent);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param
     */
    public FavoriteUpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (ACTION_INSERT.equals(intent.getAction())) {
            ContentValues values = intent.getParcelableExtra(EXTRA_VALUES);
            performInsert(values);
        } else if (ACTION_UPDATE.equals(intent.getAction())) {
            ContentValues values = intent.getParcelableExtra(EXTRA_VALUES);
            performUpdate(intent.getData(), values);
        } else if (ACTION_DELETE.equals(intent.getAction())) {
            ContentValues values = intent.getParcelableExtra(EXTRA_VALUES);
            performDelete(intent.getData(), values);
        }
    }
    private void performInsert(ContentValues values){
        if (getContentResolver().insert(FavoriteContract.CONTENT_URI, values) != null) {
            Log.d(TAG, "performInsert: inserted new task");
        } else {
            Log.d(TAG, "Error inserting new  task");
        }
    }
    private void performUpdate(Uri uri, ContentValues values){
        int count = getContentResolver().update(uri, values, null, null);
        Log.d(TAG, "updated" + count + "tasks");
    }
    private void performDelete(Uri uri, ContentValues values){
        int count = getContentResolver().delete(uri, values.getAsString(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_ID), null);
        Log.d(TAG, "deleted" + count +"tasks");
        /*PendingIntent operation =
                ReminderAlarmService.getReminderPendingIntent(this, uri);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        manager.cancel(operation);*/

    }
}
