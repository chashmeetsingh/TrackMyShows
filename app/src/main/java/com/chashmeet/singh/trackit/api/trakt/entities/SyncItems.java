package com.chashmeet.singh.trackit.api.trakt.entities;

import java.util.ArrayList;
import java.util.List;

public class SyncItems {

    public List<SyncEpisode> episodes;
    public List<Integer> ids;

    public SyncItems episodes(SyncEpisode episode) {
        ArrayList<SyncEpisode> list = new ArrayList<>(1);
        list.add(episode);
        return episodes(list);
    }

    public SyncItems episodes(List<SyncEpisode> episodes) {
        this.episodes = episodes;
        return this;
    }

    public SyncItems ids(int id) {
        ArrayList<Integer> list = new ArrayList<>(1);
        list.add(id);
        return ids(list);
    }

    public SyncItems ids(List<Integer> ids) {
        this.ids = ids;
        return this;
    }

}
