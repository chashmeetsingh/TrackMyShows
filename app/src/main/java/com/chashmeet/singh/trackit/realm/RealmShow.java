package com.chashmeet.singh.trackit.realm;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmShow extends RealmObject {

    @PrimaryKey
    private int showID;
    private String title;
    private String genre;
    private Date firstAired;
    private Date airTime;
    private String network;
    private String overview;
    private float rating;
    private int ratingCount;
    private int runTime;
    private String status;
    private String bannerUrl;
    private String fanartUrl;
    private String posterUrl;
    private int traktID;
    private String timeZone;
    private RealmList<RealmEpisode> episodes;
    private boolean enableNotification;
    private boolean updateImages;
    private String imdbID;
    private int tmdbID;
    private long timeOffset;
    private double userRating;
    private boolean hidden;
    private long lastUpdated;

    // Get RealmShow Title
    public int getShowID() {
        return showID;
    }

    // Set RealmShow Title
    public void setShowID(int showID) {
        this.showID = showID;
    }

    // Get RealmShow Title
    public String getShowTitle() {
        return title;
    }

    // Set RealmShow Title
    public void setTitle(String title) {
        this.title = title;
    }

    // Get genre
    public String getGenre() {
        return genre;
    }

    // Set Genre
    public void setGenre(String genre) {
        this.genre = genre;
    }

    // Get First Air Date
    public Date getFirstAired() {
        return firstAired;
    }

    // Set First Aired
    public void setFirstAired(Date firstAired) {
        this.firstAired = firstAired;
    }

    // Get Air Time
    public Date getAirTime() {
        return airTime;
    }

    // Set Air Time
    public void setAirTime(Date airTime) {
        this.airTime = airTime;
    }

    // Get Network
    public String getNetwork() {
        return network;
    }

    // Set Network
    public void setNetwork(String network) {
        this.network = network;
    }

    // Get Overview
    public String getOverview() {
        return overview;
    }

    // Set OverView
    public void setOverview(String overview) {
        this.overview = overview;
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

    // Get RunTime
    public int getRunTime() {
        return runTime;
    }

    // Set Run Time
    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }

    // Get Status
    public String getStatus() {
        return status;
    }

    // Set Status
    public void setStatus(String status) {
        this.status = status;
    }

    // Get Banner Url
    public String getBannerUrl() {
        return bannerUrl;
    }

    // Set Banner Url
    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    // Get Fanart Url
    public String getFanartUrl() {
        return fanartUrl;
    }

    // Set Fanart Url
    public void setFanartUrl(String fanartUrl) {
        this.fanartUrl = fanartUrl;
    }

    // Get Banner Url
    public String getPosterUrl() {
        return posterUrl;
    }

    // Set Banner Url
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public int getTraktID() {
        return traktID;
    }

    public void setTraktID(int traktID) {
        this.traktID = traktID;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    // Get all episodes for a show
    public RealmList<RealmEpisode> getEpisodes() {
        return episodes;
    }

    // Add one episode for a show
    public void setEpisodes(RealmList<RealmEpisode> episodes) {
        this.episodes = episodes;
    }

    public boolean isEnableNotification() {
        return enableNotification;
    }

    public void setEnableNotification(boolean enableNotification) {
        this.enableNotification = enableNotification;
    }

    public boolean isUpdateImages() {
        return updateImages;
    }

    public void setUpdateImages(boolean updateImages) {
        this.updateImages = updateImages;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    // Used to display notifications at user specified time
    public long getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(long timeOffset) {
        this.timeOffset = timeOffset;
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

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
