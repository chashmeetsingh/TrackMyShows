package com.chashmeet.singh.trackit.activity;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import mbanje.kurt.fabbutton.FabButton;
import retrofit2.Call;
import retrofit2.Callback;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.api.API;
import com.chashmeet.singh.trackit.api.thetvdb.TheTvdb;
import com.chashmeet.singh.trackit.api.thetvdb.entities.SeriesImageQueryResults;
import com.chashmeet.singh.trackit.api.trakt.enums.IdType;
import com.chashmeet.singh.trackit.helper.DataHelper;
import com.chashmeet.singh.trackit.helper.DatabaseHelper;
import com.chashmeet.singh.trackit.helper.ProgressHelper;
import com.chashmeet.singh.trackit.misc.AutoResizeTextView;
import com.chashmeet.singh.trackit.realm.RealmShow;
import com.chashmeet.singh.trackit.realm.RealmSingleton;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class AddShowActivity extends AppCompatActivity {

    //private String TAG = "AddShowActivity";
    private int show_id;
    private ImageView poster;
    private ProgressBar wheel;
    private CoordinatorLayout coordinatorLayout;
    private FabButton fab;
    private RelativeLayout mSlidingUpPanel;
    private ProgressHelper helper;
    private String mShowName;
    private String mOverview;
    private String mYear;
    private String mStatus;
    private String mPoster;
    private AutoResizeTextView show_name;
    private ExpandableTextView overview;
    private TextView status;
    private TextView year;
    private boolean mTMDBShow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialise();
        displayData();
        showTutorial();
        checkShowPresent();
        handleAddShow();
    }

    @Override
    public void onBackPressed() {
        if (mSlidingUpPanel.isActivated()) {
            mSlidingUpPanel.setActivated(false);
        } else {
            super.onBackPressed();
        }
    }

    private void initialise() {
        setContentView(R.layout.activity_add_show);
        wheel = (ProgressBar) findViewById(R.id.progress_wheel);
        poster = (ImageView) findViewById(R.id.poster);
        year = (TextView) findViewById(R.id.tv_year);
        status = (TextView) findViewById(R.id.tv_status);
        overview = (ExpandableTextView) findViewById(R.id.tv_overview)
                .findViewById(R.id.expand_text_view);
        show_name = (AutoResizeTextView) findViewById(R.id.tv_show_name);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        fab = (FabButton) findViewById(R.id.fab_add_show);
        mSlidingUpPanel = (RelativeLayout) findViewById(R.id.dragView);

        ImageView backButton = (ImageView) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Bundle bundle = getIntent().getExtras();
        show_id = bundle.getInt("id");
        mShowName = bundle.getString("title", "Not Available");
        mOverview = bundle.getString("overview", "No information available at this time.");
        mYear = bundle.getString("year");
        mStatus = bundle.getString("status", "Unknown");
        mPoster = bundle.getString("poster", "");
        mTMDBShow = bundle.getBoolean("tmdb", false);
        helper = new ProgressHelper(fab, this);

        if (mOverview.equals("null")) {
            mOverview = "No information available at this time.";
        }
        if (mYear.equals("0")) {
            mYear = "Unknown";
        }
    }

    private void displayData() {
        show_name.setText(mShowName);
        overview.setText(mOverview);
        year.setText(mYear);
        if (mStatus == null) {
            mStatus = "Unknown";
        }
        status.setText(mStatus);

        if (mPoster.equals("")) {
            getPosterPath();
        } else {
            mPoster = API.TMDB_POSTER_PATH + mPoster;
            loadPoster(mPoster);
        }
    }

    private void getPosterPath() {
        TheTvdb theTvdb = new TheTvdb(DataHelper.TVDB_KEY);
        Call<SeriesImageQueryResults> call = theTvdb.series()
                .imagesQuery(show_id, "poster", null, null, null);

        call.enqueue(new Callback<SeriesImageQueryResults>() {
            @Override
            public void onResponse(Call<SeriesImageQueryResults> call,
                                   retrofit2.Response<SeriesImageQueryResults> response) {
                if (response.body() == null) {
                    poster.setImageDrawable(ContextCompat
                            .getDrawable(AddShowActivity.this, R.drawable.placeholder_poster));
                } else {
                    List<SeriesImageQueryResults.SeriesImageQueryResult> imageQueryResults = response.body().data;
                    Collections.sort(imageQueryResults, new
                            Comparator<SeriesImageQueryResults.SeriesImageQueryResult>() {
                                @Override
                                public int compare(SeriesImageQueryResults.SeriesImageQueryResult t1,
                                                   SeriesImageQueryResults.SeriesImageQueryResult t2) {
                                    return t2.ratingsInfo.average.compareTo(t1.ratingsInfo.average);
                                }
                            });

                    mPoster = API.TVDB_LINK + "banners/" + imageQueryResults.get(0).fileName;

                    loadPoster(mPoster);
                }
            }

            @Override
            public void onFailure(Call<SeriesImageQueryResults> call, Throwable t) {
                poster.setImageDrawable(ContextCompat
                        .getDrawable(AddShowActivity.this, R.drawable.placeholder_poster));
                wheel.setVisibility(View.GONE);
            }
        });
    }

    private void loadPoster(String posterPath) {
        try {
            Glide.with(AddShowActivity.this)
                    .load(posterPath)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model,
                                                   Target<GlideDrawable> target, boolean isFirstResource) {
                            poster.setImageDrawable(ContextCompat
                                    .getDrawable(AddShowActivity.this, R.drawable.placeholder_poster));
                            wheel.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model,
                                                       Target<GlideDrawable> target,
                                                       boolean isFromMemoryCache, boolean isFirstResource) {
                            wheel.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(poster);
        } catch (IllegalArgumentException e) {
            // Occurs when image is loaded after activity is destroyed
        }
    }

    private void handleAddShow() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkShowPresent()) {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Show already present", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    fab.setEnabled(false);
                    fab.setIndeterminate(true);
                    helper.startIndeterminate();
                    IdType idType;
                    if (mTMDBShow) {
                        idType = IdType.TMDB;
                    } else {
                        idType = IdType.TVDB;
                    }
                    RealmList<RealmShow> showList = new RealmList<>();
                    RealmShow realmShow = new RealmShow();
                    realmShow.setShowID(show_id);
                    showList.add(realmShow);

                    DatabaseHelper databaseHelper = new DatabaseHelper();
                    databaseHelper.updateDB(idType, showList, new DatabaseHelper.onUpdateListener() {
                        @Override
                        public void onResults(boolean result) {
                            if (result) {
                                fab.setIndeterminate(false);
                                helper.startDeterminate();
                                fab.setProgress(100);
                                fab.setEnabled(true);
                            } else {
                                fab.setEnabled(true);
                                fab.showProgress(false);
                                Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                        "Check your connection", Snackbar.LENGTH_SHORT);
                                snackbar.show();
                            }
                        }
                    });
                }
            }

        });
    }

    private boolean checkShowPresent() {
        Realm realm = RealmSingleton.getInstance().getRealm();
        RealmShow show;
        if (mTMDBShow) {
            show = realm.where(RealmShow.class)
                    .equalTo("tmdbID", show_id)
                    .findFirst();
        } else {
            show = realm.where(RealmShow.class)
                    .equalTo("showID", show_id)
                    .findFirst();
        }
        if (show != null) {
            fab.setIcon(R.drawable.ic_fab_complete, R.drawable.ic_fab_complete);
            return true;
        } else {
            return false;
        }
    }

    private void showTutorial() {
        new MaterialShowcaseView.Builder(this)
                .setTarget(mSlidingUpPanel)
                .singleUse("slideuppanel")
                .setDismissText("GOT IT")
                .setDelay(800)
                .setShapePadding(0)
                .setMaskColour(ContextCompat.getColor(this, R.color.tutorialBackground))
                .setDismissTextColor(ContextCompat.getColor(this, R.color.tutorialDismiss))
                .withRectangleShape()
                .setContentText("Swipe up or tap the bottom bar to view more information.")
                .show();
    }
}

