package com.example.meditime;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class YourAdapter extends RecyclerView.Adapter<YourAdapter.YourViewHolder> {
    private List<DataModel> dataList;

    public YourAdapter(List<DataModel> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public YourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.your_item_layout, parent, false);
        return new YourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YourViewHolder holder, int position) {
        DataModel data = dataList.get(position);
        holder.medicineTextView.setText(data.getMedicine());
        holder.dateTextView.setText(data.getDate());
        holder.timeTextView.setText(data.getTime());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class YourViewHolder extends RecyclerView.ViewHolder {
        TextView medicineTextView;
        TextView dateTextView;
        TextView timeTextView;

        public YourViewHolder(@NonNull View itemView) {
            super(itemView);
            medicineTextView = itemView.findViewById(R.id.medicineTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.dateTimeTextView);
        }
    }
}
