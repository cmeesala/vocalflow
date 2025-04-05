package com.vocalflow;

public class InteractionEvent {
    public long timestamp;
    public int viewId;
    public String screenName;
    public String actionType; 
    public String additionalData;

    public InteractionEvent(long timestamp, int viewId, String screenName, String actionType, String additionalData) {
        this.timestamp = timestamp;
        this.viewId = viewId;
        this.screenName = screenName;
        this.actionType = actionType;
        this.additionalData = additionalData;
    }

    @Override
    public String toString() {
        return "InteractionEvent{" +
                "timestamp=" + timestamp +
                ", viewId=" + viewId +
                ", screenName='" + screenName + '\'' +
                ", actionType='" + actionType + '\'' +
                ", additionalData='" + additionalData + '\'' +
                '}';
    }
} 