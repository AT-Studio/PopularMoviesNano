package com.example.alit.popularmoviesnano.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.alit.popularmoviesnano.R;
import com.example.alit.popularmoviesnano.MyDatastructures.ReviewsListItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AliT on 9/26/17.
 */

public class MovieReviewsAdapter extends RecyclerView.Adapter<MovieReviewsAdapter.MovieReviewsViewHolder> {

    ArrayList<ReviewsListItem> reviews;

    public MovieReviewsAdapter(ArrayList<ReviewsListItem> reviews) {

        this.reviews = reviews;

    }

    @Override
    public MovieReviewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_reviews_adapter_layout, parent, false);

        return new MovieReviewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MovieReviewsViewHolder holder, int position) {

        ReviewsListItem item = reviews.get(position);

        holder.author.setText(item.author);

        holder.content.setText(item.content);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class MovieReviewsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.author) TextView author;

        @BindView(R.id.content) TextView content;

        public MovieReviewsViewHolder(View itemView) {

            super(itemView);

            ButterKnife.bind(this, itemView);

        }

    }
}
