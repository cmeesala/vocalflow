package com.vocalflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class BillsActivity extends AppCompatActivity {
    private RecyclerView billsRecyclerView;
    private BillsAdapter adapter;
    private List<Bill> bills;
    private Button payButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bills);

        // Initialize views
        billsRecyclerView = findViewById(R.id.billsRecyclerView);
        payButton = findViewById(R.id.payButton);

        // Setup RecyclerView
        billsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bills = getBills();
        adapter = new BillsAdapter(bills, this::onBillSelected);
        billsRecyclerView.setAdapter(adapter);

        // Setup pay button
        payButton.setOnClickListener(v -> {
            Bill selectedBill = adapter.getSelectedBill();
            if (selectedBill != null) {
                Intent intent = new Intent(this, PaymentMethodsActivity.class);
                intent.putExtra("billTitle", selectedBill.getTitle());
                intent.putExtra("billAmount", selectedBill.getAmount());
                startActivity(intent);
            }
        });
    }

    private void onBillSelected(Bill bill) {
        payButton.setEnabled(true);
    }

    private List<Bill> getBills() {
        List<Bill> bills = new ArrayList<>();
        // Add sample bills
        bills.add(new Bill("Electricity Bill", "$120.50", "Due: 15th March"));
        bills.add(new Bill("Water Bill", "$45.75", "Due: 20th March"));
        bills.add(new Bill("Internet Bill", "$89.99", "Due: 25th March"));
        bills.add(new Bill("Phone Bill", "$65.00", "Due: 28th March"));
        return bills;
    }
} 