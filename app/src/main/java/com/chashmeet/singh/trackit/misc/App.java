package com.chashmeet.singh.trackit.misc;

import android.app.Application;
import android.content.Context;

import java.security.GeneralSecurityException;

import com.chashmeet.singh.trackit.helper.DataHelper;

public class App extends Application {

    public static boolean ENABLE_LOGGING = false;
    private static App mInstance;

    public static App getInstance() {
        return mInstance;
    }

    public static Context getAppContext() {
        return mInstance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        try {
            new DataHelper(this);
        } catch (GeneralSecurityException e) {
            //App might be tampered!
        }
    }
}