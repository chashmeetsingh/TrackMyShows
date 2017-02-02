package com.chashmeet.singh.trackit.utility;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import com.chashmeet.singh.trackit.activity.MainActivity;
import com.chashmeet.singh.trackit.api.trakt.TraktV2;
import com.chashmeet.singh.trackit.api.trakt.entities.BaseEpisode;
import com.chashmeet.singh.trackit.api.trakt.entities.BaseSeason;
import com.chashmeet.singh.trackit.api.trakt.entities.BaseShow;
import com.chashmeet.singh.trackit.api.trakt.entities.EpisodeIds;
import com.chashmeet.singh.trackit.api.trakt.entities.LastActivities;
import com.chashmeet.singh.trackit.api.trakt.entities.LastActivityMore;
import com.chashmeet.singh.trackit.api.trakt.entities.Show;
import com.chashmeet.singh.trackit.api.trakt.entities.SyncEpisode;
import com.chashmeet.singh.trackit.api.trakt.entities.SyncItems;
import com.chashmeet.singh.trackit.api.trakt.entities.SyncResponse;
import com.chashmeet.singh.trackit.api.trakt.enums.Extended;
import com.chashmeet.singh.trackit.helper.DataHelper;
import com.chashmeet.singh.trackit.misc.App;
import com.chashmeet.singh.trackit.realm.RealmEpisode;
import com.chashmeet.singh.trackit.realm.RealmSingleton;
import com.chashmeet.singh.trackit.realm.RealmSync;

public class TraktClient {

    public static final String CLIENT_DATA_TYPE = "progress_bar";

    public static void syncEpisode(final int[] episodeIDs, final int itemType,
                                   final int itemState, final boolean showToast,
                                   final syncListener listener) {
        Intent in = new Intent(MainActivity.ACTION);
        in.putExtra("data_type", CLIENT_DATA_TYPE);
        in.putExtra("show", true);
        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

        Realm realm = RealmSingleton.getInstance().getRealm();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(final Realm realm) {
                for (int episodeID : episodeIDs) {

                    RealmEpisode episode = realm.where(RealmEpisode.class)
                            .equalTo("episodeID", episodeID)
                            .findFirst();

                    RealmSync item = realm.where(RealmSync.class)
                            .equalTo("itemID", episodeID)
                            .equalTo("itemType", itemType)
                            .findFirst();

                    if (item == null) {
                        item = realm.createObject(RealmSync.class);
                        item.setItemID(episodeID);
                        item.setItemType(itemType);
                        item.setItemState(itemState);
                        if (episode != null) {
                            item.setWatchedAt(episode.getWatchedAt());
                            item.setCollectedAt(episode.getCollectedAt());
                        }
                    } else {
                        item.setItemState(itemState);
                        if (episode != null) {
                            item.setWatchedAt(episode.getWatchedAt());
                            item.setCollectedAt(episode.getCollectedAt());
                        }
                    }
                }
                final RealmResults<RealmSync> syncItems = realm.where(RealmSync.class)
                        .equalTo("itemType", itemType)
                        .equalTo("itemState", itemState)
                        .findAll();
                if (syncItems.size() > 0) {
                    SyncItems items = new SyncItems();
                    List<SyncEpisode> syncEpisodes = new ArrayList<>();
                    if (itemState == 1) {

                        if (itemType == 0) {

                            for (int j = 0; j < syncItems.size(); j++) {
                                SyncEpisode episode = new SyncEpisode();
                                episode.watched_at = syncItems.get(j).getWatchedAt();
                                EpisodeIds episodeIds = new EpisodeIds();
                                episodeIds.tvdb = syncItems.get(j).getItemID();
                                episode.ids = episodeIds;
                                syncEpisodes.add(episode);
                            }
                            items.episodes(syncEpisodes);

                        } else {

                            for (int j = 0; j < syncItems.size(); j++) {
                                SyncEpisode episode = new SyncEpisode();
                                episode.collected_at = syncItems.get(j).getCollectedAt();
                                EpisodeIds episodeIds = new EpisodeIds();
                                episodeIds.tvdb = syncItems.get(j).getItemID();
                                episode.ids = episodeIds;
                                syncEpisodes.add(episode);
                            }
                            items.episodes(syncEpisodes);

                        }

                    } else {

                        for (int j = 0; j < syncItems.size(); j++) {
                            SyncEpisode episode = new SyncEpisode();
                            EpisodeIds episodeIds = new EpisodeIds();
                            episodeIds.tvdb = syncItems.get(j).getItemID();
                            episode.ids = episodeIds;
                            syncEpisodes.add(episode);
                        }
                        items.episodes(syncEpisodes);

                    }
                    if (listener != null) {
                        syncEpisodeCall(items, itemType, itemState, showToast,
                                new syncListener() {
                                    @Override
                                    public void onResults(boolean result) {
                                        listener.onResults(result);
                                    }
                                });
                    } else {
                        syncEpisodeCall(items, itemType, itemState, showToast, null);
                    }
                } else {
                    Intent in = new Intent(MainActivity.ACTION);
                    in.putExtra("data_type", CLIENT_DATA_TYPE);
                    in.putExtra("show", false);
                    LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

                    if (listener != null) {
                        listener.onResults(false);
                    }
                }
            }
        });
    }

    private static void syncEpisodeCall(SyncItems items, final int itemType,
                                        final int itemState, final boolean showToast,
                                        final syncListener listener) {

        TraktV2 traktV2 = new TraktV2(DataHelper.TRAKT_CLIENT_ID);
        traktV2.accessToken(DataHelper.TRAKT_ACCESS_TOKEN);
        Call<SyncResponse> call;
        if (itemType == 0) {
            if (itemState == 0) {
                call = traktV2.sync().deleteItemsFromWatchedHistory(items);
            } else {
                call = traktV2.sync().addItemsToWatchedHistory(items);
            }
        } else {
            if (itemState == 0) {
                call = traktV2.sync().deleteItemsFromCollection(items);
            } else {
                call = traktV2.sync().addItemsToCollection(items);
            }
        }

        call.enqueue(new Callback<com.chashmeet.singh.trackit.api.trakt.entities.SyncResponse>() {
            @Override
            public void onResponse(Call<SyncResponse> call,
                                   final retrofit2.Response<SyncResponse> response) {

                if (response.body() != null && (response.code() == 201 || response.code() == 200)) {

                    if (listener != null) {
                        listener.onResults(true);
                    }
                    Toast.makeText(App.getAppContext(), "Sent to Trakt", Toast.LENGTH_SHORT).show();
                    Realm realm = RealmSingleton.getInstance().getRealm();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.where(RealmSync.class)
                                    .equalTo("itemType", itemType)
                                    .equalTo("itemState", itemState)
                                    .findAll()
                                    .deleteAllFromRealm();

                            //TODO Handle not found episodes
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            Intent in = new Intent(MainActivity.ACTION);
                            in.putExtra("data_type", CLIENT_DATA_TYPE);
                            in.putExtra("show", false);
                            LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

                            if (itemType == 0) {
                                DataHelper.setLastTraktUpdate(App.getAppContext(),
                                        new DateTime(DateTimeZone.UTC).toString());
                            } else {
                                DataHelper.setLastTraktCollected(App.getAppContext(),
                                        new DateTime(DateTimeZone.UTC).toString());
                            }
                        }
                    });

                } else {
                    Intent in = new Intent(MainActivity.ACTION);
                    in.putExtra("data_type", CLIENT_DATA_TYPE);
                    in.putExtra("show", false);
                    LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                }
            }

            @Override
            public void onFailure(Call<SyncResponse> call, Throwable t) {
                Intent in = new Intent(MainActivity.ACTION);
                in.putExtra("data_type", CLIENT_DATA_TYPE);
                in.putExtra("show", false);
                LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

                if (showToast) {
                    Toast.makeText(App.getAppContext(), "Error connecting to Trakt", Toast.LENGTH_SHORT).show();
                }
                if (listener != null) {
                    listener.onResults(false);
                }
            }
        });
    }

    public static void syncShowData() {

        Intent in = new Intent(MainActivity.ACTION);
        in.putExtra("data_type", CLIENT_DATA_TYPE);
        in.putExtra("show", true);
        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

        TraktV2 traktV2 = new TraktV2(DataHelper.TRAKT_CLIENT_ID);
        traktV2.accessToken(DataHelper.TRAKT_ACCESS_TOKEN);
        Call<LastActivities> call = traktV2.sync().lastActivities();

        call.enqueue(new Callback<LastActivities>() {
            @Override
            public void onResponse(Call<LastActivities> call,
                                   final retrofit2.Response<LastActivities> response) {

                if (response.body() != null && response.code() == 200) {
                    LastActivityMore episodeData = response.body().episodes;
                    if (!episodeData.watched_at.equals(DataHelper.TRAKT_LAST_WATCHED)) {
                        DataHelper.setLastTraktUpdate(App.getAppContext(),
                                episodeData.watched_at);
                        syncShowHistory();
                    } else {
                        Intent in = new Intent(MainActivity.ACTION);
                        in.putExtra("data_type", CLIENT_DATA_TYPE);
                        in.putExtra("show", false);
                        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                    }
                    if (!episodeData.collected_at.equals(DataHelper.TRAKT_LAST_COLLECTED)) {
                        DataHelper.setLastTraktCollected(App.getAppContext(),
                                episodeData.collected_at);
                        syncShowCollection();
                    } else {
                        Intent in = new Intent(MainActivity.ACTION);
                        in.putExtra("data_type", CLIENT_DATA_TYPE);
                        in.putExtra("show", false);
                        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                    }
                } else {
                    Intent in = new Intent(MainActivity.ACTION);
                    in.putExtra("data_type", CLIENT_DATA_TYPE);
                    in.putExtra("show", false);
                    LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                }
            }

            @Override
            public void onFailure(Call<LastActivities> call, Throwable t) {
                Intent in = new Intent(MainActivity.ACTION);
                in.putExtra("data_type", CLIENT_DATA_TYPE);
                in.putExtra("show", false);
                LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                Toast.makeText(App.getAppContext(), "Error connecting to Trakt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void syncShowHistory() {
        Intent in = new Intent(MainActivity.ACTION);
        in.putExtra("data_type", CLIENT_DATA_TYPE);
        in.putExtra("show", true);
        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

        Realm realm = RealmSingleton.getInstance().getRealm();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                TraktV2 traktV2 = new TraktV2(DataHelper.TRAKT_CLIENT_ID);
                traktV2.accessToken(DataHelper.TRAKT_ACCESS_TOKEN);

                try {
                    retrofit2.Response<List<BaseShow>> response = traktV2.sync().watchedShows(Extended.DEFAULT_MIN).execute();
                    if (response.code() == 200 && response.body() != null) {
                        List<BaseShow> body = response.body();
                        for (BaseShow baseShow : body) {
                            Show show = baseShow.show;
                            RealmResults<RealmEpisode> episodes = realm.where(RealmEpisode.class)
                                    .equalTo("showID", show.ids.tvdb)
                                    .findAll();
                            if (episodes.size() == 0) {
                                continue;
                            }
                            for (BaseSeason season : baseShow.seasons) {
                                int seasonNumber = season.number;
                                for (BaseEpisode episode : season.episodes) {
                                    RealmEpisode realmEpisode = episodes.where()
                                            .equalTo("seasonNumber", seasonNumber)
                                            .equalTo("episodeNumber", episode.number)
                                            .findFirst();
                                    if (realmEpisode != null) {
                                        realmEpisode.setWatched(true, false);
                                        realmEpisode.setWatchedAt(episode.last_watched_at);
                                    }
                                }
                            }
                        }
                    } else {
                        Intent in = new Intent(MainActivity.ACTION);
                        in.putExtra("data_type", CLIENT_DATA_TYPE);
                        in.putExtra("show", false);
                        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                        Toast.makeText(App.getAppContext(), "Error connecting to Trakt", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Intent in = new Intent(MainActivity.ACTION);
                in.putExtra("data_type", CLIENT_DATA_TYPE);
                in.putExtra("show", false);
                LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
            }
        });
    }

    public static void syncShowCollection() {
        Intent in = new Intent(MainActivity.ACTION);
        in.putExtra("data_type", CLIENT_DATA_TYPE);
        in.putExtra("show", true);
        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

        Realm realm = RealmSingleton.getInstance().getRealm();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                TraktV2 traktV2 = new TraktV2(DataHelper.TRAKT_CLIENT_ID);
                traktV2.accessToken(DataHelper.TRAKT_ACCESS_TOKEN);

                try {
                    retrofit2.Response<List<BaseShow>> response = traktV2.sync().collectionShows(Extended.DEFAULT_MIN).execute();
                    if (response.code() == 200 && response.body() != null) {
                        List<BaseShow> body = response.body();
                        for (BaseShow baseShow : body) {
                            Show show = baseShow.show;
                            RealmResults<RealmEpisode> episodes = realm.where(RealmEpisode.class)
                                    .equalTo("showID", show.ids.tvdb)
                                    .findAll();
                            if (episodes.size() == 0) {
                                continue;
                            }
                            for (BaseSeason season : baseShow.seasons) {
                                int seasonNumber = season.number;
                                for (BaseEpisode episode : season.episodes) {
                                    RealmEpisode realmEpisode = episodes.where()
                                            .equalTo("seasonNumber", seasonNumber)
                                            .equalTo("episodeNumber", episode.number)
                                            .findFirst();
                                    if (realmEpisode != null) {
                                        realmEpisode.setCollection(true, false);
                                        realmEpisode.setCollectedAt(episode.collected_at);
                                    }
                                }
                            }
                        }
                    } else {
                        Intent in = new Intent(MainActivity.ACTION);
                        in.putExtra("data_type", CLIENT_DATA_TYPE);
                        in.putExtra("show", false);
                        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                        Toast.makeText(App.getAppContext(), "Error connecting to Trakt", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Intent in = new Intent(MainActivity.ACTION);
                in.putExtra("data_type", CLIENT_DATA_TYPE);
                in.putExtra("show", false);
                LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
            }
        });
    }

    public static void firstSync() {

        Intent in = new Intent(MainActivity.ACTION);
        in.putExtra("data_type", CLIENT_DATA_TYPE);
        in.putExtra("show", true);
        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

        Realm realm = RealmSingleton.getInstance().getRealm();
        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        RealmResults<RealmEpisode> realmWatchedEpisodes = realm.where(RealmEpisode.class)
                                .equalTo("watched", true)
                                .findAll();

                        RealmResults<RealmEpisode> realmCollectedEpisodes = realm.where(RealmEpisode.class)
                                .equalTo("collection", true)
                                .findAll();

                        realm.where(RealmSync.class)
                                .findAll()
                                .deleteAllFromRealm();

                        for (RealmEpisode episode : realmWatchedEpisodes) {
                            RealmSync item = realm.createObject(RealmSync.class);
                            item.setItemID(episode.getEpisodeID());
                            item.setItemType(0);
                            item.setItemState(1);
                            item.setWatchedAt(episode.getWatchedAt());
                            item.setCollectedAt(episode.getCollectedAt());
                        }

                        for (RealmEpisode episode : realmCollectedEpisodes) {
                            RealmSync item = realm.createObject(RealmSync.class);
                            item.setItemID(episode.getEpisodeID());
                            item.setItemType(1);
                            item.setItemState(1);
                            item.setWatchedAt(episode.getWatchedAt());
                            item.setCollectedAt(episode.getCollectedAt());
                        }

                        TraktV2 traktV2 = new TraktV2(DataHelper.TRAKT_CLIENT_ID);
                        traktV2.accessToken(DataHelper.TRAKT_ACCESS_TOKEN);

                        // Sync History
                        try {
                            retrofit2.Response<List<BaseShow>> response = traktV2.sync().watchedShows(Extended.DEFAULT_MIN).execute();
                            if (response.code() == 200 && response.body() != null) {
                                List<BaseShow> body = response.body();
                                for (BaseShow baseShow : body) {
                                    Show show = baseShow.show;
                                    RealmResults<RealmEpisode> episodes = realm.where(RealmEpisode.class)
                                            .equalTo("showID", show.ids.tvdb)
                                            .findAll();
                                    if (episodes.size() == 0) {
                                        continue;
                                    }
                                    for (BaseSeason season : baseShow.seasons) {
                                        int seasonNumber = season.number;
                                        for (BaseEpisode episode : season.episodes) {
                                            RealmEpisode realmEpisode = episodes.where()
                                                    .equalTo("seasonNumber", seasonNumber)
                                                    .equalTo("episodeNumber", episode.number)
                                                    .findFirst();
                                            if (realmEpisode != null) {
                                                realmEpisode.setWatched(true, false);
                                                realmEpisode.setWatchedAt(episode.last_watched_at);

                                                RealmSync realmSync = realm.where(RealmSync.class)
                                                        .equalTo("itemID", realmEpisode.getEpisodeID())
                                                        .equalTo("itemType", 0)
                                                        .findFirst();
                                                if (realmSync != null) {
                                                    realmSync.deleteFromRealm();
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Intent in = new Intent(MainActivity.ACTION);
                                in.putExtra("data_type", CLIENT_DATA_TYPE);
                                in.putExtra("show", false);
                                LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                                Toast.makeText(App.getAppContext(), "Error connecting to Trakt", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Sync Collection
                        try {
                            retrofit2.Response<List<BaseShow>> response = traktV2.sync().collectionShows(Extended.DEFAULT_MIN).execute();
                            if (response.code() == 200 && response.body() != null) {
                                List<BaseShow> body = response.body();
                                for (BaseShow baseShow : body) {
                                    Show show = baseShow.show;
                                    RealmResults<RealmEpisode> episodes = realm.where(RealmEpisode.class)
                                            .equalTo("showID", show.ids.tvdb)
                                            .findAll();
                                    if (episodes.size() == 0) {
                                        continue;
                                    }
                                    for (BaseSeason season : baseShow.seasons) {
                                        int seasonNumber = season.number;
                                        for (BaseEpisode episode : season.episodes) {
                                            RealmEpisode realmEpisode = episodes.where()
                                                    .equalTo("seasonNumber", seasonNumber)
                                                    .equalTo("episodeNumber", episode.number)
                                                    .findFirst();
                                            if (realmEpisode != null) {
                                                realmEpisode.setCollection(true, false);
                                                realmEpisode.setCollectedAt(episode.collected_at);

                                                RealmSync first = realm.where(RealmSync.class)
                                                        .equalTo("itemID", realmEpisode.getEpisodeID())
                                                        .equalTo("itemType", 1)
                                                        .findFirst();
                                                if (first != null) {
                                                    first.deleteFromRealm();
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Intent in = new Intent(MainActivity.ACTION);
                                in.putExtra("data_type", CLIENT_DATA_TYPE);
                                in.putExtra("show", false);
                                LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                                Toast.makeText(App.getAppContext(), "Error connecting to Trakt", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Intent in = new Intent(MainActivity.ACTION);
                        in.putExtra("data_type", CLIENT_DATA_TYPE);
                        in.putExtra("show", false);
                        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                    }
                });
    }

    public interface syncListener {
        void onResults(boolean result);
    }
}
