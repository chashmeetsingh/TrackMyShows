package com.chashmeet.singh.trackit.fragment.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.fabtransitionactivity.SheetLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.activity.MainActivity;
import com.chashmeet.singh.trackit.activity.SearchActivity;
import com.chashmeet.singh.trackit.activity.ShowActivity;
import com.chashmeet.singh.trackit.adapter.Tab2Adapter;
import com.chashmeet.singh.trackit.helper.DataHelper;
import com.chashmeet.singh.trackit.listener.RecyclerItemClickListener;
import com.chashmeet.singh.trackit.misc.App;
import com.chashmeet.singh.trackit.misc.SpaceItemDecorator;
import com.chashmeet.singh.trackit.model.Tab2Item;
import com.chashmeet.singh.trackit.realm.RealmEpisode;
import com.chashmeet.singh.trackit.realm.RealmShow;
import com.chashmeet.singh.trackit.realm.RealmSingleton;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

import static com.chashmeet.singh.trackit.helper.DatabaseHelper.DATABASE_DATA_TYPE;

public class Tab2 extends Fragment implements SheetLayout.OnFabAnimationEndListener, RecyclerItemClickListener.OnItemClickListener {

    //private String LOG_TAG = "Tab2";
    private static final int REQUEST_CODE = 1;
    public RealmResults<RealmShow> realmShowList;
    private SheetLayout mSheetLayout;
    private TextView tvTab2;
    private List<Tab2Item> showList = new ArrayList<>();
    private Tab2Adapter mAdapter;
    public RealmChangeListener<RealmResults<RealmShow>> callback = new RealmChangeListener<RealmResults<RealmShow>>() {
        @Override
        public void onChange(RealmResults<RealmShow> element) {
            if (realmShowList.isLoaded()) {
                if (realmShowList.size() != showList.size()) {
                    try {
                        Tab1 tab1 = new Tab1();
                        tab1.getRealmData();
                        Tab3 tab3 = new Tab3();
                        tab3.getRealmData();
                    } catch (Exception ignored) {
                        //
                    }
                }
                loadData(DataHelper.SORT_ORDER);
            }
        }
    };
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_2, container, false);
        setHasOptionsMenu(true);

        mSheetLayout = (SheetLayout) v.findViewById(R.id.bottom_sheet);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerViewMain);
        tvTab2 = (TextView) v.findViewById(R.id.tvTab2);
        tvTab2.setVisibility(View.GONE);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity().getApplicationContext(), this));
        recyclerView.addItemDecoration(new SpaceItemDecorator(getActivity(), R.dimen.list_space_tab_2, true, true));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new Tab2Adapter(showList);
        recyclerView.setAdapter(mAdapter);

        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fab.isShown())
                    fab.hide();
                else if (dy < 0 && !fab.isShown())
                    fab.show();
            }
        });
        return v;
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
        inflater.inflate(R.menu.menu_tab_2_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sortOrder;
        switch (item.getItemId()) {
            case R.id.action_by_name:
                if (DataHelper.SORT_ORDER == 0) {
                    sortOrder = 2;
                } else {
                    sortOrder = 0;
                }
                DataHelper.setSortOrder(sortOrder, getActivity());
                loadData(sortOrder);
                item.setChecked(true);
                break;
            case R.id.action_by_date:
                if (DataHelper.SORT_ORDER == 1) {
                    sortOrder = 3;
                } else {
                    sortOrder = 1;
                }
                DataHelper.setSortOrder(sortOrder, getActivity());
                loadData(sortOrder);
                item.setChecked(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFabAnimationEnd() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            mSheetLayout.contractFab();
        }
    }

    @Override
    public void onItemClick(View childView, int position) {
        Intent intent = new Intent(getActivity(), ShowActivity.class);
        intent.putExtra("showFanart", showList.get(position).getFanArt());
        intent.putExtra("showTitle", showList.get(position).getTitle());
        intent.putExtra("showID", showList.get(position).getShowID());
        getActivity().startActivity(intent);
    }

    @Override
    public void onItemLongPress(View childView, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirm Delete");
        builder.setMessage("Remove '" + showList.get(position).getTitle() + "' from your list?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int which) {

                dialog.dismiss();
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Deleting Show...");
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.show();
                Realm realm = RealmSingleton.getInstance().getRealm();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmShow show = realm.where(RealmShow.class)
                                .equalTo("showID", showList.get(position).getShowID())
                                .findFirst();
                        RealmResults<RealmEpisode> episodes = realm.where(RealmEpisode.class)
                                .equalTo("showID", showList.get(position).getShowID())
                                .findAll();
                        show.deleteFromRealm();
                        for (RealmEpisode episode : episodes) {
                            episode.deleteFromRealm();
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();
                        Intent in = new Intent(MainActivity.ACTION);
                        in.putExtra("data_type", DATABASE_DATA_TYPE);
                        in.putExtra("data", "Show deleted");
                        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                    }
                });
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        getShows();
    }

    @Override
    public void onPause() {
        if (realmShowList != null) {
            realmShowList.removeChangeListener(callback);
        }
        super.onPause();
    }

    public void getShows() {
        Realm realm = RealmSingleton.getInstance().getRealm();
        realmShowList = realm.where(RealmShow.class)
                .findAllAsync();
        realmShowList.addChangeListener(callback);
    }

    public void loadData(int sortOrder) {
        showList.clear();
        if (realmShowList != null && realmShowList.size() > 0) {

            if (tvTab2 != null) {
                tvTab2.setVisibility(View.GONE);
            }

            ArrayList<Tab2Item> nullShowList = new ArrayList<>();

            for (int showIndex = 0; showIndex < realmShowList.size(); showIndex++) {
                RealmShow currentShow = realmShowList.get(showIndex);

                RealmResults<RealmEpisode> unseenEpisodes = currentShow.getEpisodes()
                        .where()
                        .notEqualTo("seasonNumber", 0)
                        .equalTo("watched", false)
                        .findAllSorted("details", Sort.ASCENDING);

                if (unseenEpisodes.size() == 0) {
                    if (sortOrder == 0) {
                        showList.add(new Tab2Item(currentShow.getShowID(),
                                currentShow.getShowTitle(), currentShow.getBannerUrl(),
                                null, showIndex, currentShow.getStatus(), currentShow.getFanartUrl()));
                    } else {
                        nullShowList.add(new Tab2Item(currentShow.getShowID(),
                                currentShow.getShowTitle(), currentShow.getBannerUrl(),
                                null, showIndex, currentShow.getStatus(), currentShow.getFanartUrl()));
                    }
                } else {
                    RealmEpisode lastUnseenEpisode = unseenEpisodes.get(0);
                    showList.add(new Tab2Item(currentShow.getShowID(),
                            currentShow.getShowTitle(), currentShow.getBannerUrl(),
                            new Date(lastUnseenEpisode.getAirDateTime()), showIndex,
                            lastUnseenEpisode.getDetails(), currentShow.getFanartUrl()));
                }
            }
            if (sortOrder == 0) {
                showList.addAll(nullShowList);
                Collections.sort(showList, new Comparator<Tab2Item>() {
                    @Override
                    public int compare(Tab2Item t1, Tab2Item t2) {
                        return t1.getTitle().compareTo(t2.getTitle());
                    }
                });
            } else if (sortOrder == 1) {
                Collections.sort(showList, new Comparator<Tab2Item>() {
                    @Override
                    public int compare(Tab2Item t1, Tab2Item t2) {
                        return t2.getAirDate().compareTo(t1.getAirDate());
                    }
                });
                showList.addAll(nullShowList);
            } else if (sortOrder == 2) {
                showList.addAll(nullShowList);
                Collections.sort(showList, new Comparator<Tab2Item>() {
                    @Override
                    public int compare(Tab2Item t1, Tab2Item t2) {
                        return t2.getTitle().compareTo(t1.getTitle());
                    }
                });
            } else if (sortOrder == 3) {
                Collections.sort(showList, new Comparator<Tab2Item>() {
                    @Override
                    public int compare(Tab2Item t1, Tab2Item t2) {
                        return t1.getAirDate().compareTo(t2.getAirDate());
                    }
                });
                showList.addAll(nullShowList);
            }
        } else {
            if (tvTab2 != null) {
                tvTab2.setVisibility(View.VISIBLE);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void showTutorial() {
        if (recyclerView != null && recyclerView.getChildCount() > 0) {
            new MaterialShowcaseView.Builder(getActivity())
                    .setTarget(recyclerView.getChildAt(0))
                    .singleUse("tab2delete")
                    .withRectangleShape()
                    .setContentText("Long press on a show to delete it.")
                    .setDismissText("GOT IT")
                    .setShapePadding(0)
                    .setMaskColour(ContextCompat.getColor(getActivity(), R.color.tutorialBackground))
                    .setDismissTextColor(ContextCompat.getColor(getActivity(), R.color.tutorialDismiss))
                    .show();
        }
    }
}