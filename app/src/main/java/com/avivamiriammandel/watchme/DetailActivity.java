package com.avivamiriammandel.watchme;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Database;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avivamiriammandel.watchme.adapter.ReviewsAdapter;
import com.avivamiriammandel.watchme.adapter.TrailersAdapter;

import com.avivamiriammandel.watchme.database.AppDatabase;
import com.avivamiriammandel.watchme.error.ApiError;
import com.avivamiriammandel.watchme.error.ReviewErrorUtils;
import com.avivamiriammandel.watchme.error.TrailerErrorUtils;
import com.avivamiriammandel.watchme.glide.GlideApp;
import com.avivamiriammandel.watchme.glide.MoviesAppGlideModule;
import com.avivamiriammandel.watchme.model.Movie;
import com.avivamiriammandel.watchme.model.Review;
import com.avivamiriammandel.watchme.model.ReviewsResponse;
import com.avivamiriammandel.watchme.model.Trailer;
import com.avivamiriammandel.watchme.model.TrailersResponse;
import com.avivamiriammandel.watchme.rest.Client;
import com.avivamiriammandel.watchme.rest.Service;
import com.avivamiriammandel.watchme.viewmodel.AppExecutors;
import com.avivamiriammandel.watchme.viewmodel.MovieViewModel;
import com.avivamiriammandel.watchme.viewmodel.MovieViewModelFactory;
import com.github.florent37.glidepalette.GlidePalette;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();
    AppDatabase db;
    Context context;
    Movie movie;
    android.support.v4.widget.NestedScrollView detailScrollView, detailScrollViewRecycler,
            detailScrollViewRecyclerReview;
    TextView plotSynopsis, userRating, releaseDate, trailersTitle, reviewsTitle;
    ImageView backdropView;
    MaterialRatingBar ratingStars;
    RecyclerView recyclerView, recyclerViewReview;
    TrailersAdapter adapter;
    List<Trailer> trailerList;
    List<Review> reviewList;
    CardView cardView;
    Toolbar toolbar;
    BottomNavigationView navigation;
    ConstraintLayout constraintLayoutDetails, constraintLayoutRecycler,
            constraintLayoutRecyclerReview;
    Boolean recyclerNull = false, nav_detail, nav_trailer, nav_review,
            isMovie;
    float dpWidth;
    float dpHeight;

    private AppCompatActivity activity = DetailActivity.this;
    MaterialFavoriteButton materialFavoriteButton;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);




        context = getApplicationContext();

        navigation = findViewById(R.id.navigation_detail);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        toolbar = findViewById(R.id.toolbar);




        detailScrollView = findViewById(R.id.detail_scroll_view_movie_details);
        detailScrollViewRecycler = findViewById(R.id.detail_scroll_view_movie_recycler);
        detailScrollViewRecyclerReview = findViewById(R.id.detail_scroll_view_movie_recycler_review);
        backdropView = findViewById(R.id.backdrop_image_view);

        trailersTitle = findViewById(R.id.text_view_trailer_title);
        reviewsTitle = findViewById(R.id.text_view_review_title);
        plotSynopsis = findViewById(R.id.text_view_plot_synopsis);
        userRating = findViewById(R.id.text_view_user_rating);
        ratingStars = findViewById(R.id.rating_bar);
        releaseDate = findViewById(R.id.text_view_release_date);
        constraintLayoutDetails = findViewById(R.id.constraint_layout_movie_details);
        constraintLayoutRecycler = findViewById(R.id.constraint_layout_movie_recycler);
        constraintLayoutRecyclerReview = findViewById(R.id.constraint_layout_movie_recycler_review);
        materialFavoriteButton = findViewById(R.id.button_favorite);


        final Intent intent = getIntent();
        if ((intent.hasExtra(context.getString(R.string.movies_parcelable_object)))
        || (savedInstanceState != null)) {
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            dpWidth = displayMetrics.widthPixels / displayMetrics.density;
            dpHeight = displayMetrics.heightPixels / displayMetrics.density;


            detailScrollView.setVisibility(View.VISIBLE);
            detailScrollView.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            detailScrollViewRecycler.setVisibility(View.INVISIBLE);
            detailScrollViewRecycler.setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
            detailScrollViewRecyclerReview.setVisibility(View.INVISIBLE);
            detailScrollViewRecyclerReview.setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));

            fillViews(savedInstanceState);

            db = AppDatabase.getInstance(getApplicationContext());

            MovieViewModelFactory modelFactory = new MovieViewModelFactory(db, movie.getId());
            final MovieViewModel movieViewModel = ViewModelProviders.of(this, modelFactory).get(MovieViewModel.class);
            movieViewModel.getMovie().observe(DetailActivity.this, new Observer<Movie>() {
                @Override
                public void onChanged(@Nullable Movie movie) {
                    if (movie == null) {
                        movieViewModel.getMovie().removeObserver(this);
                        materialFavoriteButton.setFavorite(false);
                        // deselect the favorite button
                    } else {
                        movieViewModel.getMovie().removeObserver(this);
                        materialFavoriteButton.setFavorite(true);
                        // select the button
                    }
                }
            });





            materialFavoriteButton.setOnFavoriteChangeListener(
                    new MaterialFavoriteButton.OnFavoriteChangeListener() {
                        @Override
                        public void onFavoriteChanged(final MaterialFavoriteButton buttonView, final boolean favorite) {
                            MovieViewModelFactory modelFactory = new MovieViewModelFactory(db, movie.getId());
                            final MovieViewModel movieViewModel = ViewModelProviders.of(DetailActivity.this, modelFactory).get(MovieViewModel.class);
                            movieViewModel.getMovie().observe(DetailActivity.this, new Observer<Movie>() {
                                @Override
                                public void onChanged(@Nullable Movie movie) {
                                    Log.d(TAG, "onFavoriteChanged: " + favorite + movieViewModel.getMovie());
                                    if ((favorite) && (movie == null)) {
                                        Log.d(TAG, "onFavoriteChanged: saved favorite" );
                                        movieViewModel.getMovie().removeObserver(this);
                                        saveFavorite();
                                        Snackbar.make(buttonView, "Added to favorite",
                                                Snackbar.LENGTH_LONG).show();

                                    } else if (!(favorite) && (movie != null)) {
                                        Log.d(TAG, "onFavoriteChanged: deleted favorite" );
                                        movieViewModel.getMovie().removeObserver(this);
                                        Snackbar.make(buttonView, "Removed from favorite",
                                                Snackbar.LENGTH_LONG).show();
                                        deleteFavorite();
                                    }
                                }
                            });
                            return;
                                }

                            });



            initViews();
        }else {
            Toast.makeText(context, "No Api Data", Toast.LENGTH_SHORT).show();
        }

    }

     private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_list:
                    detailScrollView.setVisibility(View.VISIBLE);
                    detailScrollView.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    detailScrollViewRecycler.setVisibility(View.INVISIBLE);
                    detailScrollViewRecycler.setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
                    detailScrollViewRecyclerReview.setVisibility(View.INVISIBLE);
                    detailScrollViewRecyclerReview.setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
                    nav_detail = true;
                    nav_review = false;
                    nav_trailer = false;
                    return true;
                case R.id.navigation_trailer:
                    if (isOnline()) {

                    // Its Available...
                        detailScrollView.setVisibility(View.INVISIBLE);
                        detailScrollView.setLayoutParams(new ConstraintLayout.LayoutParams(0,0));
                        detailScrollViewRecycler.setVisibility(View.VISIBLE);
                        detailScrollViewRecycler.setLayoutParams(new ConstraintLayout.LayoutParams
                                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        detailScrollViewRecyclerReview.setVisibility(View.INVISIBLE);
                        detailScrollViewRecyclerReview.setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
                        trailersTitle.setText(R.string.title_trailers);
                        loadJSON();
                        nav_detail = false;
                        nav_review = false;
                        nav_trailer = true;
                    } else {
                        // Not Available...
                        Toast.makeText(context, R.string.onNoInternetError, Toast.LENGTH_LONG).show();
                        finish();

                    }
                    return true;
                case R.id.navigation_review:
                    if (isOnline()) {
                        loadJSON1();
                        detailScrollView.setVisibility(View.INVISIBLE);
                        detailScrollView.setLayoutParams(new ConstraintLayout.LayoutParams(0,0));
                        detailScrollViewRecycler.setVisibility(View.INVISIBLE);
                        detailScrollViewRecycler.setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
                        detailScrollViewRecyclerReview.setVisibility(View.VISIBLE);
                        detailScrollViewRecyclerReview.setLayoutParams(new ConstraintLayout.LayoutParams
                                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        if (recyclerNull) {
                            reviewsTitle.setText(R.string.title_reviews+ R.string.no_reviews);
                        } else {
                            reviewsTitle.setText(R.string.title_reviews);
                        }

                        nav_detail = false;
                        nav_review = true;
                        nav_trailer = false;
                        }
                    else {
                        // Not Available...
                        Toast.makeText(context, R.string.onNoInternetError, Toast.LENGTH_LONG).show();
                        finish();
                    }
                        return true;
                default:
                    return false;
            }
        }
    };

    private void fillViews(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            movie = getIntent().getParcelableExtra(context.getString(R.string.movies_parcelable_object));
            initViews();
        } else {
            movie = savedInstanceState.getParcelable(context.getString(R.string.movies_parcelable_object));
            if (savedInstanceState.getBoolean(context.getString(R.string.title_trailer))){
                navigation.setSelectedItemId(R.id.navigation_trailer);
            } else if (savedInstanceState.getBoolean(context.getString(R.string.title_review))) {
                navigation.setSelectedItemId(R.id.navigation_review);
            } else if (savedInstanceState.getBoolean(context.getString(R.string.movie_details))){
                navigation.setSelectedItemId(R.id.navigation_detail);
            }
        }



        final String thumbnailUrl = movie.getPosterPath();
        final String backdropUrl = movie.getBackdropPath();

        final String movieTitle = movie.getTitle();
        toolbar.setTitle(movieTitle);
        final String synopsis = movie.getOverview();

        final Double voteDoubleSpare = movie.getVoteAverage();
        final DecimalFormat format = new DecimalFormat("##.0");
        final String vote = (format.format(voteDoubleSpare));
        final String ratingOutOfTen = vote + " /" + " 10";
        Log.d(TAG, "onCreate: " + ratingOutOfTen);

        final Double voteInHalf = movie.getVoteAverage() / 2;
        final DecimalFormat format1 = new DecimalFormat("##.0");
        final String voteHalved = (format1.format(voteInHalf));
        ratingStars.setRating(Float.valueOf(voteHalved));


        final String release = movie.getReleaseDate();

        plotSynopsis.setText(synopsis);
        userRating.setText(ratingOutOfTen);
        releaseDate.setText(release);
        int error;
        if (dpWidth >= dpHeight)
            error = R.drawable.the_movie_db_error_loading_poster_land ;
        else
            error = R.drawable.the_movie_db_error_loading_poster ;


        try {
            GlideApp.with(context)
                    .load(thumbnailUrl)
                    .listener(GlidePalette.with(thumbnailUrl)
                            .use(GlidePalette.Profile.MUTED)
                            .intoBackground(detailScrollView)
                            .crossfade(true)

                            .use(GlidePalette.Profile.VIBRANT_DARK)
                            .intoTextColor(plotSynopsis)
                            .intoTextColor(userRating)
                            .intoTextColor(releaseDate)
                            .crossfade(true)
                    )
                    .placeholder(R.drawable.the_movie_db_loading_poster)
                    .error(error)
                    .into(backdropView);
        } catch (final IllegalArgumentException e) {
            Log.e(TAG, "onBindViewHolder:  " + e.getMessage());
        }

        try {
            GlideApp.with(context)
                    .load(backdropUrl)
                    .listener(GlidePalette.with(backdropUrl)
                            .use(GlidePalette.Profile.MUTED)
                            .intoBackground(toolbar)
                            .crossfade(true)

                    )

                    .placeholder(R.drawable.the_movie_db_loading_poster)
                    .error(error)
                    .into(backdropView);
        } catch (final IllegalArgumentException e) {
            Log.e(TAG, "onBindViewHolder:  " + e.getMessage());
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setElevation(10.f);
    }

    private void initViews(){
        trailerList = new ArrayList<>();
        adapter = new TrailersAdapter(this, trailerList);


        if (isOnline()) {
            // Its Available...
            loadJSON();
        } else {
            // Not Available...
            Toast.makeText(context, R.string.onNoInternetError, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadJSON() {
        int movieId = movie.getId();
        try {
            if (BuildConfig.API_KEY.isEmpty()) {
                Toast.makeText(getApplicationContext(), R.string.apiKeyError, Toast.LENGTH_LONG).show();
                return;
            }

            Client client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<TrailersResponse> call = apiService.getMovieTrailers(movieId, BuildConfig.API_KEY);
            call.enqueue(new Callback<TrailersResponse>() {
                @Override
                public void onResponse(@NonNull Call<TrailersResponse> call, @NonNull Response<TrailersResponse> response) {
                    if (response.isSuccessful()) {
                        TrailersResponse results = response.body();
                        trailerList = null;
                        if (results != null) {
                            recyclerView = findViewById(R.id.recycler_view_trailer_details);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(layoutManager);
                            trailerList = results.getResults();
                            recyclerView.setAdapter(new TrailersAdapter(getApplicationContext(), trailerList));
                            recyclerView.smoothScrollToPosition(0);
                        } else {

                            ApiError apiError = TrailerErrorUtils.parseError(response);
                            Toast.makeText(getApplicationContext(), apiError.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "onResponse: " + apiError.getMessage() + " " + apiError.getStatusCode() + " " + apiError.getEndpoint());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<TrailersResponse> call, @NonNull Throwable t) {
                    Toast.makeText(getApplicationContext(), R.string.onNoInternetError, Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onFailure" + t.getMessage());
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            Log.d(TAG, "on Exception" +e.getMessage());
        }
    }


    private void loadJSON1() {
        int movieId = movie.getId();
        try {
            if (BuildConfig.API_KEY.isEmpty()) {
                Toast.makeText(getApplicationContext(), R.string.apiKeyError, Toast.LENGTH_LONG).show();
                return;
            }

            Client client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<ReviewsResponse> call = apiService.getMovieReviews(movieId, BuildConfig.API_KEY);
            call.enqueue(new Callback<ReviewsResponse>() {
            @Override
                public void onResponse(@NonNull Call<ReviewsResponse> call, @NonNull Response<ReviewsResponse> response) {
                    if (response.isSuccessful()) {
                        ReviewsResponse results = response.body();
                        reviewList = null;

                        if (results != null) {
                            recyclerViewReview = findViewById(R.id.recycler_view_review_details);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerViewReview.setLayoutManager(layoutManager);

                            reviewList = results.getResults();
                            recyclerNull = false;
                            if (reviewList.size() == 0) {
                                recyclerNull = true;
                            } else {
                                recyclerViewReview.setAdapter(new ReviewsAdapter(context, reviewList));
                                recyclerViewReview.smoothScrollToPosition(0);
                            }
                        } else {

                            ApiError apiError = ReviewErrorUtils.parseError(response);
                            Toast.makeText(getApplicationContext(), apiError.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "onResponse: " + apiError.getMessage() + " " + apiError.getStatusCode() + " " + apiError.getEndpoint());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ReviewsResponse> call, @NonNull Throwable t) {
                    Toast.makeText(getApplicationContext(), R.string.onNoInternetError, Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onFailure" + t.getMessage());
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            Log.d(TAG, "on Exception" + e.getMessage());
        }
    }

    public void saveFavorite() {
        final Movie favoriteMovie = new Movie(
                movie.getId(),
                movie.getVoteAverage(),
                movie.getTitle(),
                movie.getPosterPath(),
                movie.getBackdropPath(),
                movie.getOverview(),
                movie.getReleaseDate()
                
        );

        Log.d(TAG, "saveFavorite: " + movie.getPosterPath());
        final AppDatabase database = AppDatabase.getInstance(context);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                //if (!(movie.getId().equals(database.movieDao().loadMovieById(movie.getId()).getValue().getId()))) {
                    database.movieDao().insertMovie(favoriteMovie);
                //}
            }
        });
    }

    private void deleteFavorite() {
        final Movie favoriteMovie = new Movie(
                movie.getId(),
                movie.getVoteAverage(),
                movie.getTitle(),
                movie.getPosterPath(),
                movie.getBackdropPath(),
                movie.getOverview(),
                movie.getReleaseDate()
        );
        final AppDatabase database = AppDatabase.getInstance(context);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                //if (movie.getId().equals(database.movieDao().loadMovieById(movie.getId()).getValue().getId())) {
                    database.movieDao().deleteMovie(favoriteMovie);
                //}
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        isMovie = false;
        db = AppDatabase.getInstance(getApplicationContext());

        MovieViewModelFactory modelFactory = new MovieViewModelFactory(db, movie.getId());
        final MovieViewModel movieViewModel = ViewModelProviders.of(this, modelFactory).get(MovieViewModel.class);
        movieViewModel.getMovie().observe(DetailActivity.this, new Observer<Movie>() {
                    @Override
                    public void onChanged(@Nullable Movie movie) {
                        if (movie != null) {
                            movieViewModel.getMovie().removeObserver(this);
                            isMovie = true;
                            DetailActivity.this.movie = movie;
                            Log.d(TAG, "onChanged: instant State" + DetailActivity.this.movie);
                        }
                    }
                });
        if (isMovie) {
            outState.putParcelable(getString(R.string.movies_parcelable_object), DetailActivity.this.movie);
            outState.putBoolean(getString(R.string.movie_details), nav_detail);
            outState.putBoolean(getString(R.string.title_trailer), nav_trailer);
            outState.putBoolean(getString(R.string.title_review), nav_review);
        }
    }


    public boolean isOnline() {
        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}


