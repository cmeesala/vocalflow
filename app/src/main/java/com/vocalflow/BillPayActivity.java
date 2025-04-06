package com.vocalflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class BillPayActivity extends AppCompatActivity {
    private static final String API_URL = "https://web-production-9ea4.up.railway.app/api/get_bills/?user_id=abc";
    private TextView electricityAmount;
    private TextView waterAmount;
    private TextView internetAmount;
    private TextView phoneAmount;

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

        // Initialize amount TextViews
        electricityAmount = findViewById(R.id.electricityAmount);
        waterAmount = findViewById(R.id.waterAmount);
        internetAmount = findViewById(R.id.internetAmount);
        phoneAmount = findViewById(R.id.phoneAmount);

        // Set up click listeners for bill cards
        setupBillCardClickListeners();

        // Fetch bill amounts from API
        fetchBillAmounts();
    }

    private void setupBillCardClickListeners() {
        CardView electricityCard = findViewById(R.id.electricityCard);
        CardView waterCard = findViewById(R.id.waterCard);
        CardView internetCard = findViewById(R.id.internetCard);
        CardView phoneCard = findViewById(R.id.phoneCard);

        electricityCard.setOnClickListener(v -> {
            String amount = electricityAmount.getText().toString().replace("₹", "");
            navigateToPaymentMethods("Electricity", Double.parseDouble(amount));
        });

        waterCard.setOnClickListener(v -> {
            String amount = waterAmount.getText().toString().replace("₹", "");
            navigateToPaymentMethods("Water", Double.parseDouble(amount));
        });

        internetCard.setOnClickListener(v -> {
            String amount = internetAmount.getText().toString().replace("₹", "");
            navigateToPaymentMethods("Internet", Double.parseDouble(amount));
        });

        phoneCard.setOnClickListener(v -> {
            String amount = phoneAmount.getText().toString().replace("₹", "");
            navigateToPaymentMethods("Phone", Double.parseDouble(amount));
        });
    }

    private void fetchBillAmounts() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.GET,
            API_URL,
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        // Update bill amounts from API response
                        electricityAmount.setText(String.format("₹%.2f", Double.parseDouble(response.getString("electricity_bill"))));
                        waterAmount.setText(String.format("₹%.2f", Double.parseDouble(response.getString("water_bill"))));
                        internetAmount.setText(String.format("₹%.2f", Double.parseDouble(response.getString("internet_bill"))));
                        phoneAmount.setText(String.format("₹%.2f", Double.parseDouble(response.getString("phone_bill"))));
                    } catch (Exception e) {
                        Toast.makeText(BillPayActivity.this, "Error parsing bill amounts", Toast.LENGTH_SHORT).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(BillPayActivity.this, "Error fetching bill amounts", Toast.LENGTH_SHORT).show();
                }
            }
        );

        queue.add(jsonObjectRequest);
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
            onBackPressed();
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