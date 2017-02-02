package com.chashmeet.singh.trackit.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.adapter.SearchAdapter;
import com.chashmeet.singh.trackit.api.API;
import com.chashmeet.singh.trackit.api.thetvdb.TheTvdb;
import com.chashmeet.singh.trackit.api.thetvdb.entities.Series;
import com.chashmeet.singh.trackit.api.thetvdb.entities.SeriesResultsWrapper;
import com.chashmeet.singh.trackit.helper.DataHelper;
import com.chashmeet.singh.trackit.listener.RecyclerItemClickListener;
import com.chashmeet.singh.trackit.misc.SpaceItemDecorator;
import com.chashmeet.singh.trackit.model.SearchSuggestions;
import com.chashmeet.singh.trackit.utility.VolleySingleton;

public class SearchActivity extends AppCompatActivity {

    private final String TAG = "SearchActivity";
    private String mQuery;
    private List<Series> searchResults = new ArrayList<>();
    private FloatingSearchView searchView;
    private RelativeLayout tapToRetry;
    private RelativeLayout relativeLayout;
    private SearchAdapter mAdapter;
    private ProgressBar wheel;
    private TextView showError, connectionError;
    private Timer mSearchTimer = new Timer();
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initialise();
        configure();
        setSearchListeners();
    }

    private void initialise() {
        wheel = (ProgressBar) findViewById(R.id.progress_wheel_search);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView1);
        searchView = (FloatingSearchView) findViewById(R.id.search_view);
        tapToRetry = (RelativeLayout) findViewById(R.id.tap_to_retry);
        showError = (TextView) findViewById(R.id.tv_no_shows);
        connectionError = (TextView) findViewById(R.id.tv_error);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
    }

    private void configure() {
        wheel.setVisibility(View.GONE);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new OnItemClickListener()));
        recyclerView.addItemDecoration(new SpaceItemDecorator(SearchActivity.this, R.dimen.search_list_space, true, true));
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        mAdapter = new SearchAdapter(searchResults);
        recyclerView.setAdapter(mAdapter);
        searchView.setSearchFocused(true);

        tapToRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuery != null && !mQuery.equals("")) {
                    loadData(mQuery);
                } else {
                    //loadTraktShows();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchBarFocused()) {
            searchView.clearSearchFocus();
        } else {
            super.onBackPressed();
        }
    }

    private void setSearchListeners() {
        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                mSearchTimer.cancel();
                mQuery = newQuery;
                mSearchTimer = new Timer();
                mSearchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (searchView.isSearchBarFocused() && !mQuery.equals("")) {
                                    loadSearchSuggestions(mQuery);
                                    searchView.clearSuggestions();
                                    searchView.showProgress();
                                }
                            }
                        });
                    }
                }, 700);
            }
        });
        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                SearchSuggestions suggestion = (SearchSuggestions) searchSuggestion;
                mQuery = suggestion.getName();
                searchView.setSearchText(mQuery);
                loadData(mQuery);
                searchView.hideProgress();
            }

            @Override
            public void onSearchAction(String currentQuery) {
                mQuery = currentQuery;
                searchView.setSearchText(currentQuery);
                searchView.clearSearchFocus();
                loadData(currentQuery);
                searchView.hideProgress();
            }
        });
        searchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon,
                                         TextView textView, SearchSuggestion item,
                                         int itemPosition) {
                leftIcon.setImageResource(R.drawable.ic_suggestion);
            }
        });
        searchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                searchView.clearQuery();
            }

            @Override
            public void onFocusCleared() {
                mSearchTimer.cancel();
                searchView.clearSuggestions();
            }
        });
    }

    private void loadSearchSuggestions(final String query) {
        String showName = query.replaceAll("\\s", "+");
        RequestQueue requestQueue = VolleySingleton.getInstance().getRequestQueue();
        JsonArrayRequest req = new JsonArrayRequest(
                API.TV_MAZE_SEARCH + showName,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<SearchSuggestions> searchSuggestions = new ArrayList<>();
                        JSONObject object;
                        try {
                            for (int j = 0; j < response.length() && searchSuggestions.size() < 5; j++) {
                                object = response.getJSONObject(j).getJSONObject("show");
                                SearchSuggestions suggestion = new SearchSuggestions(object.getString("name"));
                                if (!searchSuggestions.contains(suggestion)) {
                                    searchSuggestions.add(suggestion);
                                }
                            }
                        } catch (JSONException e) {
                            //Log.e("JSON exception", e.getMessage());
                        }
                        if (searchView.isSearchBarFocused()) {
                            searchView.swapSuggestions(searchSuggestions);
                        }
                        searchView.hideProgress();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        searchView.hideProgress();
                    }
                });
        req.setRetryPolicy(new DefaultRetryPolicy(
                5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(req);
    }

    private void loadData(final String query) {
        tapToRetry.setVisibility(View.GONE);
        showError.setVisibility(View.GONE);
        searchResults.clear();
        wheel.setVisibility(View.VISIBLE);
        String showName = query.replaceAll("\\s", "+");
        showName = showName.replace(".", "");

        TheTvdb theTvdb = new TheTvdb(DataHelper.TVDB_KEY);
        Call<SeriesResultsWrapper> call = theTvdb.search()
                .series(showName, null, null, null);

        call.enqueue(new Callback<SeriesResultsWrapper>() {
            @Override
            public void onResponse(Call<SeriesResultsWrapper> call, retrofit2.Response<SeriesResultsWrapper> response) {
                tapToRetry.setVisibility(View.GONE);
                searchResults.clear();
                if (response.body() == null) {
                    showError.setVisibility(View.VISIBLE);
                } else {
                    searchResults.addAll(response.body().data);
                }
                wheel.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<SeriesResultsWrapper> call, Throwable t) {
                tapToRetry.setVisibility(View.VISIBLE);
                wheel.setVisibility(View.GONE);
                showError.setVisibility(View.GONE);
            }
        });
    }

    private class OnItemClickListener extends RecyclerItemClickListener.SimpleOnItemClickListener {
        @Override
        public void onItemClick(View childView, int i) {
            Intent launchNewIntent = new Intent(SearchActivity.this, AddShowActivity.class);
            launchNewIntent.putExtra("id", searchResults.get(i).id);
            launchNewIntent.putExtra("title", searchResults.get(i).seriesName);
            launchNewIntent.putExtra("overview", searchResults.get(i).overview);
            launchNewIntent.putExtra("status", searchResults.get(i).status);
            launchNewIntent.putExtra("year", searchResults.get(i).firstAired);
            startActivity(launchNewIntent);
        }
    }
}

