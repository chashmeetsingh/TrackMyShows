package com.chashmeet.singh.trackit.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.util.ArrayList;

import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.adapter.ShowImageAdapter;
import com.chashmeet.singh.trackit.api.API;
import com.chashmeet.singh.trackit.model.ShowImageItem;
import com.chashmeet.singh.trackit.utility.VolleySingleton;

public class ShowImageActivity extends AppCompatActivity {

    private int showID, imageType;
    private String[] title = {"Poster", "Banner", "Fan Art"};
    private String[] imageName = {"poster", "series", "fanart"};
    private RelativeLayout tapToRetry;
    private ProgressBar wheel;
    private TextView tvError;
    private ArrayList<ShowImageItem> imageArray = new ArrayList<>();
    private ShowImageAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        showID = getIntent().getIntExtra("showID", 0);
        imageType = getIntent().getIntExtra("type", 0);

        tapToRetry = (RelativeLayout) findViewById(R.id.tap_to_retry);
        tvError = (TextView) findViewById(R.id.tv_error);
        wheel = (ProgressBar) findViewById(R.id.progress_wheel);
        recyclerView = (RecyclerView) findViewById(R.id.iamge_list);
        setTitle(title[imageType]);

        retryListener();
        initializeRecyclerView();
        fetchData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void retryListener() {
        tapToRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tapToRetry.setVisibility(View.GONE);
                fetchData();
            }
        });
    }

    private void initializeRecyclerView() {
        //recyclerView.addItemDecoration(new SpaceItemDecorator(this, R.dimen.list_space, true, true));
        switch (imageType) {
            case 0:
                recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                break;
            case 1:
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                break;
            case 2:
                recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
                break;
        }
        adapter = new ShowImageAdapter(imageArray, showID, imageType);
        recyclerView.setAdapter(adapter);
    }

    private void fetchData() {
        wheel.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = VolleySingleton.getInstance().getRequestQueue();
        StringRequest req = new StringRequest(Request.Method.GET, API.BASE_IMAGE_URL + showID + "/banners.xml",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        final String imageURL = API.TVDB_LINK + "banners/";
                        imageArray.clear();
                        try {
                            JSONObject jsonObjectResponse = XML.toJSONObject(response);
                            JSONArray bannerList = jsonObjectResponse.getJSONObject("Banners").getJSONArray("Banner");

                            for (int i = 0; i < bannerList.length(); i++) {
                                JSONObject imageObject = bannerList.getJSONObject(i);
                                if (imageObject.optString("BannerType").equals(imageName[imageType])) {
                                    ShowImageItem imageItem = new ShowImageItem();
                                    imageItem.setImagePath(imageURL + imageObject.optString("BannerPath"));
                                    String thumbnailPath = imageObject.optString("ThumbnailPath", "");
                                    if (!thumbnailPath.equals("")) {
                                        imageItem.setThumbnailPath(imageURL + thumbnailPath);
                                    } else {
                                        imageItem.setThumbnailPath("");
                                    }
                                    imageArray.add(imageItem);
                                }
                            }
                            wheel.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            wheel.setVisibility(View.GONE);
                            Log.e("ShowImageActivity", String.valueOf(e));
                            tvError.setText(R.string.no_images_available);
                            tapToRetry.setVisibility(View.VISIBLE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    tvError.setText(R.string.timeout_error);
                } else if (error instanceof ServerError) {
                    tvError.setText(R.string.server_error);
                } else {
                    tvError.setText(R.string.connection_error);
                }
                tapToRetry.setVisibility(View.VISIBLE);
                wheel.setVisibility(View.GONE);
            }
        });
        requestQueue.add(req);
    }
}
