package com.example.SOS;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistoryEntry> entries;

    public HistoryAdapter(List<HistoryEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryEntry entry = entries.get(position);
        holder.tvMessage.setText(entry.messageSent);
        holder.tvContacts.setText("Contacts reached: " + entry.contactsReached);
        
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(entry.timestamp);
        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
        holder.tvTimestamp.setText(date);
    }

    @Override
    public int getItemCount() {
        return entries != null ? entries.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp, tvMessage, tvContacts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvContacts = itemView.findViewById(R.id.tvContacts);
        }
    }
}
