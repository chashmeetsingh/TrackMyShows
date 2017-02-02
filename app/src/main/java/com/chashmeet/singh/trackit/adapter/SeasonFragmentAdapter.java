package com.chashmeet.singh.trackit.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import ch.halcyon.squareprogressbar.SquareProgressBar;
import io.realm.Realm;
import io.realm.RealmResults;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.activity.EpisodeDetail;
import com.chashmeet.singh.trackit.helper.DataHelper;
import com.chashmeet.singh.trackit.misc.App;
import com.chashmeet.singh.trackit.model.SeasonItem;
import com.chashmeet.singh.trackit.realm.RealmEpisode;
import com.chashmeet.singh.trackit.realm.RealmSingleton;
import com.chashmeet.singh.trackit.utility.TraktClient;

public class SeasonFragmentAdapter extends RecyclerView.Adapter<SeasonFragmentAdapter.ViewHolder> {

    public static int showID;
    private static int totalSeasons;
    private List<SeasonItem> itemsData;
    private String showTitle;
    private Calendar cal = Calendar.getInstance();

    public SeasonFragmentAdapter(List<SeasonItem> itemsData, int showID, String showTitle) {
        this.itemsData = itemsData;
        SeasonFragmentAdapter.showID = showID;
        this.showTitle = showTitle;
    }

    @Override
    public SeasonFragmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fragment_season, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final SeasonFragmentAdapter.ViewHolder holder, int position) {

        if (position == 0) {
            totalSeasons = itemsData.get(position).getSeasonNumber();
        }
        holder.seasonNumber.setText(itemsData.get(position).getSeasonTitle());

        Realm realm = RealmSingleton.getInstance().getRealm();
        RealmResults<RealmEpisode> episodes = realm.where(RealmEpisode.class)
                .equalTo("showID", showID)
                .equalTo("seasonNumber", itemsData.get(position).getSeasonNumber())
                .findAll();

        int totalEpisodes = episodes.size();
        int watchedEpisodes = episodes.where()
                .equalTo("watched", true)
                .findAll()
                .size();
        int collectedEpisodes = episodes.where()
                .equalTo("collection", true)
                .findAll()
                .size();
        int aired = episodes.where()
                .lessThanOrEqualTo("airDateTime", cal.getTimeInMillis())
                .findAll()
                .size();

        String watched;
        if (totalEpisodes == aired) {
            watched = watchedEpisodes + "/" + aired;
        } else {
            watched = watchedEpisodes + "/" + aired + " (+" + (totalEpisodes - aired) + " unaired)";
        }
        holder.watchedTextview.setText(watched);
        String collection = "(Collected: " + collectedEpisodes + ")";
        holder.collectionTextview.setText(collection);

        double percent = ((double) watchedEpisodes / aired) * 100;
        holder.squareProgressBar.setProgress(percent);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(App.getInstance(), EpisodeDetail.class);
                myIntent.putExtra("episode_id", itemsData.get(holder.getAdapterPosition()).getEpisode());
                myIntent.putExtra("show_title", showTitle);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                App.getInstance().startActivity(myIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemsData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        public final ImageView menu;
        final TextView seasonNumber, watchedTextview, collectionTextview;
        final View mView;
        final SquareProgressBar squareProgressBar;
        private int count, totalCount;
        private int[] episodeIDs;

        ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            mView = itemLayoutView;
            squareProgressBar = (SquareProgressBar) itemLayoutView.findViewById(R.id.sprogressbar);
            squareProgressBar.setWidth(2);
            squareProgressBar.setColor("#4CAF50");

            seasonNumber = (TextView) squareProgressBar.findViewById(R.id.season_number);
            watchedTextview = (TextView) squareProgressBar.findViewById(R.id.watched_count);
            collectionTextview = (TextView) squareProgressBar.findViewById(R.id.collection_count);
            menu = (ImageView) squareProgressBar.findViewById(R.id.menu);
            menu.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view == menu) {
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                popup.inflate(R.menu.season_menu);
                popup.setOnMenuItemClickListener(this);
                popup.show();
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Realm realm = RealmSingleton.getInstance().getRealm();
            final Calendar cal = Calendar.getInstance();
            switch (item.getItemId()) {
                case R.id.watched_all:
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<RealmEpisode> allEpisodes = realm.where(RealmEpisode.class)
                                    .equalTo("showID", SeasonFragmentAdapter.showID)
                                    .equalTo("seasonNumber", SeasonFragmentAdapter.totalSeasons - getAdapterPosition())
                                    .findAll();
                            totalCount = allEpisodes.size();
                            RealmResults<RealmEpisode> airedEpisodes = allEpisodes.where()
                                    .lessThanOrEqualTo("airDateTime", cal.getTimeInMillis())
                                    .findAll();
                            count = airedEpisodes.size();
                            episodeIDs = new int[count];
                            for (int i = 0; i < count; i++) {
                                RealmEpisode episode = airedEpisodes.get(i);
                                if (!episode.getWatched()) {
                                    episode.setWatched(true, true);
                                    episodeIDs[i] = episode.getEpisodeID();
                                }
                            }
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            String textChange;
                            if (totalCount == count) {
                                textChange = count + "/" + count;
                            } else {
                                textChange = count + "/" + count + " (+" + (totalCount - count) + " unaired)";
                            }
                            watchedTextview.setText(textChange);
                            if (count != 0) {
                                squareProgressBar.setProgress(100);
                                if (!DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
                                    TraktClient.syncEpisode(episodeIDs, 0, 1, true, null);
                                }
                            }
                        }
                    });
                    break;
                case R.id.watched_none:
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<RealmEpisode> allEpisodes = realm.where(RealmEpisode.class)
                                    .equalTo("showID", SeasonFragmentAdapter.showID)
                                    .equalTo("seasonNumber", SeasonFragmentAdapter.totalSeasons - getAdapterPosition())
                                    .findAll();
                            totalCount = allEpisodes.size();
                            RealmResults<RealmEpisode> airedEpisodes = allEpisodes.where()
                                    .lessThanOrEqualTo("airDateTime", cal.getTimeInMillis())
                                    .findAll();
                            count = airedEpisodes.size();
                            episodeIDs = new int[count];
                            for (int i = 0; i < count; i++) {
                                RealmEpisode episode = airedEpisodes.get(i);
                                if (episode.getWatched()) {
                                    episode.setWatched(false, true);
                                    episodeIDs[i] = episode.getEpisodeID();
                                }
                            }
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            String textChange;
                            if (totalCount == count) {
                                textChange = "0/" + count;
                            } else {
                                textChange = "0/" + count + " (+" + (totalCount - count) + " unaired)";
                            }
                            watchedTextview.setText(textChange);
                            squareProgressBar.setProgress(0);
                            if (!DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
                                TraktClient.syncEpisode(episodeIDs, 0, 0, true, null);
                            }
                        }
                    });
                    break;
                case R.id.collected_all:
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<RealmEpisode> episodes = realm.where(RealmEpisode.class)
                                    .equalTo("showID", SeasonFragmentAdapter.showID)
                                    .equalTo("seasonNumber", SeasonFragmentAdapter.totalSeasons - getAdapterPosition())
                                    .lessThanOrEqualTo("airDateTime", cal.getTimeInMillis())
                                    .findAll();
                            count = episodes.size();
                            episodeIDs = new int[count];
                            for (int i = 0; i < count; i++) {
                                RealmEpisode episode = episodes.get(i);
                                if (!episode.isCollection()) {
                                    episode.setCollection(true, true);
                                    episodeIDs[i] = episode.getEpisodeID();
                                }
                            }
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            String textChange = "(Collected: " + count + ")";
                            collectionTextview.setText(textChange);
                            if (!DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
                                TraktClient.syncEpisode(episodeIDs, 1, 1, true, null);
                            }
                        }
                    });
                    break;
                case R.id.collected_none:
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<RealmEpisode> episodes = realm.where(RealmEpisode.class)
                                    .equalTo("showID", SeasonFragmentAdapter.showID)
                                    .equalTo("seasonNumber", SeasonFragmentAdapter.totalSeasons - getAdapterPosition())
                                    .lessThanOrEqualTo("airDateTime", cal.getTimeInMillis())
                                    .findAll();
                            count = episodes.size();
                            episodeIDs = new int[count];
                            for (int i = 0; i < count; i++) {
                                RealmEpisode episode = episodes.get(i);
                                if (episode.isCollection()) {
                                    episode.setCollection(false, true);
                                    episodeIDs[i] = episode.getEpisodeID();
                                }
                            }
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            String textChange1 = "(Collected: 0)";
                            collectionTextview.setText(textChange1);
                            if (!DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
                                TraktClient.syncEpisode(episodeIDs, 1, 0, true, null);
                            }
                        }
                    });
                    break;
            }
            return true;
        }
    }
}