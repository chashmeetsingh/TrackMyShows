package com.chashmeet.singh.trackit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.api.API;
import com.chashmeet.singh.trackit.fragment.EpisodeDetailFragment;
import com.chashmeet.singh.trackit.realm.RealmEpisode;
import com.chashmeet.singh.trackit.realm.RealmNotification;
import com.chashmeet.singh.trackit.realm.RealmSingleton;

public class EpisodeDetail extends AppCompatActivity {

    private ImageView episodeImage;
    private RealmResults<RealmEpisode> episodeList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_detail);

        Intent intent = getIntent();
        String showTitle = intent.getStringExtra("show_title");
        int episodeID = intent.getIntExtra("episode_id", 0);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(showTitle);
        }

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);

        episodeImage = (ImageView) findViewById(R.id.image);
        ImageView seasonImage = (ImageView) findViewById(R.id.season_background);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        Realm realm = RealmSingleton.getInstance().getRealm();

        boolean realmNotification = intent.getBooleanExtra("realm_notification", false);
        if (realmNotification) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.delete(RealmNotification.class);
                }
            });
        }

        RealmEpisode episode = realm.where(RealmEpisode.class)
                .equalTo("episodeID", episodeID)
                .findFirst();
        episodeList = realm.where(RealmEpisode.class)
                .equalTo("showID", episode.getShowID())
                .equalTo("seasonNumber", episode.getSeasonNumber())
                .findAllSorted("episodeNumber", Sort.ASCENDING);
        setupViewPager(viewPager);

        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.addOnTabSelectedListener(
                    new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {
                            int numTab = tab.getPosition();
                            Glide.with(EpisodeDetail.this)
                                    .load(episodeList.get(numTab).getBannerUrl())
                                    .placeholder(R.drawable.placeholder_fanart)
                                    .crossFade(1000)
                                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                    .into(episodeImage);
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {

                        }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {

                        }
                    }
            );
        }
        viewPager.setCurrentItem(episode.getEpisodeNumber() - 1);
        Glide.with(EpisodeDetail.this)
                .load(episode.getBannerUrl())
                .placeholder(R.drawable.placeholder_fanart)
                .crossFade(1000)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(episodeImage);

        String seasonImageURL = API.TVDB_LINK + "banners/seasons/" + episode.getShowID()
                + "-" + episode.getSeasonNumber() + ".jpg";
        Glide.with(EpisodeDetail.this)
                .load(seasonImageURL)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache, boolean isFirstResource) {
                        findViewById(R.id.season_overlay).setVisibility(View.VISIBLE);
                        return false;
                    }
                }).into(seasonImage);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        for (int i = 0; i < episodeList.size(); i++) {
            RealmEpisode temp = episodeList.get(i);
            Bundle bundleShow = new Bundle();
            bundleShow.putInt("episode_id", temp.getEpisodeID());

            EpisodeDetailFragment mEpisode = new EpisodeDetailFragment();
            mEpisode.setArguments(bundleShow);
            adapter.addFragment(mEpisode, temp.getDetails());
        }
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
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

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}