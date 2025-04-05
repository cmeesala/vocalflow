package com.vocalflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BillsAdapter extends RecyclerView.Adapter<BillsAdapter.BillViewHolder> {
    private List<Bill> bills;
    private OnBillSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnBillSelectedListener {
        void onBillSelected(Bill bill);
    }

    public BillsAdapter(List<Bill> bills, OnBillSelectedListener listener) {
        this.bills = bills;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = bills.get(position);
        holder.billTitle.setText(bill.getTitle());
        holder.billAmount.setText(bill.getAmount());
        holder.billDueDate.setText(bill.getDueDate());
        holder.radioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();
            listener.onBillSelected(bill);
        });
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    public Bill getSelectedBill() {
        if (selectedPosition != -1) {
            return bills.get(selectedPosition);
        }
        return null;
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView billTitle;
        TextView billAmount;
        TextView billDueDate;
        RadioButton radioButton;

        BillViewHolder(@NonNull View itemView) {
            super(itemView);
            billTitle = itemView.findViewById(R.id.billTitle);
            billAmount = itemView.findViewById(R.id.billAmount);
            billDueDate = itemView.findViewById(R.id.billDueDate);
            radioButton = itemView.findViewById(R.id.billRadioButton);
        }
    }
} 