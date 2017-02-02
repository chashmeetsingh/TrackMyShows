package com.chashmeet.singh.trackit.api.thetvdb.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import com.chashmeet.singh.trackit.api.thetvdb.entities.LoginData;
import com.chashmeet.singh.trackit.api.thetvdb.entities.Token;

public interface Authentication {

    String PATH_LOGIN = "login";

    /**
     * Returns a session token to be included in the rest of the requests. Note that API key authentication is required
     * for all subsequent requests and user auth is required for routes in the User section.
     */
    @POST(PATH_LOGIN)
    Call<Token> login(@Body LoginData loginData);
}
