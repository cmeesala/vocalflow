package com.vocalflow;

import android.app.Application;

public class VocalFlowApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AutoInteractionTracker.getInstance().init(this);
    }
} 