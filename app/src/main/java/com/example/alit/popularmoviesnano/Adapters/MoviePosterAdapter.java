package com.example.alit.popularmoviesnano.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alit.popularmoviesnano.MyDatastructures.MovieListItem;
import com.example.alit.popularmoviesnano.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AliT on 9/16/17.
 */

public class MoviePosterAdapter extends RecyclerView.Adapter<MoviePosterAdapter.MovieViewHolder> {

    private ArrayList<MovieListItem> movieListItems;
    private MoviePosterItemClickListener clickListener;
    private Context context;

    private final String size = "w342";
    private final String baseUrl = "http://image.tmdb.org/t/p/" + size + "/";

    Drawable loadingDrawable;
    Drawable errorDrawable;

    public MoviePosterAdapter(ArrayList<MovieListItem> movieListItems, MoviePosterItemClickListener clickListener) {
        this.movieListItems = movieListItems;
        this.clickListener = clickListener;
        this.context = (AppCompatActivity) clickListener;
        this.loadingDrawable = ContextCompat.getDrawable(context, R.drawable.cloud_loading);
        this.errorDrawable = ContextCompat.getDrawable(context, R.drawable.error_frame);
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.movie_poster_item_layout, parent, false);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MovieViewHolder holder, int position) {

        holder.movieTitle.setVisibility(View.INVISIBLE);

        final MovieListItem item = movieListItems.get(position);

        if (item.poster != null) {
            holder.posterImageView.setImageBitmap(item.poster);
        }
        else {
            Picasso.with(context).load(baseUrl + item.posterURL)
                    .placeholder(loadingDrawable)
                    .error(errorDrawable)
                    .into(holder.posterImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            holder.movieTitle.setText(item.title);
                            holder.movieTitle.setVisibility(View.VISIBLE);
                        }
                    });
        }

    }

    @Override
    public int getItemCount() {
        return movieListItems.size();
    }

    public void updateMovies(ArrayList<MovieListItem> newList) {
        movieListItems = newList;
        notifyDataSetChanged();
    }

    public interface MoviePosterItemClickListener {

        void movieItemClicked(int position);

    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.posterImageView) ImageView posterImageView;

        @BindView(R.id.movieTitle) TextView movieTitle;

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
