package com.vocalflow;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodsActivity extends AppCompatActivity {
    private TextView billTitleTextView;
    private TextView billAmountTextView;
    private RecyclerView paymentMethodsRecyclerView;
    private PaymentMethodsAdapter adapter;
    private List<PaymentMethod> paymentMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);

        // Initialize views
        billTitleTextView = findViewById(R.id.billTitleTextView);
        billAmountTextView = findViewById(R.id.billAmountTextView);
        paymentMethodsRecyclerView = findViewById(R.id.paymentMethodsRecyclerView);

        // Get bill details from intent
        String billTitle = getIntent().getStringExtra("billTitle");
        String billAmount = getIntent().getStringExtra("billAmount");

        // Set bill details
        billTitleTextView.setText(billTitle);
        billAmountTextView.setText(billAmount);

        // Setup RecyclerView
        paymentMethodsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        paymentMethods = getPaymentMethods();
        adapter = new PaymentMethodsAdapter(paymentMethods);
        paymentMethodsRecyclerView.setAdapter(adapter);
    }

    private List<PaymentMethod> getPaymentMethods() {
        List<PaymentMethod> methods = new ArrayList<>();
        // Add sample payment methods
        methods.add(new PaymentMethod("Credit Card", "**** **** **** 1234", R.drawable.ic_credit_card));
        methods.add(new PaymentMethod("Bank Account", "Checking Account", R.drawable.ic_bank));
        return methods;
    }
} 