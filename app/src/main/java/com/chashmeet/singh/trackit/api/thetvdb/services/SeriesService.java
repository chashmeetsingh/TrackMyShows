package com.chashmeet.singh.trackit.api.thetvdb.services;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import com.chashmeet.singh.trackit.api.thetvdb.TheTvdb;
import com.chashmeet.singh.trackit.api.thetvdb.entities.SeriesImageQueryResults;

public interface SeriesService {
    @GET("series/{id}/images/query")
    Call<SeriesImageQueryResults> imagesQuery(
            @Path("id") int id,
            @Query("keyType") String keyType,
            @Query("resolution") String resolution,
            @Query("subKey") String subKey,
            @Header(TheTvdb.HEADER_ACCEPT_LANGUAGE) String language
    );
}
