package com.chashmeet.singh.trackit.api.trakt.enums;

import java.util.HashMap;
import java.util.Map;

public enum Status implements TraktEnum {

    ENDED("ended"),
    RETURNING("returning series"),
    CANCELED("canceled"),
    IN_PRODUCTION("in production"),
    UNKNOWN("unknown");

    private static final Map<String, Status> STRING_MAPPING = new HashMap<>();

    static {
        for (Status via : Status.values()) {
            STRING_MAPPING.put(via.toString().toUpperCase(), via);
        }
    }

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public static Status fromValue(String value) {
        return STRING_MAPPING.get(value.toUpperCase());
    }

    @Override
    public String toString() {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

}
