package com.example.alit.popularmoviesnano;

/**
 * Created by AliT on 9/18/17.
 */

public class MovieListItem {

    String title;
    String posterURL;
    String plotSummary;
    float userRating;
    String releaseDate;

    public MovieListItem(String title, String posterURL, String plotSummary, float userRating, String releaseDate) {
        this.title = title;
        this.posterURL = posterURL;
        this.plotSummary = plotSummary;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
    }

}
