package com.example.alit.popularmoviesnano.MyDatastructures;

/**
 * Created by AliT on 9/26/17.
 */

public class TrailersListItem {

    public static final String TRAILER_KEY = "key";
    public static final String NAME = "name";
    public static final String SITE = "site";
    public static final String TYPE = "type";
    public static final String TYPE_TRAILER = "Trailer";

    public String trailerKey;
    public String name;
    public String site;

    public TrailersListItem(String trailerKey, String name, String site) {

        this.trailerKey = trailerKey;
        this.name = name;
        this.site = site;

    }

}
