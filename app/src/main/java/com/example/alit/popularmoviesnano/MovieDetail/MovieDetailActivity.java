package com.example.alit.popularmoviesnano.MovieDetail;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alit.popularmoviesnano.MyDatastructures.MovieListItem;
import com.example.alit.popularmoviesnano.Adapters.MovieTrailersAdapter;
import com.example.alit.popularmoviesnano.NetworkUtils.NetworkUtils;
import com.example.alit.popularmoviesnano.R;
import com.example.alit.popularmoviesnano.MyDatastructures.ReviewsListItem;
import com.example.alit.popularmoviesnano.MyDatastructures.TrailersListItem;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.alit.popularmoviesnano.R.id.toolbar_layout;

public class MovieDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>,
        MovieTrailersAdapter.MovieTrailerItemClickListener {

//    private static final String posterSize = "w780";
//    private static final String posterBaseUrl = "http://image.tmdb.org/t/p/" + posterSize + "/";

    private static final String MOVIE_ID_EXTRA = "movieID";
    private static final String TITLE_EXTRA = "title";
    private static final String POSTER_URL_EXTRA = "posterURL";
    private static final String PLOT_SUMMARY_EXTRA = "plotSummary";
    private static final String USER_RATING_EXTRA = "userRating";
    private static final String RELEASE_DATE_EXTRA = "releaseDate";
    private static final String TRAILERS_QUERY_URL_EXTRA = "trailers_query";
    private static final String REVIEWS_QUERY_URL_EXTRA = "reviews_query";

    private static final String YOUTUBE_APP_BASE_URI = "vnd.youtube:";
    private static final String YOUTUBE_WEB_BASE_URI = "https://www.youtube.com/watch?v=";

    private static final int TRAILERS_LOADER_ID = 1;
    private static final int REVIEWS_LOADER_ID = 2;

    private static final int PAGER_NUM_PAGES = 2;

    public final String TAG = "MovieDetailActivity";

    @BindView(R.id.nestedScrollView) NestedScrollView nestedScrollView;

    @BindView(R.id.posterImageView) ImageView posterImageView;

    @BindView(R.id.app_bar) AppBarLayout appBarLayout;

    @BindView(toolbar_layout) CollapsingToolbarLayout toolbarLayout;

    @BindView(R.id.progressBar) ProgressBar progressBar;

    @BindView(R.id.failedTextView) TextView failedTextView;

    @BindView(R.id.titleText) TextView titleText;

    @BindView(R.id.releaseDateText) TextView releaseDateText;

    @BindView(R.id.ratingText) TextView ratingText;

    @BindView(R.id.synopsisText) TextView synopsisText;

    @BindView(R.id.trailerIcon) ImageView trailerIcon;

    @BindView(R.id.commentIcon) ImageView commentIcon;

    @BindView(R.id.numberOfTrailers) TextView numberOfTrailers;

    @BindView(R.id.numberOfReviews) TextView numberOfReviews;

    @BindView(R.id.tabLayout) TabLayout tabLayout;

    @BindView(R.id.viewPager) ViewPager viewPager;

    @BindView(R.id.followWrapper) CardView followWrapper;

    @BindView(R.id.followStarIcon) ImageView followStarIcon;

    private String movieID;
    private String title;
    private String posterURL;
    private String plotSummary;
    private float userRating;
    private String releaseDate;

    private DisplayMetrics displayMetrics;

    private Target picassoTarget;

    private String trailersQueryUrl;
    private String reviewsQueryUrl;

    private ArrayList<TrailersListItem> trailersListItems;
    private ArrayList<ReviewsListItem> reviewsListItems;

    private MovieTrailersFragment movieTrailersFragment;
    private MovieReviewsFragment movieReviewsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        displayMetrics = getResources().getDisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        if (bundle != null) {
            movieID = bundle.getString(MovieListItem.MOVIE_ID);
            title = bundle.getString(MovieListItem.ORIGINAL_TITLE);
            posterURL = bundle.getString(MovieListItem.POSTER_PATH);
            plotSummary = bundle.getString(MovieListItem.OVERVIEW);
            userRating = bundle.getFloat(MovieListItem.VOTE_AVERAGE);
            releaseDate = bundle.getString(MovieListItem.RELEASE_DATE);
        }
        else if (savedInstanceState != null) {
            trailersQueryUrl = savedInstanceState.getString(TRAILERS_QUERY_URL_EXTRA);
            reviewsQueryUrl = savedInstanceState.getString(REVIEWS_QUERY_URL_EXTRA);
            movieID = savedInstanceState.getString(MOVIE_ID_EXTRA);
            title = savedInstanceState.getString(TITLE_EXTRA);
            posterURL = savedInstanceState.getString(POSTER_URL_EXTRA);
            plotSummary = savedInstanceState.getString(PLOT_SUMMARY_EXTRA);
            userRating = savedInstanceState.getFloat(USER_RATING_EXTRA);
            releaseDate = savedInstanceState.getString(RELEASE_DATE_EXTRA);
        }
        else {
            finish();
        }

        titleText.setText(title);
        releaseDateText.setText(releaseDate);
        if ((int) userRating == 10) ratingText.setText("10/10");
        else ratingText.setText(userRating + "/10");
        synopsisText.setText(plotSummary);

        picassoTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                float ratio = (float) bitmap.getHeight() / bitmap.getWidth();

                int height = (int) (displayMetrics.widthPixels * ratio);

                progressBar.setVisibility(View.GONE);

                animateAppBar(height, bitmap);

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                progressBar.setVisibility(View.INVISIBLE);
                failedTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.icons), PorterDuff.Mode.SRC_IN);

        progressBar.setVisibility(View.VISIBLE);

        Picasso.with(this).load(NetworkUtils.getPosterBaseUrl() + posterURL).into(picassoTarget);

        tabLayout.post(new Runnable() {
            @Override
            public void run() {

                Log.d("pagerStuff", "tabLayout height: " + tabLayout.getHeight());
                Log.d("pagerStuff", "actionBar height: " + getSupportActionBar().getHeight());

                int pagerHeight = displayMetrics.heightPixels - (tabLayout.getHeight() + getSupportActionBar().getHeight() + getStatusBarHeight());

                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) viewPager.getLayoutParams();
                params.height = pagerHeight;
                viewPager.setLayoutParams(params);

            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    toolbarLayout.setTitle(title);
                }
                else toolbarLayout.setTitle(" ");
            }
        });

        trailerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        commentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        followWrapper.setOnClickListener(new View.OnClickListener() {

            boolean isFollwing;


            @Override
            public void onClick(View view) {
                if (isFollwing) {
                    followStarIcon.setImageDrawable(ContextCompat.getDrawable(MovieDetailActivity.this, R.drawable.star_unchecked_24dp));
                }
                else {
                    followStarIcon.setImageDrawable(ContextCompat.getDrawable(MovieDetailActivity.this, R.drawable.star_checked_24dp));
                    Toast.makeText(MovieDetailActivity.this, "You have added " + title + " to your favorites!", Toast.LENGTH_LONG).show();
                }
                isFollwing = !isFollwing;
            }
        });

        setUpPager();
    }

    public void setUpPager() {
        tabLayout.setupWithViewPager(viewPager);
        TrailersAndReviewsPagerAdapter pagerAdapter = new TrailersAndReviewsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            switch (i) {
                case (0) :
                    tabLayout.getTabAt(0).setIcon(R.drawable.trailer_icon_selector);
                    break;
                case (1) :
                    tabLayout.getTabAt(1).setIcon(R.drawable.comment_icon_selector);
                    break;
            }
        }
        getMovieTrailers();
        getMovieReviews();
    }

    public void playTrailer(String key) {

        Log.d("MovieDetailActivity", "called playTrailer");

        Intent YouTubeAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_APP_BASE_URI + key));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_WEB_BASE_URI + key));

        try {
            Log.d("MovieDetailActivity", "trying to start youtube app");
            startActivity(YouTubeAppIntent);
        }
        catch (ActivityNotFoundException e) {
            Log.d("MovieDetailActivity", "failed to open youtube app");
            startActivity(webIntent);
        }

    }

    public void animateAppBar(final int finalHeight, final Bitmap bitmap) {

        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(appBarLayout.getHeight(), finalHeight + getStatusBarHeight()).setDuration(400);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.height = (int) valueAnimator.getAnimatedValue();
                appBarLayout.setLayoutParams(params);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                CollapsingToolbarLayout.LayoutParams params1 = (CollapsingToolbarLayout.LayoutParams) posterImageView.getLayoutParams();
                params1.height = finalHeight;
                posterImageView.setLayoutParams(params1);
                posterImageView.setImageBitmap(bitmap);
//                nestedScrollView.fullScroll(View.FOCUS_UP);
                fadeIn(posterImageView, 200);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();

    }

    public void getMovieTrailers() {

        if (trailersQueryUrl == null) trailersQueryUrl = NetworkUtils.getMovieTrailersUrl(movieID, getResources());

        LoaderManager manager = getSupportLoaderManager();

        manager.restartLoader(TRAILERS_LOADER_ID, null, this);

    }

    public void getMovieReviews() {

        if (reviewsQueryUrl == null) reviewsQueryUrl = NetworkUtils.getMovieReviewsUrl(movieID, getResources());

        LoaderManager manager = getSupportLoaderManager();

        manager.restartLoader(REVIEWS_LOADER_ID, null, this);

    }

    public void fadeIn(View layout, int time)    {
        layout.setAlpha(0f);
        layout.setVisibility(View.VISIBLE);
        layout.animate()
                .alpha(1f)
                .setDuration(time)
                .setListener(null);

    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<String> onCreateLoader(final int id, Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            String JSONResponse;

            @Override
            protected void onStartLoading() {

                if (JSONResponse != null) deliverResult(JSONResponse);
                else {
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {

                URL url;

                if (id == TRAILERS_LOADER_ID) {
                    Log.d(TAG, "trailerQueryUrl: " + trailersQueryUrl);
                    url = NetworkUtils.getURL(trailersQueryUrl);
                }
                else {
                    Log.d(TAG, "reviewsQueryUrl: " + reviewsQueryUrl);
                    url = NetworkUtils.getURL(reviewsQueryUrl);
                }

                StringBuilder stringBuilder = new StringBuilder();

                HttpURLConnection connection = null;

                try {
                    connection = (HttpURLConnection) url.openConnection();
                    Scanner scanner = new Scanner(connection.getInputStream());
                    scanner.useDelimiter("\\A");
                    while (scanner.hasNext()) {
                        String next = scanner.next();
                        stringBuilder.append(next);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    if (connection != null) connection.disconnect();
                }

                return stringBuilder.toString();

            }

            @Override
            public void deliverResult(String data) {
                JSONResponse = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (loader.getId() == TRAILERS_LOADER_ID) {

            if (movieTrailersFragment == null) {
                setUpPager();
                return;
            }

            trailersListItems = NetworkUtils.getMovieTrailerList(data);

            if (trailersListItems == null) {
                //TODO: show error;
                movieTrailersFragment.showError();
                return;
            }
            else if (trailersListItems.isEmpty()) {
                movieTrailersFragment.showNoData();
            }
            else {
                movieTrailersFragment.setTrailerData(trailersListItems);
            }

//            if (trailersListItems == null) {
//                //TODO: show error
//                Log.d(TAG, "there was an error");
//                return;
//            }
//
//            for (TrailersListItem listItem : trailersListItems) {
//
//                Log.d(TAG, "trailer name: " + listItem.name);
//
//            }
//
//            numberOfTrailers.setText("(" + trailersListItems.size() + ")");
//
//            //TODO: bug where it wont show recyclerView.. is fragment null?
//
//            if (movieTrailersFragment != null) movieTrailersFragment.setTrailerData(trailersListItems);

        }
        else {

            if (movieReviewsFragment == null) {
                setUpPager();
                return;
            }

            reviewsListItems = NetworkUtils.getMovieReviewsList(data);

            if (reviewsListItems == null) {
                //TODO: show error;
                movieReviewsFragment.showError();
                return;
            }
            else if (reviewsListItems.isEmpty()) {
                movieReviewsFragment.showNoData();
            }
            else {
                movieReviewsFragment.setReviewsData(reviewsListItems);
            }

//            for (ReviewsListItem listItem : reviewsListItems) {
//
//                Log.d(TAG, "review author: " + listItem.author);
//
//            }
//
//            numberOfReviews.setText("(" + reviewsListItems.size() + ")");
//
//            if (movieReviewsFragment != null) movieReviewsFragment.setReviewsData(reviewsListItems);

        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    public class TrailersAndReviewsPagerAdapter extends FragmentStatePagerAdapter {

        String[] pageTitles = {"Trailers", "Reviews"};

        public TrailersAndReviewsPagerAdapter(FragmentManager fm)  {
            super(fm);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                //TODO: create Trailers Fragment
                return new MovieTrailersFragment();
            }
            else {
                //TODO: create Reviews Fragment
                return new MovieReviewsFragment();
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            switch (position)   {
                case 0 : movieTrailersFragment = (MovieTrailersFragment) fragment; break;
                case 1 : movieReviewsFragment = (MovieReviewsFragment) fragment; break;
                default: return null;
            }
            return super.instantiateItem(container, position);
        }

        @Override
        public int getCount() {
            return PAGER_NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
        }
    }

    @Override
    public void trailerClicked(int position) {
        Log.d("MovieDetailActivity", "triggered trailerClicked");
        playTrailer(trailersListItems.get(position).trailerKey);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (trailersQueryUrl != null) outState.putString(TRAILERS_QUERY_URL_EXTRA, trailersQueryUrl);
        if (reviewsQueryUrl != null) outState.putString(REVIEWS_QUERY_URL_EXTRA, reviewsQueryUrl);
        outState.putString(MOVIE_ID_EXTRA, movieID);
        outState.putString(TITLE_EXTRA, title);
        outState.putString(POSTER_URL_EXTRA, posterURL);
        outState.putString(PLOT_SUMMARY_EXTRA, plotSummary);
        outState.putFloat(USER_RATING_EXTRA, userRating);
        outState.putString(RELEASE_DATE_EXTRA, releaseDate);
        super.onSaveInstanceState(outState);
    }
}
