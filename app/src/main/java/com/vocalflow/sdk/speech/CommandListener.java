package com.vocalflow.sdk.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
    private android.os.Handler handler = new android.os.Handler();

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
                // Start listening again if we're still active
                if (isListening) {
                    startListening();
                }
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
                
                // Handle specific error codes
                switch (error) {
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        Log.d(TAG, "Recognizer busy, will retry after delay");
                        new android.os.Handler().postDelayed(() -> {
                            if (isListening) {
                                startListening();
                            }
                        }, 1000);
                        break;
                        
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        Log.d(TAG, "No speech detected, will retry");
                        if (isListening) {
                            startListening();
                        }
                        break;
                        
                    default:
                        Log.e(TAG, "Unhandled error: " + error);
                        if (isListening) {
                            // Retry after a longer delay for other errors
                            new android.os.Handler().postDelayed(() -> {
                                if (isListening) {
                                    startListening();
                                }
                            }, 2000);
                        }
                        break;
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
                    Log.d(TAG, "Continuing to listen for more commands");
                    startListening();
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
        if (!isListening) {
            Log.d(TAG, "Starting command recognition");
            isListening = true;
            speechRecognizerManager.startListening();
        }
    }

    public void stopListening() {
        if (isListening) {
            Log.d(TAG, "Stopping command recognition");
            isListening = false;
            speechRecognizerManager.stopListening();
        }
    }

    public void destroy() {
        Log.d(TAG, "Destroying command listener");
        stopListening();
        speechRecognizerManager.destroy();
    }
} 