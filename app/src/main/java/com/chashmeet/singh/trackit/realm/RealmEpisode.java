package com.chashmeet.singh.trackit.realm;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class RealmEpisode extends RealmObject {

    @PrimaryKey
    private int episodeID; //tvdb ID
    private int showID;
    private String episodeTitle;
    private long airDateTime;
    private String overView;
    private String bannerUrl;
    private float rating;
    private int ratingCount;
    private boolean watched;
    private int episodeNumber;
    @Index
    private int seasonNumber;
    private String details;
    private boolean collection;
    private int traktID;
    private int tmdbID;
    private double userRating;
    private String collectedAt;
    private String watchedAt;

    // Get Episode ID
    public int getEpisodeID() {
        return episodeID;
    }

    // Set Episode ID
    public void setEpisodeID(int episodeID) {
        this.episodeID = episodeID;
    }

    // Get RealmShow ID
    public int getShowID() {
        return showID;
    }

    // Set RealmShow ID
    public void setShowID(int showID) {
        this.showID = showID;
    }

    // Get RealmEpisode Title
    public String getEpisodeTitle() {
        return this.episodeTitle;
    }

    // Set RealmEpisode Title
    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    // Get Air Date Time
    public long getAirDateTime() {
        return airDateTime;
    }

    // Set Air Date Time
    public void setAirDateTime(long airDateTime) {
        this.airDateTime = airDateTime;
    }

    // Get Over View
    public String getOverView() {
        return overView;
    }

    // Set Over View
    public void setOverView(String overView) {
        this.overView = overView;
    }

    // Get Banner Url
    public String getBannerUrl() {
        return bannerUrl;
    }

    // Set Banner Url
    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    // Get Rating
    public float getRating() {
        return rating;
    }

    // Set Rating
    public void setRating(float rating) {
        this.rating = rating;
    }

    // Get Rating Count
    public int getRatingCount() {
        return ratingCount;
    }

    // Set Rating Count
    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    // Get Watch Status
    public boolean getWatched() {
        return watched;
    }

    // Set Watched
    public void setWatched(boolean watched, boolean setWatchedAt) {
        this.watched = watched;

        if (setWatchedAt) {
            if (watched) {
                setWatchedAt(new DateTime(DateTimeZone.UTC).toString());
            } else {
                setWatchedAt("");
            }
        }
    }

    // Get RealmEpisode Number
    public int getEpisodeNumber() {
        return episodeNumber;
    }

    // Set RealmEpisode Number
    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    // Get Season Number
    public int getSeasonNumber() {
        return seasonNumber;
    }

    // Set Season Number
    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    // Get Season number and episode number
    public String getDetails() {
        return details;
    }

    // Set Season number and episode number
    public void setDetails() {
        this.details = "S" + String.format("%02d", seasonNumber) + "E" +
                String.format("%02d", episodeNumber);
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection, boolean setCollectedAt) {
        this.collection = collection;

        if (setCollectedAt) {
            if (collection) {
                setCollectedAt(new DateTime(DateTimeZone.UTC).toString());
            } else {
                setCollectedAt("");
            }
        }
    }

    public int getTraktID() {
        return traktID;
    }

    public void setTraktID(int traktID) {
        this.traktID = traktID;
    }

    public int getTmdbID() {
        return tmdbID;
    }

    public void setTmdbID(int tmdbID) {
        this.tmdbID = tmdbID;
    }

    public double getUserRating() {
        return userRating;
    }

    public void setUserRating(double userRating) {
        this.userRating = userRating;
    }

    public String getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(String collectedAt) {
        this.collectedAt = collectedAt;
    }

    public String getWatchedAt() {
        return watchedAt;
    }

    public void setWatchedAt(String watchedAt) {
        this.watchedAt = watchedAt;
    }

}
