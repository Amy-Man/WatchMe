package com.avivamiriammandel.watchme;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.avivamiriammandel.watchme.adapter.MoviesAdapter;
import com.avivamiriammandel.watchme.database.AppDatabase;
import com.avivamiriammandel.watchme.error.ApiError;
import com.avivamiriammandel.watchme.error.ErrorUtils;
import com.avivamiriammandel.watchme.model.Movie;
import com.avivamiriammandel.watchme.model.MoviesResponse;
import com.avivamiriammandel.watchme.rest.Client;
import com.avivamiriammandel.watchme.rest.Service;
import com.avivamiriammandel.watchme.viewmodel.AppExecutors;
import com.avivamiriammandel.watchme.viewmodel.MainViewModel;

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
    private AppCompatActivity activity = MainActivity.this;
    private static final int WIDTH_OF_COLUMNS = 120;
    public static final String TAG = MoviesAdapter.class.getName();
    private BottomNavigationView navigation;
    private GridLayoutManager gridLayoutManager;
    private Context context;
    private Boolean nav_popular, nav_top_rated, nav_favorite;
    private int recycler_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        nav_popular = true;
        nav_favorite = false;
        nav_top_rated = false;
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        if (movieList  != null)
            movieList.clear();
        recycler_position = 0;
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
        gridLayoutManager = new GridLayoutManager(MainActivity.this, numColumns);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);


        loadJSON();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_popular:
                    loadJSON();
                    nav_popular = true;
                    nav_favorite = false;
                    nav_top_rated = false;
                    return true;
                case R.id.navigation_top_rated:
                    loadJSON1();
                    nav_popular = false;
                    nav_favorite = false;
                    nav_top_rated = true;
                    return true;
                case R.id.navigation_favorite:
                    initViews1();
                    nav_popular = false;
                    nav_favorite = true;
                    nav_top_rated = false;
                    return true;
                default:
                    return true;
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
            final Client client = new Client();
            final Service apiService = Client.getClient().create(Service.class);
            final Call<MoviesResponse> call = apiService.getPopularMovies(BuildConfig.API_KEY);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(@NonNull Call<MoviesResponse> call, @NonNull Response<MoviesResponse> response) {
                    if (response.isSuccessful()) {
                        final MoviesResponse results = response.body();
                        if (movieList  != null)
                            movieList.clear();
                        if (results != null) {
                            movieList = results.getResults();
                        }

                        recyclerView = findViewById(R.id.recycler_view);
                        recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movieList));
                        recyclerView.smoothScrollToPosition(recycler_position);
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

            Client client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<MoviesResponse> call = apiService.getTopRatedMovies(BuildConfig.API_KEY);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(@NonNull Call<MoviesResponse> call, @NonNull Response<MoviesResponse> response) {
                    if (response.isSuccessful()) {
                        MoviesResponse results = response.body();
                        if (movieList  != null)
                            movieList.clear();
                        if (results != null) {
                            movieList = results.getResults();
                        }

                        recyclerView = findViewById(R.id.recycler_view);
                        recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movieList));
                        recyclerView.smoothScrollToPosition(recycler_position);
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

    private void initViews1() {
        progressBar = findViewById(R.id.progress);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setVisibility(View.INVISIBLE);



        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        final int numColumns = (int) (dpWidth / WIDTH_OF_COLUMNS);
        gridLayoutManager = new GridLayoutManager(MainActivity.this, numColumns);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());


        loadFavorites();

    }


    @SuppressLint("StaticFieldLeak")
    public void loadFavorites() {


        if (movieList  != null)
            movieList.clear();

        final MainViewModel viewModel = ViewModelProviders.of(MainActivity.this).get(MainViewModel.class);
        viewModel.getMovies().observe(MainActivity.this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                Log.d(TAG, "Updating list of movies from LiveData in ViewModel");
                if (movies != null) {
                    adapter = new MoviesAdapter(MainActivity.this, movies );
                    recyclerView.setAdapter(adapter);
                    recyclerView.smoothScrollToPosition(recycler_position);

                    recyclerView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);

                } else {
                    navigation.setSelectedItemId(R.id.navigation_popular);
                    Toast.makeText(MainActivity.this, R.string.no_favorite_movies, Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(context.getString(R.string.title_movie_id), gridLayoutManager.findFirstVisibleItemPosition());
        outState.putBoolean(getString(R.string.title_popular), nav_popular);
        outState.putBoolean(getString(R.string.title_favorite), nav_favorite);
        outState.putBoolean(getString(R.string.title_top_rated), nav_top_rated);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        recycler_position = savedInstanceState.getInt(getString(R.string.title_movie_id));
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(context.getString(R.string.title_popular))) {
                if (movieList != null) {
                    movieList.clear();
                }
                navigation.setSelectedItemId(R.id.navigation_popular);
                recyclerView.smoothScrollToPosition(recycler_position);
            } else if (savedInstanceState.getBoolean(context.getString(R.string.title_favorite))) {
                final MainViewModel viewModel = ViewModelProviders.of(MainActivity.this).get(MainViewModel.class);
                viewModel.getMovies().observe(MainActivity.this, new Observer<List<Movie>>() {
                    @Override
                    public void onChanged(@Nullable List<Movie> movies) {
                        if (movies != null) {
                            viewModel.getMovies().removeObserver(this);
                            adapter = new MoviesAdapter(MainActivity.this, movies );
                            recyclerView.setAdapter(adapter);
                            recyclerView.smoothScrollToPosition(recycler_position);

                            recyclerView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);

                        } else {
                            navigation.setSelectedItemId(R.id.navigation_popular);
                            Toast.makeText(MainActivity.this, R.string.no_favorite_movies, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            } else if (savedInstanceState.getBoolean(context.getString(R.string.title_top_rated))) {
                navigation.setSelectedItemId(R.id.navigation_top_rated);
                recyclerView.smoothScrollToPosition(recycler_position);

        }
    }
}


