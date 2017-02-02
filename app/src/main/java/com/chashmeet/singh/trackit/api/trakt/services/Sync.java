package com.chashmeet.singh.trackit.api.trakt.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import com.chashmeet.singh.trackit.api.trakt.entities.BaseShow;
import com.chashmeet.singh.trackit.api.trakt.entities.LastActivities;
import com.chashmeet.singh.trackit.api.trakt.entities.SyncItems;
import com.chashmeet.singh.trackit.api.trakt.entities.SyncResponse;
import com.chashmeet.singh.trackit.api.trakt.enums.Extended;

public interface Sync {

    @GET("sync/last_activities")
    Call<LastActivities> lastActivities();

    @GET("sync/collection/shows")
    Call<List<BaseShow>> collectionShows(
            @Query(value = "extended", encoded = true) Extended extended
    );

    @POST("sync/collection")
    Call<SyncResponse> addItemsToCollection(
            @Body SyncItems items
    );

    @POST("sync/collection/remove")
    Call<SyncResponse> deleteItemsFromCollection(
            @Body SyncItems items
    );

    @GET("sync/watched/shows")
    Call<List<BaseShow>> watchedShows(
            @Query(value = "extended", encoded = true) Extended extended
    );

    @POST("sync/history")
    Call<SyncResponse> addItemsToWatchedHistory(
            @Body SyncItems items
    );

    @POST("sync/history/remove")
    Call<SyncResponse> deleteItemsFromWatchedHistory(
            @Body SyncItems items
    );
}
