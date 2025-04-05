package com.vocalflow;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.vocalflow.sdk.service.VoiceAgentService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_VOICE_ENABLED = "voice_enabled";
    
    private SwitchCompat voiceAgentSwitch;
    private VoiceAgentService voiceAgentService;
    private boolean isBound = false;
    private SharedPreferences prefs;
    private SharedPreferences userPrefs;

    private final ActivityResultLauncher<String[]> permissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean allGranted = true;
            for (Boolean granted : result.values()) {
                if (!granted) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted && voiceAgentSwitch.isChecked()) {
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

        // Check if user is logged in
        userPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String username = userPrefs.getString(KEY_USERNAME, null);
        if (username == null) {
            // User not logged in, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Luma (" + username + ")");
        }

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        voiceAgentSwitch = findViewById(R.id.voiceAgentSwitch);
        
        // Set voice agent off by default
        boolean voiceEnabled = prefs.getBoolean(KEY_VOICE_ENABLED, false);
        voiceAgentSwitch.setChecked(voiceEnabled);
        
        voiceAgentSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_VOICE_ENABLED, isChecked).apply();
            if (isChecked) {
                if (checkPermissions()) {
                    startVoiceAgentService();
                } else {
                    showPermissionRationaleDialog();
                }
            } else {
                stopVoiceAgentService();
            }
        });

        // Set up click listeners for service cards
        CardView payBillCard = findViewById(R.id.payBillCard);
        CardView sendMoneyCard = findViewById(R.id.sendMoneyCard);
        CardView logoutCard = findViewById(R.id.logoutCard);
        CardView manageCardsCard = findViewById(R.id.manageCardsCard);

        payBillCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BillPayActivity.class);
            startActivity(intent);
        });

        sendMoneyCard.setOnClickListener(v -> {
            Toast.makeText(this, "Send Money feature coming soon", Toast.LENGTH_SHORT).show();
        });

        logoutCard.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear user session
                    userPrefs.edit().remove(KEY_USERNAME).apply();
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
        });

        manageCardsCard.setOnClickListener(v -> {
            Toast.makeText(this, "Manage Cards feature coming soon", Toast.LENGTH_SHORT).show();
        });

        // Only check permissions if voice is enabled
        if (voiceEnabled) {
            if (checkPermissions()) {
                Log.d(TAG, "All permissions granted, starting service");
                startVoiceAgentService();
            } else {
                Log.d(TAG, "Some permissions not granted, showing rationale dialog");
                showPermissionRationaleDialog();
            }
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

    private void stopVoiceAgentService() {
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        Intent intent = new Intent(this, VoiceAgentService.class);
        stopService(intent);
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
        if (!voiceAgentSwitch.isChecked()) {
            return;
        }
        Intent intent = new Intent(this, VoiceAgentService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
} 