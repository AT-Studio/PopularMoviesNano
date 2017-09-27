package com.example.alit.popularmoviesnano.Adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.alit.popularmoviesnano.R;
import com.example.alit.popularmoviesnano.MyDatastructures.TrailersListItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AliT on 9/26/17.
 */

public class MovieTrailersAdapter extends RecyclerView.Adapter<MovieTrailersAdapter.MovieTrailersViewHolder> {

    ArrayList<TrailersListItem> trailers;

    MovieTrailerItemClickListener listener;

    public MovieTrailersAdapter(ArrayList<TrailersListItem> trailers, MovieTrailerItemClickListener listener) {

        this.trailers = trailers;

        this.listener = listener;

    }

    @Override
    public MovieTrailersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_trailers_adapter_layout, parent, false);

        return new MovieTrailersViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MovieTrailersViewHolder holder, int position) {

        TrailersListItem item = trailers.get(position);

        holder.trailerName.setText(item.name);

    }

    @Override
    public int getItemCount() {
        return trailers.size();
    }

    public interface MovieTrailerItemClickListener {

        public void trailerClicked(int position);

    }

    public class MovieTrailersViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.trailerName) TextView trailerName;

        public MovieTrailersViewHolder(View itemView) {

            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("MovieDetailActivity", "clicked position: " + getAdapterPosition());
                    listener.trailerClicked(getAdapterPosition());
                }
            });

        }
    }

}
