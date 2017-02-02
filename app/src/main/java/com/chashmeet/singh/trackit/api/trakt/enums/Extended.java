package com.chashmeet.singh.trackit.api.trakt.enums;

public enum Extended implements TraktEnum {

    DEFAULT_MIN("min"),
    IMAGES("images"),
    FULL("full"),
    FULLIMAGES("full,images"),
    NOSEASONS("noseasons"),
    NOSEASONSIMAGES("noseasons,images"),
    EPISODES("full,episodes");

    private final String value;

    Extended(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
