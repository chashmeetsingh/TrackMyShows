package com.chashmeet.singh.trackit.helper;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.Callback;
import com.chashmeet.singh.trackit.activity.MainActivity;
import com.chashmeet.singh.trackit.api.API;
import com.chashmeet.singh.trackit.api.thetvdb.TheTvdb;
import com.chashmeet.singh.trackit.api.thetvdb.entities.SeriesImageQueryResults;
import com.chashmeet.singh.trackit.api.trakt.TraktV2;
import com.chashmeet.singh.trackit.api.trakt.entities.Episode;
import com.chashmeet.singh.trackit.api.trakt.entities.SearchResult;
import com.chashmeet.singh.trackit.api.trakt.entities.Season;
import com.chashmeet.singh.trackit.api.trakt.entities.Show;
import com.chashmeet.singh.trackit.api.trakt.enums.Extended;
import com.chashmeet.singh.trackit.api.trakt.enums.IdType;
import com.chashmeet.singh.trackit.api.trakt.enums.Type;
import com.chashmeet.singh.trackit.api.trakt.validators.EpisodeValidator;
import com.chashmeet.singh.trackit.api.trakt.validators.ShowValidator;
import com.chashmeet.singh.trackit.misc.App;
import com.chashmeet.singh.trackit.realm.RealmEpisode;
import com.chashmeet.singh.trackit.realm.RealmShow;
import com.chashmeet.singh.trackit.realm.RealmSingleton;
import com.chashmeet.singh.trackit.utility.TraktClient;

public class DatabaseHelper {

    public static final String DATABASE_DATA_TYPE = "update";
    private static final String TAG = "DatabaseHelper";
    private boolean resultSent, syncTraktData = false;
    private int index;
    private ArrayList<UpdateItems> updatedRealmShows;

    public DatabaseHelper() {
        updatedRealmShows = new ArrayList<>();
    }

    public void updateDB(final IdType idType, final RealmList<RealmShow> realmShows,
                         final onUpdateListener listener) {

        Intent in = new Intent(MainActivity.ACTION);
        in.putExtra("data_type", TraktClient.CLIENT_DATA_TYPE);
        in.putExtra("show", true);
        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

        resultSent = false;
        index = 0;
        final int size = realmShows.size();
        for (int i = 0; i < size; i++) {
            RealmShow show = realmShows.get(i);
            getShowData(idType, show.getShowID(), new onUpdateListener() {
                @Override
                public void onResults(boolean result) {
                    if (result && index == size && !resultSent) {

                        writeToDB(listener);
                        resultSent = true;

                        Intent in = new Intent(MainActivity.ACTION);
                        in.putExtra("data_type", DATABASE_DATA_TYPE);
                        in.putExtra("data", "Fetching show data: " + index + "/" + size);
                        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

                    } else if (!result && !resultSent) {

                        resultSent = true;
                        listener.onResults(false);

                        Intent in = new Intent(MainActivity.ACTION);
                        in.putExtra("data_type", TraktClient.CLIENT_DATA_TYPE);
                        in.putExtra("show", false);
                        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

                        Intent in1 = new Intent(MainActivity.ACTION);
                        in1.putExtra("data_type", DATABASE_DATA_TYPE);
                        in1.putExtra("data", "Error fetching data");
                        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in1);

                    }
                    /*if (!resultSent) {

                        Intent in = new Intent(MainActivity.ACTION);
                        in.putExtra("data_type", DATABASE_DATA_TYPE);
                        in.putExtra("data", "Fetching show data: " + index + "/" + size);
                        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

                    }*/
                }
            });
        }
    }

    private void writeToDB(final onUpdateListener listener) {
        Realm realm = RealmSingleton.getInstance().getRealm();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                for (int i = 0; i < updatedRealmShows.size(); i++) {

                    UpdateItems data = updatedRealmShows.get(i);

                    RealmShow show = realm.where(RealmShow.class)
                            .equalTo("showID", data.showID)
                            .findFirst();

                    if (show == null) {
                        show = realm.createObject(RealmShow.class);
                        show.setEnableNotification(true);
                        show.setUpdateImages(true);
                        show.setHidden(false);
                        show.setTimeOffset(0);
                        show.setShowID(data.showID);
                        show.setPosterUrl("");
                        show.setBannerUrl("");
                        show.setFanartUrl("");
                        syncTraktData = true;
                    }

                    Show showData = data.show;

                    show.setTitle(showData.title);

                    String genres = "";
                    for (String genre : showData.genres) {
                        genres += genre.substring(0, 1).toUpperCase() + genre.substring(1) + ", ";
                    }
                    if (genres.length() >= 2) {
                        genres = genres.substring(0, genres.length() - 2);
                    } else {
                        genres = "Unknown";
                    }
                    show.setGenre(genres);

                    String timeZone = showData.airs.timezone;

                    DateTime dateTime = new DateTime(showData.first_aired, DateTimeZone.UTC);
                    show.setFirstAired(dateTime.toDateTime(DateTimeZone.getDefault()).toDate());

                    show.setAirTime(new Date(0));
                    show.setNetwork(showData.network);
                    show.setOverview(showData.overview);
                    show.setRating(showData.rating);
                    show.setRatingCount(showData.votes);
                    show.setRunTime(showData.runtime);
                    show.setStatus(showData.status.toString());
                    show.setTimeZone(timeZone);
                    show.setTraktID(showData.ids.trakt);
                    show.setImdbID(showData.ids.imdb);
                    show.setTmdbID(showData.ids.tmdb);

                    List<Season> seasonList = data.seasons;

                    if (seasonList != null) {

                        for (Season season : seasonList) {

                            List<Episode> episodeList = season.episodes;

                            if (episodeList != null) {

                                for (Episode episode : episodeList) {

                                    EpisodeValidator validator = new EpisodeValidator(episode);
                                    episode = validator.episodeKeyCheck();

                                    RealmEpisode realmEpisode = show.getEpisodes()
                                            .where()
                                            .equalTo("seasonNumber", episode.season)
                                            .equalTo("episodeNumber", episode.number)
                                            .findFirst();

                                    if (realmEpisode == null) {
                                        realmEpisode = new RealmEpisode();
                                        realmEpisode.setEpisodeID(episode.ids.tvdb);
                                        realmEpisode.setWatched(false, true);
                                        realmEpisode.setCollection(false, true);
                                        realmEpisode.setShowID(show.getShowID());
                                        realmEpisode.setUserRating(0);
                                    }

                                    DateTime episodeDateTime = new DateTime(episode.first_aired, DateTimeZone.UTC);
                                    realmEpisode.setAirDateTime(episodeDateTime.toDateTime(DateTimeZone.getDefault()).getMillis());

                                    realmEpisode.setEpisodeTitle(episode.title);
                                    realmEpisode.setOverView(episode.overview);
                                    realmEpisode.setBannerUrl(API.TVDB_LINK +
                                            "banners/episodes/" +
                                            show.getShowID() + "/" +
                                            episode.ids.tvdb + ".jpg");
                                    realmEpisode.setRating(episode.rating);
                                    realmEpisode.setRatingCount(episode.votes);
                                    realmEpisode.setEpisodeNumber(episode.number);
                                    realmEpisode.setSeasonNumber(episode.season);
                                    realmEpisode.setDetails();
                                    realmEpisode.setTraktID(episode.ids.trakt);
                                    realmEpisode.setTmdbID(episode.ids.tmdb);

                                    show.getEpisodes().add(realmEpisode);
                                }
                            }
                        }
                        try {
                            DateTime firstAired = seasonList.get(seasonList.size() - 1).episodes.get(0).first_aired;
                            firstAired.withZone(DateTimeZone.UTC);
                            show.setAirTime(firstAired.toDateTime(DateTimeZone.getDefault()).toDate());
                        } catch (Exception e) {
                            //
                        }
                    } else {
                        Toast.makeText(App.getAppContext(), "No episode data available for "
                                + show.getShowTitle(), Toast.LENGTH_LONG).show();
                    }

                    if (show.isUpdateImages()) {
                        getShowImages(show.getShowID(), realm);
                    }
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                listener.onResults(true);

                Intent in = new Intent(MainActivity.ACTION);
                in.putExtra("data_type", TraktClient.CLIENT_DATA_TYPE);
                in.putExtra("show", false);
                LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

                if (syncTraktData && !DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
                    TraktClient.syncShowHistory();
                    TraktClient.syncShowCollection();

                    Intent intent = new Intent(MainActivity.ACTION);
                    intent.putExtra("data_type", DATABASE_DATA_TYPE);
                    intent.putExtra("data", "Syncing Trakt Data");
                    LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(intent);
                }
            }
        });
    }

    private void getShowData(final IdType idType, final int showId, final onUpdateListener listener) {

        TraktV2 traktV2 = new TraktV2(DataHelper.TRAKT_CLIENT_ID);
        Call<List<SearchResult>> call = traktV2.search().idLookup(idType,
                String.valueOf(showId), Type.SHOW, Extended.FULL, null, null);

        call.enqueue(new Callback<List<SearchResult>>() {
            @Override
            public void onResponse(Call<List<SearchResult>> call,
                                   final retrofit2.Response<List<SearchResult>> response) {

                if (response.body() != null && response.code() == 200) {

                    if (response.body().size() == 0) {

                        Toast.makeText(App.getAppContext(), "No data available", Toast.LENGTH_LONG).show();

                    } else {

                        ShowValidator validator = new ShowValidator(response.body().get(0).show);
                        final Show showData = validator.showKeyCheck();

                        final int tvdbID = showData.ids.tvdb;
                        final int traktID = showData.ids.trakt;

                        UpdateItems show = new UpdateItems();
                        show.showID = tvdbID;
                        show.show = showData;

                        updatedRealmShows.add(show);

                        getSeasonData(tvdbID, traktID, listener);
                    }
                } else {
                    listener.onResults(false);
                }
            }

            @Override
            public void onFailure(Call<List<SearchResult>> call, Throwable t) {
                listener.onResults(false);
            }
        });
    }

    private void getSeasonData(final int tvdbID, final int traktID, final onUpdateListener listener) {

        TraktV2 traktV2 = new TraktV2(DataHelper.TRAKT_CLIENT_ID);
        Call<List<Season>> call = traktV2.seasons().summary(String.valueOf(traktID), Extended.EPISODES);

        call.enqueue(new Callback<List<Season>>() {
                         @Override
                         public void onResponse(Call<List<Season>> call,
                                                final retrofit2.Response<List<Season>> response) {

                             if (response.body() != null && response.code() == 200) {

                                 List<Season> seasonList = response.body();

                                 UpdateItems showData = new UpdateItems();
                                 for (int i = 0; i < updatedRealmShows.size(); i++) {
                                     if (updatedRealmShows.get(i).showID == tvdbID) {
                                         showData = updatedRealmShows.get(i);
                                         break;
                                     }
                                 }
                                 showData.seasons = new ArrayList<>();
                                 showData.seasons.addAll(seasonList);
                             }
                             index++;
                             listener.onResults(true);
                         }

                         @Override
                         public void onFailure(Call<List<Season>> call, Throwable t) {
                             listener.onResults(false);
                         }
                     }
        );
    }

    private void getShowImages(final int showID, Realm realm) {

        final TheTvdb theTvdb = new TheTvdb(DataHelper.TVDB_KEY);
        try {
            SeriesImageQueryResults posterCall = theTvdb.series()
                    .imagesQuery(showID, "poster", null, null, null).execute().body();
            SeriesImageQueryResults fanartCall = theTvdb.series()
                    .imagesQuery(showID, "fanart", null, null, null).execute().body();
            SeriesImageQueryResults bannerCall = theTvdb.series()
                    .imagesQuery(showID, "series", null, null, null).execute().body();

            RealmShow show = realm.where(RealmShow.class)
                    .equalTo("showID", showID)
                    .findFirst();

            if (posterCall != null) {
                List<SeriesImageQueryResults.SeriesImageQueryResult> imageQueryResults = posterCall.data;
                Collections.sort(imageQueryResults, new
                        Comparator<SeriesImageQueryResults.SeriesImageQueryResult>() {
                            @Override
                            public int compare(SeriesImageQueryResults.SeriesImageQueryResult t1,
                                               SeriesImageQueryResults.SeriesImageQueryResult t2) {
                                return t2.ratingsInfo.average.compareTo(t1.ratingsInfo.average);
                            }
                        });
                show.setPosterUrl(API.TVDB_LINK + "banners/" + imageQueryResults.get(0).fileName);
            }

            if (fanartCall != null) {
                List<SeriesImageQueryResults.SeriesImageQueryResult> imageQueryResults = fanartCall.data;
                Collections.sort(imageQueryResults, new
                        Comparator<SeriesImageQueryResults.SeriesImageQueryResult>() {
                            @Override
                            public int compare(SeriesImageQueryResults.SeriesImageQueryResult t1,
                                               SeriesImageQueryResults.SeriesImageQueryResult t2) {
                                return t2.ratingsInfo.average.compareTo(t1.ratingsInfo.average);
                            }
                        });
                show.setFanartUrl(API.TVDB_LINK + "banners/" + imageQueryResults.get(0).fileName);
            }

            if (bannerCall != null) {
                List<SeriesImageQueryResults.SeriesImageQueryResult> imageQueryResults = bannerCall.data;
                Collections.sort(imageQueryResults, new
                        Comparator<SeriesImageQueryResults.SeriesImageQueryResult>() {
                            @Override
                            public int compare(SeriesImageQueryResults.SeriesImageQueryResult t1,
                                               SeriesImageQueryResults.SeriesImageQueryResult t2) {
                                return t2.ratingsInfo.average.compareTo(t1.ratingsInfo.average);
                            }
                        });
                show.setBannerUrl(API.TVDB_LINK + "banners/" + imageQueryResults.get(0).fileName);
            }
        } catch (IOException e) {
            Log.d(TAG, String.valueOf(e));
        }
    }

    public interface onUpdateListener {
        void onResults(boolean result);
    }

    private class UpdateItems {
        public int showID;
        public Show show;
        public List<Season> seasons;
    }
}