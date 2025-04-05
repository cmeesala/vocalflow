package com.vocalflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PaymentMethodsAdapter extends RecyclerView.Adapter<PaymentMethodsAdapter.PaymentMethodViewHolder> {
    private List<PaymentMethod> paymentMethods;
    private int selectedPosition = -1;

    public PaymentMethodsAdapter(List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_method, parent, false);
        return new PaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
        PaymentMethod paymentMethod = paymentMethods.get(position);
        holder.paymentMethodName.setText(paymentMethod.getName());
        holder.paymentMethodDetails.setText(paymentMethod.getDetails());
        holder.paymentMethodIcon.setImageResource(paymentMethod.getIconResourceId());
        holder.paymentMethodRadioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    public PaymentMethod getSelectedPaymentMethod() {
        if (selectedPosition != -1) {
            return paymentMethods.get(selectedPosition);
        }
        return null;
    }

    static class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
        ImageView paymentMethodIcon;
        TextView paymentMethodName;
        TextView paymentMethodDetails;
        RadioButton paymentMethodRadioButton;

        PaymentMethodViewHolder(@NonNull View itemView) {
            super(itemView);
            paymentMethodIcon = itemView.findViewById(R.id.paymentMethodIcon);
            paymentMethodName = itemView.findViewById(R.id.paymentMethodName);
            paymentMethodDetails = itemView.findViewById(R.id.paymentMethodDetails);
            paymentMethodRadioButton = itemView.findViewById(R.id.paymentMethodRadioButton);
        }
    }
} 