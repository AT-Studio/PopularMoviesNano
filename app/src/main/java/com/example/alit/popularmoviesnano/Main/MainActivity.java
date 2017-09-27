package com.example.alit.popularmoviesnano.Main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.alit.popularmoviesnano.MovieDetail.MovieDetailActivity;
import com.example.alit.popularmoviesnano.MyDatastructures.MovieListItem;
import com.example.alit.popularmoviesnano.Adapters.MoviePosterAdapter;
import com.example.alit.popularmoviesnano.NetworkUtils.NetworkUtils;
import com.example.alit.popularmoviesnano.R;
import com.example.alit.popularmoviesnano.Settings.SettingsActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MoviePosterAdapter.MoviePosterItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<String>{

//    private static final String SCHEME = "https";
//    private static final String AUTHORITY = "api.themoviedb.org";
//    private static final String BASE_PATH = "3/movie";
//    private static final String SORT_PARAM = "sort_by";
//    private static final String API_PARAM = "api_key";

    private static final int MOVIES_LOADER_ID = 1;

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

    private ArrayList<String> moviePosterURLs;
    private HashMap<String, MovieListItem> movieMap;

    private String querySetting;

    private boolean querySettingsChanged;

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
                getMovieData();
            }
        });

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));

        if (savedInstanceState != null) {
            movieQueryUrl = savedInstanceState.getString(MOVIE_QUERY_URL_EXTRA);
            Log.d(TAG, "movieQueryUrl: " + movieQueryUrl);
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
            if (querySetting.equals(getResources().getString(R.string.top_rated_path))) {
                getSupportActionBar().setTitle(resources.getString(R.string.main_top_rated));
            }
            else {
                getSupportActionBar().setTitle(resources.getString(R.string.main_most_popular));
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

//        Uri.Builder builder = new Uri.Builder();
//
//        builder.scheme(SCHEME)
//                .authority(AUTHORITY)
//                .path(BASE_PATH)
//                .appendPath(querySetting)
//                .appendQueryParameter(API_PARAM, getResources().getString(R.string.api_key));
//
//        Uri uri = builder.build();
//
//        movieQueryUrl = uri.toString();

        movieQueryUrl = NetworkUtils.getMovieQueryUrl(querySetting, getResources());

    }

    public void getMovieData() {

        Log.d(TAG, "called getMovieData");

        if (movieQueryUrl == null) {
            setMovieQueryUrl();
        }

        LoaderManager loaderManager = getSupportLoaderManager();
//        Loader<Boolean> movieLoader = loaderManager.getLoader(MOVIES_LOADER_ID);

        loaderManager.restartLoader(MOVIES_LOADER_ID, null, this);

//        if (movieLoader == null) {
//            Log.d(TAG, "loader was null");
//            loaderManager.initLoader(MOVIES_LOADER_ID, null, this);
//        }
//        else {
//            Log.d(TAG, "loader was not null");
//            loaderManager.restartLoader(MOVIES_LOADER_ID, null, this);
//        }

    }

//    public URL getURL(String uriString) {
//
//        URL url = null;
//        try {
//            url = new URL(uriString);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return url;
//    }

    public void showError() {
        moviesRecyclerView.setVisibility(View.INVISIBLE);
        failedTextView.setVisibility(View.VISIBLE);
    }

    public void showMovieData() {
        failedTextView.setVisibility(View.INVISIBLE);
        moviesRecyclerView.setVisibility(View.VISIBLE);
    }

    public void populateMovieList(String result) {

        if (moviePosterURLs == null) {
            moviePosterURLs = new ArrayList<>();
            movieMap = new HashMap<>();
        }
        else {
            moviePosterURLs.clear();
            movieMap.clear();
        }

        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray results = jsonObject.getJSONArray(NetworkUtils.JSON_RESULTS);

            for (int i = 0; i < results.length(); i++) {

                JSONObject movie = (JSONObject) results.get(i);

                String movieID = movie.getString(MovieListItem.MOVIE_ID);
                String title = movie.getString(MovieListItem.ORIGINAL_TITLE);
                String posterURL = movie.getString(MovieListItem.POSTER_PATH);
                String plotSummary = movie.getString(MovieListItem.OVERVIEW);
                float userRating = Float.parseFloat(movie.getString(MovieListItem.VOTE_AVERAGE));
                String releaseDate = movie.getString(MovieListItem.RELEASE_DATE);

                moviePosterURLs.add(posterURL);
                movieMap.put(posterURL, (new MovieListItem(movieID, title, posterURL, plotSummary, userRating, releaseDate)));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void movieItemClicked(int position) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        MovieListItem item = movieMap.get(moviePosterURLs.get(position));
        Bundle bundle = new Bundle();
        bundle.putString(MovieListItem.MOVIE_ID, item.movieID);
        bundle.putString(MovieListItem.ORIGINAL_TITLE, item.title);
        bundle.putString(MovieListItem.POSTER_PATH, item.posterURL);
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
                if (querySetting.equals(getResources().getString(R.string.top_rated_path))) {
                    getSupportActionBar().setTitle(resources.getString(R.string.main_top_rated));
                }
                else {
                    getSupportActionBar().setTitle(resources.getString(R.string.main_most_popular));
                }
            }
            Log.d(TAG, "going to change query settings");
            setMovieQueryUrl();
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

                Log.d(TAG, "called onStartLoading");

                if (movieQueryUrl == null) return;

                if (!swipeRefreshLayout.isRefreshing()) {

                    if (!querySettingsChanged && queryResult != null) {

                        Log.d(TAG, "have cached result");

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

        Log.d(TAG, "result: " + result);

        if (!result.isEmpty()) {
            populateMovieList(result);
            if (adapter != null) {
                Log.d(TAG, "calling updateMovies");
                adapter.updateMovies();
            }
            else {
                adapter = new MoviePosterAdapter(moviePosterURLs, MainActivity.this);
                moviesRecyclerView.setAdapter(adapter);
            }
            showMovieData();
        }
        else {
            showError();
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
