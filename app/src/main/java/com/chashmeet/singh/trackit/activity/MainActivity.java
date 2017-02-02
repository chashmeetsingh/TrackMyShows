package com.chashmeet.singh.trackit.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.github.fabtransitionactivity.SheetLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.api.trakt.enums.IdType;
import com.chashmeet.singh.trackit.fragment.main.Tab1;
import com.chashmeet.singh.trackit.fragment.main.Tab2;
import com.chashmeet.singh.trackit.fragment.main.Tab3;
import com.chashmeet.singh.trackit.helper.DataHelper;
import com.chashmeet.singh.trackit.helper.DatabaseHelper;
import com.chashmeet.singh.trackit.misc.App;
import com.chashmeet.singh.trackit.misc.Constants;
import com.chashmeet.singh.trackit.realm.RealmEpisode;
import com.chashmeet.singh.trackit.realm.RealmNotification;
import com.chashmeet.singh.trackit.realm.RealmSetNotification;
import com.chashmeet.singh.trackit.realm.RealmShow;
import com.chashmeet.singh.trackit.realm.RealmSingleton;
import com.chashmeet.singh.trackit.utility.TraktClient;
import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MainActivity extends AppCompatActivity implements SheetLayout.OnFabAnimationEndListener {

    public static final String ACTION = "main_activity_action";
    private static int previousPosition = 1;
    private ProgressBar mProgressBar;
    private RelativeLayout relativeLayout;
    private FloatingActionButton fab;
    private SheetLayout mSheetLayout;
    private TabLayout mTabLayout;
    private String mTabNames[] = {"AIRED", "SHOWS", "UPCOMING"};
    private Tab2 mTab2;
    private RealmResults<RealmShow> shows;
    private RealmChangeListener<RealmResults<RealmShow>> callback;
    private TextSwitcher mSwitcher;
    private int syncCount = 0;

    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("data_type");
            if (type.equals(DatabaseHelper.DATABASE_DATA_TYPE)) {
                displaySnackbar(intent.getStringExtra("data"));
            } else if (type.equals(TraktClient.CLIENT_DATA_TYPE)) {
                showProgressBar(intent.getBooleanExtra("show", false));
            }
        }
    };

    private void showProgressBar(boolean show) {
        try {
            if (show) {
                mProgressBar.setVisibility(View.VISIBLE);
            } else {
                mProgressBar.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            // ignored
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        mSheetLayout = (SheetLayout) findViewById(R.id.bottom_sheet);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_wheel_main);

        setSupportActionBar(mToolbar);

        setupViewPager(viewPager);
        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(viewPager);
        }
        viewPager.setCurrentItem(1);
        setupTabIcons();

        if (mSheetLayout != null) {
            mSheetLayout.setFab(fab);
            mSheetLayout.setFabAnimationEndListener(this);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSheetLayout.expandFab();
            }
        });

        mSwitcher = (TextSwitcher) findViewById(R.id.textSwitcher);
        mSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                TextView textView = new TextView(MainActivity.this);
                textView.setGravity(Gravity.START);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    textView.setTextAppearance(R.style.TitleTextApperance);
                } else {
                    textView.setTextAppearance(MainActivity.this, R.style.TitleTextApperance);
                }
                return textView;
            }
        });
        mSwitcher.setText(mTabNames[1]);

        viewPagerListener(viewPager);

        //showFeedback();
        updateRealmDB();
    }

    @Override
    public void onFabAnimationEnd() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.SEARCH_REQUEST_CODE) {
            mSheetLayout.contractFab();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DataHelper.SHOW_NOTIFICATION) {
            setNotification();
        }
        if (!DataHelper.TRAKT_SYNC) {
            syncTraktData();
        }
        IntentFilter iff = new IntentFilter(ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                View viewTrending = findViewById(R.id.action_trending);
                new MaterialShowcaseView.Builder(MainActivity.this)
                        .setTarget(viewTrending)
                        .singleUse("trending")
                        .setDismissText("NEXT")
                        .setMaskColour(ContextCompat.getColor(MainActivity.this, R.color.tutorialBackground))
                        .setDelay(100)
                        .setContentText("All the trending shows here.")
                        .setDismissTextColor(ContextCompat.getColor(MainActivity.this, R.color.tutorialDismiss))
                        .setListener(new IShowcaseListener() {
                            @Override
                            public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {
                            }

                            @Override
                            public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
                                showTutorial();
                            }
                        })
                        .show();
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_trending:
                Intent launchNewIntent = new Intent(MainActivity.this, TrendingActivity.class);
                startActivityForResult(launchNewIntent, 0);
                return true;
            case R.id.action_update:
                backgroundUpdate(this, true);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupTabIcons() {
        try {
            mTabLayout.getTabAt(0).setIcon(R.drawable.tab_1_icons);
            mTabLayout.getTabAt(1).setIcon(R.drawable.tab_2_icons);
            mTabLayout.getTabAt(2).setIcon(R.drawable.tab_3_icons);
        } catch (NullPointerException e) {
            //
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Tab1 mTab1 = new Tab1();
        adapter.addFragment(mTab1);
        mTab2 = new Tab2();
        adapter.addFragment(mTab2);
        Tab3 mTab3 = new Tab3();
        adapter.addFragment(mTab3);
        viewPager.setAdapter(adapter);
    }

    private void viewPagerListener(ViewPager viewPager) {
        /**
         * Set IN an OUT animation for the {@link Toolbar} title
         * ({@link TextSwitcher} in this case) when pager is swiped to the left
         */
        final Animation IN_SWIPE_BACKWARD = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
        final Animation OUT_SWIPE_BACKWARD = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);

        /**
         * Set IN an OUT animation for the {@link Toolbar} title
         * ({@link TextSwitcher} in this case) when pager is swiped to the right
         */
        final Animation IN_SWIPE_FORWARD = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        final Animation OUT_SWIPE_FORWARD = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position >= previousPosition) {
                    mSwitcher.setInAnimation(IN_SWIPE_FORWARD);
                    mSwitcher.setOutAnimation(OUT_SWIPE_FORWARD);
                } else {
                    mSwitcher.setInAnimation(IN_SWIPE_BACKWARD);
                    mSwitcher.setOutAnimation(OUT_SWIPE_BACKWARD);
                }
                mSwitcher.setText(mTabNames[position]);

                switch (position) {
                    case 1:
                        fab.show();
                        break;
                    default:
                        fab.hide();
                }
                previousPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void displaySnackbar(String snackbarText) {
        Snackbar snackbar = Snackbar.make(relativeLayout, snackbarText, Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        textView.setHeight(mTabLayout.getHeight());
        textView.setGravity(Gravity.CENTER_VERTICAL);
        snackbar.show();
    }

    private void showTutorial() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this);
        sequence.setConfig(config);
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(fab)
                .singleUse("fab")
                .setDismissText("NEXT")
                .setMaskColour(ContextCompat.getColor(this, R.color.tutorialBackground))
                .setDismissTextColor(ContextCompat.getColor(this, R.color.tutorialDismiss))
                .setShapePadding(0)
                .setContentText("Add new shows using this button.")
                .build());
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(mTabLayout)
                .singleUse("tabs")
                .setDismissText("GOT IT")
                .setMaskColour(ContextCompat.getColor(this, R.color.tutorialBackground))
                .setDismissTextColor(ContextCompat.getColor(this, R.color.tutorialDismiss))
                .withRectangleShape()
                .setShapePadding(0)
                .setContentText("Swipe to see Aired and Upcoming episodes.")
                .build());
        sequence.start();
    }

    public void backgroundUpdate(final Context context, final boolean forceUpdate) {
        if (DataHelper.LAST_UPDATE == 0 && !forceUpdate) {
            DataHelper.setLastUpdate(context);
        } else if ((TimeUnit.DAYS.convert(System.currentTimeMillis() - DataHelper.LAST_UPDATE,
                TimeUnit.MILLISECONDS) >= 3) || forceUpdate) {

            showProgressBar(true);

            try {
                mTab2.realmShowList.removeChangeListener(mTab2.callback);
            } catch (Exception e) {
                // ignored
            }

            Realm realm = RealmSingleton.getInstance().getRealm();
            callback = new RealmChangeListener<RealmResults<RealmShow>>() {
                @Override
                public void onChange(RealmResults<RealmShow> element) {
                    if (shows.isLoaded()) {
                        shows.removeChangeListener(callback);
                        if (shows.size() == 0 && forceUpdate) {
                            displaySnackbar("Add a show first!");
                            showProgressBar(false);
                        } else {
                            RealmList<RealmShow> showRealmList = new RealmList<>();
                            showRealmList.addAll(shows);
                            DatabaseHelper databaseHelper = new DatabaseHelper();
                            databaseHelper.updateDB(IdType.TVDB, showRealmList, new DatabaseHelper.onUpdateListener() {
                                @Override
                                public void onResults(boolean result) {
                                    if (result) {
                                        DataHelper.setLastUpdate(context);
                                        mTab2.getShows();
                                        if (forceUpdate) {
                                            displaySnackbar("All shows updated");
                                        }
                                    } else if (forceUpdate) {
                                        mTab2.realmShowList.addChangeListener(mTab2.callback);
                                        displaySnackbar("Error updating shows");
                                    }
                                    showProgressBar(false);
                                }
                            });
                        }
                    }
                }
            };
            shows = realm.where(RealmShow.class)
                    .findAllAsync();
            shows.addChangeListener(callback);
        }
    }

    private void syncTraktData() {
        if (DataHelper.TRAKT_ACCESS_TOKEN != null && !DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
            DataHelper.TRAKT_SYNC = true;
            showProgressBar(true);
            int[] episodeIDs = new int[0];
            syncCount = 0;
            TraktClient.syncEpisode(episodeIDs, 0, 1, false,
                    new TraktClient.syncListener() {
                        @Override
                        public void onResults(boolean result) {
                            syncCount++;
                            if (syncCount == 4) {
                                showProgressBar(false);
                                TraktClient.syncShowData();
                            }
                        }
                    });
            TraktClient.syncEpisode(episodeIDs, 1, 1, false,
                    new TraktClient.syncListener() {
                        @Override
                        public void onResults(boolean result) {
                            syncCount++;
                            if (syncCount == 4) {
                                showProgressBar(false);
                                TraktClient.syncShowData();
                            }
                        }
                    });
            TraktClient.syncEpisode(episodeIDs, 0, 0, false,
                    new TraktClient.syncListener() {
                        @Override
                        public void onResults(boolean result) {
                            syncCount++;
                            if (syncCount == 4) {
                                showProgressBar(false);
                                TraktClient.syncShowData();
                            }
                        }
                    });
            TraktClient.syncEpisode(episodeIDs, 1, 0, false,
                    new TraktClient.syncListener() {
                        @Override
                        public void onResults(boolean result) {
                            syncCount++;
                            if (syncCount == 4) {
                                showProgressBar(false);
                                TraktClient.syncShowData();
                            }
                        }
                    });
        }
    }

    public void setNotification() {
        Realm realm = RealmSingleton.getInstance().getRealm();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
                long today = c.getTimeInMillis();
                c.add(Calendar.DATE, 7);
                long later = c.getTimeInMillis();
                realm.delete(RealmNotification.class);

                RealmResults<RealmSetNotification> setNotifications = realm
                        .where(RealmSetNotification.class)
                        .findAll();

                for (int i = 0; i < setNotifications.size(); i++) {
                    Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    notificationIntent.addCategory("android.intent.category.DEFAULT");
                    PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this,
                            setNotifications.get(i).getNotificationID(),
                            notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.cancel(broadcast);
                }

                realm.delete(RealmSetNotification.class);
                final RealmResults<RealmEpisode> nextEpisodesUp = realm.where(RealmEpisode.class)
                        .between("airDateTime", today, later)
                        .findAllSorted("airDateTime");

                for (int episodeIndex = 0; episodeIndex < nextEpisodesUp.size(); episodeIndex++) {

                    RealmEpisode episode = nextEpisodesUp.get(episodeIndex);
                    RealmShow show = realm.where(RealmShow.class)
                            .equalTo("showID", episode.getShowID())
                            .findFirst();
                    if (!show.isEnableNotification()) {
                        continue;
                    }

                    long offset = show.getTimeOffset();
                    Calendar cal = Calendar.getInstance();

                    if ((episode.getAirDateTime() + offset) >= cal.getTimeInMillis()) {

                        int notificationID = episode.getEpisodeID();

                        RealmSetNotification setNotification = realm.createObject(RealmSetNotification.class);
                        setNotification.setNotificationID(notificationID);
                        setNotification.setShowID(episode.getShowID());

                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
                        notificationIntent.putExtra("episodeID", notificationID);
                        notificationIntent.putExtra("show_name", show.getShowTitle());
                        notificationIntent.putExtra("details", episode.getDetails());
                        notificationIntent.addCategory("android.intent.category.DEFAULT");
                        PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, notificationID,
                                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        cal.setTimeInMillis(episode.getAirDateTime() + offset);
                        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);
                    }
                }
            }
        });
    }

    private void updateRealmDB() {

        SharedPreferences sharedPref = this.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        if (sharedPref.getBoolean("update_db", true)) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Updating Database...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();

            Realm realm = RealmSingleton.getInstance().getRealm();
            RealmResults<RealmShow> shows = realm.where(RealmShow.class)
                    .findAll();

            if (shows.size() == 0) {

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("update_db", false);
                editor.apply();
                progressDialog.dismiss();

            } else {

                RealmList<RealmShow> showRealmList = new RealmList<>();
                showRealmList.addAll(shows);
                DatabaseHelper databaseHelper = new DatabaseHelper();
                databaseHelper.updateDB(IdType.TVDB, showRealmList, new DatabaseHelper.onUpdateListener() {
                    @Override
                    public void onResults(boolean result) {
                        progressDialog.dismiss();
                        if (result) {
                            DataHelper.setLastUpdate(MainActivity.this);
                            mTab2.getShows();
                            SharedPreferences sharedPref = App.getAppContext()
                                    .getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("update_db", false);
                            editor.apply();
                        } else {
                            Toast.makeText(MainActivity.this, "Database update failed!",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
            }
        } else {
            backgroundUpdate(this, false);
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }
}