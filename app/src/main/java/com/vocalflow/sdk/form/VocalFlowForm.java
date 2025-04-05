package com.vocalflow.sdk.form;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import com.vocalflow.sdk.form.steps.BasicFormStep;
import java.util.List;
import java.util.function.Consumer;

public class VocalFlowForm {
    private final Context context;
    private final List<BasicFormStep> steps;
    private final Consumer<FormData> onComplete;
    private final FormData formData;
    private int currentStepIndex;

    public VocalFlowForm(Context context, List<BasicFormStep> steps, Consumer<FormData> onComplete) {
        this.context = context;
        this.steps = steps;
        this.onComplete = onComplete;
        this.formData = new FormData();
        this.currentStepIndex = 0;
    }

    public View createView() {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        if (!steps.isEmpty()) {
            BasicFormStep currentStep = steps.get(currentStepIndex);
            View stepView = currentStep.createView(context, formData);
            container.addView(stepView);
        }

        return container;
    }

    public void nextStep() {
        if (currentStepIndex < steps.size() - 1) {
            currentStepIndex++;
        } else {
            onComplete.accept(formData);
        }
    }

    public void previousStep() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
        }
    }

    public BasicFormStep getCurrentStep() {
        return steps.get(currentStepIndex);
    }

    public boolean isLastStep() {
        return currentStepIndex == steps.size() - 1;
    }

    public boolean isFirstStep() {
        return currentStepIndex == 0;
    }

    public void updateField(String fieldId, String value) {
        formData.setValue(fieldId, value);
    }

    public void complete() {
        onComplete.accept(formData);
    }
}
 
 