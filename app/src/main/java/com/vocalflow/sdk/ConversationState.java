package com.vocalflow.sdk;

import java.util.List;

public class ConversationState {
    private final String workflowId;
    private final String currentStepId;
    private final List<Message> messages;

    public ConversationState(String workflowId, String currentStepId, List<Message> messages) {
        this.workflowId = workflowId;
        this.currentStepId = currentStepId;
        this.messages = messages;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getCurrentStepId() {
        return currentStepId;
    }

    public List<Message> getMessages() {
        return messages;
    }
} 