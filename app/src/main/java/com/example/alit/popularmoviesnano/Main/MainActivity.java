package com.example.alit.popularmoviesnano.Main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.alit.popularmoviesnano.Adapters.MoviePosterAdapter;
import com.example.alit.popularmoviesnano.ContentProvider.MovieDbContract;
import com.example.alit.popularmoviesnano.MovieDetail.MovieDetailActivity;
import com.example.alit.popularmoviesnano.MyDatastructures.MovieListItem;
import com.example.alit.popularmoviesnano.R;
import com.example.alit.popularmoviesnano.Settings.SettingsActivity;
import com.example.alit.popularmoviesnano.Utils.DbUtils;
import com.example.alit.popularmoviesnano.Utils.NetworkUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MoviePosterAdapter.MoviePosterItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<String>{

    private static final int MOVIES_LOADER_ID = 1;

    private static final int CURSOR_LOADER_ID = 2;

    private static final String MOVIE_QUERY_URL_EXTRA = "movie_query";

    private static final int posterWidth = 342;

    private static final String TAG = "MainActivityy";

    private String movieQueryUrl;

    @BindView(R.id.moviesRecyclerView) RecyclerView moviesRecyclerView;
    private MoviePosterAdapter adapter;
    private GridLayoutManager layoutManager;

    @BindView(R.id.failedTextView) TextView failedTextView;

    @BindView(R.id.progressBar) ProgressBar progressBar;

    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;

    private String querySetting;

    private boolean querySettingsChanged;

    private ArrayList<MovieListItem> movieListItems;

    private LoaderManager.LoaderCallbacks<Cursor> checkDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        layoutManager = new GridLayoutManager(this, getNumColumns());
        moviesRecyclerView.setLayoutManager(layoutManager);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (querySetting.equals(getResources().getString(R.string.favorites_path))) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                else getMovieData();
            }
        });

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));

        if (savedInstanceState != null) {
            movieQueryUrl = savedInstanceState.getString(MOVIE_QUERY_URL_EXTRA);
        }

        getSharedPreferences();
        getMovieData();
    }

    public void getSharedPreferences() {

        Resources resources = getResources();

        ActionBar actionBar = getSupportActionBar();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        querySetting = prefs.getString(resources.getString(R.string.search_pref_key), getResources().getString(R.string.top_rated_path));

        if (actionBar != null) {
            if (querySetting.equals(resources.getString(R.string.top_rated_path))) {
                getSupportActionBar().setTitle(resources.getString(R.string.main_top_rated));
            }
            else if (querySetting.equals(resources.getString(R.string.most_popular_path))) {
                getSupportActionBar().setTitle(resources.getString(R.string.main_most_popular));
            }
            else {
                getSupportActionBar().setTitle(resources.getString(R.string.main_favorites));
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(this);

    }

    public int getNumColumns() {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int numColumns = displayMetrics.widthPixels / posterWidth;
        if (numColumns < 2) numColumns = 2;
        return numColumns;

    }

    public void setMovieQueryUrl() {

        movieQueryUrl = NetworkUtils.getMovieQueryUrl(querySetting, getResources());

    }

    public void getMovieData() {

        if (querySetting.equals(getResources().getString(R.string.favorites_path))) {

            if (checkDb == null) instantiateCursorLoader();

            LoaderManager loaderManager = getSupportLoaderManager();

            loaderManager.restartLoader(CURSOR_LOADER_ID, null, checkDb);

        }
        else {

            if (movieQueryUrl == null) {
                setMovieQueryUrl();
            }

            LoaderManager loaderManager = getSupportLoaderManager();

            loaderManager.restartLoader(MOVIES_LOADER_ID, null, this);

        }

    }

    public void instantiateCursorLoader() {

        checkDb = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(MainActivity.this,
                        MovieDbContract.MovieEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

                if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
                else progressBar.setVisibility(View.INVISIBLE);

                if (querySetting.equals(getResources().getString(R.string.favorites_path))) {

                    movieListItems = DbUtils.getMovieListItems(data);

                    if (adapter != null) {
                        adapter.updateMovies(movieListItems);
                    }
                    else {
                        adapter = new MoviePosterAdapter(movieListItems, MainActivity.this);
                        moviesRecyclerView.setAdapter(adapter);
                    }
                    showMovieData();

                }

            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };

    }

    public void showError() {
        moviesRecyclerView.setVisibility(View.INVISIBLE);
        failedTextView.setVisibility(View.VISIBLE);
    }

    public void showMovieData() {
        failedTextView.setVisibility(View.INVISIBLE);
        moviesRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void movieItemClicked(int position) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        MovieListItem item = movieListItems.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt(MovieListItem.MOVIE_ID, item.movieID);
        bundle.putString(MovieListItem.ORIGINAL_TITLE, item.title);
        if (item.poster != null) {
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = new File(storageDir.getAbsolutePath(), "poster.jpg");
            if (file.exists()) file.delete();

            try {
                FileOutputStream out = new FileOutputStream(file);
                if (out!= null) {
                    item.poster.compress(Bitmap.CompressFormat.JPEG, 85, out);
                    out.flush();
                    out.close();
                }
                String posterLocation = file.getAbsolutePath();
                bundle.putString(MovieListItem.POSTER, posterLocation);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else bundle.putString(MovieListItem.POSTER_PATH, item.posterURL);
        bundle.putString(MovieListItem.OVERVIEW, item.plotSummary);
        bundle.putFloat(MovieListItem.VOTE_AVERAGE, item.userRating);
        bundle.putString(MovieListItem.RELEASE_DATE, item.releaseDate);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Resources resources = getResources();

        ActionBar actionBar = getSupportActionBar();

        if (key.equals(resources.getString(R.string.search_pref_key))) {
            String newSetting = sharedPreferences.getString(key, getResources().getString(R.string.top_rated_path));
            if (!newSetting.equals(querySetting)) querySettingsChanged = true;
            querySetting = newSetting;
            if (actionBar != null) {
                if (querySetting.equals(resources.getString(R.string.top_rated_path))) {
                    getSupportActionBar().setTitle(resources.getString(R.string.main_top_rated));
                    setMovieQueryUrl();
                }
                else if (querySetting.equals(resources.getString(R.string.most_popular_path))) {
                    getSupportActionBar().setTitle(resources.getString(R.string.main_most_popular));
                    setMovieQueryUrl();
                }
                else {
                    getSupportActionBar().setTitle(resources.getString(R.string.main_favorites));
                }
            }
            getMovieData();
        }
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {

        return new AsyncTaskLoader<String>(this) {

            String queryResult;

            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if (movieQueryUrl == null) return;

                if (!swipeRefreshLayout.isRefreshing()) {

                    if (!querySettingsChanged && queryResult != null) {

                        deliverResult(queryResult);

                        return;

                    }
                    else {
                        querySettingsChanged = false;
                        progressBar.setVisibility(View.VISIBLE);
                    }

                }

                forceLoad();
            }

            @Override
            public String loadInBackground() {

                URL url = NetworkUtils.getURL(movieQueryUrl);

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
                queryResult = data;
                super.deliverResult(data);
            }
        };

    }

    @Override
    public void onLoadFinished(Loader<String> loader, String result) {

        if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
        else progressBar.setVisibility(View.INVISIBLE);

        if (!querySetting.equals(getResources().getString(R.string.favorites_path))) {

            if (!result.isEmpty()) {
                movieListItems = NetworkUtils.getMovieItemList(result);
                if (adapter != null) {
                    adapter.updateMovies(movieListItems);
                }
                else {
                    adapter = new MoviePosterAdapter(movieListItems, MainActivity.this);
                    moviesRecyclerView.setAdapter(adapter);
                }
                showMovieData();
            }
            else {
                showError();
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.settings_icon_20dp_primdark);
        drawable.setColorFilter(ContextCompat.getColor(this, R.color.icons), PorterDuff.Mode.SRC_IN);
        MenuItem item = menu.findItem(R.id.action_settings);
        item.setIcon(drawable);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (movieQueryUrl != null) outState.putString(MOVIE_QUERY_URL_EXTRA, movieQueryUrl);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }
}
