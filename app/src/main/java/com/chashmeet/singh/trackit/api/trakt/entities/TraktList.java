package com.chashmeet.singh.trackit.api.trakt.entities;

public class TraktList {

    public ListIds ids;
    public String name;

    public TraktList name(String name) {
        this.name = name;
        return this;
    }
}
