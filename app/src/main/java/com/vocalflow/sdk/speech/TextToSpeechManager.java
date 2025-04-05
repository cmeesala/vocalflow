package com.vocalflow.sdk.speech;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.Locale;

public class TextToSpeechManager {
    private static final String TAG = "TextToSpeechManager";
    private final Context context;
    private TextToSpeech textToSpeech;
    private boolean isInitialized = false;

    public TextToSpeechManager(Context context) {
        this.context = context;
        setupTextToSpeech();
    }

    private void setupTextToSpeech() {
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "Language not supported");
                    } else {
                        isInitialized = true;
                    }
                } else {
                    Log.e(TAG, "TextToSpeech initialization failed");
                }
            }
        });
    }

    public void speak(String text) {
        if (isInitialized && textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void setSpeechRate(float rate) {
        if (textToSpeech != null) {
            textToSpeech.setSpeechRate(rate);
        }
    }

    public void setPitch(float pitch) {
        if (textToSpeech != null) {
            textToSpeech.setPitch(pitch);
        }
    }

    public void destroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }
} 