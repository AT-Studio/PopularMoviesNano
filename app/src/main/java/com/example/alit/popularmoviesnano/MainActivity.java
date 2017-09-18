package com.example.alit.popularmoviesnano;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MoviePosterAdapter.MoviePosterItemClickListener{

    private static final String SCHEME = "https";
    private static final String AUTHORITY = "api.themoviedb.org";
    private static final String PATH = "3/movie";
    private static final String SORT_PARAM = "sort_by";
    private static final String API_PARAM = "api_key";

    private static final String RESULTS = "results";
    private static final String ORIGINAL_TITLE = "original_title";
    private static final String POSTER_PATH = "poster_path";
    private static final String OVERVIEW = "overview";
    private static final String VOTE_AVERAGE = "vote_average";
    private static final String RELEASE_DATE = "release_date";

    private static final int posterWidth = 342;

    private static final String TAG = "MainActivity";

    @BindView(R.id.moviesRecyclerView) RecyclerView moviesRecyclerView;
    private MoviePosterAdapter adapter;
    private GridLayoutManager layoutManager;

    @BindView(R.id.failedTextView) TextView failedTextView;

    @BindView(R.id.progressBar) ProgressBar progressBar;

    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<String> moviePosterURLs;
    private HashMap<String, MovieListItem> movieMap;

    private String querySetting;

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

    }

    @Override
    protected void onStart() {
        super.onStart();

        Resources resources = getResources();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        querySetting = prefs.getString(resources.getString(R.string.search_pref_key), getResources().getString(R.string.top_rated_path));

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            if (querySetting.equals(getResources().getString(R.string.top_rated_path))) {
                getSupportActionBar().setTitle(resources.getString(R.string.main_top_rated));
            }
            else {
                getSupportActionBar().setTitle(resources.getString(R.string.main_most_popular));
            }
        }

        getMovieData();

    }

    public int getNumColumns() {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int numColumns = displayMetrics.widthPixels / posterWidth;
        if (numColumns < 2) numColumns = 2;
        return numColumns;

    }

    public void getMovieData() {
        new FetchMovieData().execute(getURL());
    }

    public URL getURL() {

        Uri.Builder builder = new Uri.Builder();

        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .path(PATH)
                .appendPath(querySetting)
                .appendQueryParameter(API_PARAM, getResources().getString(R.string.api_key));

        Uri uri = builder.build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return url;
    }

    public void showError() {
        moviesRecyclerView.setVisibility(View.INVISIBLE);
        failedTextView.setVisibility(View.VISIBLE);
    }

    public void showMovieData() {
        failedTextView.setVisibility(View.INVISIBLE);
        moviesRecyclerView.setVisibility(View.VISIBLE);
    }

    public boolean populateMovieList(String result) {

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
            JSONArray results = jsonObject.getJSONArray(RESULTS);

            for (int i = 0; i < results.length(); i++) {

                JSONObject movie = (JSONObject) results.get(i);

                String title = movie.getString(ORIGINAL_TITLE);
                String posterURL = movie.getString(POSTER_PATH);
                String plotSummary = movie.getString(OVERVIEW);
                float userRating = Float.parseFloat(movie.getString(VOTE_AVERAGE));
                String releaseDate = movie.getString(RELEASE_DATE);

                moviePosterURLs.add(posterURL);
                movieMap.put(posterURL, (new MovieListItem(title, posterURL, plotSummary, userRating, releaseDate)));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void movieItemClicked(int position) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        MovieListItem item = movieMap.get(moviePosterURLs.get(position));
        Bundle bundle = new Bundle();
        bundle.putString(ORIGINAL_TITLE, item.title);
        bundle.putString(POSTER_PATH, item.posterURL);
        bundle.putString(OVERVIEW, item.plotSummary);
        bundle.putFloat(VOTE_AVERAGE, item.userRating);
        bundle.putString(RELEASE_DATE, item.releaseDate);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public class FetchMovieData extends AsyncTask<URL, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            if (!swipeRefreshLayout.isRefreshing()) progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(URL... urls) {

            URL url = urls[0];

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

            String result = stringBuilder.toString();

            if (result != null && !result.isEmpty()) {

                return populateMovieList(result);

            }
            else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean wasSuccessful) {

            if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
            else progressBar.setVisibility(View.INVISIBLE);

            if (wasSuccessful) {
                if (adapter != null) adapter.updateMovies();
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
}
