package com.vocalflow;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.util.Log;
import android.view.ViewGroup;

import java.util.List;

public class InteractionReplayManager {
    private static final String TAG = "InteractionReplayManager";
    private static InteractionReplayManager instance;
    private final Application app;
    private boolean isReplaying = false;
    private Thread replayThread;

    private InteractionReplayManager(Application app) {
        this.app = app;
    }

    public static synchronized InteractionReplayManager getInstance(Application app) {
        if (instance == null) {
            instance = new InteractionReplayManager(app);
        }
        return instance;
    }

    public void replay(List<InteractionEvent> events) {
        if (isReplaying) {
            Log.w(TAG, "Replay already in progress");
            return;
        }

        isReplaying = true;
        replayThread = new Thread(() -> {
            try {
                for (InteractionEvent event : events) {
                    if (!isReplaying) break; // Check if replay was stopped
                    replayEvent(event);
                    Thread.sleep(300); // Simulate delay between interactions
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Replay interrupted: " + e.getMessage());
            } finally {
                isReplaying = false;
            }
        });
        replayThread.start();
    }

    public void stop() {
        isReplaying = false;
        if (replayThread != null) {
            replayThread.interrupt();
        }
    }

    private void replayEvent(InteractionEvent event) {
        Log.d(TAG, "Replaying event: " + event);
        Activity currentActivity = getCurrentActivity();
        
        if (currentActivity == null || !currentActivity.getClass().getSimpleName().equals(event.getScreenName())) {
            navigateTo(event.getScreenName());
            sleepForUi();
            return; // Wait for next iteration after navigation
        }

        // Find view by resource name
        View view = findViewByResourceName(currentActivity, event.getViewResourceName());
        if (view == null) {
            Log.w(TAG, "View not found for resource name: " + event.getViewResourceName());
            return;
        }

        // Add delay before performing the action
        try {
            Thread.sleep(500); // 500ms delay to make the replay more visible
        } catch (InterruptedException e) {
            Log.e(TAG, "Delay interrupted: " + e.getMessage());
            return;
        }

        switch (event.getActionType()) {
            case "click":
                view.post(() -> {
                    Log.d(TAG, "Performing click on view: " + event.getViewResourceName());
                    view.performClick();
                });
                break;
            case "input":
                if (view instanceof EditText) {
                    view.post(() -> {
                        Log.d(TAG, "Setting text on EditText: " + event.getViewResourceName());
                        ((EditText) view).setText("");  // Clear existing text
                    });
                }
                break;
            default:
                Log.w(TAG, "Unknown action type: " + event.getActionType());
        }
    }

    private View findViewByResourceName(Activity activity, String resourceName) {
        if (resourceName == null || resourceName.equals("NO_ID")) {
            return null;
        }

        // Try to find the view by traversing the view hierarchy
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView instanceof ViewGroup) {
            return findViewInHierarchy((ViewGroup) rootView, resourceName);
        }
        return null;
    }

    private View findViewInHierarchy(ViewGroup root, String resourceName) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            
            // Check if this view matches the resource name
            try {
                if (child.getId() != View.NO_ID) {
                    String childResourceName = child.getResources().getResourceName(child.getId());
                    if (resourceName.equals(childResourceName)) {
                        return child;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error getting resource name for view: " + e.getMessage());
            }

            // Recursively check children if this is a ViewGroup
            if (child instanceof ViewGroup) {
                View found = findViewInHierarchy((ViewGroup) child, resourceName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void navigateTo(String screenName) {
        Log.d(TAG, "Navigating to screen: " + screenName);
        Class<?> targetActivity = ScreenMapper.getActivityClass(screenName);
        if (targetActivity != null && getCurrentActivity() != null) {
            Intent intent = new Intent(app, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.startActivity(intent);
        } else {
            Log.e(TAG, "Failed to navigate to screen: " + screenName);
        }
    }

    private void sleepForUi() {
        try {
            Thread.sleep(1000); // wait for Activity to load and layout to render
        } catch (InterruptedException e) {
            Log.e(TAG, "Sleep interrupted: " + e.getMessage());
        }
    }

    private Activity getCurrentActivity() {
        return CurrentActivityHolder.getInstance().getActivity();
    }
} 