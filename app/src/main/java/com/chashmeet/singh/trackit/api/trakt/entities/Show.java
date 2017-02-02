package com.chashmeet.singh.trackit.api.trakt.entities;

import java.util.List;

import com.chashmeet.singh.trackit.api.trakt.enums.Status;

public class Show extends BaseEntity {

    public Integer year;
    public ShowIds ids;

    // extended info
    public String first_aired;
    public Airs airs;
    public Integer runtime;
    public String certification;
    public String network;
    public String country;
    public String trailer;
    public String homepage;
    public Status status;
    public String language;
    public List<String> genres;

}
