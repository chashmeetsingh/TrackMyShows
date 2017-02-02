package com.chashmeet.singh.trackit.model;

import com.chashmeet.singh.trackit.realm.RealmEpisode;

public class EpisodeItem {

    private RealmEpisode episode;

    public EpisodeItem(RealmEpisode show) {
        this.episode = show;
    }

    public RealmEpisode getEpisode() {
        return episode;
    }
}
