package com.vocalflow.sdk.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.ArrayList;
import java.util.Locale;
import com.vocalflow.sdk.form.VocalFlowForm;
import com.vocalflow.sdk.form.steps.BasicFormStep;
import com.vocalflow.sdk.form.fields.TextField;

public class VoiceAgent {
    private static final String TAG = "VoiceAgent";
    private final Context context;
    private final String wakeWord;
    private final String sleepWord;
    private boolean isActive = false;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private VoiceCallback callback;
    private VocalFlowForm currentForm;
    private boolean isListening;

    public interface VoiceCallback {
        void onResult(String result);
        void onError(int error);
    }

    public VoiceAgent(Context context) {
        this(context, "hey pandora", "bye pandora");
    }

    public VoiceAgent(Context context, String wakeWord, String sleepWord) {
        this.context = context;
        this.wakeWord = wakeWord;
        this.sleepWord = sleepWord;
        initializeSpeechRecognizer();
        initializeTextToSpeech();
    }

    private void initializeSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "Speech recognition is not available");
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        setupSpeechRecognizer();
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported");
                }
            } else {
                Log.e(TAG, "TextToSpeech initialization failed");
            }
        });
    }

    private void setupSpeechRecognizer() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
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
                // Not needed
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Not needed
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "End of speech");
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Error: " + error);
                if (isListening) {
                    startListening();
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String transcription = matches.get(0).toLowerCase(Locale.US);
                    processVoiceCommand(transcription);
                }
                if (isListening) {
                    startListening();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Not needed
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Not needed
            }
        });
    }

    public void start() {
        isActive = true;
        speak("Hello! I'm ready to help you fill out the form.");
        isListening = true;
        startListening();
    }

    public void stop() {
        isActive = false;
        isListening = false;
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
        }
        speak("Goodbye!");
    }

    private void startListening() {
        if (!isActive) return;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString());
        speechRecognizer.startListening(intent);
    }

    private void processVoiceCommand(String transcription) {
        if (currentForm == null) {
            return;
        }

        BasicFormStep currentStep = currentForm.getCurrentStep();
        for (TextField field : currentStep.getFields()) {
            String fieldLabel = field.getLabel().toLowerCase(Locale.US);
            if (transcription.contains(fieldLabel)) {
                String value = extractValue(transcription, fieldLabel);
                if (value != null) {
                    currentForm.updateField(field.getId(), value);
                    speak("Updated " + fieldLabel + " to " + value);
                }
            }
        }

        if (transcription.contains("next") && !currentForm.isLastStep()) {
            currentForm.nextStep();
            speak("Moving to the next step.");
        } else if (transcription.contains("back") && !currentForm.isFirstStep()) {
            currentForm.previousStep();
            speak("Going back to the previous step.");
        } else if (transcription.contains("complete")) {
            currentForm.complete();
            speak("Form completed.");
        }
    }

    private String extractValue(String transcription, String fieldLabel) {
        int labelIndex = transcription.indexOf(fieldLabel);
        if (labelIndex >= 0) {
            String afterLabel = transcription.substring(labelIndex + fieldLabel.length()).trim();
            if (afterLabel.startsWith("is") || afterLabel.startsWith("are")) {
                return afterLabel.substring(2).trim();
            }
        }
        return null;
    }

    public void setCallback(VoiceCallback callback) {
        this.callback = callback;
    }

    public void speak(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void cleanup() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    public void destroy() {
        speechRecognizer.destroy();
    }

    public void setCurrentForm(VocalFlowForm form) {
        this.currentForm = form;
    }
} 