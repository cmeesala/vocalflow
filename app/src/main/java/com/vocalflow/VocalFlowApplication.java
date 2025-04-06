package com.vocalflow;

import android.app.Application;

public class VocalFlowApplication extends Application {
    private static VocalFlowApplication instance;
    private AutoInteractionTracker interactionTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Initialize CurrentActivityHolder
        CurrentActivityHolder.init(this);
        
        // Initialize interaction tracking
        interactionTracker = AutoInteractionTracker.getInstance();
        interactionTracker.init(this);
        
        // Initialize replay manager singleton
        InteractionReplayManager.getInstance(this);
    }

    public AutoInteractionTracker getInteractionTracker() {
        return interactionTracker;
    }

    public static InteractionReplayManager getReplayManager() {
        return InteractionReplayManager.getInstance(instance);
    }
} 