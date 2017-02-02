package com.chashmeet.singh.trackit.api.trakt.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import com.chashmeet.singh.trackit.api.trakt.entities.Season;
import com.chashmeet.singh.trackit.api.trakt.enums.Extended;

public interface Seasons {

    @GET("shows/{id}/seasons")
    Call<List<Season>> summary(
            @Path("id") String showId,
            @Query(value = "extended", encoded = true) Extended extended
    );
}
