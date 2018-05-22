package com.avivamiriammandel.watchme;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.avivamiriammandel.watchme.adapter.TrailersAdapter;
import com.avivamiriammandel.watchme.error.ApiError;
import com.avivamiriammandel.watchme.error.TrailerErrorUtils;
import com.avivamiriammandel.watchme.glide.GlideApp;
import com.avivamiriammandel.watchme.model.Movie;
import com.avivamiriammandel.watchme.model.Trailer;
import com.avivamiriammandel.watchme.model.TrailersResponse;
import com.avivamiriammandel.watchme.rest.Client;
import com.avivamiriammandel.watchme.rest.Service;
import com.github.florent37.glidepalette.BitmapPalette;
import com.github.florent37.glidepalette.GlidePalette;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.avivamiriammandel.watchme.MainActivity.TAG;

public class DetailActivity extends AppCompatActivity {


    Context context;
    Movie movie;
    android.support.v4.widget.NestedScrollView detailScrollView, detailScrollViewRecycler;
    //ScrollView detailScrollView, detailScrollViewRecycler;
    TextView plotSynopsis, userRating, releaseDate, trailersTitle;
    ImageView backdropView;
    MaterialRatingBar ratingStars;
    RecyclerView recyclerView;
    TrailersAdapter adapter;
    List<Trailer> trailerList;
    CardView cardView;
    Toolbar toolbar;
    BottomNavigationView navigation;
    ConstraintLayout constraintLayoutDetails, constraintLayoutRecycler;


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
        backdropView = findViewById(R.id.backdrop_image_view);
        trailersTitle = findViewById(R.id.text_view_trailer_title);
        plotSynopsis = findViewById(R.id.text_view_plot_synopsis);
        userRating = findViewById(R.id.text_view_user_rating);
        ratingStars = findViewById(R.id.rating_bar);
        releaseDate = findViewById(R.id.text_view_release_date);
        constraintLayoutDetails = findViewById(R.id.constraint_layout_movie_details);
        constraintLayoutRecycler = findViewById(R.id.constraint_layout_movie_recycler);


        final Intent intent = getIntent();
        if (intent.hasExtra(context.getString(R.string.movies_parcelable_object))) {
            detailScrollView.setVisibility(View.VISIBLE);
            detailScrollView.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            detailScrollViewRecycler.setVisibility(View.INVISIBLE);
            detailScrollViewRecycler.setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));

            fillViews();
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

                    return true;
                case R.id.navigation_trailer:
                    if (isOnline()) {

                    // Its Available...
                        detailScrollView.setVisibility(View.INVISIBLE);
                        detailScrollView.setLayoutParams(new ConstraintLayout.LayoutParams(0,0));
                        detailScrollViewRecycler.setVisibility(View.VISIBLE);
                        detailScrollViewRecycler.setLayoutParams(new ConstraintLayout.LayoutParams
                                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        trailersTitle.setText(R.string.title_trailers);
                        loadJSON();
                    } else {
                        // Not Available...
                        Toast.makeText(context, R.string.onNoInternetError, Toast.LENGTH_LONG).show();
                        finish();

                    }
                    return true;
                case R.id.navigation_review:

                    if (isOnline()) {
                        detailScrollView.setVisibility(View.INVISIBLE);
                        detailScrollView.setLayoutParams(new ConstraintLayout.LayoutParams(0,0));
                        detailScrollViewRecycler.setVisibility(View.VISIBLE);
                        detailScrollViewRecycler.setLayoutParams(new ConstraintLayout.LayoutParams
                                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        trailersTitle.setText(R.string.title_reviews);loadJSON1();
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

    private void fillViews() {
        movie = getIntent().getParcelableExtra(context.getString(R.string.movies_parcelable_object));

        initViews();

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
                    .error(R.drawable.the_movie_db_error_loading_poster)
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
                    .error(R.drawable.the_movie_db_error_loading_poster)
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
        recyclerView = findViewById(R.id.recycler_view_trailer_details);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

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
            Call<TrailersResponse> call = apiService.getMovieTrailers(movieId, BuildConfig.API_KEY);
            call.enqueue(new Callback<TrailersResponse>() {
                @Override
                public void onResponse(@NonNull Call<TrailersResponse> call, @NonNull Response<TrailersResponse> response) {
                    if (response.isSuccessful()) {
                        TrailersResponse results = response.body();
                        trailerList = null;
                        if (results != null) {
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
            Log.d(TAG, "on Exception" + e.getMessage());
        }
    }

    public boolean isOnline() {
        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}


