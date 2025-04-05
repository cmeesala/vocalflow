package com.vocalflow.sdk.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import java.util.ArrayList;
import java.util.Locale;

public class CommandListener {
    private static final String TAG = "CommandListener";
    private final Context context;
    private SpeechRecognizerManager speechRecognizerManager;
    private OnCommandListener commandListener;
    private boolean isListening = false;

    public interface OnCommandListener {
        void onCommandReceived(String command);
        void onError(int error);
    }

    public CommandListener(Context context) {
        this.context = context;
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
                // Not needed for command recognition
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Not needed for command recognition
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "End of speech");
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Command recognition error: " + error);
                
                if (commandListener != null) {
                    commandListener.onError(error);
                }
                
                // Handle specific error codes
                if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    Log.d(TAG, "Recognizer busy, recreating");
                    speechRecognizerManager.destroy();
                    setupSpeechRecognizer();
                }
                
                // Restart listening after a delay if still active
                if (isListening) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        startListening();
                    }, 1000);
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String command = matches.get(0);
                    Log.d(TAG, "Command received: " + command);
                    if (commandListener != null) {
                        commandListener.onCommandReceived(command);
                    }
                }
                
                // Continue listening for more commands if still active
                if (isListening) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        startListening();
                    }, 500);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Not needed for command recognition
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Not needed for command recognition
            }
        });
    }

    public void setCommandListener(OnCommandListener listener) {
        this.commandListener = listener;
    }

    public void startListening() {
        Log.d(TAG, "Starting command recognition");
        isListening = true;
        if (speechRecognizerManager == null) {
            setupSpeechRecognizer();
        }
        speechRecognizerManager.startListening();
    }

    public void stopListening() {
        Log.d(TAG, "Stopping command recognition");
        isListening = false;
        if (speechRecognizerManager != null) {
            speechRecognizerManager.stopListening();
        }
    }

    public void destroy() {
        Log.d(TAG, "Destroying command listener");
        isListening = false;
        if (speechRecognizerManager != null) {
            speechRecognizerManager.destroy();
            speechRecognizerManager = null;
        }
    }
} 