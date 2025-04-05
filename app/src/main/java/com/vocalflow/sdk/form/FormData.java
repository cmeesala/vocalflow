package com.vocalflow.sdk.form;

import java.util.HashMap;
import java.util.Map;

public class FormData {
    private final Map<String, String> data;

    public FormData() {
        this.data = new HashMap<>();
    }

    public void setValue(String fieldId, String value) {
        data.put(fieldId, value);
    }

    public String getValue(String fieldId) {
        return data.get(fieldId);
    }

    public Map<String, String> getAllData() {
        return new HashMap<>(data);
    }

    @Override
    public String toString() {
        return "FormData{" +
                "data=" + data +
                '}';
    }
} 