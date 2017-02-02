package com.chashmeet.singh.trackit.api.trakt.entities;

public class SyncEpisode {

    public Integer season;
    public Integer number;
    public EpisodeIds ids;

    public String collected_at;
    public String watched_at;

    public SyncEpisode number(int number) {
        this.number = number;
        return this;
    }

    public SyncEpisode season(int season) {
        this.season = season;
        return this;
    }

    public SyncEpisode id(EpisodeIds id) {
        this.ids = id;
        return this;
    }
}
