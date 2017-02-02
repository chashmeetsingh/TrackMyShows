package com.chashmeet.singh.trackit.realm;

import io.realm.RealmObject;

public class RealmSetNotification extends RealmObject {

    private int notificationID;
    private int showID;

    public int getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(int notificationID) {
        this.notificationID = notificationID;
    }

    public int getShowID() {
        return showID;
    }

    public void setShowID(int showID) {
        this.showID = showID;
    }
}