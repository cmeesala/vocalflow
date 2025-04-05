package com.vocalflow;

public class InteractionEvent {
    private final long timestamp;
    private final int viewId;
    private final String viewResourceName;
    private final String screenName;
    private final String actionType;

    public InteractionEvent(long timestamp, int viewId, String viewResourceName, String screenName, String actionType) {
        this.timestamp = timestamp;
        this.viewId = viewId;
        this.viewResourceName = viewResourceName;
        this.screenName = screenName;
        this.actionType = actionType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getViewId() {
        return viewId;
    }

    public String getViewResourceName() {
        return viewResourceName;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getActionType() {
        return actionType;
    }

    @Override
    public String toString() {
        return "InteractionEvent{" +
                "timestamp=" + timestamp +
                ", viewId=" + viewId +
                ", viewResourceName='" + viewResourceName + '\'' +
                ", screenName='" + screenName + '\'' +
                ", actionType='" + actionType + '\'' +
                '}';
    }
} 