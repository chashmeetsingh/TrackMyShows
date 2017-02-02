package com.chashmeet.singh.trackit.helper;

import android.app.Activity;
import android.os.Handler;

import mbanje.kurt.fabbutton.FabButton;

public class ProgressHelper {

    private final FabButton button;
    private final Activity activity;
    private int currentProgress = 0;
    private Handler handle = new Handler();

    public ProgressHelper(FabButton button, Activity activity) {
        this.button = button;
        this.activity = activity;
    }

    private Runnable getRunnable(final Activity activity) {
        return new Runnable() {
            @Override
            public void run() {
                currentProgress += 1;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button.setProgress(currentProgress);
                        if (currentProgress <= 100) {
                            handle.postDelayed(getRunnable(activity), 50);
                        }
                    }
                });
            }
        };
    }

    public void startIndeterminate() {
        button.showProgress(true);
    }

    public void startDeterminate() {
        button.resetIcon();
        currentProgress = 0;
        button.showProgress(true);
        button.setProgress(currentProgress);
        getRunnable(activity).run();
    }


}