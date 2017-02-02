package com.chashmeet.singh.trackit.model;

public class TrendingItem {

    private String title, image, watchers;

    public TrendingItem(String title, String image, String watchers) {
        this.title = title;
        this.image = image;
        this.watchers = watchers;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getWatchers() {
        return watchers;
    }
}
