package com.vocalflow.sdk.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.speech.RecognizerIntent;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;

public class WakeWordDetector {
    private static final String TAG = "WakeWordDetector";
    private final Context context;
    private final String wakeWord;
    private SpeechRecognizer speechRecognizer;
    private WakeWordListener wakeWordListener;
    private boolean isListening = false;
    private boolean isProcessing = false;  // Flag to prevent multiple simultaneous requests
    private static final int RETRY_DELAY_MS = 2000;  // 2 seconds between retries

    public WakeWordDetector(Context context, String wakeWord) {
        this.context = context;
        this.wakeWord = wakeWord.toLowerCase();
        setupSpeechRecognizer();
    }

    private void setupSpeechRecognizer() {
        Log.d(TAG, "Setting up speech recognizer");
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.d(TAG, "Ready for speech");
                isProcessing = false;  // Reset processing flag when ready
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Beginning of speech");
            }

            @Override
            public void onRmsChanged(float v) {
                // Intentionally empty
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                // Intentionally empty
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "End of speech");
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Speech recognition error: " + error);
                isProcessing = false;  // Reset processing flag on error
                
                if (wakeWordListener != null) {
                    wakeWordListener.onError(error);
                }
                
                // Only restart if we're still supposed to be listening
                if (!isListening) {
                    return;
                }

                // Use longer delay for busy errors
                int delay = (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) ? 
                           RETRY_DELAY_MS * 2 : RETRY_DELAY_MS;
                
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isListening && !isProcessing) {
                        startDetection();
                    }
                }, delay);
            }

            @Override
            public void onResults(Bundle results) {
                isProcessing = false;  // Reset processing flag when results received
                
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String transcription = matches.get(0).toLowerCase();
                    Log.d(TAG, "Received transcription: " + transcription);
                    Log.d(TAG, "Looking for wake word: " + wakeWord);
                    
                    if (transcription.contains(wakeWord)) {
                        Log.d(TAG, "Wake word detected: " + transcription);
                        isListening = false;  // Stop listening when wake word detected
                        if (wakeWordListener != null) {
                            Log.d(TAG, "Notifying wake word listener");
                            wakeWordListener.onWakeWordDetected();
                        } else {
                            Log.e(TAG, "Wake word listener is null!");
                        }
                    } else {
                        Log.d(TAG, "No wake word found in transcription");
                        // Not the wake word, continue listening after a delay
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isListening && !isProcessing) {
                                Log.d(TAG, "Continuing wake word detection");
                                startDetection();
                            }
                        }, RETRY_DELAY_MS);
                    }
                } else {
                    Log.d(TAG, "No matches in results");
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                // Intentionally empty
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
                // Intentionally empty
            }
        });
    }

    public void startDetection() {
        Log.d(TAG, "Starting wake word detection");
        if (isProcessing) {
            Log.d(TAG, "Skipping start detection - already processing");
            return;
        }
        
        isListening = true;
        isProcessing = true;
        
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
            
            speechRecognizer.startListening(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting detection: " + e.getMessage());
            isProcessing = false;  // Reset processing flag on error
            
            // Try to recover by recreating the recognizer after a delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isListening && !isProcessing) {
                    setupSpeechRecognizer();
                    startDetection();
                }
            }, RETRY_DELAY_MS);
        }
    }

    public void stopDetection() {
        Log.d(TAG, "Stopping wake word detection");
        isListening = false;
        isProcessing = false;
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    public void destroy() {
        Log.d(TAG, "Destroying wake word detector");
        isListening = false;
        isProcessing = false;
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    public void setWakeWordListener(WakeWordListener listener) {
        this.wakeWordListener = listener;
    }

    public interface WakeWordListener {
        void onWakeWordDetected();
        void onError(int error);
    }
} 