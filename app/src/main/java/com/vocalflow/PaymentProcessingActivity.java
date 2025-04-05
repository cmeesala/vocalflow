package com.vocalflow;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentProcessingActivity extends AppCompatActivity {
    private static final long PROCESSING_DELAY = 1000; // 1 second delay
    private boolean isProcessing = true;

    private LinearLayout processingLayout;
    private LinearLayout successLayout;
    private TextView tvSuccessMessage;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_processing);

        // Get payment details from intent
        String billType = getIntent().getStringExtra("bill_type");
        double amount = getIntent().getDoubleExtra("amount", 0.0);
        String paymentMethod = getIntent().getStringExtra("payment_method");

        // Configure action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Processing Payment");
            // Hide back button during processing
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        // Initialize views
        processingLayout = findViewById(R.id.processingLayout);
        successLayout = findViewById(R.id.successLayout);
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);
        btnDone = findViewById(R.id.btnDone);

        // Update processing details
        TextView tvPaymentDetails = findViewById(R.id.tvPaymentDetails);
        tvPaymentDetails.setText(String.format("Processing %s Bill Payment...", billType));

        // Set up success message
        String successMessage = String.format("Your %s Bill of ₹%.2f has been\nsuccessfully paid through %s.\n\nThank you for using Luma!", 
            billType, amount, paymentMethod);
        tvSuccessMessage.setText(successMessage);

        // Set up done button to go to home
        btnDone.setOnClickListener(v -> navigateToHome());

        // Simulate payment processing with a delay
        new Handler().postDelayed(this::showSuccessState, PROCESSING_DELAY);
    }

    private void showSuccessState() {
        isProcessing = false;
        
        // Update action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Payment Successful");
            // Show home button after success
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Fade out processing layout
        processingLayout.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction(() -> {
                processingLayout.setVisibility(View.GONE);
                
                // Show and fade in success layout
                successLayout.setVisibility(View.VISIBLE);
                successLayout.setAlpha(0f);
                successLayout.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
            })
            .start();
    }

    private void navigateToHome() {
        // Create an intent to go to MainActivity (home)
        Intent intent = new Intent(this, MainActivity.class);
        // Clear the back stack so user can't go back to payment screens
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateToHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If still processing, don't allow back navigation
        if (isProcessing) {
            return;
        }
        // If on success screen, go to home
        navigateToHome();
    }
} 