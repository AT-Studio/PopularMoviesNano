package com.example.alit.popularmoviesnano.MovieDetail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.alit.popularmoviesnano.Adapters.MovieReviewsAdapter;
import com.example.alit.popularmoviesnano.R;
import com.example.alit.popularmoviesnano.MyDatastructures.ReviewsListItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by AliT on 9/26/17.
 */

public class MovieReviewsFragment extends Fragment {

    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    LinearLayoutManager layoutManager;

    @BindView(R.id.progressBar) ProgressBar progressBar;

    @BindView(R.id.errorTextView) TextView errorTextView;

    @BindView(R.id.noDataTextView) TextView noDataTextView;

    Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.movie_reviews_fragment_layout, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView.setFocusable(false);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

    }

    public void setReviewsData(ArrayList<ReviewsListItem> reviews) {

        progressBar.setVisibility(View.INVISIBLE);
        adapter = new MovieReviewsAdapter(reviews);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);

    }

    public void showError() {

        progressBar.setVisibility(View.INVISIBLE);
        errorTextView.setVisibility(View.VISIBLE);

    }

    public void showNoData() {

        progressBar.setVisibility(View.INVISIBLE);
        noDataTextView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
