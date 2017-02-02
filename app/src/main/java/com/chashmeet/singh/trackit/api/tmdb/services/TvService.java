package com.chashmeet.singh.trackit.api.tmdb.services;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.chashmeet.singh.trackit.api.tmdb.entities.TvResultsPage;

public interface TvService {
    @GET("tv/popular")
    Call<TvResultsPage> popular(
            @Query("page") Integer page,
            @Query("language") String language
    );
}
