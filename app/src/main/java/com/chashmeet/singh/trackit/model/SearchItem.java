package com.chashmeet.singh.trackit.model;

public class SearchItem {

    private String banner;
    private String showName;

    public SearchItem(String banner, String showName) {
        this.banner = banner;
        this.showName = showName;
    }

    public String getImageUrl() {
        return banner;
    }

    public String getShowName() {
        return showName;
    }
}