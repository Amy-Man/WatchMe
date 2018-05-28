package com.avivamiriammandel.watchme.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.avivamiriammandel.watchme.DetailActivity;
import com.avivamiriammandel.watchme.R;
import com.avivamiriammandel.watchme.data.FavoriteContract;
import com.avivamiriammandel.watchme.glide.GlideApp;
import com.avivamiriammandel.watchme.model.Movie;
import com.avivamiriammandel.watchme.model.MoviesResponse;
import com.github.florent37.glidepalette.GlidePalette;

import java.text.DecimalFormat;
import java.util.List;



public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    Cursor cursor;
    private Context context;
    private List<Movie> favoriteMoviesList;
    private static final String TAG = FavoritesAdapter.class.getSimpleName();

    public FavoritesAdapter(Cursor cursor, Context context) {
        this.cursor = cursor;
        this.context = context;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_card, parent, false);

        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.title.setText(cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_TITLE)));
        //Double voteDoubleSpare = cursor.getDouble(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_USER_RATING));
        //DecimalFormat f = new DecimalFormat("##.0");
        //String vote = (f.format(voteDoubleSpare));
        //String ratingOutOfTen = vote + " /" + "10";
        holder.userRating.setText("");
        String thumbnailUrl = (cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_POSTER_PATH)));
        try {
            GlideApp.with(context)
                    .load(thumbnailUrl)
                    .listener(GlidePalette.with(thumbnailUrl)
                            .use(GlidePalette.Profile.MUTED)
                            .intoBackground(holder.cardView)
                            .crossfade(true)

                            .use(GlidePalette.Profile.VIBRANT_DARK)
                            .intoTextColor(holder.userRating)
                            .intoTextColor(holder.title)
                            .crossfade(true)
                    )
                    .placeholder(R.drawable.the_movie_db_loading_poster)
                    .error(R.drawable.the_movie_db_error_loading_poster)
                    .into(holder.thumbnail);

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "onBindViewHolder:  "+ e.getMessage());
        }

    }


    @Override
    public int getItemCount() {
        return (cursor != null)?cursor.getCount():0;
    }

    public class FavoritesViewHolder extends RecyclerView.ViewHolder{
        public ConstraintLayout cardLayout;
        public CardView cardView;
        public TextView title, userRating;
        public ImageView thumbnail, ratingStar;


        public FavoritesViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_view_movie_title);
            userRating = itemView.findViewById(R.id.text_view_rating);
            thumbnail = itemView.findViewById(R.id.image_view_poster);
            ratingStar = itemView.findViewById(R.id.image_view_rating);
            cardLayout = itemView.findViewById(R.id.card_view_layout_root);
            cardView = itemView.findViewById(R.id.card_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Movie movieItemClicked = favoriteMoviesList.get(position);
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra(context.getString(R.string.movies_parcelable_object), (Parcelable) movieItemClicked);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            });
        }
    }


    public void swapCursor(Cursor cursorToSwap) {
        if (cursor != null){
            cursor.close();
        }
        cursor = cursorToSwap;
        notifyDataSetChanged();
    }

}


