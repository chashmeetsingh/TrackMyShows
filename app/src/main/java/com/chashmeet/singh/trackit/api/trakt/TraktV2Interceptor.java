package com.chashmeet.singh.trackit.api.trakt;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TraktV2Interceptor implements Interceptor {

    private TraktV2 trakt;

    public TraktV2Interceptor(TraktV2 trakt) {
        this.trakt = trakt;
    }

    public static Response handleIntercept(Chain chain, String apiKey, String accessToken) throws IOException {
        Request request = chain.request();
        if (!TraktV2.API_HOST.equals(request.url().host())) {
            return chain.proceed(request);
        }

        Request.Builder builder = request.newBuilder();

        builder.header(TraktV2.HEADER_CONTENT_TYPE, TraktV2.CONTENT_TYPE_JSON);
        builder.header(TraktV2.HEADER_TRAKT_API_KEY, apiKey);
        builder.header(TraktV2.HEADER_TRAKT_API_VERSION, TraktV2.API_VERSION);

        if (hasNoAuthorizationHeader(request) && accessTokenIsNotEmpty(accessToken)) {
            builder.header(TraktV2.HEADER_AUTHORIZATION, "Bearer" + " " + accessToken);
        }
        return chain.proceed(builder.build());
    }

    private static boolean hasNoAuthorizationHeader(Request request) {
        return request.header(TraktV2.HEADER_AUTHORIZATION) == null;
    }

    private static boolean accessTokenIsNotEmpty(String accessToken) {
        return accessToken != null && accessToken.length() != 0;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return handleIntercept(chain, trakt.apiKey(), trakt.accessToken());
    }
}
