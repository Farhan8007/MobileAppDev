package com.example.SOS;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history_entries")
public class HistoryEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long timestamp;
    public int contactsReached;
    public String messageSent;

    public HistoryEntry(long timestamp, int contactsReached, String messageSent) {
        this.timestamp = timestamp;
        this.contactsReached = contactsReached;
        this.messageSent = messageSent;
    }
}
