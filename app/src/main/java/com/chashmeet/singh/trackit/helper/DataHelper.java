package com.chashmeet.singh.trackit.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

import com.chashmeet.singh.trackit.api.API;
import com.chashmeet.singh.trackit.misc.CryptData;
import com.chashmeet.singh.trackit.utility.VolleySingleton;

public class DataHelper {

    public static int SORT_ORDER;
    public static long LAST_UPDATE;
    public static String TVDB_KEY;
    public static String TRAKT_CLIENT_ID;
    public static String TRAKT_CLIENT_SECRET;
    public static String TRAKT_ACCESS_TOKEN;
    public static String TRAKT_LAST_WATCHED;
    public static String TRAKT_LAST_COLLECTED;
    public static boolean SHOW_NOTIFICATION;
    public static boolean TRAKT_SYNC;
    public static boolean HIDE_WATCHED_EPISODES;
    public static boolean TAB_1_INFINITE_TIME_FRAME;
    public static boolean TAB_3_INFINITE_TIME_FRAME;

    public DataHelper(Context context) throws GeneralSecurityException {
        SORT_ORDER = getSortOrder(context);
        LAST_UPDATE = getLastUpdate(context);
        TVDB_KEY = CryptData.decrypt(API.TVDB_KEY);
        TRAKT_CLIENT_ID = CryptData.decrypt(API.TRAKT_CLIENT_ID);
        TRAKT_CLIENT_SECRET = CryptData.decrypt(API.TRAKT_CLIENT_SECRET);
        TRAKT_ACCESS_TOKEN = getAccessToken(context);
        TRAKT_LAST_WATCHED = getLastTraktUpdate(context);
        TRAKT_LAST_COLLECTED = getLastTraktCollected(context);
        SHOW_NOTIFICATION = getNotificationPreference(context);
        TRAKT_SYNC = false;
        HIDE_WATCHED_EPISODES = getEpisodeVisibilityPreference(context);
        TAB_1_INFINITE_TIME_FRAME = getTab1TimeFramePreference(context);
        TAB_3_INFINITE_TIME_FRAME = getTab3TimeFramePreference(context);
    }

    public static void setSortOrder(int sortID, Context context) {
        SORT_ORDER = sortID;
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("sort_pref", sortID);
        editor.apply();
    }

    public static void setLastUpdate(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("update_time", System.currentTimeMillis());
        editor.apply();
    }

    public static void setNotificationPreference(Context context, boolean pref) {
        SHOW_NOTIFICATION = pref;
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("notification_pref", pref);
        editor.apply();
    }

    public static void setEpisodeVisibilityPreference(Context context, boolean pref) {
        HIDE_WATCHED_EPISODES = pref;
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("episode_pref", pref);
        editor.apply();
    }

    public static void setTraktData(Context context, String token, String refresh)
            throws GeneralSecurityException {
        TRAKT_ACCESS_TOKEN = token;
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("token", CryptData.encrypt(token));
        editor.putLong("token_time", System.currentTimeMillis());
        editor.putString("refresh", CryptData.encrypt(refresh));
        editor.apply();
    }

    public static void removeTraktData(Context context) {
        TRAKT_ACCESS_TOKEN = "";
        TRAKT_LAST_WATCHED = "";
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("token");
        editor.remove("token_time");
        editor.remove("refresh");
        editor.apply();
    }

    public static void setLastTraktUpdate(Context context, String updateTime) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("trakt_watched_update_time", updateTime);
        editor.apply();
    }

    public static void setLastTraktCollected(Context context, String updateTime) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("trakt_collected_update_time", updateTime);
        editor.apply();
    }

    public static void setTab1TimeFramePreference(Context context, boolean pref) {
        TAB_1_INFINITE_TIME_FRAME = pref;
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("tab_1_infinite", pref);
        editor.apply();
    }

    public static void setTab3TimeFramePreference(Context context, boolean pref) {
        TAB_3_INFINITE_TIME_FRAME = pref;
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("tab_3_infinite", pref);
        editor.apply();
    }

    private int getSortOrder(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        return sharedPref.getInt("sort_pref", 0);
    }

    private long getLastUpdate(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        return sharedPref.getLong("update_time", 0);
    }

    private boolean getNotificationPreference(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("notification_pref", true);
    }

    private boolean getEpisodeVisibilityPreference(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("episode_pref", false);
    }

    private boolean getTab1TimeFramePreference(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("tab_1_infinite", false);
    }

    private boolean getTab3TimeFramePreference(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("tab_3_infinite", false);
    }

    private String getAccessToken(Context context) throws GeneralSecurityException {
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        Long tokenTime = sharedPref.getLong("token_time", 0);
        String token = sharedPref.getString("token", "");
        if (!token.equals("") && TimeUnit.DAYS.convert(System.currentTimeMillis() - tokenTime, TimeUnit.MILLISECONDS) >= 30) {
            refreshToken(context, CryptData.decrypt(sharedPref.getString("refresh", "")));
        }
        if (token.equals("")) {
            return token;
        } else {
            return CryptData.decrypt(token);
        }
    }

    private String getLastTraktUpdate(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        return sharedPref.getString("trakt_watched_update_time", "");
    }

    private String getLastTraktCollected(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        return sharedPref.getString("trakt_collected_update_time", "");
    }

    private void refreshToken(final Context context, final String refreshToken) {
        RequestQueue requestQueue = VolleySingleton.getInstance().getRequestQueue();
        JSONObject jsonBody = null;
        try {
            jsonBody = new JSONObject("{\"refresh_token\": \"" + refreshToken + "\"," +
                    "\"client_id\": \"" + DataHelper.TRAKT_CLIENT_ID + "\"," +
                    "\"client_secret\": \"" + DataHelper.TRAKT_CLIENT_SECRET + "\"," +
                    "\"redirect_uri\": \"" + API.TRAKT_REDIRECT_URI + "\"," +
                    "\"grant_type\": \"" + API.TRAKT_REFRESH_GRANT_TYPE + "\"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                API.TRAKT_TOKEN_URL, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        String tok = json.optString("access_token");
                        String refresh = json.optString("refresh_token");
                        Log.d("Token Access", tok);
                        Log.d("Refresh", refresh);
                        try {
                            setTraktData(context, tok, refresh);
                        } catch (GeneralSecurityException e) {
                            //e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("LoginActivity", String.valueOf(error));
                    }
                }) {
        };
        req.setRetryPolicy(new DefaultRetryPolicy(
                10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(req);
    }
}