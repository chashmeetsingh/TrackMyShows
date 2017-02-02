package com.chashmeet.singh.trackit.api.thetvdb.services;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import com.chashmeet.singh.trackit.api.thetvdb.TheTvdb;
import com.chashmeet.singh.trackit.api.thetvdb.entities.SeriesResultsWrapper;

public interface Search {
    @GET("search/series")
    Call<SeriesResultsWrapper> series(
            @Query("name") String name,
            @Query("imdbId") String imdbId,
            @Query("zap2itId") String zap2itId,
            @Header(TheTvdb.HEADER_ACCEPT_LANGUAGE) String languages
    );
}
