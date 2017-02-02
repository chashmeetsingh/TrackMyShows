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
import com.chashmeet.singh.trackit.api.thetvdb.entities.Series;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<Series> itemsData;

    public SearchAdapter(List<Series> itemsData) {
        this.itemsData = itemsData;
    }

    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        viewHolder.title.setText(itemsData.get(position).seriesName);
        String banner = API.TVDB_LINK + "banners/" + itemsData.get(position).banner;
        Glide.with(viewHolder.showBannerImage.getContext())
                .load(banner)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(viewHolder.showBannerImage);
    }

    @Override
    public int getItemCount() {
        return itemsData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        ImageView showBannerImage;

        ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            showBannerImage = (ImageView) itemLayoutView.findViewById(R.id.search_image);
            title = (TextView) itemLayoutView.findViewById(R.id.title);
        }
    }
}