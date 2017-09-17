package com.example.alit.popularmoviesnano;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AliT on 9/16/17.
 */

public class MoviePosterAdapter extends RecyclerView.Adapter<MoviePosterAdapter.MovieViewHolder> {

    private ArrayList<String> posterURLs;
    private MoviePosterItemClickListener clickListener;
    private Context context;

    private final String size = "w342";
    private final String baseUrl = "http://image.tmdb.org/t/p/" + size + "/";

    public MoviePosterAdapter(ArrayList<String> posterURls, MoviePosterItemClickListener clickListener) {
        this.posterURLs = posterURls;
        this.clickListener = clickListener;
        this.context = (AppCompatActivity) clickListener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.movie_poster_item_layout, parent, false);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        Picasso.with(context).load(baseUrl + posterURLs.get(position)).into(holder.posterImageView);

    }

    @Override
    public int getItemCount() {
        return posterURLs.size();
    }

    public void updateMovies() {
        notifyDataSetChanged();
    }

    public interface MoviePosterItemClickListener {

        void movieItemClicked(int position);

    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.posterImageView) ImageView posterImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.movieItemClicked(getAdapterPosition());
        }
    }
}
