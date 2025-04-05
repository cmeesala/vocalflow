package com.vocalflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import android.widget.TextView;

public class PaymentMethodsActivity extends AppCompatActivity {
    public static final String EXTRA_BILL_TYPE = "bill_type";
    public static final String EXTRA_BILL_AMOUNT = "bill_amount";

    private String billType;
    private double amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);

        // Get bill details from intent
        billType = getIntent().getStringExtra(EXTRA_BILL_TYPE);
        amount = getIntent().getDoubleExtra(EXTRA_BILL_AMOUNT, 0.0);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Payment Method");
        }

        // Update title with bill details
        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText(String.format("Pay %s Bill - ₹%.2f", billType, amount));

        // Set up click listeners for payment methods
        setupPaymentMethodClickListeners();
    }

    private void setupPaymentMethodClickListeners() {
        CardView creditCardCard = findViewById(R.id.creditCardCard);
        CardView debitCardCard = findViewById(R.id.debitCardCard);
        CardView upiCard = findViewById(R.id.upiCard);
        CardView netBankingCard = findViewById(R.id.netBankingCard);

        View.OnClickListener paymentMethodClickListener = v -> {
            String paymentMethod = "";
            if (v.getId() == R.id.creditCardCard) {
                paymentMethod = "Credit Card";
            } else if (v.getId() == R.id.debitCardCard) {
                paymentMethod = "Debit Card";
            } else if (v.getId() == R.id.upiCard) {
                paymentMethod = "UPI";
            } else if (v.getId() == R.id.netBankingCard) {
                paymentMethod = "Net Banking";
            }

            // Navigate to payment processing screen
            Intent intent = new Intent(this, PaymentProcessingActivity.class);
            intent.putExtra("bill_type", billType);
            intent.putExtra("amount", amount);
            intent.putExtra("payment_method", paymentMethod);
            startActivity(intent);
        };

        creditCardCard.setOnClickListener(paymentMethodClickListener);
        debitCardCard.setOnClickListener(paymentMethodClickListener);
        upiCard.setOnClickListener(paymentMethodClickListener);
        netBankingCard.setOnClickListener(paymentMethodClickListener);
    }

    private void showPaymentDialog(String paymentMethod, String billType, double amount) {
        Toast.makeText(this, 
            String.format("Processing %s payment of INR %.2f for %s bill...", 
                paymentMethod, amount, billType), 
            Toast.LENGTH_SHORT).show();
        // TODO: Implement actual payment processing
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Go back to previous screen
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Simply finish this activity to go back to BillPayActivity
        finish();
    }
} 