package com.vocalflow.sdk;

import java.util.List;

public class Workflow {
    private final String id;
    private final String name;
    private final List<WorkflowStep> steps;

    public Workflow(String id, String name, List<WorkflowStep> steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<WorkflowStep> getSteps() {
        return steps;
    }
} 