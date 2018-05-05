package com.avivamiriammandel.watchme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.avivamiriammandel.watchme.glide.GlideApp;
import com.avivamiriammandel.watchme.model.Movie;
import com.github.florent37.glidepalette.BitmapPalette;
import com.github.florent37.glidepalette.GlidePalette;

import java.text.DecimalFormat;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static com.avivamiriammandel.watchme.MainActivity.TAG;

public class DetailActivity extends AppCompatActivity {

    Context context;
    Movie movie;
    ScrollView detailScrollView;
    TextView movieName, plotSynopsis, userRating, releaseDate;
    ImageView imageView, backdropView;
    MaterialRatingBar ratingStars;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);



        context = getApplicationContext();

        Toolbar toolbar = findViewById(R.id.toolbar);



        detailScrollView = findViewById(R.id.detail_scroll_view);
        backdropView = findViewById(R.id.backdrop_image_view);

        //movieName = findViewById(R.id.text_view_movie_name);
        plotSynopsis = findViewById(R.id.text_view_plot_synopsis);
        userRating = findViewById(R.id.text_view_user_rating);
        ratingStars = findViewById(R.id.rating_bar);
        releaseDate = findViewById(R.id.text_view_release_date);


        Intent intent = getIntent();
        if (intent.hasExtra(context.getString(R.string.movies_parcelable_object))) {
            movie = getIntent().getParcelableExtra(context.getString(R.string.movies_parcelable_object));

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
                                .intoTextColor(movieName)
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
                                .crossfade(true)

                        )

                        .placeholder(R.drawable.the_movie_db_loading_poster)
                        .error(R.drawable.the_movie_db_error_loading_poster)
                        .into(backdropView);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "onBindViewHolder:  " + e.getMessage());
            }
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setElevation(10.f);
        }else {
            Toast.makeText(context, "No Api Data", Toast.LENGTH_SHORT).show();
        }

    }
}
