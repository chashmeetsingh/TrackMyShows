package com.chashmeet.singh.trackit.api;

import com.chashmeet.singh.trackit.helper.DataHelper;

public interface API {

    String TVDB_KEY = "Eb7PeXmnG8Gt0KIwOCtK/RD0nCMN1Wu5yeD7I51UuAM=";

    String TRAKT_CLIENT_ID = "X0NzQP2KRxtmvyBUqjt0cAfWfHKzMr5P0NUfxvTZduMPfKw80wCqf2t0wJGaulfIISgKi4+9rTC7AgPEFqTQNJpKEqcS1/xK7LNkb8hmvSE=";

    String TRAKT_CLIENT_SECRET = "FK+1DoeyhlO3xwrNC/spaNS8JB77vANu9MBS2ntn42Q91lgBTyLx05hrUzwiwFzTM3bEjtP31hKQWRI4m+2A9lIi/EW6/PMH0Bdg9virZcU=";

    String TMDB_API_KEY = "7f9b14a792d24a0c682ba3039469b811";

    String TVDB_LINK = "http://thetvdb.com/";

    String BASE_IMAGE_URL = TVDB_LINK + "api/" + DataHelper.TVDB_KEY + "/series/";

    String TV_MAZE_SEARCH = "http://api.tvmaze.com/search/shows?q=";

    String TRAKT_TOKEN_URL = "https://api.trakt.tv/oauth/token";

    String TRAKT_OAUTH_URL = "https://api.trakt.tv/oauth/authorize";

    String TRAKT_REDIRECT_URI = "http://localhost";

    String TRAKT_GRANT_TYPE = "authorization_code";

    String TRAKT_REFRESH_GRANT_TYPE = "refresh_token";

    String TMDB_BACKDROP_PATH = "https://image.tmdb.org/t/p/w300";

    String TMDB_POSTER_PATH = "https://image.tmdb.org/t/p/w500";
}