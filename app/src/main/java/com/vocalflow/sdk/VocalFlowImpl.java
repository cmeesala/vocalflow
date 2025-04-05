package com.vocalflow.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VocalFlowImpl implements VocalFlow {
    private final Map<String, Workflow> workflows = new HashMap<>();
    private final Map<String, ConversationState> conversations = new HashMap<>();

    @Override
    public void addWorkflow(Workflow workflow) {
        workflows.put(workflow.getId(), workflow);
    }

    @Override
    public void removeWorkflow(String workflowId) {
        workflows.remove(workflowId);
    }

    @Override
    public List<Workflow> getWorkflows() {
        return new ArrayList<>(workflows.values());
    }

    @Override
    public ConversationState startConversation(String workflowId) {
        Workflow workflow = workflows.get(workflowId);
        if (workflow == null) {
            throw new IllegalArgumentException("Workflow not found: " + workflowId);
        }

        String conversationId = generateConversationId();
        List<Message> initialMessages = new ArrayList<>();
        initialMessages.add(new Message("System", workflow.getSteps().get(0).getPrompt()));

        ConversationState state = new ConversationState(
            workflowId,
            workflow.getSteps().get(0).getId(),
            initialMessages
        );
        conversations.put(conversationId, state);
        return state;
    }

    @Override
    public ConversationState processInput(String conversationId, String input) {
        ConversationState currentState = conversations.get(conversationId);
        if (currentState == null) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Workflow workflow = workflows.get(currentState.getWorkflowId());
        if (workflow == null) {
            throw new IllegalStateException("Workflow not found: " + currentState.getWorkflowId());
        }

        List<Message> updatedMessages = new ArrayList<>(currentState.getMessages());
        updatedMessages.add(new Message("User", input));

        // TODO: Implement actual processing logic
        String response = "This is a placeholder response";
        updatedMessages.add(new Message("System", response));

        ConversationState newState = new ConversationState(
            currentState.getWorkflowId(),
            currentState.getCurrentStepId(),
            updatedMessages
        );
        conversations.put(conversationId, newState);
        return newState;
    }

    @Override
    public void endConversation(String conversationId) {
        conversations.remove(conversationId);
    }

    private String generateConversationId() {
        return "conv_" + System.currentTimeMillis();
    }
} 