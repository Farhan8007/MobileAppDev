package com.example.SOS;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    void insert(HistoryEntry entry);

    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC")
    List<HistoryEntry> getAllEntries();
}
