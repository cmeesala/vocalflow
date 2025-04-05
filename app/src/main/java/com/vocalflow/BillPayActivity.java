package com.vocalflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

public class BillPayActivity extends AppCompatActivity {
    // Hardcoded bill amounts (in a real app, these would come from an API)
    private static final double ELECTRICITY_AMOUNT = 4000.0;
    private static final double WATER_AMOUNT = 1500.0;
    private static final double INTERNET_AMOUNT = 2000.0;
    private static final double PHONE_AMOUNT = 1000.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_pay);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pay Bills");
        }

        // Set up bill amounts
        TextView electricityAmount = findViewById(R.id.electricityAmount);
        TextView waterAmount = findViewById(R.id.waterAmount);
        TextView internetAmount = findViewById(R.id.internetAmount);
        TextView phoneAmount = findViewById(R.id.phoneAmount);

        electricityAmount.setText(String.format("₹%.2f", ELECTRICITY_AMOUNT));
        waterAmount.setText(String.format("₹%.2f", WATER_AMOUNT));
        internetAmount.setText(String.format("₹%.2f", INTERNET_AMOUNT));
        phoneAmount.setText(String.format("₹%.2f", PHONE_AMOUNT));

        // Set up click listeners for bill cards
        CardView electricityCard = findViewById(R.id.electricityCard);
        CardView waterCard = findViewById(R.id.waterCard);
        CardView internetCard = findViewById(R.id.internetCard);
        CardView phoneCard = findViewById(R.id.phoneCard);

        electricityCard.setOnClickListener(v -> navigateToPaymentMethods("Electricity", ELECTRICITY_AMOUNT));
        waterCard.setOnClickListener(v -> navigateToPaymentMethods("Water", WATER_AMOUNT));
        internetCard.setOnClickListener(v -> navigateToPaymentMethods("Internet", INTERNET_AMOUNT));
        phoneCard.setOnClickListener(v -> navigateToPaymentMethods("Phone", PHONE_AMOUNT));
    }

    private void navigateToPaymentMethods(String billType, double amount) {
        Intent intent = new Intent(this, PaymentMethodsActivity.class);
        intent.putExtra(PaymentMethodsActivity.EXTRA_BILL_TYPE, billType);
        intent.putExtra(PaymentMethodsActivity.EXTRA_BILL_AMOUNT, amount);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to home screen
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Navigate back to home screen
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
} 