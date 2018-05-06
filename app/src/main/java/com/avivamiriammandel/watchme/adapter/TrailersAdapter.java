package com.avivamiriammandel.watchme.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.avivamiriammandel.watchme.R;
import com.avivamiriammandel.watchme.model.Trailer;

import java.util.List;

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailersViewHolder> {
    private Context context;
    private List<Trailer> trailerList;

    public TrailersAdapter(Context context, List<Trailer> trailerList) {
        this.context = context;
        this.trailerList = trailerList;
    }

    @NonNull
    @Override
    public TrailersAdapter.TrailersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trailer_card, parent, false);
        return new TrailersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailersAdapter.TrailersViewHolder holder, int position) {
        holder.title.setText(trailerList.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return trailerList.size();
    }

    public class TrailersViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public ImageView thumbnail;

        public TrailersViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_view_trailer_name);
            thumbnail = itemView.findViewById(R.id.image_view_youtube);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        Trailer clickedDataItem = trailerList.get(position);
                        String videoId = clickedDataItem.getKey();
                        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
                        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.youtube.com/watch?v=" + videoId));
                        try {
                            context.startActivity(appIntent);
                        } catch (ActivityNotFoundException ex) {
                            context.startActivity(webIntent);
                        }
                    }
                }
            });
        }
    }
}
