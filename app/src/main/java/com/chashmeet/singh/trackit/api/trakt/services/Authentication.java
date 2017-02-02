package com.chashmeet.singh.trackit.api.trakt.services;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import com.chashmeet.singh.trackit.api.trakt.TraktV2;
import com.chashmeet.singh.trackit.api.trakt.entities.AccessToken;

public interface Authentication {
    @FormUrlEncoded
    @POST(TraktV2.OAUTH2_TOKEN_URL)
    Call<AccessToken> refreshAccessToken(
            @Field("grant_type") String grantType,
            @Field("refresh_token") String refreshToken,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret,
            @Field("redirect_uri") String redirectUri
    );

    @FormUrlEncoded
    @POST(TraktV2.OAUTH2_REVOKE_TOKEN_URL)
    Call<AccessToken> revokeAccessToken(
            @Field("token") String token
    );
}
