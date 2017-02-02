package com.chashmeet.singh.trackit.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

public class SearchSuggestions implements SearchSuggestion {

    public static final Parcelable.Creator<SearchSuggestions> CREATOR =
            new Parcelable.Creator<SearchSuggestions>() {

                @Override
                public SearchSuggestions createFromParcel(Parcel in) {
                    return new SearchSuggestions(in);
                }

                @Override
                public SearchSuggestions[] newArray(int size) {
                    return new SearchSuggestions[size];
                }
            };

    private String mShowName;
    //private int mID;

    public SearchSuggestions(String name/*, int id*/) {
        this.mShowName = name;
        //this.mID = id;
    }

    public SearchSuggestions(Parcel source) {
        this.mShowName = source.readString();
    }

    @Override
    public String getBody() {
        return mShowName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public String getName() {
        return mShowName;
    }

    /*public int getmID() {
        return mID;
    }*/

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;
        if (object != null && object instanceof SearchSuggestions) {
            isEqual = (this.mShowName.equals(((SearchSuggestions) object).mShowName));
        }
        return isEqual;
    }
}