package com.chashmeet.singh.trackit.fragment.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.adapter.Tab1Adapter;
import com.chashmeet.singh.trackit.helper.DataHelper;
import com.chashmeet.singh.trackit.misc.SpaceItemDecorator;
import com.chashmeet.singh.trackit.model.Tab1Item;
import com.chashmeet.singh.trackit.realm.RealmEpisode;
import com.chashmeet.singh.trackit.realm.RealmShow;
import com.chashmeet.singh.trackit.realm.RealmSingleton;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class Tab1 extends Fragment {

    ///private static final String TAG = "Tab1";
    private static TextView aired, noRecent;
    private static ArrayList<Tab1Item> airedEpisode;
    private static Tab1Adapter mAdapter;
    private RecyclerView recyclerView;
    private RealmResults<RealmEpisode> episodes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_1, container, false);
        setHasOptionsMenu(true);

        aired = (TextView) v.findViewById(R.id.textView);
        noRecent = (TextView) v.findViewById(R.id.textView1);

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new SpaceItemDecorator(getActivity(), R.dimen.list_space, true, true));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        airedEpisode = new ArrayList<>();
        mAdapter = new Tab1Adapter(airedEpisode);
        recyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getRealmData();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            showTutorial();
                        }
                    });
                }
            }, 500);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_tab_1_fragment, menu);
        menu.getItem(1).setChecked(DataHelper.HIDE_WATCHED_EPISODES);
        menu.getItem(2).setChecked(DataHelper.TAB_1_INFINITE_TIME_FRAME);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_hide:
                item.setChecked(!DataHelper.HIDE_WATCHED_EPISODES);
                DataHelper.setEpisodeVisibilityPreference(getActivity(), item.isChecked());
                getRealmData();
                break;

            case R.id.action_range:
                item.setChecked(!DataHelper.TAB_1_INFINITE_TIME_FRAME);
                DataHelper.setTab1TimeFramePreference(getActivity(), item.isChecked());
                getRealmData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showTutorial() {
        if (recyclerView != null && recyclerView.getChildCount() > 0) {
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(100);
            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity());
            sequence.setConfig(config);
            sequence.addSequenceItem(new MaterialShowcaseView.Builder(getActivity())
                    .setTarget(recyclerView.getChildAt(0))
                    .singleUse("tab1")
                    .withRectangleShape()
                    .setContentText("Tap on the right side to set the episode as watched.")
                    .setDismissText("NEXT")
                    .setMaskColour(ContextCompat.getColor(getActivity(), R.color.tutorialBackground))
                    .setDismissTextColor(ContextCompat.getColor(getActivity(), R.color.tutorialDismiss))
                    .build());
            sequence.addSequenceItem(new MaterialShowcaseView.Builder(getActivity())
                    .setTarget(recyclerView.getChildAt(0).findViewById(R.id.watched_indicator))
                    .singleUse("tab1indicator")
                    .setContentText("This indicates whether an episode is watched or not.")
                    .setDismissText("GOT IT")
                    .setShapePadding(15)
                    .setMaskColour(ContextCompat.getColor(getActivity(), R.color.tutorialBackground))
                    .setDismissTextColor(ContextCompat.getColor(getActivity(), R.color.tutorialDismiss))
                    .build());
            sequence.start();
        }
    }

    public void getRealmData() {
        RealmChangeListener<RealmResults<RealmEpisode>> callback = new RealmChangeListener<RealmResults<RealmEpisode>>() {
            @Override
            public void onChange(RealmResults<RealmEpisode> element) {
                if (episodes.isLoaded()) {
                    getAired(episodes);
                    episodes.removeChangeListeners();
                }
            }
        };

        Realm realm = RealmSingleton.getInstance().getRealm();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        long today = c.getTimeInMillis();

        if (DataHelper.TAB_1_INFINITE_TIME_FRAME) {

            if (DataHelper.HIDE_WATCHED_EPISODES) {
                episodes = realm.where(RealmEpisode.class)
                        .greaterThan("seasonNumber", 0)
                        .equalTo("watched", false)
                        .lessThanOrEqualTo("airDateTime", today)
                        .findAllSortedAsync("airDateTime", Sort.DESCENDING);
            } else {
                episodes = realm.where(RealmEpisode.class)
                        .greaterThan("seasonNumber", 0)
                        .lessThanOrEqualTo("airDateTime", today)
                        .findAllSortedAsync("airDateTime", Sort.DESCENDING);
            }
        } else {

            c.add(Calendar.DATE, -30);
            long before = c.getTimeInMillis();

            if (DataHelper.HIDE_WATCHED_EPISODES) {
                episodes = realm.where(RealmEpisode.class)
                        .greaterThan("seasonNumber", 0)
                        .between("airDateTime", before, today)
                        .equalTo("watched", false)
                        .findAllSortedAsync("airDateTime", Sort.DESCENDING);
            } else {
                episodes = realm.where(RealmEpisode.class)
                        .greaterThan("seasonNumber", 0)
                        .between("airDateTime", before, today)
                        .findAllSortedAsync("airDateTime", Sort.DESCENDING);
            }
        }

        episodes.addChangeListener(callback);
    }

    private void getAired(RealmResults<RealmEpisode> episodeList) {
        if (airedEpisode == null) {
            airedEpisode = new ArrayList<>();
        }
        airedEpisode.clear();
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

                long timeDifference = today - episodeAirDate;
                long days = TimeUnit.DAYS.convert(currentDate - c.getTimeInMillis(), TimeUnit.MILLISECONDS);
                long hours = TimeUnit.HOURS.convert(timeDifference, TimeUnit.MILLISECONDS);
                long minutes = TimeUnit.MINUTES.convert(timeDifference, TimeUnit.MILLISECONDS);

                RealmShow show = realm.where(RealmShow.class)
                        .equalTo("showID", episode.getShowID())
                        .findFirst();

                String duration;
                if (hours < 24) {
                    if (hours == 1) {
                        duration = "1 hour ago";
                    } else if (hours > 1) {
                        duration = hours + " hours ago";
                    } else {
                        if (minutes == 1) {
                            duration = "1 minute ago";
                        } else {
                            duration = minutes + " minutes ago";
                        }
                    }
                } else {
                    if (days == 1) {
                        duration = "Yesterday";
                    } else if (days < 30) {
                        duration = days + " days ago";
                    } else {
                        duration = DateFormat.getDateInstance(DateFormat.MEDIUM).format(episodeAirDate);
                    }
                }
                airedEpisode.add(new Tab1Item(
                        episode.getEpisodeTitle(), duration, show.getPosterUrl(),
                        show.getShowTitle(), episode.getDetails(), episode.getEpisodeID(),
                        episode.getWatched()));
            }

        }
        mAdapter.notifyDataSetChanged();
        try {
            if (airedEpisode.size() == 0) {
                noRecent.setVisibility(View.VISIBLE);
            } else {
                noRecent.setVisibility(View.GONE);
            }
            aired.setVisibility(View.GONE);
        } catch (Exception e) {
            //
        }
    }
}