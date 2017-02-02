package com.chashmeet.singh.trackit.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
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
import com.chashmeet.singh.trackit.adapter.ActorAdapter;
import com.chashmeet.singh.trackit.api.API;
import com.chashmeet.singh.trackit.misc.SpaceItemDecorator;
import com.chashmeet.singh.trackit.model.ActorItem;
import com.chashmeet.singh.trackit.utility.VolleySingleton;

public class ActorDetailActivity extends AppCompatActivity {

    private int showID;
    private ArrayList<ActorItem> actorArray = new ArrayList<>();
    private ActorAdapter adapter;
    private RelativeLayout tapToRetry;
    private TextView tvError;
    private ProgressBar wheel;
    private RecyclerView recyclerView;
    private TextView tvActorError;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actor_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tapToRetry = (RelativeLayout) findViewById(R.id.tap_to_retry);
        tvError = (TextView) findViewById(R.id.tv_error);
        wheel = (ProgressBar) findViewById(R.id.progress_wheel);
        recyclerView = (RecyclerView) findViewById(R.id.actor_list);
        tvActorError = (TextView) findViewById(R.id.actor_error);

        showID = getIntent().getIntExtra("showID", 0);

        setRetry();
        initializeRecyclerView();
        fetchData();
    }

    private void setRetry() {
        tapToRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapToRetry.setVisibility(View.GONE);
                fetchData();
            }
        });
    }

    private void initializeRecyclerView() {
        recyclerView.addItemDecoration(new SpaceItemDecorator(this, R.dimen.list_space, true, true));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        RelativeLayout overlay = (RelativeLayout) findViewById(R.id.overlay);
        ImageView expandedImageView = (ImageView) findViewById(R.id.expanded_image);
        adapter = new ActorAdapter(actorArray, container, expandedImageView, overlay);
        recyclerView.setAdapter(adapter);
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

    private void fetchData() {
        wheel.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = VolleySingleton.getInstance().getRequestQueue();
        StringRequest req = new StringRequest(Request.Method.GET, API.BASE_IMAGE_URL + showID + "/actors.xml",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        new loadData().execute(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    tvError.setText(R.string.timeout_error);
                } else if (error instanceof ServerError) {
                    tvError.setText(R.string.server_error);
                } else if (error instanceof NetworkError) {
                    tvError.setText(R.string.network_error);
                } else {
                    tvError.setText(R.string.connection_error);
                }
                tapToRetry.setVisibility(View.VISIBLE);
                wheel.setVisibility(View.GONE);
            }
        });
        requestQueue.add(req);
    }

    private class loadData extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... response) {
            final String imageURL = API.TVDB_LINK + "banners/";
            actorArray.clear();
            try {
                JSONObject jsonObjectResponse = XML.toJSONObject(response[0]);
                JSONArray actorList = jsonObjectResponse.getJSONObject("Actors").getJSONArray("Actor");

                for (int i = 0; i < actorList.length(); i++) {
                    JSONObject actorObject = actorList.getJSONObject(i);
                    String image = imageURL + actorObject.optString("Image", "");
                    String name = actorObject.optString("Name", "");
                    String role = "as " + actorObject.optString("Role", "");

                    if (!name.equals("")) {
                        ActorItem actor = new ActorItem();
                        actor.setId(actorObject.optInt("id"));
                        actor.setImageURL(image);
                        actor.setName(name);
                        if (!role.equals("as ")) {
                            actor.setRole(role);
                        }
                        actorArray.add(actor);
                    }
                }
            } catch (JSONException e) {
                //e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (actorArray.size() == 0) {
                tvActorError.setVisibility(View.VISIBLE);
            } else {
                adapter.notifyDataSetChanged();
            }
            wheel.setVisibility(View.GONE);
        }
    }
}
