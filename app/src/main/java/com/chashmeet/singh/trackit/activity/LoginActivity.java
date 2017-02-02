package com.chashmeet.singh.trackit.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;

import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.api.API;
import com.chashmeet.singh.trackit.helper.DataHelper;
import com.chashmeet.singh.trackit.utility.VolleySingleton;

public class LoginActivity extends AppCompatActivity {

    private final String url = API.TRAKT_OAUTH_URL + "?response_type=code&client_id=" +
            DataHelper.TRAKT_CLIENT_ID + "&redirect_uri=" + API.TRAKT_REDIRECT_URI;
    private WebView web;
    private RelativeLayout tapToRetry;
    private TextView tvError;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tapToRetry = (RelativeLayout) findViewById(R.id.tap_to_retry);
        tvError = (TextView) findViewById(R.id.tv_error);
        web = (WebView) findViewById(R.id.web_view);
        progressBar = (ProgressBar) findViewById(R.id.progress_wheel);

        retryListener();
        setWeb();
    }

    private void retryListener() {
        tapToRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tapToRetry.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                web.setVisibility(View.VISIBLE);
                setWeb();
            }
        });
    }

    private void setWeb() {
        web.setWebViewClient(new WebViewClient() {

            boolean authComplete = false;

            @Override
            public void onPageFinished(WebView view, String url) {
                String authCode;

                progressBar.setVisibility(View.GONE);

                if (url.contains("?code=") && !authComplete) {
                    authComplete = true;
                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("code");
                    web.setVisibility(View.GONE);
                    tapToRetry.setVisibility(View.GONE);
                    getToken(authCode);
                } else if (url.contains("error=access_denied")) {
                    authComplete = true;
                    Toast.makeText(getApplicationContext(), "Trakt access denied", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    finish();
                }
                super.onPageFinished(view, url);
            }
        });

        if (!DetectConnection.checkInternetConnection(this)) {
            tvError.setText(R.string.network_error);
            tapToRetry.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            web.setVisibility(View.GONE);
        } else {
            web.clearCache(true);
            web.clearHistory();
            web.loadUrl(url);
        }
    }

    private void getToken(String authCode) {
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Verifying...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue requestQueue = VolleySingleton.getInstance().getRequestQueue();
        JSONObject jsonBody = null;
        try {
            jsonBody = new JSONObject("{\"code\": \"" + authCode + "\"," +
                    "\"client_id\": \"" + DataHelper.TRAKT_CLIENT_ID + "\"," +
                    "\"client_secret\": \"" + DataHelper.TRAKT_CLIENT_SECRET + "\"," +
                    "\"redirect_uri\": \"" + API.TRAKT_REDIRECT_URI + "\"," +
                    "\"grant_type\": \"" + API.TRAKT_GRANT_TYPE + "\"}");
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                API.TRAKT_TOKEN_URL, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        progressDialog.dismiss();
                        String tok = json.optString("access_token");
                        String refresh = json.optString("refresh_token");
                        Toast.makeText(getApplicationContext(), "Login Successful",
                                Toast.LENGTH_SHORT).show();
                        try {
                            DataHelper.setTraktData(LoginActivity.this, tok, refresh);
                        } catch (GeneralSecurityException e) {
                            //e.printStackTrace();
                        }
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.e("LoginActivity", String.valueOf(error));
                        progressDialog.dismiss();
                        String errorText = "";
                        if (error instanceof TimeoutError) {
                            errorText = ". Connection timed out";
                        } else if (error instanceof ServerError) {
                            errorText = ". Server refused connection";
                        } else if (error instanceof NetworkError) {
                            errorText = ". No network detected";
                        }
                        Toast.makeText(getApplicationContext(), "Error logging in" + errorText,
                                Toast.LENGTH_LONG).show();
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_CANCELED, resultIntent);
                        finish();
                    }
                });
        req.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(req);
    }

    public static class DetectConnection {

        public static boolean checkInternetConnection(Context context) {

            ConnectivityManager con_manager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

            return con_manager.getActiveNetworkInfo() != null
                    && con_manager.getActiveNetworkInfo().isAvailable()
                    && con_manager.getActiveNetworkInfo().isConnected();
        }
    }
}
