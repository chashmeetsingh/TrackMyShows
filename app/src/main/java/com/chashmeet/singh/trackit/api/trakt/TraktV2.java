package com.chashmeet.singh.trackit.api.trakt;

import org.apache.oltu.oauth2.common.message.types.GrantType;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.chashmeet.singh.trackit.api.trakt.entities.AccessToken;
import com.chashmeet.singh.trackit.api.trakt.entities.CheckinError;
import com.chashmeet.singh.trackit.api.trakt.services.Authentication;
import com.chashmeet.singh.trackit.api.trakt.services.Search;
import com.chashmeet.singh.trackit.api.trakt.services.Seasons;
import com.chashmeet.singh.trackit.api.trakt.services.Sync;
import com.chashmeet.singh.trackit.misc.App;

public class TraktV2 {

    public static final String API_HOST = "api.trakt.tv";
    public static final String API_URL = "https://" + API_HOST + "/";
    public static final String API_VERSION = "2";

    public static final String SITE_URL = "https://trakt.tv";
    public static final String OAUTH2_TOKEN_URL = SITE_URL + "/oauth/token";
    public static final String OAUTH2_REVOKE_TOKEN_URL = SITE_URL + "/oauth/revoke";

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String HEADER_TRAKT_API_VERSION = "trakt-api-version";
    public static final String HEADER_TRAKT_API_KEY = "trakt-api-key";

    private OkHttpClient okHttpClient;
    private Retrofit retrofit;

    private String apiKey;
    private String accessToken;
    private String refreshToken;
    private String clientSecret;
    private String redirectUri;

    public TraktV2(String apiKey) {
        this.apiKey = apiKey;
    }

    public String apiKey() {
        return apiKey;
    }

    public String accessToken() {
        return accessToken;
    }

    public TraktV2 accessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String refreshToken() {
        return refreshToken;
    }

    public TraktV2 refreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    protected Retrofit.Builder retrofitBuilder() {
        return new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create(TraktV2Helper.getGsonBuilder().create()))
                .client(okHttpClient());
    }

    protected synchronized OkHttpClient okHttpClient() {
        if (okHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            setOkHttpClientDefaults(builder);
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }

    protected void setOkHttpClientDefaults(OkHttpClient.Builder builder) {
        builder.addNetworkInterceptor(new TraktV2Interceptor(this));
        builder.authenticator(new TraktV2Authenticator(this));

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (App.ENABLE_LOGGING) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        builder.addInterceptor(logging);
    }

    protected Retrofit retrofit() {
        if (retrofit == null) {
            retrofit = retrofitBuilder().build();
        }
        return retrofit;
    }

    public Response<AccessToken> refreshAccessToken() throws IOException {
        return authentication().refreshAccessToken(
                GrantType.REFRESH_TOKEN.toString(),
                refreshToken(),
                apiKey(),
                clientSecret,
                redirectUri
        ).execute();
    }

    public Response<AccessToken> revokeAccessToken(String accessToken) throws IOException {
        return authentication().revokeAccessToken(
                accessToken
        ).execute();
    }

    public CheckinError checkForCheckinError(Response response) throws IOException {
        if (response.code() != 409) {
            return null; // only code 409 can be a check-in error
        }
        Converter<ResponseBody, CheckinError> errorConverter =
                retrofit.responseBodyConverter(CheckinError.class, new Annotation[0]);
        return errorConverter.convert(response.errorBody());
    }

    public Authentication authentication() {
        return retrofit().create(Authentication.class);
    }

    public Search search() {
        return retrofit().create(Search.class);
    }

    public Seasons seasons() {
        return retrofit().create(Seasons.class);
    }

    public Sync sync() {
        return retrofit().create(Sync.class);
    }
}