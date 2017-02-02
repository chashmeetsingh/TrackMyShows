package com.chashmeet.singh.trackit.model;

import java.util.Date;

public class Tab2Item {

    private String title, image, details, fanArt;
    private int position, showID;
    private Date airDate;

    public Tab2Item(int showID, String title, String image, Date airDate, int position, String details, String fanArt) {
        this.showID = showID;
        this.title = title;
        this.image = image;
        this.airDate = airDate;
        this.position = position;
        this.details = details;
        this.fanArt = fanArt;
    }

    public int getShowID() {
        return showID;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public Date getAirDate() {
        return airDate;
    }

    public int getPosition() {
        return position;
    }

    public String getDetails() {
        return details;
    }

    public String getFanArt() {
        return fanArt;
    }
}
