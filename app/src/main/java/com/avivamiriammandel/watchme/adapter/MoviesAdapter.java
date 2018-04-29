package com.avivamiriammandel.watchme.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.avivamiriammandel.watchme.DetailActivity;
import com.avivamiriammandel.watchme.R;
import com.avivamiriammandel.watchme.model.Movie;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.ErrorRequestCoordinator;
import com.github.florent37.glidepalette.GlidePalette;

import java.util.List;

import static android.support.v4.content.ContextCompat.getDrawable;
import static com.avivamiriammandel.watchme.MainActivity.TAG;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesViewHolder> {

    private Context context;
    private List<Movie> movieList;

    public MoviesAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public MoviesAdapter.MoviesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_card, parent, false);
        return new MoviesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviesAdapter.MoviesViewHolder holder, int position) {
        holder.title.setText(movieList.get(position).getOriginalTitle());
        String ratingOutOfTen = String.valueOf(movieList.get(position).getVoteCount())+ R.string.rating_out_of_ten;
        holder.userrating.setText(ratingOutOfTen);
        String thumbnailUrl = movieList.get(position).getPosterPath();
        RequestBuilder requestBuilder;
        try {
            Glide.with(context)
                    .load(thumbnailUrl)
                    .listener(GlidePalette.with(thumbnailUrl)
                            .use(GlidePalette.Profile.VIBRANT)
                            .intoBackground(holder.cardLayout)
                            .crossfade(true)

                            .use(GlidePalette.Profile.MUTED_DARK)
                            .intoTextColor(holder.userrating)
                            .intoTextColor(holder.title)
                            .crossfade(true)
                    )
                    //.error(requestBuilder<getDrawable(context, R.drawable.the_movie_db_error_loading_poster)>, )
                    .into(holder.thumbnail)
                    .onLoadStarted(getDrawable(context, R.drawable.the_movie_db_loading_poster));


        } catch (IllegalArgumentException e) {
            Log.e(TAG, "onBindViewHolder:  "+ e.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public class MoviesViewHolder extends RecyclerView.ViewHolder{
        public ConstraintLayout cardLayout;
        public TextView title, userrating;
        public ImageView thumbnail, ratingStar;


        public MoviesViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_view_movie_title);
            userrating = itemView.findViewById(R.id.text_view_rating);
            thumbnail = itemView.findViewById(R.id.image_view_poster);
            ratingStar = itemView.findViewById(R.id.image_view_rating);
            cardLayout = itemView.findViewById(R.id.card_view_layout_root);

           itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Movie movieItemClicked = movieList.get(position);
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra("movies", (Parcelable) movieItemClicked);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                      //  context.startActivity(intent);
                    }
                }
            });
        }
    }
}
