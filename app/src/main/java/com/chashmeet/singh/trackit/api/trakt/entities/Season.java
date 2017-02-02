package com.chashmeet.singh.trackit.api.trakt.entities;

import java.util.List;

public class Season {

    public Integer number;
    public SeasonIds ids;

    public String overview;
    public Double rating;

    public List<Episode> episodes;
}
