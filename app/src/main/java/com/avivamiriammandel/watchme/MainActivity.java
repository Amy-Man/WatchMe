package com.avivamiriammandel.watchme;

import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.avivamiriammandel.watchme.adapter.FavoritesAdapter;
import com.avivamiriammandel.watchme.adapter.MoviesAdapter;
import com.avivamiriammandel.watchme.data.FavoriteDbHelper;
import com.avivamiriammandel.watchme.data.FavoriteProvider;
import com.avivamiriammandel.watchme.error.ApiError;
import com.avivamiriammandel.watchme.error.ErrorUtils;
import com.avivamiriammandel.watchme.model.Movie;
import com.avivamiriammandel.watchme.model.MoviesResponse;
import com.avivamiriammandel.watchme.rest.Client;
import com.avivamiriammandel.watchme.rest.Service;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;
    private List<Movie> movieList;
    private FavoriteDbHelper favoriteDbHelper;
    private AppCompatActivity activity = MainActivity.this;
    private static final int WIDTH_OF_COLUMNS = 120;
    public static final String TAG = MoviesAdapter.class.getName();
    private BottomNavigationView navigation;
    private SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        initViews();

    }

    private void initViews() {
        progressBar = findViewById(R.id.progress);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setVisibility(View.INVISIBLE);

        movieList = new ArrayList<>();

        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        final int numColumns = (int) (dpWidth / WIDTH_OF_COLUMNS);
        recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, numColumns));

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        favoriteDbHelper = new FavoriteDbHelper(activity);


        loadJSON();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_popular:
                    loadJSON();
                    return true;
                case R.id.navigation_top_rated:
                    loadJSON1();
                    return true;
                case R.id.navigation_favorite:
                    initViews1();
                    return true;
                default:
                    return false;
            }
        }
    };


    private void loadJSON() {

        try {
            if (BuildConfig.API_KEY.isEmpty()) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), R.string.apiKeyError, Toast.LENGTH_LONG).show();
                return;
            }
            sharedPreference();
            final Client client = new Client();
            final Service apiService = Client.getClient().create(Service.class);
            final Call<MoviesResponse> call = apiService.getPopularMovies(BuildConfig.API_KEY);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(@NonNull Call<MoviesResponse> call, @NonNull Response<MoviesResponse> response) {
                    if (response.isSuccessful()) {
                        final MoviesResponse results = response.body();
                        List<Movie> movieList = null;
                        if (results != null) {
                            movieList = results.getResults();
                        }

                        recyclerView = findViewById(R.id.recycler_view);
                        recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movieList));
                        recyclerView.smoothScrollToPosition(0);
                        progressBar.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {

                        progressBar.setVisibility(View.INVISIBLE);
                        final ApiError apiError = ErrorUtils.parseError(response);
                        Toast.makeText(MainActivity.this, apiError.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "onResponse: " + apiError.getMessage() + " " + apiError.getStatusCode() + " " + apiError.getEndpoint());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MoviesResponse> call, @NonNull Throwable t) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, R.string.onNoInternetError, Toast.LENGTH_LONG).show();
                    Log.d(TAG, t.getMessage());
                }
            });
        } catch (final Exception e) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            Log.d(TAG, e.getMessage());
        }

    }

    private void loadJSON1() {

        try {
            if (BuildConfig.API_KEY.isEmpty()) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), R.string.apiKeyError, Toast.LENGTH_LONG).show();
                return;
            }
            sharedPreference1();
            Client client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<MoviesResponse> call = apiService.getTopRatedMovies(BuildConfig.API_KEY);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(@NonNull Call<MoviesResponse> call, @NonNull Response<MoviesResponse> response) {
                    if (response.isSuccessful()) {
                        MoviesResponse results = response.body();
                        List<Movie> movieList = null;
                        if (results != null) {
                            movieList = results.getResults();
                        }

                        recyclerView = findViewById(R.id.recycler_view);
                        recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movieList));
                        recyclerView.smoothScrollToPosition(0);
                        progressBar.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {

                        progressBar.setVisibility(View.INVISIBLE);
                        ApiError apiError = ErrorUtils.parseError(response);
                        Toast.makeText(MainActivity.this, apiError.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "onResponse: " + apiError.getMessage() + " " + apiError.getStatusCode() + " " + apiError.getEndpoint());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MoviesResponse> call, @NonNull Throwable t) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, R.string.onNoInternetError, Toast.LENGTH_LONG).show();
                    Log.d(TAG, t.getMessage());
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            Log.d(TAG, e.getMessage());
        }

    }

    private void  initViews1() {
        progressBar = findViewById(R.id.progress);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setVisibility(View.INVISIBLE);

        movieList = new ArrayList<>();

        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        final int numColumns = (int) (dpWidth / WIDTH_OF_COLUMNS);
        recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, numColumns));

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        favoriteDbHelper = new FavoriteDbHelper(activity);

        loadFavorites();

    }


    @SuppressLint("StaticFieldLeak")
    public void loadFavorites() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                movieList.clear();
                //FavoritesAdapter favoritesAdapter = new FavoritesAdapter();
                //recyclerView.setAdapter(favoritesAdapter);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                sharedPreference2();
            }
        }.execute();

    }

    public void sharedPreference(){

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (null != sharedPreferences) {
            sharedPreferences.getBoolean(String.valueOf(R.string.preference_most_popular), true);
            sharedPreferences.getBoolean(String.valueOf(R.string.preference_highest_rated), false);
            sharedPreferences.getBoolean(String.valueOf(R.string.preference_favorites), false);
        }else {
            SharedPreferences.Editor editor =
                    getSharedPreferences("com.avivamiriammandel.watchme.MainActivity",
                            MODE_PRIVATE).edit();
            editor.putBoolean(String.valueOf(R.string.preference_most_popular), true);
            editor.putBoolean(String.valueOf(R.string.preference_highest_rated), false);
            editor.putBoolean(String.valueOf(R.string.preference_favorites), false);
            editor.commit();
        }
    }

    public void sharedPreference1(){

    SharedPreferences.Editor editor =
            getSharedPreferences("com.avivamiriammandel.watchme.MainActivity",
                    MODE_PRIVATE).edit();
    editor.putBoolean(String.valueOf(R.string.preference_most_popular), false);
    editor.putBoolean(String.valueOf(R.string.preference_highest_rated), true);
    editor.putBoolean(String.valueOf(R.string.preference_favorites), false);
    editor.commit();

    }

    public void sharedPreference2(){

    SharedPreferences.Editor editor =
            getSharedPreferences("com.avivamiriammandel.watchme.MainActivity",
                    MODE_PRIVATE).edit();
    editor.putBoolean(String.valueOf(R.string.preference_most_popular), false);
    editor.putBoolean(String.valueOf(R.string.preference_highest_rated), false);
    editor.putBoolean(String.valueOf(R.string.preference_favorites), true);
    editor.commit();
    }


}



