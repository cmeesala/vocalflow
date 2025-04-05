package com.vocalflow.sdk;

import java.util.List;

public interface VocalFlow {
    void addWorkflow(Workflow workflow);
    void removeWorkflow(String workflowId);
    List<Workflow> getWorkflows();
    ConversationState startConversation(String workflowId);
    ConversationState processInput(String conversationId, String input);
    void endConversation(String conversationId);
} 