package com.vocalflow.sdk.form.steps;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.vocalflow.sdk.form.FormData;
import com.vocalflow.sdk.form.fields.TextField;
import java.util.List;

public class BasicFormStep {
    private final String id;
    private final String title;
    private final String description;
    private final List<TextField> fields;

    public BasicFormStep(String id, String title, String description, List<TextField> fields) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.fields = fields;
    }

    public View createView(Context context, FormData formData) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(16, 16, 16, 16);

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(24);
        container.addView(titleView);

        TextView descriptionView = new TextView(context);
        descriptionView.setText(description);
        descriptionView.setTextSize(16);
        descriptionView.setPadding(0, 8, 0, 16);
        container.addView(descriptionView);

        for (TextField field : fields) {
            View fieldView = field.createView(context, formData);
            container.addView(fieldView);
        }

        return container;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<TextField> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "BasicFormStep{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", fields=" + fields +
                '}';
    }
} 