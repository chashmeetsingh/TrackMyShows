package com.chashmeet.singh.trackit.api.trakt.validators;

import java.util.ArrayList;

import com.chashmeet.singh.trackit.api.trakt.entities.Airs;
import com.chashmeet.singh.trackit.api.trakt.entities.Show;
import com.chashmeet.singh.trackit.api.trakt.enums.Status;

/**
 * Checks {@link Show} keys for null values
 */

public class ShowValidator {

    private Show show;

    public ShowValidator(Show show) {
        this.show = show;
    }

    public Show showKeyCheck() {

        if (show.ids.tvdb == null) {
            show.ids.tvdb = 0;
        }

        if (show.ids.trakt == null) {
            show.ids.trakt = 0;
        }

        if (show.ids.tmdb == null) {
            show.ids.tmdb = 0;
        }

        if (show.first_aired == null) {
            show.first_aired = "2000-01-01T12:00:00.000Z";
        }

        if (show.title == null) {
            show.title = "Unknown";
        }

        if (show.overview == null) {
            show.overview = "Not available at the moment.";
        }

        if (show.rating == null) {
            show.rating = 0f;
        }

        if (show.votes == null) {
            show.votes = 0;
        }

        if (show.genres == null) {
            show.genres = new ArrayList<>();
            show.genres.add("Unavailable");
        }

        if (show.airs == null) {
            show.airs = new Airs();
            show.airs.day = "Unknown";
            show.airs.time = "12:00";
            show.airs.timezone = "America/New_York";
        } else {
            if (show.airs.day == null) {
                show.airs.day = "Unknown";
            }
            if (show.airs.time == null) {
                show.airs.time = "12:00";
            }
            if (show.airs.timezone == null) {
                show.airs.timezone = "America/New_York";
            }
        }

        if (show.network == null) {
            show.network = "Unknown";
        }

        if (show.status == null) {
            show.status = Status.UNKNOWN;
        }

        return show;
    }
}