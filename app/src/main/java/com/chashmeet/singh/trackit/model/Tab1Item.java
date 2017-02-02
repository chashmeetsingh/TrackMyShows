package com.chashmeet.singh.trackit.model;

public class Tab1Item {

    private String title, image, duration, showTitle, episodeNumber;
    private int episodeID;
    private boolean watched;

    public Tab1Item(String title, String duration, String image, String showTitle, String episodeNumber,
                    int episodeID, boolean watched) {
        this.title = title;
        this.duration = duration;
        this.image = image;
        this.showTitle = showTitle;
        this.episodeNumber = episodeNumber;
        this.episodeID = episodeID;
        this.watched = watched;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getShowTitle() {
        return showTitle;
    }

    public String getDuration() {
        return duration;
    }

    public String getEpisodeNumber() {
        return episodeNumber;
    }

    public int getEpisodeID() {
        return episodeID;
    }

    public boolean isWatched() {
        return watched;
    }
}
