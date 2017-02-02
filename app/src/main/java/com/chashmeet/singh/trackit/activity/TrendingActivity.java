package com.chashmeet.singh.trackit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.adapter.TrendingAdapter;
import com.chashmeet.singh.trackit.api.API;
import com.chashmeet.singh.trackit.api.tmdb.Tmdb;
import com.chashmeet.singh.trackit.api.tmdb.entities.TvResultsPage;
import com.chashmeet.singh.trackit.api.tmdb.entities.TvShow;
import com.chashmeet.singh.trackit.listener.RecyclerItemClickListener;

public class TrendingActivity extends AppCompatActivity {

    //private final String TAG = "TrendingActivity";
    private TrendingAdapter mAdapter;
    private RelativeLayout tapToRetry;
    private List<TvShow> arrayList;
    private ProgressBar wheel;
    private TextView tvError;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        wheel = (ProgressBar) findViewById(R.id.progress_wheel_trending);
        tapToRetry = (RelativeLayout) findViewById(R.id.tap_to_retry);
        tvError = (TextView) findViewById(R.id.tv_error);
        arrayList = new ArrayList<>();

        configureRetry();
        initializeRecyclerView();
        getTrendingShows();
    }

    private void configureRetry() {
        tapToRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapToRetry.setVisibility(View.GONE);
                getTrendingShows();
            }
        });
    }

    private void initializeRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mAdapter = new TrendingAdapter(arrayList);
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int i) {
                Intent intent = new Intent(TrendingActivity.this, AddShowActivity.class);
                intent.putExtra("id", arrayList.get(i).id);
                intent.putExtra("title", arrayList.get(i).name);
                intent.putExtra("overview", arrayList.get(i).overview);
                intent.putExtra("status", "Unknown");
                intent.putExtra("year", arrayList.get(i).first_air_date);
                intent.putExtra("poster", arrayList.get(i).poster_path);
                intent.putExtra("tmdb", true);
                startActivity(intent);
            }

            @Override
            public void onItemLongPress(View childView, int position) {
            }
        }));
    }

    private void getTrendingShows() {
        wheel.setVisibility(View.VISIBLE);

        Tmdb tmdb = new Tmdb(API.TMDB_API_KEY);
        Call<TvResultsPage> call = tmdb.tvService().popular(1, "en-US");

        call.enqueue(new Callback<TvResultsPage>() {
            @Override
            public void onResponse(Call<TvResultsPage> call, retrofit2.Response<TvResultsPage> response) {
                arrayList.clear();
                tapToRetry.setVisibility(View.GONE);
                if (response.body() != null && response.code() == 200) {
                    arrayList.addAll(response.body().results);
                    mAdapter.notifyDataSetChanged();
                } else {
                    tapToRetry.setVisibility(View.VISIBLE);
                }
                wheel.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<TvResultsPage> call, Throwable t) {
                tvError.setText(R.string.network_error);
                tapToRetry.setVisibility(View.VISIBLE);
                wheel.setVisibility(View.GONE);
            }
        });
    }
}

