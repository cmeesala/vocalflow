package com.vocalflow.sdk.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import com.vocalflow.BuildConfig;
import com.vocalflow.VocalFlowApplication;
import com.vocalflow.AutoInteractionTracker;
import com.vocalflow.InteractionEvent;
import com.vocalflow.InteractionReplayManager;
import com.vocalflow.sdk.llm.LLMService;
import com.vocalflow.sdk.speech.CommandListener;
import com.vocalflow.sdk.speech.WakeWordDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoiceAgentService extends Service {
    private static final String TAG = "VoiceAgentService";
    private static final String WAKE_WORD = "hey luma";
    private static final String SLEEP_WORD = "goodbye";

    private final IBinder binder = new LocalBinder();
    private WakeWordDetector wakeWordDetector;
    private CommandListener commandListener;
    private LLMService llmService;
    private TextToSpeech textToSpeech;
    private boolean isListeningForCommands = false;
    private TextView speechTextView;

    public class LocalBinder extends Binder {
        public VoiceAgentService getService() {
            return VoiceAgentService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "VoiceAgentService created");
        setupVoiceAgent();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "VoiceAgentService destroyed");
        cleanup();
    }

    public void setSpeechTextView(TextView textView) {
        this.speechTextView = textView;
    }

    private void setupVoiceAgent() {
        // Initialize LLM service with API key from BuildConfig
        String apiKey = BuildConfig.OPENAI_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            Log.e(TAG, "OPENAI_API_KEY not configured in BuildConfig");
            return;
        }
        
        llmService = new LLMService(this, apiKey);

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(java.util.Locale.US);
            } else {
                Log.e(TAG, "TextToSpeech initialization failed");
            }
        });

        // Create and initialize wake word detector
        if (wakeWordDetector != null) {
            wakeWordDetector.destroy();
        }
        wakeWordDetector = new WakeWordDetector(this, WAKE_WORD);
        wakeWordDetector.setWakeWordListener(new WakeWordDetector.WakeWordListener() {
            @Override
            public void onWakeWordDetected() {
                Log.d(TAG, "Wake word detected in VoiceAgentService");
                updateUI(() -> {
                    if (speechTextView != null) {
                        speechTextView.setText("Wake word detected: " + WAKE_WORD);
                    }
                });
                Log.d(TAG, "Speaking greeting");
                textToSpeech.speak("Hi there! How can I help you today", TextToSpeech.QUEUE_FLUSH, null, null);
                Log.d(TAG, "Starting command mode");
                startCommandMode();
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Wake word detection error: " + error);
                if (!isListeningForCommands) {
                    new android.os.Handler(getMainLooper()).postDelayed(() -> {
                        if (wakeWordDetector != null) {
                            wakeWordDetector.startDetection();
                        }
                    }, 1000);
                }
            }
        });

        // Initialize command listener (but don't start it yet)
        if (commandListener != null) {
            commandListener.destroy();
        }
        commandListener = new CommandListener(this);
        commandListener.setCommandListener(new CommandListener.OnCommandListener() {
            @Override
            public void onCommandReceived(String command) {
                Log.d(TAG, "Command received: " + command);
                updateUI(() -> {
                    if (speechTextView != null) {
                        speechTextView.setText("Processing: " + command);
                    }
                });
                
                // Check for sleep word
                if (command.toLowerCase().contains(SLEEP_WORD)) {
                    Log.d(TAG, "Sleep word detected, stopping command mode");
                    updateUI(() -> {
                        if (speechTextView != null) {
                            speechTextView.setText("Goodbye!");
                        }
                    });
                    textToSpeech.speak("Goodbye!", TextToSpeech.QUEUE_FLUSH, null, null);
                    stopCommandMode();
                    return;
                }

                stopCommandMode();
                executeCommand(command);
            }

            public void executeCommand(String command) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    try {
                        // Make API call
                        String apiUrl = "https://web-production-9ea4.up.railway.app/api/get_interactions/?intent_text=" + 
                                      java.net.URLEncoder.encode(command, "UTF-8");
                        URL url = new URL(apiUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        
                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String inputLine;
                            StringBuilder response = new StringBuilder();
                            
                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();
                            
                            // Parse JSON response
                            JSONArray jsonArray = new JSONArray(response.toString());
                            List<InteractionEvent> events = new ArrayList<>();
                            
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject eventJson = jsonArray.getJSONObject(i);
                                // Convert timestamp from ISO format to milliseconds
                                String timestampStr = eventJson.getString("timestamp");
                                long timestamp = java.time.Instant.parse(timestampStr).toEpochMilli();
                                
                                InteractionEvent event = new InteractionEvent(
                                    timestamp,
                                    eventJson.getInt("view_id"),
                                    eventJson.getString("view_resource_name"),
                                    eventJson.getString("screen_name"),
                                    eventJson.getString("action_type")
                                );
                                events.add(event);
                            }
                            
                            // Sort events by timestamp in ascending order
                            Collections.sort(events, (e1, e2) -> Long.compare(e1.getTimestamp(), e2.getTimestamp()));
                            
                            // Replay events on main thread
                            new android.os.Handler(getMainLooper()).post(() -> {
                                final InteractionReplayManager replayManager = VocalFlowApplication.getReplayManager();
                                replayManager.replay(events);
                            });
                        } else {
                            Log.e(TAG, "API call failed with response code: " + responseCode);
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error executing command: " + e.getMessage());
                    }
                });
                executor.shutdown();
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Command recognition error: " + error);
                if (isListeningForCommands) {
                    new android.os.Handler(getMainLooper()).postDelayed(() -> {
                        if (commandListener != null) {
                            commandListener.startListening();
                        }
                    }, 1000);
                }
            }
        });

        // Start wake word detection
        isListeningForCommands = false;
        wakeWordDetector.startDetection();
    }

    private void startCommandMode() {
        Log.d(TAG, "Starting command mode");
        isListeningForCommands = true;
        
        if (wakeWordDetector != null) {
            Log.d(TAG, "Stopping and destroying wake word detector");
            wakeWordDetector.stopDetection();
            wakeWordDetector.destroy();
            wakeWordDetector = null;
        } else {
            Log.e(TAG, "Wake word detector is null in startCommandMode");
        }
        
        // Start command listening with a delay
        new android.os.Handler(getMainLooper()).postDelayed(() -> {
            if (commandListener != null) {
                Log.d(TAG, "Starting command listener with delay");
                commandListener.startListeningWithDelay();
            } else {
                Log.e(TAG, "Command listener is null, cannot start listening");
            }
        }, 1000);
    }

    private void stopCommandMode() {
        Log.d(TAG, "Stopping command mode");
        isListeningForCommands = false;
        
        if (commandListener != null) {
            commandListener.stopListening();
        }
        
        // Reinitialize wake word detector
        if (wakeWordDetector == null) {
            wakeWordDetector = new WakeWordDetector(this, WAKE_WORD);
            wakeWordDetector.setWakeWordListener(new WakeWordDetector.WakeWordListener() {
                @Override
                public void onWakeWordDetected() {
                    Log.d(TAG, "Wake word detected");
                    updateUI(() -> {
                        if (speechTextView != null) {
                            speechTextView.setText("Wake word detected: " + WAKE_WORD);
                        }
                    });
                    textToSpeech.speak("Hi there! How can I help you today", TextToSpeech.QUEUE_FLUSH, null, null);
                    startCommandMode();
                }

                @Override
                public void onError(int error) {
                    Log.e(TAG, "Wake word detection error: " + error);
                    if (!isListeningForCommands) {
                        new android.os.Handler(getMainLooper()).postDelayed(() -> {
                            if (wakeWordDetector != null) {
                                wakeWordDetector.startDetection();
                            }
                        }, 1000);
                    }
                }
            });
        }
        // wakeWordDetector.startDetection();
    }

    private void cleanup() {
        if (wakeWordDetector != null) {
            wakeWordDetector.destroy();
            wakeWordDetector = null;
        }
        if (commandListener != null) {
            commandListener.destroy();
            commandListener = null;
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    private void updateUI(Runnable action) {
        new android.os.Handler(getMainLooper()).post(action);
    }
} 