package com.avivamiriammandel.watchme;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.avivamiriammandel.watchme.adapter.TrailersAdapter;
import com.avivamiriammandel.watchme.error.ApiError;
import com.avivamiriammandel.watchme.error.TrailerErrorUtils;
import com.avivamiriammandel.watchme.glide.GlideApp;
import com.avivamiriammandel.watchme.internet.ConnectivityReceiver;
import com.avivamiriammandel.watchme.internet.MyApplication;
import com.avivamiriammandel.watchme.model.Movie;
import com.avivamiriammandel.watchme.model.Trailer;
import com.avivamiriammandel.watchme.model.TrailersResponse;
import com.avivamiriammandel.watchme.rest.Client;
import com.avivamiriammandel.watchme.rest.Service;
import com.github.florent37.glidepalette.GlidePalette;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.avivamiriammandel.watchme.MainActivity.TAG;

public class DetailActivity extends AppCompatActivity
        implements ConnectivityReceiver.ConnectivityReceiverListener{

    Context context;
    Movie movie;
    ScrollView detailScrollView;
    TextView plotSynopsis, userRating, releaseDate, trailersTitle;
    ImageView backdropView;
    MaterialRatingBar ratingStars;
    RecyclerView recyclerView;
    TrailersAdapter adapter;
    List<Trailer> trailerList;
    CardView cardView;
    public Boolean connected;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        context = getApplicationContext();

        Toolbar toolbar = findViewById(R.id.toolbar);



        detailScrollView = findViewById(R.id.detail_scroll_view);
        backdropView = findViewById(R.id.backdrop_image_view);
        trailersTitle = findViewById(R.id.text_view_trailer_title);
        plotSynopsis = findViewById(R.id.text_view_plot_synopsis);
        userRating = findViewById(R.id.text_view_user_rating);
        ratingStars = findViewById(R.id.rating_bar);
        releaseDate = findViewById(R.id.text_view_release_date);


        Intent intent = getIntent();
        if (intent.hasExtra(context.getString(R.string.movies_parcelable_object))) {

            movie = getIntent().getParcelableExtra(context.getString(R.string.movies_parcelable_object));

            initViews();

            CardView cardView = findViewById(R.id.trailer_card_view);
            String thumbnailUrl = movie.getPosterPath();
            String backdropUrl = movie.getBackdropPath();

            String movieTitle = movie.getTitle();
            toolbar.setTitle(movieTitle);
            String synopsis = movie.getOverview();

            Double voteDoubleSpare = movie.getVoteAverage();
            DecimalFormat format = new DecimalFormat("##.0");
            String vote = (format.format(voteDoubleSpare));
            String ratingOutOfTen = vote + " /" + " 10";
            Log.d(TAG, "onCreate: " + ratingOutOfTen);

            Double voteInHalf = movie.getVoteAverage() / 2;
            DecimalFormat format1 = new DecimalFormat("##.0");
            String voteHalved = (format1.format(voteInHalf));
            ratingStars.setRating(Float.valueOf(voteHalved));
            recyclerView = findViewById(R.id.recycler_view_trailer_details);

            String release = movie.getReleaseDate();

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
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "onBindViewHolder:  " + e.getMessage());
            }

            try {
                GlideApp.with(context)
                        .load(backdropUrl)
                        .listener(GlidePalette.with(backdropUrl)
                                .use(GlidePalette.Profile.MUTED)
                                .intoBackground(toolbar)
                                .intoBackground(detailScrollView)
                                .crossfade(true)

                                .use(GlidePalette.Profile.VIBRANT_DARK)
                                .intoTextColor(trailersTitle)
                                .intoTextColor(plotSynopsis)
                                .intoTextColor(userRating)
                                .intoTextColor(releaseDate)
                                .crossfade(true)
                        )
                        .placeholder(R.drawable.the_movie_db_loading_poster)
                        .error(R.drawable.the_movie_db_error_loading_poster)
                        .into(backdropView);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "onBindViewHolder:  " + e.getMessage());
            }
            setSupportActionBar(toolbar);
            toolbar.setElevation(10.f);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }else {
            Toast.makeText(context, "No Api Data", Toast.LENGTH_SHORT).show();
        }

    }

    private void initViews(){
        trailerList = new ArrayList<>();
        adapter = new TrailersAdapter(this, trailerList);
        recyclerView = findViewById(R.id.recycler_view_trailer_details);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        checkConnection();
        if (connected)
            loadJSON();
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
                public void onResponse(Call<TrailersResponse> call, Response<TrailersResponse> response) {
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
                public void onFailure(Call<TrailersResponse> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), R.string.onNoInternetError, Toast.LENGTH_LONG).show();
                    Log.d(TAG, t.getMessage());
                    }
                });
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                Log.d(TAG, e.getMessage());
            }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            connected = true;
        } else {
            connected = false;
        }
    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        if (isConnected) {
            connected = true;
        } else {
            connected = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        ConnectivityReceiver connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, intentFilter);

        /*register connection status listener*/
        MyApplication.getInstance().setConnectivityListener(this);
    }
}
