package com.chashmeet.singh.trackit.api.thetvdb;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.chashmeet.singh.trackit.api.thetvdb.services.Authentication;
import com.chashmeet.singh.trackit.api.thetvdb.services.Search;
import com.chashmeet.singh.trackit.api.thetvdb.services.SeriesService;
import com.chashmeet.singh.trackit.misc.App;

@SuppressWarnings("WeakerAccess")
public class TheTvdb {

    public static final String API_HOST = "api.thetvdb.com";
    public static final String API_URL = "https://" + API_HOST + "/";
    public static final String API_VERSION = "2.1.1";

    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String HEADER_AUTHORIZATION = "Authorization";

    private OkHttpClient okHttpClient;
    private Retrofit retrofit;
    private HttpLoggingInterceptor logging;

    private boolean enableDebugLogging;

    private String apiKey;
    private String currentJsonWebToken;

    /**
     * Create a new manager instance.
     */
    public TheTvdb(String apiKey) {
        this.apiKey = apiKey;
        enableDebugLogging(App.ENABLE_LOGGING);
    }

    public String apiKey() {
        return apiKey;
    }

    public String jsonWebToken() {
        return currentJsonWebToken;
    }

    public void jsonWebToken(String value) {
        this.currentJsonWebToken = value;
    }

    public TheTvdb enableDebugLogging(boolean enable) {
        this.enableDebugLogging = enable;
        if (logging != null) {
            logging.setLevel(enable ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        }
        return this;
    }

    protected Retrofit.Builder retrofitBuilder() {
        return new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
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
        builder.addNetworkInterceptor(new TheTvdbInterceptor(this))
                .authenticator(new TheTvdbAuthenticator(this));
        if (enableDebugLogging) {
            logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
    }

    protected Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = retrofitBuilder().build();
        }
        return retrofit;
    }

    public Authentication authentication() {
        return getRetrofit().create(Authentication.class);
    }

    public SeriesService series() {
        return getRetrofit().create(SeriesService.class);
    }

    public Search search() {
        return getRetrofit().create(Search.class);
    }

}
