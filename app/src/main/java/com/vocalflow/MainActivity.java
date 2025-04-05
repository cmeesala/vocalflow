package com.vocalflow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import com.vocalflow.sdk.speech.WakeWordDetector;
import com.vocalflow.sdk.speech.CommandListener;
import android.widget.TextView;
import com.vocalflow.sdk.llm.LLMService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String WAKE_WORD = "hey pandora";
    private static final String SLEEP_WORD = "goodbye";
    
    private WakeWordDetector wakeWordDetector;
    private CommandListener commandListener;
    private TextToSpeech textToSpeech;
    private boolean isListeningForCommands = false;
    private TextView speechTextView;
    private LLMService llmService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        speechTextView = findViewById(R.id.speechTextView);
        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting microphone permission");
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.RECORD_AUDIO}, 
                PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "Microphone permission already granted");
            initializeTTS();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Microphone permission granted");
                initializeTTS();
            } else {
                Log.e(TAG, "Microphone permission denied");
                Toast.makeText(this, "Microphone permission is required for voice commands", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeTTS() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                setupVoiceAgent();
            } else {
                Log.e(TAG, "Failed to initialize TTS");
            }
        });
    }

    private void setupVoiceAgent() {
        // Initialize LLM service with API key from environment variable
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            Log.e(TAG, "OPENAI_API_KEY environment variable not set");
            runOnUiThread(() -> {
                speechTextView.setText("Error: API key not configured. Please set OPENAI_API_KEY environment variable.");
            });
            return;
        }
        
        llmService = new LLMService(apiKey);

        wakeWordDetector = new WakeWordDetector(this, WAKE_WORD);
        wakeWordDetector.setWakeWordListener(new WakeWordDetector.WakeWordListener() {
            @Override
            public void onWakeWordDetected() {
                Log.d(TAG, "Wake word detected");
                runOnUiThread(() -> {
                    speechTextView.setText("Wake word detected: " + WAKE_WORD);
                    textToSpeech.speak("Hi there! How can I help you today", TextToSpeech.QUEUE_FLUSH, null, null);
                });
                startCommandMode();
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Wake word detection error: " + error);
                if (!isListeningForCommands) {
                    new android.os.Handler().postDelayed(() -> {
                        if (wakeWordDetector != null) {
                            wakeWordDetector.startDetection();
                        }
                    }, 1000);
                }
            }
        });

        commandListener = new CommandListener(this);
        commandListener.setCommandListener(new CommandListener.OnCommandListener() {
            @Override
            public void onCommandReceived(String command) {
                Log.d(TAG, "Command received: " + command);
                runOnUiThread(() -> {
                    speechTextView.setText("Processing: " + command);
                });
                
                // Check for sleep word
                if (command.toLowerCase().contains(SLEEP_WORD)) {
                    Log.d(TAG, "Sleep word detected, stopping command mode");
                    runOnUiThread(() -> {
                        speechTextView.setText("Goodbye!");
                        textToSpeech.speak("Goodbye!", TextToSpeech.QUEUE_FLUSH, null, null);
                    });
                    stopCommandMode();
                    return;
                }
                
                // Process command with LLM
                llmService.getResponse(command, new LLMService.LLMResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "LLM Response: " + response);
                        runOnUiThread(() -> {
                            speechTextView.setText("Response: " + response);
                            textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null, "tts1");
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "LLM Error: " + error);
                        runOnUiThread(() -> {
                            speechTextView.setText("Error: " + error);
                            textToSpeech.speak("Sorry, I encountered an error. Please try again.", TextToSpeech.QUEUE_FLUSH, null, "tts1");
                        });
                    }
                });
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Command recognition error: " + error);
                if (isListeningForCommands) {
                    new android.os.Handler().postDelayed(() -> {
                        if (commandListener != null) {
                            commandListener.startListening();
                        }
                    }, 1000);
                }
            }
        });

        wakeWordDetector.startDetection();
    }

    private void startCommandMode() {
        Log.d(TAG, "Starting command mode");
        isListeningForCommands = true;
        
        // First stop wake word detection
        if (wakeWordDetector != null) {
            wakeWordDetector.stopDetection();
        }
        
        // Add a delay to ensure recognizer is fully released
        new android.os.Handler().postDelayed(() -> {
            if (commandListener != null) {
                commandListener.startListening();
            }
        }, 1000);
    }

    private void stopCommandMode() {
        Log.d(TAG, "Stopping command mode");
        isListeningForCommands = false;
        
        // First stop command listening
        if (commandListener != null) {
            commandListener.stopListening();
        }
        
        // Add a delay to ensure recognizer is fully released
        new android.os.Handler().postDelayed(() -> {
            if (wakeWordDetector != null) {
                wakeWordDetector.startDetection();
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wakeWordDetector != null) {
            wakeWordDetector.destroy();
        }
        if (commandListener != null) {
            commandListener.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
} 