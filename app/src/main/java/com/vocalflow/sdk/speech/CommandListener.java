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
    private boolean isProcessing = false;
    private static final int RETRY_DELAY_MS = 2000;
    private static final int TRANSITION_DELAY_MS = 1000; // Delay after wake word detection
    private static final int TTS_COOLDOWN_MS = 3000; // Delay after TTS to prevent feedback
    private static final int INITIAL_DELAY_MS = 4000; // Delay after initialization to prevent picking up greeting

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
        if (speechRecognizerManager != null) {
            speechRecognizerManager.destroy();
        }
        speechRecognizerManager = new SpeechRecognizerManager(context);
        speechRecognizerManager.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
                isProcessing = false;
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
                isProcessing = false;
                
                if (commandListener != null) {
                    commandListener.onError(error);
                }
                
                if (!isListening) {
                    Log.d(TAG, "Not restarting - not listening");
                    return;
                }

                // Handle specific error codes
                if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    Log.d(TAG, "No match detected, continuing to listen");
                    int delay = RETRY_DELAY_MS;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isListening && !isProcessing) {
                            Log.d(TAG, "Restarting after no match");
                            startListening();
                        }
                    }, delay);
                } else if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    Log.d(TAG, "Speech timeout, continuing to listen");
                    int delay = RETRY_DELAY_MS;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isListening && !isProcessing) {
                            Log.d(TAG, "Restarting after timeout");
                            startListening();
                        }
                    }, delay);
                } else if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    Log.d(TAG, "Recognizer busy, recreating");
                    speechRecognizerManager.destroy();
                    setupSpeechRecognizer();
                    int delay = RETRY_DELAY_MS * 2;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isListening && !isProcessing) {
                            Log.d(TAG, "Restarting after busy error");
                            startListening();
                        }
                    }, delay);
                }
            }

            @Override
            public void onResults(Bundle results) {
                isProcessing = false;
                
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String command = matches.get(0);
                    Log.d(TAG, "Received transcription: " + command);
                    if (commandListener != null) {
                        commandListener.onCommandReceived(command);
                    }
                } else {
                    Log.d(TAG, "No matches in results");
                }
                
                // Add delay after receiving results to prevent TTS feedback
                if (isListening && !isProcessing) {
                    Log.d(TAG, "Waiting for TTS cooldown before continuing to listen");
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isListening && !isProcessing) {
                            Log.d(TAG, "Continuing to listen for commands after cooldown");
                            startListening();
                        }
                    }, TTS_COOLDOWN_MS);
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
        if (isProcessing) {
            Log.d(TAG, "Skipping start listening - already processing");
            return;
        }
        
        isListening = true;
        isProcessing = true;
        
        if (speechRecognizerManager == null) {
            Log.d(TAG, "Creating new speech recognizer manager");
            setupSpeechRecognizer();
        }
        
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000);
            
            Log.d(TAG, "Starting speech recognition");
            speechRecognizerManager.startListening();
        } catch (Exception e) {
            Log.e(TAG, "Error starting recognition: " + e.getMessage());
            isProcessing = false;
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isListening && !isProcessing) {
                    Log.d(TAG, "Retrying after error");
                    setupSpeechRecognizer();
                    startListening();
                }
            }, RETRY_DELAY_MS);
        }
    }

    public void startListeningWithDelay() {
        Log.d(TAG, "Scheduling command recognition with initial delay");
        isListening = true;  // Set the flag before starting the delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isListening && !isProcessing) {
                Log.d(TAG, "Starting command recognition after initial delay");
                startListening();
            } else {
                Log.d(TAG, "Skipping start listening - isListening: " + isListening + ", isProcessing: " + isProcessing);
            }
        }, INITIAL_DELAY_MS);
    }

    public void stopListening() {
        Log.d(TAG, "Stopping command recognition");
        isListening = false;
        isProcessing = false;
        if (speechRecognizerManager != null) {
            speechRecognizerManager.stopListening();
        }
    }

    public void destroy() {
        Log.d(TAG, "Destroying command listener");
        isListening = false;
        isProcessing = false;
        if (speechRecognizerManager != null) {
            speechRecognizerManager.destroy();
            speechRecognizerManager = null;
        }
    }
} 