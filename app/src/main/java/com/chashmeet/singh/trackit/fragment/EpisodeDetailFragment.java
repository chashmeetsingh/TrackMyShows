package com.chashmeet.singh.trackit.fragment;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.text.DateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.fragment.show.Season;
import com.chashmeet.singh.trackit.helper.DataHelper;
import com.chashmeet.singh.trackit.realm.RealmEpisode;
import com.chashmeet.singh.trackit.realm.RealmSingleton;
import com.chashmeet.singh.trackit.utility.TraktClient;

public class EpisodeDetailFragment extends Fragment {

    private final Realm realm = RealmSingleton.getInstance().getRealm();
    private TextView tvTitle, tvDetails, tvVoters, tvRating, tvAirDate, tvEpisodeError;
    private ExpandableTextView tvOverView;
    private RatingBar ratingBar;
    private Button seenButton, collectionButton;
    private LinearLayout buttonLayout;
    private LinearLayout mainLinearLayout;
    private boolean watched, collection, traktWatched;
    private int episodeID, showID;
    private int traktEpisodeID; // Used to store the watched/collected episode ID
    private RealmResults<RealmEpisode> result;
    private RealmEpisode singleEpisode;
    private RealmChangeListener<RealmEpisode> singleEpisodeCallback = new RealmChangeListener<RealmEpisode>() {
        @Override
        public void onChange(RealmEpisode element) {
            if (singleEpisode.isLoaded()) {
                loadData(singleEpisode);
            }
        }
    };
    private RealmChangeListener<RealmResults<RealmEpisode>> callback = new RealmChangeListener<RealmResults<RealmEpisode>>() {
        @Override
        public void onChange(RealmResults<RealmEpisode> element) {
            if (result.isLoaded()) {
                if (result.size() == 0) {
                    mainLinearLayout.setVisibility(View.GONE);
                    tvEpisodeError.setVisibility(View.VISIBLE);
                } else {
                    loadData(result.get(0));
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_episode_detail, container, false);

        tvOverView = (ExpandableTextView) v.findViewById(R.id.tv_overview)
                .findViewById(R.id.expand_text_view);
        tvTitle = (TextView) v.findViewById(R.id.title);
        tvDetails = (TextView) v.findViewById(R.id.details);
        ratingBar = (RatingBar) v.findViewById(R.id.rating);
        tvVoters = (TextView) v.findViewById(R.id.voters);
        tvRating = (TextView) v.findViewById(R.id.tv_rating);
        tvAirDate = (TextView) v.findViewById(R.id.aired);
        seenButton = (Button) v.findViewById(R.id.button_seen);
        collectionButton = (Button) v.findViewById(R.id.button_collection);
        buttonLayout = (LinearLayout) v.findViewById(R.id.button_layout);
        mainLinearLayout = (LinearLayout) v.findViewById(R.id.episode_linear_layout);
        tvEpisodeError = (TextView) v.findViewById(R.id.tv_episode_error);

        episodeID = getArguments().getInt("episode_id", -1);
        showID = getArguments().getInt("showID", -1);

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#FF5252"), PorterDuff.Mode.SRC_ATOP); // for filled stars
        stars.getDrawable(1).setColorFilter(Color.parseColor("#616161"), PorterDuff.Mode.SRC_ATOP); // for half filled stars
        stars.getDrawable(0).setColorFilter(Color.parseColor("#616161"), PorterDuff.Mode.SRC_ATOP); // for empty stars
        return v;
    }

    @Override
    public void onPause() {
        if (result != null) {
            result.removeChangeListener(callback);
        }
        if (singleEpisode != null) {
            singleEpisode.removeChangeListener(singleEpisodeCallback);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    private void getData() {
        if (showID != -1) {

            result = realm.where(RealmEpisode.class)
                    .equalTo("showID", showID)
                    .equalTo("watched", false)
                    .notEqualTo("seasonNumber", 0)
                    .findAllSortedAsync("details", Sort.ASCENDING);
            result.addChangeListener(callback);

        } else {

            singleEpisode = realm.where(RealmEpisode.class)
                    .equalTo("episodeID", episodeID)
                    .findFirstAsync();
            singleEpisode.addChangeListener(singleEpisodeCallback);

        }
    }

    private void loadData(final RealmEpisode episode) {

        episodeID = episode.getEpisodeID();

        final String title = episode.getEpisodeTitle();
        String overview = episode.getOverView();
        Float rating = episode.getRating();
        String ratingCount = "(" + episode.getRatingCount() + " votes)";
        String details = "(" + episode.getDetails() + ")";
        watched = episode.getWatched();
        collection = episode.isCollection();

        if (episode.getAirDateTime() != 0) {
            Date episodeAirDate = new Date(episode.getAirDateTime());
            String strAirDate = DateFormat.getDateInstance(DateFormat.FULL).format(episodeAirDate);
            tvAirDate.setText(strAirDate);
            if (episodeAirDate.compareTo(new Date()) > 0) {
                buttonLayout.setVisibility(View.GONE);
            } else {
                buttonLayout.setVisibility(View.VISIBLE);
                updateSeenButton();
                updateCollectionButton();
            }
        } else {
            tvAirDate.setText(R.string.unknown);
            updateSeenButton();
            updateCollectionButton();
        }

        if (overview.equals("null")) {
            overview = "No information available at this time.";
        }

        tvOverView.setText(overview);
        tvTitle.setText(title);
        tvDetails.setText(details);
        ratingBar.setRating(rating);
        tvVoters.setText(ratingCount);
        String ratingStr = "(" + String.format(java.util.Locale.US, "%.1f", rating) + " / 10.0)";
        tvRating.setText(ratingStr);

        seenButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Season.dataLoaded = false;
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                if (watched) {
                                    watched = false;
                                    traktWatched = false;
                                    traktEpisodeID = episodeID;
                                    RealmEpisode episode = realm.where(RealmEpisode.class)
                                            .equalTo("episodeID", episodeID)
                                            .findFirst();
                                    episode.setWatched(false, true);
                                } else {
                                    watched = true;
                                    traktWatched = true;
                                    traktEpisodeID = episodeID;
                                    RealmEpisode episode = realm.where(RealmEpisode.class)
                                            .equalTo("episodeID", episodeID)
                                            .findFirst();
                                    episode.setWatched(true, true);
                                }
                            }
                        }, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                updateSeenButton();
                                if (!DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
                                    int[] episodeIDs = {traktEpisodeID};
                                    if (traktWatched) {
                                        TraktClient.syncEpisode(episodeIDs, 0, 1, true, null);
                                        getData();
                                    } else {
                                        TraktClient.syncEpisode(episodeIDs, 0, 0, true, null);
                                    }
                                }
                            }
                        });
                    }
                }
        );
        collectionButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Season.dataLoaded = false;
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                if (collection) {
                                    collection = false;
                                    traktEpisodeID = episodeID;
                                    RealmEpisode episode = realm.where(RealmEpisode.class)
                                            .equalTo("episodeID", episodeID)
                                            .findFirst();
                                    episode.setCollection(false, true);
                                } else {
                                    collection = true;
                                    traktEpisodeID = episodeID;
                                    RealmEpisode episode = realm.where(RealmEpisode.class)
                                            .equalTo("episodeID", episodeID)
                                            .findFirst();
                                    episode.setCollection(true, true);
                                }
                            }
                        }, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                updateCollectionButton();
                                int[] episodeIDs = {traktEpisodeID};
                                if (!DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
                                    if (collection) {
                                        TraktClient.syncEpisode(episodeIDs, 1, 1, true, null);
                                    } else {
                                        TraktClient.syncEpisode(episodeIDs, 1, 0, true, null);
                                    }
                                }
                            }
                        });
                    }
                }
        );
    }

    private void updateSeenButton() {
        try {
            if (watched) {
                seenButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.ColorWatchedGreen));
            } else {
                seenButton.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.darker_gray));
            }
        } catch (Exception ignored) {
            //
        }
    }

    private void updateCollectionButton() {
        try {
            if (collection) {
                collectionButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.ColorCollectionButton));
            } else {
                collectionButton.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.darker_gray));
            }
        } catch (Exception ignored) {
            //
        }
    }
}