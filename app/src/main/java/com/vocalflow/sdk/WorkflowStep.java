package com.vocalflow.sdk;

public class WorkflowStep {
    private final String id;
    private final String prompt;
    private final String expectedResponse;

    public WorkflowStep(String id, String prompt, String expectedResponse) {
        this.id = id;
        this.prompt = prompt;
        this.expectedResponse = expectedResponse;
    }

    public String getId() {
        return id;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getExpectedResponse() {
        return expectedResponse;
    }
} 