package com.chashmeet.singh.trackit.fragment.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.adapter.Tab3Adapter;
import com.chashmeet.singh.trackit.helper.DataHelper;
import com.chashmeet.singh.trackit.misc.SpaceItemDecorator;
import com.chashmeet.singh.trackit.model.Tab1Item;
import com.chashmeet.singh.trackit.realm.RealmEpisode;
import com.chashmeet.singh.trackit.realm.RealmShow;
import com.chashmeet.singh.trackit.realm.RealmSingleton;

public class Tab3 extends Fragment {

    //private static final String TAG = "Tab3";
    private static TextView aired, noUpcoming;
    private static ArrayList<Tab1Item> upcomingEpisode;
    private static Tab3Adapter mAdapter;
    private RealmResults<RealmEpisode> episodes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_3, container, false);
        setHasOptionsMenu(true);

        aired = (TextView) v.findViewById(R.id.textView);
        noUpcoming = (TextView) v.findViewById(R.id.textView1);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recyclerViewTab3);
        recyclerView.addItemDecoration(new SpaceItemDecorator(getActivity(), R.dimen.list_space, true, true));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        upcomingEpisode = new ArrayList<>();
        mAdapter = new Tab3Adapter(upcomingEpisode);
        recyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getRealmData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_tab_3_fragment, menu);
        menu.getItem(1).setChecked(DataHelper.TAB_3_INFINITE_TIME_FRAME);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_range:
                item.setChecked(!DataHelper.TAB_3_INFINITE_TIME_FRAME);
                DataHelper.setTab3TimeFramePreference(getActivity(), item.isChecked());
                getRealmData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getRealmData() {
        RealmChangeListener<RealmResults<RealmEpisode>> callback = new RealmChangeListener<RealmResults<RealmEpisode>>() {
            @Override
            public void onChange(RealmResults<RealmEpisode> element) {
                if (episodes.isLoaded()) {
                    getUpcoming(episodes);
                    episodes.removeChangeListeners();
                }
            }
        };

        Realm realm = RealmSingleton.getInstance().getRealm();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        long today = c.getTimeInMillis();

        if (DataHelper.TAB_3_INFINITE_TIME_FRAME) {

            episodes = realm.where(RealmEpisode.class)
                    .greaterThan("seasonNumber", 0)
                    .greaterThan("airDateTime", today)
                    .findAllSortedAsync("airDateTime", Sort.ASCENDING);

        } else {

            c.add(Calendar.DATE, 30);
            long later = c.getTimeInMillis();

            episodes = realm.where(RealmEpisode.class)
                    .greaterThan("seasonNumber", 0)
                    .between("airDateTime", today, later)
                    .findAllSortedAsync("airDateTime", Sort.ASCENDING);
        }

        episodes.addChangeListener(callback);
    }

    private void getUpcoming(RealmResults<RealmEpisode> episodeList) {
        if (upcomingEpisode == null) {
            upcomingEpisode = new ArrayList<>();
        }
        upcomingEpisode.clear();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        long today = c.getTimeInMillis();

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long currentDate = c.getTimeInMillis();

        Realm realm = RealmSingleton.getInstance().getRealm();

        for (RealmEpisode episode : episodeList) {

            long episodeAirDate = episode.getAirDateTime();
            if (episodeAirDate != 0) {

                c.setTimeInMillis(episodeAirDate);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);

                long timeDifference = episodeAirDate - today;
                long days = TimeUnit.DAYS.convert(c.getTimeInMillis() - currentDate, TimeUnit.MILLISECONDS);
                long hours = TimeUnit.HOURS.convert(timeDifference, TimeUnit.MILLISECONDS);
                long minutes = TimeUnit.MINUTES.convert(timeDifference, TimeUnit.MILLISECONDS);

                RealmShow show = realm.where(RealmShow.class)
                        .equalTo("showID", episode.getShowID())
                        .findFirst();

                String duration;
                if (hours < 24) {
                    if (hours == 1) {
                        duration = "in 1 hour";
                    } else if (hours > 1) {
                        duration = "in " + hours + " hours";
                    } else {
                        if (minutes == 1) {
                            duration = "in 1 minute";
                        } else {
                            duration = "in " + minutes + " minutes";
                        }
                    }
                } else {
                    if (days == 1) {
                        duration = "Tomorrow";
                    } else if (days < 30) {
                        duration = "in " + days + " days";
                    } else {
                        duration = DateFormat.getDateInstance(DateFormat.MEDIUM).format(episodeAirDate);
                    }
                }
                upcomingEpisode.add(new Tab1Item(
                        episode.getEpisodeTitle(), duration, show.getPosterUrl(),
                        show.getShowTitle(), episode.getDetails(), episode.getEpisodeID(),
                        episode.getWatched()));
            }
        }
        mAdapter.notifyDataSetChanged();
        try {
            if (upcomingEpisode.size() == 0) {
                noUpcoming.setVisibility(View.VISIBLE);
            } else {
                noUpcoming.setVisibility(View.GONE);
            }
            aired.setVisibility(View.GONE);
        } catch (Exception e) {
            //
        }
    }
}