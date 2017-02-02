package com.chashmeet.singh.trackit.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
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
import com.chashmeet.singh.trackit.activity.EpisodeDetail;
import com.chashmeet.singh.trackit.misc.App;
import com.chashmeet.singh.trackit.model.Tab1Item;

public class Tab3Adapter extends RecyclerView.Adapter<Tab3Adapter.ViewHolder> {

    private List<Tab1Item> itemsData;

    public Tab3Adapter(List<Tab1Item> itemsData) {
        this.itemsData = itemsData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tab_3, parent, false);
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
    }

    @Override
    public int getItemCount() {
        return itemsData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, airDate, showTitle;
        public ImageView imageView;
        public CardView cardView;

        public ViewHolder(View itemLayoutView) {

            super(itemLayoutView);
            title = (TextView) itemLayoutView.findViewById(R.id.tab1title);
            imageView = (ImageView) itemLayoutView.findViewById(R.id.tab1_image);
            airDate = (TextView) itemLayoutView.findViewById(R.id.tab1airDate);
            showTitle = (TextView) itemLayoutView.findViewById(R.id.show_title);
            cardView = (CardView) itemLayoutView.findViewById(R.id.cv);
        }
    }
}
