package com.vocalflow;

import java.util.HashMap;
import java.util.Map;

public class ScreenMapper {
    private static final Map<String, Class<?>> screenToActivity = new HashMap<>();

    static {
        // Add mappings for all activities in the app
        screenToActivity.put("LoginActivity", com.vocalflow.LoginActivity.class);
        screenToActivity.put("MainActivity", com.vocalflow.MainActivity.class);
        screenToActivity.put("BillPayActivity", com.vocalflow.BillPayActivity.class);
        screenToActivity.put("PaymentMethodsActivity", com.vocalflow.PaymentMethodsActivity.class);
        screenToActivity.put("PaymentProcessingActivity", com.vocalflow.PaymentProcessingActivity.class);
    }

    public static Class<?> getActivityClass(String screenName) {
        return screenToActivity.get(screenName);
    }
} 