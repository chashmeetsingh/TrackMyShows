package com.chashmeet.singh.trackit.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.model.Tab2Item;

public class Tab2Adapter extends RecyclerView.Adapter<Tab2Adapter.ViewHolder> {

    private List<Tab2Item> itemsData;

    public Tab2Adapter(List<Tab2Item> itemsData) {
        this.itemsData = itemsData;
    }

    @Override
    public Tab2Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tab_2, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        Date airDate = itemsData.get(position).getAirDate();
        if (airDate == null) {
            viewHolder.details.setText("");
            viewHolder.airDate.setText(itemsData.get(position).getDetails());
        } else if (airDate.getTime() == 0) {
            viewHolder.details.setText(itemsData.get(position).getDetails());
            viewHolder.airDate.setText(R.string.unknown);
        } else {
            String airDateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(airDate);
            viewHolder.details.setText(itemsData.get(position).getDetails());
            viewHolder.airDate.setText(airDateString);
        }
        viewHolder.title.setText(itemsData.get(position).getTitle());
        Glide.with(viewHolder.imageView.getContext())
                .load(itemsData.get(position).getImage())
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(viewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return itemsData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView airDate, details, title;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            title = (TextView) itemLayoutView.findViewById(R.id.title);
            imageView = (ImageView) itemLayoutView.findViewById(R.id.tab2_image);
            airDate = (TextView) itemLayoutView.findViewById(R.id.airDate);
            details = (TextView) itemLayoutView.findViewById(R.id.details);
        }
    }
}
