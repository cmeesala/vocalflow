package com.vocalflow;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoInteractionTracker {
    private static final String TAG = "AutoInteractionTracker";
    private static final AutoInteractionTracker instance = new AutoInteractionTracker();
    private final List<InteractionEvent> eventBuffer = new ArrayList<>();

    public static AutoInteractionTracker getInstance() {
        return instance;
    }

    public void init(Application app) {
        Log.d(TAG, "Initializing AutoInteractionTracker");
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(Activity activity) {
                String screenName = activity.getClass().getSimpleName();
                Log.d(TAG, "Activity resumed: " + screenName);
                attachListeners(activity, screenName);
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                // Also attach listeners when activity is created
                String screenName = activity.getClass().getSimpleName();
                Log.d(TAG, "Activity created: " + screenName);
                attachListeners(activity, screenName);
            }

            // Other lifecycle methods can remain empty
            @Override public void onActivityStarted(Activity activity) {}
            @Override public void onActivityPaused(Activity activity) {}
            @Override public void onActivityStopped(Activity activity) {}
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override public void onActivityDestroyed(Activity activity) {}
        });
    }

    private void attachListeners(Activity activity, String screenName) {
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView instanceof ViewGroup) {
            Log.d(TAG, "Attaching listeners to views in: " + screenName);
            attachToViews((ViewGroup) rootView, screenName);
        } else {
            Log.w(TAG, "Root view is not a ViewGroup in: " + screenName);
        }
    }

    private void attachToViews(ViewGroup root, String screenName) {
        // First attach to the ViewGroup itself if it's clickable
        attachToView(root, screenName);
        
        // Then recursively attach to all children
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                attachToViews((ViewGroup) child, screenName);
            } else {
                attachToView(child, screenName);
            }
        }
    }

    private void attachToView(View view, String screenName) {
        // Check if view should be tracked
        boolean shouldTrack = view.isClickable() || 
                            view instanceof Button || 
                            view instanceof CardView || 
                            view instanceof ImageView || 
                            view instanceof TextView;

        if (shouldTrack && view.getTag(R.id.already_hooked) == null) {
            // Create a wrapper that will log and then allow the original click to proceed
            view.setOnTouchListener((v, event) -> {
                // Only handle click events
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    String viewName = v.getClass().getSimpleName();
                    String viewId = "NO_ID";
                    try {
                        if (v.getId() != View.NO_ID) {
                            viewId = v.getResources().getResourceName(v.getId());
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to get resource name for view ID: " + e.getMessage());
                        viewId = "id/" + v.getId();
                    }
                    
                    Log.d(TAG, String.format("Click detected - Screen: %s, View: %s, ID: %s", 
                        screenName, viewName, viewId));
                    
                    logEvent(new InteractionEvent(System.currentTimeMillis(), v.getId(), viewId, screenName, "click"));
                }
                // Return false to allow the original click to proceed
                return false;
            });
            view.setTag(R.id.already_hooked, true);
        }

        if (view instanceof EditText && view.getTag(R.id.already_hooked_input) == null) {
            ((EditText) view).addTextChangedListener(new TextWatcher() {
                @Override public void afterTextChanged(Editable s) {
                    String viewName = view.getClass().getSimpleName();
                    String viewId = "NO_ID";
                    try {
                        if (view.getId() != View.NO_ID) {
                            viewId = view.getResources().getResourceName(view.getId());
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to get resource name for view ID: " + e.getMessage());
                        viewId = "id/" + view.getId();
                    }
                    
                    Log.d(TAG, String.format("Text input detected - Screen: %s, View: %s, ID: %s, Text: %s", 
                        screenName, viewName, viewId, s.toString()));
                    
                    logEvent(new InteractionEvent(System.currentTimeMillis(), view.getId(), viewId, screenName, "input"));
                }

                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });
            view.setTag(R.id.already_hooked_input, true);
        }
    }

    private void logEvent(InteractionEvent event) {
        eventBuffer.add(event);
        Log.d(TAG, "Event logged: " + event.toString());
        if (eventBuffer.size() > 100) {
            InteractionEvent removed = eventBuffer.remove(0);
            Log.d(TAG, "Buffer full, removed oldest event: " + removed.toString());
        }
        Log.d(TAG, "Current buffer size: " + eventBuffer.size()); 
        tryRelayIntent(event);
    }

    private void tryRelayIntent(InteractionEvent event) {
        String screenName = event.getScreenName();
        if(!screenName.equals("PaymentProcessingActivity")) return;
        Activity currentActivity = getCurrentActivity();
        if (currentActivity != null) {
            SharedPreferences prefs = currentActivity.getSharedPreferences("PaymentPrefs", Context.MODE_PRIVATE);
            String billType = prefs.getString("bill_type", "");
            String amount = prefs.getString("amount", "");
            String paymentMethod = prefs.getString("payment_method", "");
            
            String intentText = String.format("Your %s Bill of %s has been successfully paid through %s.", billType, amount, paymentMethod);
            Log.d(TAG, "Intent Text: " + intentText);
            List<InteractionEvent> events = new ArrayList<>();
            for(int i=eventBuffer.size()-2; i>=0; i--) {
                InteractionEvent e = eventBuffer.get(i);
                events.add(e);
                if(e.getScreenName().equals("MainActivity")) break;
            }
            Collections.reverse(events);
        }
        eventBuffer.clear();
    }

    public List<InteractionEvent> getEvents() {
        Log.d(TAG, "Retrieving " + eventBuffer.size() + " events");
        List<InteractionEvent> events = new ArrayList<>(eventBuffer);
        Collections.reverse(events);
        return events;
    }

    private Activity getCurrentActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Object activities = activitiesField.get(activityThread);
            
            for (Object activityRecord : ((android.util.ArrayMap) activities).values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting current activity", e);
        }
        return null;
    }
} 