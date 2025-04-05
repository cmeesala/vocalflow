package com.vocalflow.sdk.form.fields;

import android.content.Context;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.vocalflow.sdk.form.FormData;

public class TextField {
    private final String id;
    private final String label;
    private final boolean required;

    public TextField(String id, String label, boolean required) {
        this.id = id;
        this.label = label;
        this.required = required;
    }

    public View createView(Context context, FormData formData) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0, 8, 0, 8);

        TextView labelView = new TextView(context);
        labelView.setText(label + (required ? " *" : ""));
        labelView.setTextSize(16);
        container.addView(labelView);

        EditText editText = new EditText(context);
        editText.setId(View.generateViewId());
        editText.setText(formData.getValue(id));
        editText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                formData.setValue(id, s.toString());
            }
        });
        container.addView(editText);

        return container;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public boolean isRequired() {
        return required;
    }
} 