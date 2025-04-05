package com.vocalflow;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vocalflow.sdk.service.VoiceAgentService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView speechTextView;
    private VoiceAgentService voiceAgentService;
    private boolean isBound = false;

    private final ActivityResultLauncher<String[]> permissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean allGranted = true;
            for (Boolean granted : result.values()) {
                if (!granted) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startVoiceAgentService();
            } else {
                showPermissionDeniedDialog();
            }
        });

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            VoiceAgentService.LocalBinder binder = (VoiceAgentService.LocalBinder) service;
            voiceAgentService = binder.getService();
            isBound = true;
            voiceAgentService.setSpeechTextView(speechTextView);
            Log.d(TAG, "VoiceAgentService connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            voiceAgentService = null;
            isBound = false;
            Log.d(TAG, "VoiceAgentService disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        speechTextView = findViewById(R.id.speechTextView);
        Log.d(TAG, "Checking permissions on startup");

        // Check and request permissions
        if (checkPermissions()) {
            Log.d(TAG, "All permissions granted, starting service");
            startVoiceAgentService();
        } else {
            Log.d(TAG, "Some permissions not granted, showing rationale dialog");
            showPermissionRationaleDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    private boolean checkPermissions() {
        String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        };

        boolean allGranted = true;
        for (String permission : permissions) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            boolean granted = result == PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "Permission " + permission + " is " + (granted ? "GRANTED" : "DENIED"));
            if (!granted) {
                allGranted = false;
                break;
            }
        }
        Log.d(TAG, "All permissions " + (allGranted ? "GRANTED" : "NOT GRANTED"));
        return allGranted;
    }

    private void showPermissionRationaleDialog() {
        // First check if we really need to show the dialog
        if (checkPermissions()) {
            Log.d(TAG, "Permissions already granted, starting service directly");
            startVoiceAgentService();
            return;
        }

        Log.d(TAG, "Showing permission rationale dialog");
        new AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("This app needs microphone permission to provide voice functionality.")
            .setPositiveButton("Grant Permissions", (dialog, which) -> {
                String[] permissions = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS
                };
                permissionLauncher.launch(permissions);
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                Toast.makeText(this, "Permissions required for voice functionality", Toast.LENGTH_LONG).show();
            })
            .setCancelable(false)
            .show();
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Voice functionality cannot work without the required permissions. Please grant them in Settings.")
            .setPositiveButton("Open Settings", (dialog, which) -> {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void startVoiceAgentService() {
        Intent intent = new Intent(this, VoiceAgentService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
} 