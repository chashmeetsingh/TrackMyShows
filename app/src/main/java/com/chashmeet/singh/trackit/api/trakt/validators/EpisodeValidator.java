package com.chashmeet.singh.trackit.api.trakt.validators;

import org.joda.time.DateTime;

import com.chashmeet.singh.trackit.api.trakt.entities.Episode;

/**
 * Checks {@link Episode} keys for null values
 */

public class EpisodeValidator {

    private Episode episode;

    public EpisodeValidator(Episode episode) {
        this.episode = episode;
    }

    public Episode episodeKeyCheck() {

        if (episode.ids.tvdb == null) {
            episode.ids.tvdb = 0;
        }

        if (episode.ids.trakt == null) {
            episode.ids.trakt = 0;
        }

        if (episode.ids.tmdb == null) {
            episode.ids.tmdb = 0;
        }

        if (episode.first_aired == null) {
            episode.first_aired = new DateTime(0);
        }

        if (episode.title == null) {
            episode.title = "Unknown";
        }

        if (episode.overview == null) {
            episode.overview = "Not available at the moment.";
        }

        if (episode.rating == null) {
            episode.rating = 0f;
        }

        if (episode.votes == null) {
            episode.votes = 0;
        }

        return episode;
    }
}
