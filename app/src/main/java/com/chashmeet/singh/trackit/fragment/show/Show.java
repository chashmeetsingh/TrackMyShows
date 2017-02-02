package com.chashmeet.singh.trackit.fragment.show;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.activity.ActorDetailActivity;
import com.chashmeet.singh.trackit.activity.ShowSettingsActivity;
import com.chashmeet.singh.trackit.realm.RealmShow;

public class Show extends Fragment {

    private int showID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_show, container, false);
        setHasOptionsMenu(true);

        showID = getArguments().getInt("showID");
        Realm realm = Realm.getDefaultInstance();
        RealmShow show = realm.where(RealmShow.class).equalTo("showID", showID).findFirst();

        ExpandableTextView overview = (ExpandableTextView) v.findViewById(R.id.tv_overview)
                .findViewById(R.id.expand_text_view);
        overview.setText(show.getOverview());

        TextView tvGenre = (TextView) v.findViewById(R.id.tv_genre);
        String genre = show.getGenre();
        tvGenre.setText(genre);

        TextView tvAired = (TextView) v.findViewById(R.id.tv_aired);
        Date firstAired = show.getFirstAired();
        if (firstAired == null) {
            tvAired.setText(R.string.unavailable);
        } else {
            tvAired.setText(DateFormat.getDateInstance(DateFormat.FULL).format(show.getFirstAired()));
        }

        TextView tvNetwork = (TextView) v.findViewById(R.id.tv_network);
        DateFormat airTimeFormat = new SimpleDateFormat("hh:mm a");
        String airTime = airTimeFormat.format(show.getAirTime());
        String network;
        if (airTime != null) {
            network = show.getNetwork() + " (" + airTime + ") (" + show.getRunTime()
                    + " minutes)";
        } else {
            network = show.getNetwork() + " (" + show.getRunTime() + " minutes)";
        }
        tvNetwork.setText(network);

        TextView tvStatus = (TextView) v.findViewById(R.id.tv_status);
        tvStatus.setText(show.getStatus());

        Float ratingFloat = show.getRating();
        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rating);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#FF5252"), PorterDuff.Mode.SRC_ATOP); // for filled stars
        stars.getDrawable(1).setColorFilter(Color.parseColor("#616161"), PorterDuff.Mode.SRC_ATOP); // for half filled stars
        stars.getDrawable(0).setColorFilter(Color.parseColor("#616161"), PorterDuff.Mode.SRC_ATOP); // for empty stars
        ratingBar.setRating(ratingFloat);

        TextView tvRating = (TextView) v.findViewById(R.id.tv_rating);
        String rating = "(" + String.format(java.util.Locale.US, "%.1f", ratingFloat) + " / 10)";
        tvRating.setText(rating);

        TextView tvRatingCount = (TextView) v.findViewById(R.id.tv_rating_count);
        String ratingCount = "(" + show.getRatingCount() + " votes)";
        tvRatingCount.setText(ratingCount);

        RelativeLayout actorLayout = (RelativeLayout) v.findViewById(R.id.actor_layout);
        actorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActorDetailActivity.class);
                intent.putExtra("showID", showID);
                getActivity().startActivity(intent);
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_show_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(getActivity(), ShowSettingsActivity.class);
                intent.putExtra("showID", showID);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}