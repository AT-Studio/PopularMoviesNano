package com.example.alit.popularmoviesnano.MovieDetail;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alit.popularmoviesnano.Adapters.MovieTrailersAdapter;
import com.example.alit.popularmoviesnano.ContentProvider.MovieDbContract;
import com.example.alit.popularmoviesnano.MyDatastructures.MovieListItem;
import com.example.alit.popularmoviesnano.MyDatastructures.ReviewsListItem;
import com.example.alit.popularmoviesnano.MyDatastructures.TrailersListItem;
import com.example.alit.popularmoviesnano.R;
import com.example.alit.popularmoviesnano.Utils.DbUtils;
import com.example.alit.popularmoviesnano.Utils.NetworkUtils;
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

    private static final String TRAILERS_QUERY_URL_EXTRA = "trailers_query";
    private static final String REVIEWS_QUERY_URL_EXTRA = "reviews_query";

    private static final String YOUTUBE_APP_BASE_URI = "vnd.youtube:";
    private static final String YOUTUBE_WEB_BASE_URI = "https://www.youtube.com/watch?v=";

    private static final int TRAILERS_LOADER_ID = 1;
    private static final int REVIEWS_LOADER_ID = 2;
    private static final int CURSOR_LOADER_ID = 3;

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

    @BindView(R.id.tabLayout) TabLayout tabLayout;

    @BindView(R.id.viewPager) ViewPager viewPager;

    @BindView(R.id.followWrapper) CardView followWrapper;

    @BindView(R.id.followStarIcon) ImageView followStarIcon;

    private int movieID;
    private String title;
    private String posterURL;
    private String plotSummary;
    private float userRating;
    private String releaseDate;
    private Bitmap poster;

    private String posterLocation;

    private DisplayMetrics displayMetrics;

    private Target picassoTarget;

    private String trailersQueryUrl;
    private String reviewsQueryUrl;

    private ArrayList<TrailersListItem> trailersListItems;
    private ArrayList<ReviewsListItem> reviewsListItems;

    private MovieTrailersFragment movieTrailersFragment;
    private MovieReviewsFragment movieReviewsFragment;

    boolean pictureIsSet;
    boolean databaseIsQueried;

    boolean isFollowed;

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

        followWrapper.setVisibility(View.INVISIBLE);

        if (bundle != null) {
            movieID = bundle.getInt(MovieListItem.MOVIE_ID);
            title = bundle.getString(MovieListItem.ORIGINAL_TITLE);
            posterLocation = bundle.getString(MovieListItem.POSTER);
            if (posterLocation == null) posterURL = bundle.getString(MovieListItem.POSTER_PATH);
            plotSummary = bundle.getString(MovieListItem.OVERVIEW);
            userRating = bundle.getFloat(MovieListItem.VOTE_AVERAGE);
            releaseDate = bundle.getString(MovieListItem.RELEASE_DATE);
        }
        else if (savedInstanceState != null) {
            trailersQueryUrl = savedInstanceState.getString(TRAILERS_QUERY_URL_EXTRA);
            reviewsQueryUrl = savedInstanceState.getString(REVIEWS_QUERY_URL_EXTRA);
            movieID = savedInstanceState.getInt(MovieListItem.MOVIE_ID);
            title = savedInstanceState.getString(MovieListItem.ORIGINAL_TITLE);
            posterLocation = savedInstanceState.getString(MovieListItem.POSTER);
            if (posterLocation == null) posterURL = savedInstanceState.getString(MovieListItem.POSTER_PATH);
            plotSummary = savedInstanceState.getString(MovieListItem.OVERVIEW);
            userRating = savedInstanceState.getFloat(MovieListItem.VOTE_AVERAGE);
            releaseDate = savedInstanceState.getString(MovieListItem.RELEASE_DATE);
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

                poster = bitmap;

                animateAppBar(height);

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

        tabLayout.post(new Runnable() {
            @Override
            public void run() {

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

        followWrapper.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (isFollowed) {

                    getContentResolver().delete(ContentUris.withAppendedId(MovieDbContract.MovieEntry.CONTENT_URI, movieID),
                            null,
                            null);

                }
                else {

                    followWrapper.setEnabled(false);

                    AsyncTask insert = new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] objects) {

                            Drawable drawable = (Drawable) objects[0];

                            ContentValues values = new ContentValues();

                            values.put(MovieDbContract.MovieEntry.COLUMN_MOVIE_ID, movieID);
                            values.put(MovieDbContract.MovieEntry.COLUMN_ORIGINAL_TITLE, title);
                            values.put(MovieDbContract.MovieEntry.COLUMN_POSTER, DbUtils.getByteArray(drawable));
                            values.put(MovieDbContract.MovieEntry.COLUMN_OVERVIEW, plotSummary);
                            values.put(MovieDbContract.MovieEntry.COLUMN_VOTE_AVERAGE, userRating);
                            values.put(MovieDbContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);

                            getContentResolver().insert(MovieDbContract.MovieEntry.CONTENT_URI, values);

                            return null;
                        }
                    };

                    insert.execute(posterImageView.getDrawable());
                }
            }
        });

        if (posterLocation != null) {

            poster = BitmapFactory.decodeFile(posterLocation);

            float ratio = (float) poster.getHeight() / poster.getWidth();

            int height = (int) (displayMetrics.widthPixels * ratio);

            progressBar.setVisibility(View.GONE);

            final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
            params.height = height + getStatusBarHeight();
            appBarLayout.setLayoutParams(params);

            CollapsingToolbarLayout.LayoutParams params1 = (CollapsingToolbarLayout.LayoutParams) posterImageView.getLayoutParams();
            params1.height = height;
            posterImageView.setLayoutParams(params1);
            posterImageView.setImageBitmap(poster);
            posterImageView.setVisibility(View.VISIBLE);
            pictureIsSet = true;
            if (databaseIsQueried) {
                fadeIn(followWrapper, 200);
            }

        }
        else {
            Picasso.with(this).load(NetworkUtils.getPosterBaseUrl() + posterURL).into(picassoTarget);
        }

        setUpPager();
        queryDb();
    }

    public void queryDb() {

        LoaderManager.LoaderCallbacks<Cursor> checkDb = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(MovieDetailActivity.this,
                        ContentUris.withAppendedId(MovieDbContract.MovieEntry.CONTENT_URI, movieID),
                        null,
                        null,
                        null,
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

                if (!(data.moveToFirst()) || data.getCount()== 0) {
                    isFollowed = false;
                    followStarIcon.setImageDrawable(ContextCompat.getDrawable(MovieDetailActivity.this, R.drawable.star_unchecked_24dp));
                }
                else {
                    isFollowed = true;
                    followStarIcon.setImageDrawable(ContextCompat.getDrawable(MovieDetailActivity.this, R.drawable.star_checked_24dp));
                    if (followWrapper.getVisibility() == View.VISIBLE) {
                        Toast.makeText(MovieDetailActivity.this, "You have added " + title + " to your favorites!", Toast.LENGTH_LONG).show();
                    }
                    followWrapper.setEnabled(true);
                }


                databaseIsQueried = true;
                if (pictureIsSet) {
                    if (followWrapper.getVisibility() == View.INVISIBLE) fadeIn(followWrapper, 200);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };

        LoaderManager loaderManager = getSupportLoaderManager();

        loaderManager.restartLoader(CURSOR_LOADER_ID, null, checkDb);

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

    public void shareMovieYouTubeWebLink(int position) {

        String link = YOUTUBE_WEB_BASE_URI + trailersListItems.get(position).trailerKey;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, link);
        startActivity(Intent.createChooser(shareIntent, "Share trailer using"));

    }

    public void playTrailer(String key) {

        Intent YouTubeAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_APP_BASE_URI + key));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_WEB_BASE_URI + key));

        try {
            startActivity(YouTubeAppIntent);
        }
        catch (ActivityNotFoundException e) {
            startActivity(webIntent);
        }

    }

    public void animateAppBar(final int finalHeight) {

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
                posterImageView.setImageBitmap(poster);
                pictureIsSet = true;
                if (databaseIsQueried) {
                    fadeIn(followWrapper, 200);
                }
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

        if (trailersQueryUrl == null) trailersQueryUrl = NetworkUtils.getMovieTrailersUrl(Integer.toString(movieID), getResources());

        LoaderManager manager = getSupportLoaderManager();

        manager.restartLoader(TRAILERS_LOADER_ID, null, this);

    }

    public void getMovieReviews() {

        if (reviewsQueryUrl == null) reviewsQueryUrl = NetworkUtils.getMovieReviewsUrl(Integer.toString(movieID), getResources());

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
                    url = NetworkUtils.getURL(trailersQueryUrl);
                }
                else {
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
                movieTrailersFragment.showError();
                return;
            }
            else if (trailersListItems.isEmpty()) {
                movieTrailersFragment.showNoData();
            }
            else {
                movieTrailersFragment.setTrailerData(trailersListItems);
            }

        }
        else {

            if (movieReviewsFragment == null) {
                setUpPager();
                return;
            }

            reviewsListItems = NetworkUtils.getMovieReviewsList(data);

            if (reviewsListItems == null) {
                movieReviewsFragment.showError();
            }
            else if (reviewsListItems.isEmpty()) {
                movieReviewsFragment.showNoData();
            }
            else {
                movieReviewsFragment.setReviewsData(reviewsListItems);
            }

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
                return new MovieTrailersFragment();
            }
            else {
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
        playTrailer(trailersListItems.get(position).trailerKey);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (trailersQueryUrl != null) outState.putString(TRAILERS_QUERY_URL_EXTRA, trailersQueryUrl);
        if (reviewsQueryUrl != null) outState.putString(REVIEWS_QUERY_URL_EXTRA, reviewsQueryUrl);
        outState.putInt(MovieListItem.MOVIE_ID, movieID);
        outState.putString(MovieListItem.ORIGINAL_TITLE, title);
        if (posterLocation != null) outState.putString(MovieListItem.POSTER, posterLocation);
        else outState.putString(MovieListItem.POSTER_PATH, posterURL);
        outState.putString(MovieListItem.OVERVIEW, plotSummary);
        outState.putFloat(MovieListItem.VOTE_AVERAGE, userRating);
        outState.putString(MovieListItem.RELEASE_DATE, releaseDate);
        super.onSaveInstanceState(outState);
    }
}
