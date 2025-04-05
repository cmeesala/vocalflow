package com.vocalflow.sdk.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import java.util.Locale;

public class SpeechRecognizerManager {
    private static final String TAG = "SpeechRecognizerManager";
    private final Context context;
    private SpeechRecognizer speechRecognizer;
    private RecognitionListener recognitionListener;
    private boolean isListening = false;
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 3;
    private static final int INITIAL_RETRY_DELAY = 1000; // 1 second

    public SpeechRecognizerManager(Context context) {
        this.context = context;
        initializeSpeechRecognizer();
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                if (speechRecognizer != null) {
                    speechRecognizer.destroy();
                }
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
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d(TAG, "Ready for speech");
                    if (recognitionListener != null) {
                        recognitionListener.onReadyForSpeech(params);
                    }
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.d(TAG, "Beginning of speech");
                    if (recognitionListener != null) {
                        recognitionListener.onBeginningOfSpeech();
                    }
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    if (recognitionListener != null) {
                        recognitionListener.onRmsChanged(rmsdB);
                    }
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    if (recognitionListener != null) {
                        recognitionListener.onBufferReceived(buffer);
                    }
                }

                @Override
                public void onEndOfSpeech() {
                    Log.d(TAG, "End of speech");
                    if (recognitionListener != null) {
                        recognitionListener.onEndOfSpeech();
                    }
                }

                @Override
                public void onError(int error) {
                    Log.e(TAG, "Speech recognition error: " + error);
                    
                    if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                        retryCount++;
                        if (retryCount > MAX_RETRY_COUNT) {
                            Log.e(TAG, "Too many retries, resetting speech recognizer");
                            retryCount = 0;
                            initializeSpeechRecognizer();
                            if (isListening) {
                                startListening();
                            }
                            return;
                        }
                        
                        // Exponential backoff
                        int delay = INITIAL_RETRY_DELAY * (1 << (retryCount - 1));
                        Log.d(TAG, "Retrying after " + delay + "ms (attempt " + retryCount + ")");
                        
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isListening) {
                                startListening();
                            }
                        }, delay);
                    } else {
                        // For other errors, use a shorter fixed delay
                        retryCount = 0;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (isListening) {
                                startListening();
                            }
                        }, INITIAL_RETRY_DELAY);
                    }
                    
                    if (recognitionListener != null) {
                        recognitionListener.onError(error);
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    Log.d(TAG, "Got results");
                    if (recognitionListener != null) {
                        recognitionListener.onResults(results);
                    }
                    
                    // Continue listening if still active
                    if (isListening) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            startListening();
                        }, 500);
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    if (recognitionListener != null) {
                        recognitionListener.onPartialResults(partialResults);
                    }
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    if (recognitionListener != null) {
                        recognitionListener.onEvent(eventType, params);
                    }
                }
            });
            Log.d(TAG, "Recognition listener set");
        }
    }

    public void startListening() {
        if (speechRecognizer != null) {
            try {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
                
                Log.d(TAG, "Starting speech recognition");
                isListening = true;
                speechRecognizer.startListening(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting speech recognition: " + e.getMessage());
                if (recognitionListener != null) {
                    recognitionListener.onError(SpeechRecognizer.ERROR_CLIENT);
                }
            }
        }
    }

    public void stopListening() {
        Log.d(TAG, "Stopping speech recognition");
        isListening = false;
        if (speechRecognizer != null) {
            try {
                speechRecognizer.stopListening();
                speechRecognizer.cancel();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping speech recognition: " + e.getMessage());
            }
        }
    }

    public void destroy() {
        Log.d(TAG, "Destroying SpeechRecognizer");
        isListening = false;
        if (speechRecognizer != null) {
            try {
                speechRecognizer.stopListening();
                speechRecognizer.cancel();
                speechRecognizer.destroy();
                speechRecognizer = null;
            } catch (Exception e) {
                Log.e(TAG, "Error destroying speech recognizer: " + e.getMessage());
            }
        }
    }
} 