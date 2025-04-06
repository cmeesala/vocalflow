package com.vocalflow;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class CurrentActivityHolder {
    private static CurrentActivityHolder instance;
    private Activity currentActivity;

    private CurrentActivityHolder(Application app) {
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(Activity activity) {}

            @Override
            public void onActivityResumed(Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (currentActivity == activity) {
                    currentActivity = null;
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override
            public void onActivityDestroyed(Activity activity) {}
        });
    }

    public static void init(Application app) {
        if (instance == null) {
            instance = new CurrentActivityHolder(app);
        }
    }

    public static CurrentActivityHolder getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CurrentActivityHolder not initialized. Call init() first.");
        }
        return instance;
    }

    public Activity getActivity() {
        return currentActivity;
    }
} 