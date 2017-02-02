package com.chashmeet.singh.trackit.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import io.realm.Realm;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.model.ShowImageItem;
import com.chashmeet.singh.trackit.realm.RealmShow;
import com.chashmeet.singh.trackit.realm.RealmSingleton;

public class ShowImageAdapter extends RecyclerView.Adapter<ShowImageAdapter.ViewHolder> {

    private List<ShowImageItem> itemsData;
    private int showID;
    private int placeholderID;
    private int imageType;
    private String imageURL;
    private ShowImageAdapter.ViewHolder selectedPosition = null;

    public ShowImageAdapter(List<ShowImageItem> itemsData, int showID, int imageType) {
        this.itemsData = itemsData;
        this.showID = showID;
        this.imageType = imageType;
        Realm realm = RealmSingleton.getInstance().getRealm();
        RealmShow show = realm.where(RealmShow.class)
                .equalTo("showID", showID)
                .findFirst();
        switch (imageType) {
            case 0:
                placeholderID = R.drawable.placeholder_poster;
                imageURL = show.getPosterUrl();
                break;
            case 1:
                placeholderID = R.drawable.placeholder_banner;
                imageURL = show.getBannerUrl();
                break;
            case 2:
                placeholderID = R.drawable.placeholder_fanart;
                imageURL = show.getFanartUrl();
                break;
        }
    }

    @Override
    public ShowImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_show_image, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ShowImageAdapter.ViewHolder holder, int position) {
        String thumbnailPath = itemsData.get(position).getThumbnailPath();

        if (thumbnailPath.equals("")) {
            Glide.with(holder.imageView.getContext())
                    .load(itemsData.get(position).getImagePath())
                    .placeholder(placeholderID)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(holder.imageView);
        } else {
            Glide.with(holder.imageView.getContext())
                    .load(thumbnailPath)
                    .placeholder(placeholderID)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(holder.imageView);
        }

        if (itemsData.get(position).getImagePath().equals(imageURL)) {
            holder.indicator.setVisibility(View.VISIBLE);
            selectedPosition = holder;
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String imagePath = itemsData.get(holder.getAdapterPosition()).getImagePath();
                Realm realm = RealmSingleton.getInstance().getRealm();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmShow show = realm.where(RealmShow.class)
                                .equalTo("showID", showID)
                                .findFirst();
                        switch (imageType) {
                            case 0:
                                show.setPosterUrl(imagePath);
                                break;
                            case 1:
                                show.setBannerUrl(imagePath);
                                break;
                            case 2:
                                show.setFanartUrl(imagePath);
                                break;
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        imageURL = imagePath;
                        if (selectedPosition != null) {
                            selectedPosition.indicator.setVisibility(View.GONE);
                        }
                        selectedPosition = holder;
                        holder.indicator.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemsData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public ImageView indicator;
        public View mView;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            imageView = (ImageView) itemLayoutView.findViewById(R.id.image);
            indicator = (ImageView) itemLayoutView.findViewById(R.id.image_indicator);
            mView = itemLayoutView;
        }
    }
}
