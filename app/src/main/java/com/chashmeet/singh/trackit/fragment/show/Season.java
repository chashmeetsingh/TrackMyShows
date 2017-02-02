package com.chashmeet.singh.trackit.fragment.show;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.adapter.SeasonFragmentAdapter;
import com.chashmeet.singh.trackit.helper.DataHelper;
import com.chashmeet.singh.trackit.misc.SpaceItemDecorator;
import com.chashmeet.singh.trackit.model.SeasonItem;
import com.chashmeet.singh.trackit.realm.RealmEpisode;
import com.chashmeet.singh.trackit.realm.RealmShow;
import com.chashmeet.singh.trackit.realm.RealmSingleton;
import com.chashmeet.singh.trackit.utility.TraktClient;

public class Season extends Fragment {

    public static boolean dataLoaded = false;
    private int showID;
    private ArrayList<SeasonItem> seasonList = new ArrayList<>();
    private SeasonFragmentAdapter mSeasonFragmentAdapter;
    private TextView tvError;
    private int[] episodeIDs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_season, container, false);
        setHasOptionsMenu(true);

        Realm realm = RealmSingleton.getInstance().getRealm();

        tvError = (TextView) v.findViewById(R.id.tv_season_error);

        showID = getArguments().getInt("showID");
        String showTitle = realm.where(RealmShow.class)
                .equalTo("showID", showID)
                .findFirst().getShowTitle();
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recyclerViewSeason);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new SpaceItemDecorator(getActivity(), R.dimen.list_space, true, true));
        mSeasonFragmentAdapter = new SeasonFragmentAdapter(seasonList, showID, showTitle);
        recyclerView.setAdapter(mSeasonFragmentAdapter);

        generateSeasons();
        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !dataLoaded) {
            generateSeasons();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!dataLoaded) {
            generateSeasons();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.season_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Realm realm = RealmSingleton.getInstance().getRealm();

        switch (item.getItemId()) {
            case R.id.watched_all:
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Calendar cal = Calendar.getInstance();
                        RealmResults<RealmEpisode> episodes = realm.where(RealmEpisode.class)
                                .equalTo("showID", showID)
                                .lessThanOrEqualTo("airDateTime", cal.getTimeInMillis())
                                .equalTo("watched", false)
                                .findAll();

                        episodeIDs = new int[episodes.size()];

                        for (int i = 0; i < episodes.size(); i++) {
                            episodes.get(i).setWatched(true, true);
                            episodeIDs[i] = episodes.get(i).getEpisodeID();
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        generateSeasons();
                        if (!DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
                            TraktClient.syncEpisode(episodeIDs, 0, 1,
                                    true, null);
                        }
                    }
                });
                break;

            case R.id.watched_none:
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Calendar cal = Calendar.getInstance();
                        RealmResults<RealmEpisode> episodes = realm.where(RealmEpisode.class)
                                .equalTo("showID", showID)
                                .lessThanOrEqualTo("airDateTime", cal.getTimeInMillis())
                                .equalTo("watched", true)
                                .findAll();

                        episodeIDs = new int[episodes.size()];

                        for (int i = 0; i < episodes.size(); i++) {
                            episodes.get(i).setWatched(false, true);
                            episodeIDs[i] = episodes.get(i).getEpisodeID();
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        generateSeasons();
                        if (!DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
                            TraktClient.syncEpisode(episodeIDs, 0, 0,
                                    true, null);
                        }
                    }
                });
                break;

            case R.id.collected_all:
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Calendar cal = Calendar.getInstance();
                        RealmResults<RealmEpisode> episodes = realm.where(RealmEpisode.class)
                                .equalTo("showID", showID)
                                .lessThanOrEqualTo("airDateTime", cal.getTimeInMillis())
                                .equalTo("collection", false)
                                .findAll();

                        episodeIDs = new int[episodes.size()];

                        for (int i = 0; i < episodes.size(); i++) {
                            episodes.get(i).setCollection(true, true);
                            episodeIDs[i] = episodes.get(i).getEpisodeID();
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        generateSeasons();
                        if (!DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
                            TraktClient.syncEpisode(episodeIDs, 1, 1,
                                    true, null);
                        }
                    }
                });
                break;

            case R.id.collected_none:
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Calendar cal = Calendar.getInstance();
                        RealmResults<RealmEpisode> episodes = realm.where(RealmEpisode.class)
                                .equalTo("showID", showID)
                                .lessThanOrEqualTo("airDateTime", cal.getTimeInMillis())
                                .equalTo("collection", true)
                                .findAll();

                        episodeIDs = new int[episodes.size()];

                        for (int i = 0; i < episodes.size(); i++) {
                            episodes.get(i).setCollection(false, true);
                            episodeIDs[i] = episodes.get(i).getEpisodeID();
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        generateSeasons();
                        if (!DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
                            TraktClient.syncEpisode(episodeIDs, 1, 0,
                                    true, null);
                        }
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void generateSeasons() {
        try {
            dataLoaded = true;
            seasonList.clear();

            Realm realm = RealmSingleton.getInstance().getRealm();
            RealmResults<RealmEpisode> episodes = realm.where(RealmEpisode.class)
                    .equalTo("showID", showID)
                    .equalTo("episodeNumber", 1)
                    .distinct("seasonNumber")
                    .sort("seasonNumber", Sort.DESCENDING);

            for (int i = 0; i < episodes.size(); i++) {
                RealmEpisode temp = episodes.get(i);
                int seasonNumber = temp.getSeasonNumber();
                if (seasonNumber == 0) {
                    seasonList.add(new SeasonItem("Specials", temp.getEpisodeID(), seasonNumber));
                } else {
                    seasonList.add(new SeasonItem("Season " + seasonNumber, temp.getEpisodeID(), seasonNumber));
                }
            }
            if (episodes.size() != 0) {
                mSeasonFragmentAdapter.notifyDataSetChanged();
            } else {
                tvError.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e("SeasonFragment", String.valueOf(e));
        }
    }
}