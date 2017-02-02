package com.chashmeet.singh.trackit.realm;

import io.realm.RealmObject;

public class RealmNotification extends RealmObject {

    private String showName;
    private String details;
    private int notificationID;

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(int notificationID) {
        this.notificationID = notificationID;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }
}