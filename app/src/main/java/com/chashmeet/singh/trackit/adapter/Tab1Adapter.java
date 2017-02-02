package com.chashmeet.singh.trackit.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import io.realm.Realm;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.activity.EpisodeDetail;
import com.chashmeet.singh.trackit.helper.DataHelper;
import com.chashmeet.singh.trackit.misc.App;
import com.chashmeet.singh.trackit.model.Tab1Item;
import com.chashmeet.singh.trackit.realm.RealmEpisode;
import com.chashmeet.singh.trackit.realm.RealmSingleton;
import com.chashmeet.singh.trackit.utility.TraktClient;

public class Tab1Adapter extends RecyclerView.Adapter<Tab1Adapter.ViewHolder> {

    private List<Tab1Item> itemsData;
    private boolean watched;

    public Tab1Adapter(List<Tab1Item> itemsData) {
        this.itemsData = itemsData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tab_1, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        String title_string;
        title_string = itemsData.get(position).getEpisodeNumber() + " - " + itemsData.get(position).getTitle();
        viewHolder.title.setText(title_string);
        Context context = viewHolder.imageView.getContext();

        viewHolder.airDate.setText(itemsData.get(position).getDuration());

        Glide.with(context)
                .load(itemsData.get(position).getImage())
                .placeholder(R.drawable.placeholder_poster)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(viewHolder.imageView);

        viewHolder.showTitle.setText(itemsData.get(position).getShowTitle());

        setWatchedImage(viewHolder.watchedImage, itemsData.get(position).isWatched());

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = viewHolder.getAdapterPosition();
                Intent intent = new Intent(App.getInstance(), EpisodeDetail.class);
                intent.putExtra("show_title", String.valueOf(itemsData.get(position).getShowTitle()));
                intent.putExtra("episode_id", itemsData.get(position).getEpisodeID());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                App.getInstance().startActivity(intent);
            }
        });

        viewHolder.watchToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int episodeID = itemsData.get(viewHolder.getAdapterPosition()).getEpisodeID();
                Realm realm = RealmSingleton.getInstance().getRealm();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmEpisode episode = realm.where(RealmEpisode.class)
                                .equalTo("episodeID", episodeID)
                                .findFirst();
                        if (episode.getWatched()) {
                            watched = false;
                            episode.setWatched(false, true);
                        } else {
                            watched = true;
                            episode.setWatched(true, true);
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        setWatchedImage(viewHolder.watchedImage, watched);
                        if (!DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
                            int[] episodeIDs = {episodeID};
                            if (watched) {
                                TraktClient.syncEpisode(episodeIDs, 0, 1, true, null);
                            } else {
                                TraktClient.syncEpisode(episodeIDs, 0, 0, true, null);
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemsData.size();
    }

    private void setWatchedImage(ImageView watchedImage, boolean watched) {
        if (watched) {
            PorterDuffColorFilter porterDuffColorFilter =
                    new PorterDuffColorFilter(ContextCompat.getColor(App.getAppContext(),
                            R.color.ColorWatchedGreen), PorterDuff.Mode.SRC_ATOP);
            watchedImage.setColorFilter(porterDuffColorFilter);
        } else {
            PorterDuffColorFilter porterDuffColorFilter =
                    new PorterDuffColorFilter(ContextCompat.getColor(App.getAppContext(),
                            R.color.ColorWatchedRed), PorterDuff.Mode.SRC_ATOP);
            watchedImage.setColorFilter(porterDuffColorFilter);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, airDate, showTitle;
        public ImageView imageView, watchedImage;
        public RelativeLayout watchToggle;
        public CardView cardView;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            title = (TextView) itemLayoutView.findViewById(R.id.tab1title);
            imageView = (ImageView) itemLayoutView.findViewById(R.id.tab1_image);
            airDate = (TextView) itemLayoutView.findViewById(R.id.tab1airDate);
            showTitle = (TextView) itemLayoutView.findViewById(R.id.show_title);
            watchedImage = (ImageView) itemLayoutView.findViewById(R.id.watched_indicator);
            watchToggle = (RelativeLayout) itemLayoutView.findViewById(R.id.watch_toggle);
            cardView = (CardView) itemLayoutView.findViewById(R.id.cv);
        }
    }
}
