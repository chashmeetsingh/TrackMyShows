package com.chashmeet.singh.trackit.utility;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import com.chashmeet.singh.trackit.misc.App;

public class VolleySingleton {
    private static VolleySingleton mInstance = null;
    private RequestQueue mRequestQueue;

    private VolleySingleton() {
        mRequestQueue = Volley.newRequestQueue(App.getAppContext());
    }

    public static VolleySingleton getInstance() {
        if(mInstance == null) {
            mInstance = new VolleySingleton();
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

}
