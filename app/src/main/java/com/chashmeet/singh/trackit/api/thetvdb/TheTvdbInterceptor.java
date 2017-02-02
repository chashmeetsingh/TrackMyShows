package com.chashmeet.singh.trackit.api.thetvdb;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TheTvdbInterceptor implements Interceptor {

    private TheTvdb theTvdb;

    public TheTvdbInterceptor(TheTvdb theTvdb) {
        this.theTvdb = theTvdb;
    }

    public static Response handleIntercept(Chain chain, String jsonWebToken) throws IOException {
        Request request = chain.request();
        if (!TheTvdb.API_HOST.equals(request.url().host())) {
            return chain.proceed(request);
        }

        Request.Builder builder = request.newBuilder();

        builder.header(TheTvdb.HEADER_ACCEPT, "application/vnd.thetvdb.v" + TheTvdb.API_VERSION);

        if (hasNoAuthorizationHeader(request) && jsonWebTokenIsNotEmpty(jsonWebToken)) {
            builder.header(TheTvdb.HEADER_AUTHORIZATION, "Bearer" + " " + jsonWebToken);
        }
        return chain.proceed(builder.build());
    }

    private static boolean hasNoAuthorizationHeader(Request request) {
        return request.header(TheTvdb.HEADER_AUTHORIZATION) == null;
    }

    private static boolean jsonWebTokenIsNotEmpty(String jsonWebToken) {
        return jsonWebToken != null && jsonWebToken.length() != 0;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return handleIntercept(chain, jsonWebToken());
    }

    public String jsonWebToken() {
        return theTvdb.jsonWebToken();
    }

}
