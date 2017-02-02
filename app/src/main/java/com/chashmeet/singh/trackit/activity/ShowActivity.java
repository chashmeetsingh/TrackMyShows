package com.chashmeet.singh.trackit.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;
import java.util.List;

import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.fragment.EpisodeDetailFragment;
import com.chashmeet.singh.trackit.fragment.show.Season;
import com.chashmeet.singh.trackit.fragment.show.Show;

public class ShowActivity extends AppCompatActivity {

    private int showID;
    private CollapsingToolbarLayout collapsingToolbar;
    private ViewPager viewPager;
    private String showFanart, showTitle;
    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        extractDataFromIntent();

        imageView = (ImageView) findViewById(R.id.backdrop);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(showTitle);
        }
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
        collapsingToolbar.setTitleEnabled(false);
        setUpImageView();
        setupViewPager();
    }

    private void setUpImageView() {
        Glide.with(this)
                .load(showFanart)
                .asBitmap()
                .placeholder(R.drawable.placeholder_fanart)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        super.onResourceReady(bitmap, anim);
                        findViewById(R.id.overlay).setVisibility(View.VISIBLE);

                        Palette.from(bitmap).maximumColorCount(24).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                int vibrantColor = palette.getVibrantColor(0);
                                int vibrantDarkColor = palette.getDarkVibrantColor(0);
                                if (vibrantColor != 0 && vibrantDarkColor != 0) {
                                    collapsingToolbar.setContentScrimColor(vibrantColor);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        getWindow().setStatusBarColor(vibrantDarkColor);
                                    }
                                }
                            }
                        });
                    }
                });
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        showFanart = intent.getStringExtra("showFanart");
        showTitle = intent.getStringExtra("showTitle");
        showID = intent.getIntExtra("showID", 0);
    }

    private void setupViewPager() {
        viewPager.setOffscreenPageLimit(2);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle bundleShow = new Bundle();
        bundleShow.putInt("showID", showID);

        Show mShow = new Show();
        mShow.setArguments(bundleShow);
        adapter.addFragment(mShow, "Show");

        EpisodeDetailFragment mEpisodeTab = new EpisodeDetailFragment();
        mEpisodeTab.setArguments(bundleShow);
        adapter.addFragment(mEpisodeTab, "On Deck");

        Season mSeason = new Season();
        mSeason.setArguments(bundleShow);
        adapter.addFragment(mSeason, "Seasons");

        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

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

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
