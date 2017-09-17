package com.example.alit.popularmoviesnano;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailActivity extends AppCompatActivity {

    private static final String size = "w780";
    private static final String baseUrl = "http://image.tmdb.org/t/p/" + size + "/";

    private static final String ORIGINAL_TITLE = "original_title";
    private static final String POSTER_PATH = "poster_path";
    private static final String OVERVIEW = "overview";
    private static final String VOTE_AVERAGE = "vote_average";
    private static final String RELEASE_DATE = "release_date";

    public final String TAG = "MovieDetailActivity";

    @BindView(R.id.posterImageView) ImageView posterImageView;

    @BindView(R.id.app_bar) AppBarLayout appBarLayout;

    @BindView(R.id.toolbar_layout) CollapsingToolbarLayout toolbar_layout;

    @BindView(R.id.progressBar) ProgressBar progressBar;

    @BindView(R.id.failedTextView) TextView failedTextView;

    @BindView(R.id.titleText) TextView titleText;

    @BindView(R.id.releaseDateText) TextView releaseDateText;

    @BindView(R.id.ratingText) TextView ratingText;

    @BindView(R.id.synopsisText) TextView synopsisText;

    private String title;
    private String posterURL;
    private String plotSummary;
    private float userRating;
    private String releaseDate;

    private DisplayMetrics displayMetrics;

    private Target picassoTarget;

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

        title = bundle.getString(ORIGINAL_TITLE);
        posterURL = bundle.getString(POSTER_PATH);
        plotSummary = bundle.getString(OVERVIEW);
        userRating = bundle.getFloat(VOTE_AVERAGE);
        releaseDate = bundle.getString(RELEASE_DATE);

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

        Picasso.with(this).load(baseUrl + posterURL).into(picassoTarget);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    toolbar_layout.setTitle(title);
                }
                else toolbar_layout.setTitle(" ");
            }
        });

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

}
