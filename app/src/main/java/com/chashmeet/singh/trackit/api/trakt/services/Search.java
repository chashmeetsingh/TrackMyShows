package com.chashmeet.singh.trackit.api.trakt.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import com.chashmeet.singh.trackit.api.trakt.entities.SearchResult;
import com.chashmeet.singh.trackit.api.trakt.enums.Extended;
import com.chashmeet.singh.trackit.api.trakt.enums.IdType;
import com.chashmeet.singh.trackit.api.trakt.enums.Type;

public interface Search {
    @GET("search/{id_type}/{id}")
    Call<List<SearchResult>> idLookup(
            @Path("id_type") IdType idType,
            @Path("id") String id,
            @Query("type") Type type,
            @Query(value = "extended", encoded = true) Extended extended,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );
}
