package com.vocalflow.sdk.speech;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import java.util.ArrayList;
import java.util.Locale;

public class WakeWordDetector {
    private static final String TAG = "WakeWordDetector";
    private static final String WAKE_WORD = "hey pandora";
    private final Context context;
    private final String wakeWord;
    private final Handler handler;
    private SpeechRecognizerManager speechRecognizerManager;
    private WakeWordListener wakeWordListener;
    private boolean isDetecting = false;
    private static final long RECOGNITION_DELAY = 1000; // 1 second delay between recognitions

    public interface WakeWordListener {
        void onWakeWordDetected();
        void onError(int error);
    }

    public WakeWordDetector(Context context, String wakeWord) {
        this.context = context;
        this.wakeWord = wakeWord.toLowerCase(Locale.US);
        this.handler = new Handler();
        setupSpeechRecognizer();
    }

    private void setupSpeechRecognizer() {
        Log.d(TAG, "Setting up speech recognizer");
        speechRecognizerManager = new SpeechRecognizerManager(context);
        speechRecognizerManager.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Beginning of speech");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Not needed for wake word detection
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Not needed for wake word detection
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "End of speech");
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Speech recognition error: " + error);
                if (wakeWordListener != null) {
                    wakeWordListener.onError(error);
                }
                if (isDetecting) {
                    handler.postDelayed(() -> startDetection(), RECOGNITION_DELAY);
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String transcription = matches.get(0).toLowerCase(Locale.US);
                    Log.d(TAG, "Received transcription: " + transcription);
                    if (transcription.contains(wakeWord)) {
                        Log.d(TAG, "Wake word detected: " + wakeWord);
                        if (wakeWordListener != null) {
                            wakeWordListener.onWakeWordDetected();
                        }
                    } else {
                        // If wake word not detected, continue listening
                        if (isDetecting) {
                            handler.postDelayed(() -> startDetection(), RECOGNITION_DELAY);
                        }
                    }
                } else {
                    // No matches, continue listening
                    if (isDetecting) {
                        handler.postDelayed(() -> startDetection(), RECOGNITION_DELAY);
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Not needed for wake word detection
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Not needed for wake word detection
            }
        });
    }

    public void setWakeWordListener(WakeWordListener listener) {
        this.wakeWordListener = listener;
    }

    public void startDetection() {
        if (!isDetecting) {
            Log.d(TAG, "Starting wake word detection");
            isDetecting = true;
        }
        speechRecognizerManager.startListening();
    }

    public void stopDetection() {
        if (isDetecting) {
            Log.d(TAG, "Stopping wake word detection");
            isDetecting = false;
            // Add a small delay before stopping to avoid busy error
            new Handler().postDelayed(() -> {
                if (speechRecognizerManager != null) {
                    speechRecognizerManager.stopListening();
                }
            }, 500);
        }
    }

    public void destroy() {
        Log.d(TAG, "Destroying wake word detector");
        stopDetection();
        if (speechRecognizerManager != null) {
            speechRecognizerManager.destroy();
            speechRecognizerManager = null;
        }
    }
} 