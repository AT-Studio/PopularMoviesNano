package com.example.alit.popularmoviesnano.NetworkUtils;

import android.content.res.Resources;
import android.net.Uri;

import com.example.alit.popularmoviesnano.MyDatastructures.ReviewsListItem;
import com.example.alit.popularmoviesnano.MyDatastructures.TrailersListItem;
import com.example.alit.popularmoviesnano.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by AliT on 9/26/17.
 */

public class NetworkUtils {

    public static final String JSON_RESULTS = "results";

    private static final String SCHEME = "https";
    private static final String AUTHORITY = "api.themoviedb.org";
    private static final String BASE_PATH = "3/movie";
    private static final String VIDEOS_PATH = "videos";
    private static final String REVIEWS_PATH = "reviews";
//    private static final String SORT_PARAM = "sort_by";
    private static final String API_PARAM = "api_key";

    private static final String posterSize = "w780";
    private static final String posterBaseUrl = "http://image.tmdb.org/t/p/" + posterSize + "/";



    public static String getMovieQueryUrl(String querySetting, Resources resources) {

        Uri.Builder builder = new Uri.Builder();

        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .path(BASE_PATH)
                .appendPath(querySetting)
                .appendQueryParameter(API_PARAM, resources.getString(R.string.api_key));

        Uri uri = builder.build();

        return uri.toString();

    }

    public static String getMovieTrailersUrl(String movieID, Resources resources) {

        Uri.Builder builder = new Uri.Builder();

        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .path(BASE_PATH)
                .appendPath(movieID)
                .appendPath(VIDEOS_PATH)
                .appendQueryParameter(API_PARAM, resources.getString(R.string.api_key));

        Uri uri = builder.build();

        return uri.toString();

    }

    public static String getMovieReviewsUrl(String movieID, Resources resources) {

        Uri.Builder builder = new Uri.Builder();

        builder.scheme(SCHEME)
                .authority(AUTHORITY)
                .path(BASE_PATH)
                .appendPath(movieID)
                .appendPath(REVIEWS_PATH)
                .appendQueryParameter(API_PARAM, resources.getString(R.string.api_key));

        Uri uri = builder.build();

        return uri.toString();

    }

    public static URL getURL(String uriString) {

        URL url = null;
        try {
            url = new URL(uriString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return url;
    }

    public static String getPosterBaseUrl() {
        return posterBaseUrl;
    }

    public static ArrayList<TrailersListItem> getMovieTrailerList(String JSONString) {

        ArrayList<TrailersListItem> trailerListItems = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(JSONString);
            JSONArray results = jsonObject.getJSONArray(JSON_RESULTS);

            for (int i = 0; i < results.length(); i++) {

                JSONObject video = (JSONObject) results.get(i);

                if (video.getString(TrailersListItem.TYPE).equals(TrailersListItem.TYPE_TRAILER)) {

                    String trailerKey = video.getString(TrailersListItem.TRAILER_KEY);
                    String name = video.getString(TrailersListItem.NAME);
                    String site = video.getString(TrailersListItem.SITE);

                    trailerListItems.add(new TrailersListItem(trailerKey, name, site));

                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return trailerListItems;

    }

    public static ArrayList<ReviewsListItem> getMovieReviewsList(String JSONString) {

        ArrayList<ReviewsListItem> reviewsListItems = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(JSONString);
            JSONArray results = jsonObject.getJSONArray(JSON_RESULTS);

            for (int i = 0; i < results.length(); i++) {

                JSONObject review = (JSONObject) results.get(i);

                String author = review.getString(ReviewsListItem.AUTHOR);
                String content = review.getString(ReviewsListItem.CONTENT);

                reviewsListItems.add(new ReviewsListItem(author, content));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return reviewsListItems;

    }

}
