package com.chashmeet.singh.trackit.model;

public class SeasonItem {

    private String seasonTitle;
    private int episodeID;
    private int seasonNumber;

    public SeasonItem(String seasonTitle, int episode, int seasonNumber) {
        this.seasonNumber = seasonNumber;
        this.seasonTitle = seasonTitle;
        this.episodeID = episode;
    }


    public String getSeasonTitle() {
        return seasonTitle;
    }

    public int getEpisode() {
        return episodeID;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }
}