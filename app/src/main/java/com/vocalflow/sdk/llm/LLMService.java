package com.vocalflow.sdk.llm;

import android.content.Context;
import android.util.Log;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LLMService {
    private static final String TAG = "LLMService";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final OkHttpClient client;
    private final String apiKey;
    private final Context context;

    public interface LLMResponseCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public LLMService(Context context, String apiKey) {
        this.context = context;
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    public void getResponse(String userInput, LLMResponseCallback callback) {
        Log.d(TAG, "Sending request to LLM: " + userInput);
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-3.5-turbo");
            
            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful AI assistant named Pandora. Keep your responses concise and friendly.");
            messages.put(systemMessage);
            
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", userInput);
            messages.put(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            Log.d(TAG, "Request body: " + requestBody.toString());

            Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API request failed", e);
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String error = "API request failed with code: " + response.code();
                        Log.e(TAG, error);
                        callback.onError(error);
                        return;
                    }

                    try {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Raw API response: " + responseBody);
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        if (choices.length() > 0) {
                            String content = choices.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                            Log.d(TAG, "Extracted response: " + content);
                            callback.onResponse(content);
                        } else {
                            String error = "No choices in API response";
                            Log.e(TAG, error);
                            callback.onError(error);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing API response", e);
                        callback.onError(e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating API request", e);
            callback.onError(e.getMessage());
        }
    }
} 