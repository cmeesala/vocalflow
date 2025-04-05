package com.vocalflow.sdk.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import java.util.Locale;

public class SpeechRecognizerManager {
    private static final String TAG = "SpeechRecognizerManager";
    private final Context context;
    private SpeechRecognizer speechRecognizer;
    private RecognitionListener recognitionListener;
    private boolean isListening = false;

    public SpeechRecognizerManager(Context context) {
        this.context = context;
        initializeSpeechRecognizer();
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
                Log.d(TAG, "SpeechRecognizer initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to create SpeechRecognizer: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Speech recognition is not available on this device");
        }
    }

    public void setRecognitionListener(RecognitionListener listener) {
        this.recognitionListener = listener;
        if (speechRecognizer != null) {
            speechRecognizer.setRecognitionListener(listener);
            Log.d(TAG, "Recognition listener set");
        }
    }

    public void startListening() {
        if (speechRecognizer != null && !isListening) {
            try {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
                
                Log.d(TAG, "Starting speech recognition");
                speechRecognizer.startListening(intent);
                isListening = true;
            } catch (Exception e) {
                Log.e(TAG, "Error starting speech recognition: " + e.getMessage());
                if (recognitionListener != null) {
                    recognitionListener.onError(SpeechRecognizer.ERROR_CLIENT);
                }
            }
        }
    }

    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            try {
                Log.d(TAG, "Stopping speech recognition");
                speechRecognizer.stopListening();
                isListening = false;
            } catch (Exception e) {
                Log.e(TAG, "Error stopping speech recognition: " + e.getMessage());
            }
        }
    }

    public void destroy() {
        Log.d(TAG, "Destroying SpeechRecognizer");
        stopListening();
        if (speechRecognizer != null) {
            try {
                speechRecognizer.destroy();
                speechRecognizer = null;
            } catch (Exception e) {
                Log.e(TAG, "Error destroying speech recognizer: " + e.getMessage());
            }
        }
    }
} 