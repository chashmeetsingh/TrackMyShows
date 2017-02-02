package com.chashmeet.singh.trackit.api.trakt.enums;

public enum IdType implements TraktEnum {

    TRAKT_MOVIE("trakt-movie"),
    TRAKT_SHOW("trakt-show"),
    TRAKT_EPISODE("trakt-episode"),
    IMDB("imdb"),
    TMDB("tmdb"),
    TVDB("tvdb"),
    TVRAGE("tvrage");

    private final String value;

    IdType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
