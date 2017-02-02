package com.chashmeet.singh.trackit.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.api.API;
import com.chashmeet.singh.trackit.api.tmdb.entities.TvShow;

public class TrendingAdapter extends RecyclerView.Adapter<TrendingAdapter.ViewHolder> {

    private List<TvShow> itemsData;

    public TrendingAdapter(List<TvShow> itemsData) {
        this.itemsData = itemsData;
    }

    @Override
    public TrendingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trending, parent, false);

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.txtViewTitle.setText(itemsData.get(position).name);

        String imageURL = API.TMDB_BACKDROP_PATH + itemsData.get(position).backdrop_path;
        Glide.with(viewHolder.imgViewIcon.getContext())
                .load(imageURL)
                .placeholder(R.drawable.placeholder_fanart)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(viewHolder.imgViewIcon);

        String rating = " \u272A " + itemsData.get(position).vote_average + " ";
        viewHolder.txtViewWatcher.setText(rating);
    }

    @Override
    public int getItemCount() {
        return itemsData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtViewWatcher;
        TextView txtViewTitle;
        ImageView imgViewIcon;

        ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txtViewTitle = (TextView) itemLayoutView.findViewById(R.id.item_title);
            txtViewWatcher = (TextView) itemLayoutView.findViewById(R.id.item_rating);
            imgViewIcon = (ImageView) itemLayoutView.findViewById(R.id.item_icon);
        }
    }
}